/*
 * Copyright (C) 2012 - 2012 NHN Corporation
 * All rights reserved.
 *
 * This file is part of The nGrinder software distribution. Refer to
 * the file LICENSE which is part of The nGrinder distribution for
 * licensing details. The nGrinder distribution is available on the
 * Internet at http://nhnopensource.org/ngrinder
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS
 * FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE
 * COPYRIGHT HOLDERS OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package liquibase.lockservice;

import static org.ngrinder.common.util.NoOp.noOp;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import liquibase.database.Database;
import liquibase.exception.DatabaseException;
import liquibase.exception.LockException;
import liquibase.exception.UnexpectedLiquibaseException;
import liquibase.executor.Executor;
import liquibase.executor.ExecutorService;
import liquibase.logging.LogFactory;
import liquibase.statement.SqlStatement;
import liquibase.statement.core.LockExDatabaseChangeLogStatement;
import liquibase.statement.core.RawSqlStatement;
import liquibase.statement.core.SelectFromDatabaseChangeLogLockStatement;
import liquibase.statement.core.UnlockDatabaseChangeLogStatement;

import org.apache.commons.lang.StringUtils;

/**
 * nGrinder customized implementation for {@link LockService}.
 * 
 * @author JunHo Yoon
 * @since 3.0
 */
public final class LockServiceEx {

	private Database database;

	private boolean hasChangeLogLock = false;

	private long changeLogLockWaitTime = 1000 * 60 * 5; // default to 5 mins
	private long changeLogLocRecheckTime = 1000 * 10; // default to every 10
														// seconds

	private static Map<Database, LockServiceEx> instances = new ConcurrentHashMap<Database, LockServiceEx>();

	private LockServiceEx(Database database) {
		this.database = database;
	}

	/**
	 * Get {@link LockServiceEx} instance.
	 * 
	 * @param database
	 *            corresponding database instance
	 * @return {@link LockServiceEx} instance
	 */
	public static LockServiceEx getInstance(Database database) {
		if (!instances.containsKey(database)) {
			instances.put(database, new LockServiceEx(database));
		}
		return instances.get(database);
	}

	public void setChangeLogLockWaitTime(long changeLogLockWaitTime) {
		this.changeLogLockWaitTime = changeLogLockWaitTime;
	}

	public void setChangeLogLockRecheckTime(long changeLogLocRecheckTime) {
		this.changeLogLocRecheckTime = changeLogLocRecheckTime;
	}

	/**
	 * Check if it has change log lock.
	 * 
	 * @return true if it has the change lock
	 */
	public boolean hasChangeLogLock() {
		return hasChangeLogLock;
	}

	/**
	 * Wait for lock.
	 * 
	 * @throws LockException
	 *             occurs when lock manipulation is failed.
	 */
	public void waitForLock() throws LockException {

		boolean locked = false;
		long timeToGiveUp = new Date().getTime() + changeLogLockWaitTime;
		while (!locked && new Date().getTime() < timeToGiveUp) {
			locked = acquireLock();
			if (!locked) {
				LogFactory.getLogger().info("Waiting for changelog lock....");
				try {
					Thread.sleep(changeLogLocRecheckTime);
				} catch (InterruptedException e) {
					noOp();
				}
			}
		}

		if (!locked) {
			DatabaseChangeLogLock[] locks = listLocks();
			String lockedBy;
			if (locks.length > 0) {
				DatabaseChangeLogLock lock = locks[0];
				lockedBy = lock.getLockedBy()
								+ " since "
								+ DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT).format(
												lock.getLockGranted());
			} else {
				lockedBy = "UNKNOWN";
			}
			throw new LockException("Could not acquire change log lock.  Currently locked by " + lockedBy);
		}
	}

	/**
	 * Acquire lock. Instead of liquibase implementation, nGrinder added the type resolution for
	 * boolean value.
	 * 
	 * @return true if successful
	 * @throws LockException
	 *             occurs when the lock aquire is failed.
	 */
	public boolean acquireLock() throws LockException {
		if (hasChangeLogLock) {
			return true;
		}

		Executor executor = ExecutorService.getInstance().getExecutor(database);

		try {
			database.rollback();
			database.checkDatabaseChangeLogLockTable();
			Object lockObject = (Object) ExecutorService.getInstance().getExecutor(database)
							.queryForObject(new SelectFromDatabaseChangeLogLockStatement("LOCKED"), Object.class);
			if (checkReturnValue(lockObject)) {
				// To here
				return false;
			} else {
				executor.comment("Lock Database");
				int rowsUpdated = executor.update(new LockExDatabaseChangeLogStatement());
				if (rowsUpdated > 1) {
					throw new LockException("Did not update change log lock correctly");
				}

				if (rowsUpdated == 0) {
					// another node was faster
					return false;
				}
				database.commit();
				LogFactory.getLogger().info("Successfully acquired change log lock");

				hasChangeLogLock = true;

				database.setCanCacheLiquibaseTableInfo(true);
				return true;
			}
		} catch (Exception e) {
			throw new LockException(e);
		} finally {
			try {
				database.rollback();
			} catch (DatabaseException e) {
				noOp();
			}
		}

	}

	/**
	 * Check return value is boolean or not.
	 * 
	 * @param value
	 *            returnValue
	 * @return true if true
	 */
	public boolean checkReturnValue(Object value) {

		if (value instanceof String) {
			String trim = StringUtils.trim((String) value);
			if ("T".equals(trim)) {
				return true;
			} else if ("F".equals(trim) || StringUtils.isEmpty((String) value) || "0".equals(trim)) {
				return false;
			} else {
				throw new UnexpectedLiquibaseException("Unknown boolean value: " + value);
			}
		} else if (value == null) {
			return false;
		} else if (value instanceof Integer) {
			return !(Integer.valueOf(0).equals(value));
		} else if (value instanceof Long) {
			return !(Long.valueOf(0).equals(value));
		} else if (value instanceof Boolean) {
			return ((Boolean) value);
		} else {
			return false;
		}
	}

	/**
	 * Release Lock.
	 * 
	 * @throws LockException
	 *             exception.
	 */
	public void releaseLock() throws LockException {
		Executor executor = ExecutorService.getInstance().getExecutor(database);
		try {
			if (database.hasDatabaseChangeLogLockTable()) {
				executor.comment("Release Database Lock");
				database.rollback();
				int updatedRows = executor.update(new UnlockDatabaseChangeLogStatement());
				if (updatedRows != 1) {
					throw new LockException("Did not update change log lock correctly.\n\n"
									+ updatedRows
									+ " rows were updated instead of the expected 1 row using executor "
									+ executor.getClass().getName()
									+ " there are "
									+ executor.queryForInt(new RawSqlStatement("select count(*) from "
													+ database.getDatabaseChangeLogLockTableName()))
									+ " rows in the table");
				}
				database.commit();
				hasChangeLogLock = false;

				instances.remove(this.database);

				database.setCanCacheLiquibaseTableInfo(false);

				LogFactory.getLogger().info("Successfully released change log lock");
			}
		} catch (Exception e) {
			throw new LockException(e);
		} finally {
			try {
				database.rollback();
			} catch (DatabaseException e) {
				noOp();
			}
		}
	}

	/**
	 * List up locks.
	 * 
	 * @return {@link DatabaseChangeLogLock} array.
	 * @throws LockException
	 *             occurs when lock list up is failed.
	 */
	@SuppressWarnings("rawtypes")
	public DatabaseChangeLogLock[] listLocks() throws LockException {
		try {
			if (!database.hasDatabaseChangeLogLockTable()) {
				return new DatabaseChangeLogLock[0];
			}

			List<DatabaseChangeLogLock> allLocks = new ArrayList<DatabaseChangeLogLock>();
			SqlStatement sqlStatement = new SelectFromDatabaseChangeLogLockStatement("ID", "LOCKED", "LOCKGRANTED",
							"LOCKEDBY");
			List<Map> rows = ExecutorService.getInstance().getExecutor(database).queryForList(sqlStatement);
			for (Map columnMap : rows) {
				Object lockedValue = columnMap.get("LOCKED");
				Boolean locked;
				if (lockedValue instanceof Number) {
					locked = ((Number) lockedValue).intValue() == 1;
				} else if (lockedValue instanceof String) {
					locked = ("T".equals(lockedValue));
				} else {
					locked = (Boolean) lockedValue;
				}
				if (locked != null && locked) {
					allLocks.add(new DatabaseChangeLogLock(((Number) columnMap.get("ID")).intValue(), (Date) columnMap
									.get("LOCKGRANTED"), (String) columnMap.get("LOCKEDBY")));
				}
			}
			return allLocks.toArray(new DatabaseChangeLogLock[allLocks.size()]);
		} catch (Exception e) {
			throw new LockException(e);
		}
	}

	/**
	 * Releases whatever locks are on the database change log table.
	 * 
	 * @throws LockException
	 *             exception
	 * @throws DatabaseException
	 *             exception
	 */
	public void forceReleaseLock() throws LockException, DatabaseException {
		database.checkDatabaseChangeLogLockTable();
		releaseLock();
		/*
		 * try { releaseLock(); } catch (LockException e) { // ignore ?
		 * LogFactory.getLogger().info("Ignored exception in forceReleaseLock: " + e.getMessage());
		 * }
		 */
	}

	/**
	 * Clears information the lock handler knows about the tables. Should only be called by
	 * Liquibase internal calls
	 */
	public void reset() {
		hasChangeLogLock = false;
	}

	/**
	 * Reset all locks.
	 */
	public static void resetAll() {
		for (Map.Entry<Database, LockServiceEx> entity : instances.entrySet()) {
			entity.getValue().reset();
		}
	}

}
