/* 
 * Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License. 
 */
package org.ngrinder.infra.init;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import liquibase.Liquibase;
import liquibase.changelog.ChangeLogIterator;
import liquibase.changelog.DatabaseChangeLog;
import liquibase.changelog.filter.ContextChangeSetFilter;
import liquibase.changelog.filter.DbmsChangeSetFilter;
import liquibase.changelog.filter.ShouldRunChangeSetFilter;
import liquibase.changelog.visitor.UpdateVisitor;
import liquibase.database.Database;
import liquibase.exception.DatabaseException;
import liquibase.exception.LiquibaseException;
import liquibase.exception.LockException;
import liquibase.lockservice.LockServiceEx;
import liquibase.parser.ChangeLogParserFactory;
import liquibase.resource.ResourceAccessor;
import liquibase.util.StringUtils;

/**
 * {@link Liquibase} extension to use {@link TypeConverter} for lock acquire.
 *
 * @author JunHo Yoon
 * @since 3.0
 */
public class LiquibaseEx extends Liquibase {
	private static final Logger LOGGER = LoggerFactory.getLogger(LiquibaseEx.class);
	private final String changeLogFile;

	/**
	 * Constructor.
	 *
	 * @param changeLogFile    changeLogFile
	 * @param resourceAccessor resource accessor
	 * @param database         database to be used
	 * @throws LiquibaseException occurs when initialization is failed.
	 */
	public LiquibaseEx(String changeLogFile, ResourceAccessor resourceAccessor, Database database)
			throws LiquibaseException {
		super(changeLogFile, resourceAccessor, database);
		this.changeLogFile = changeLogFile;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see liquibase.Liquibase#update(java.lang.String)
	 */
	@Override
	public void update(String contexts) throws LiquibaseException {
		contexts = StringUtils.trimToNull(contexts);

		LockServiceEx lockService = LockServiceEx.getInstance(database);
		lockService.waitForLock();
		try {
			getChangeLogParameters().setContexts(StringUtils.splitAndTrim(contexts, ","));

			DatabaseChangeLog changeLog = ChangeLogParserFactory.getInstance()
					.getParser(this.changeLogFile, getFileOpener())
					.parse(this.changeLogFile, getChangeLogParameters(), getFileOpener());
			checkDatabaseChangeLogTable(true, changeLog, contexts);

			changeLog.validate(database, contexts);
			ChangeLogIterator changeLogIterator = getStandardChangelogIterator(contexts, changeLog);

			changeLogIterator.run(new UpdateVisitor(database), database);
		} finally {
			try {
				lockService.releaseLock();
			} catch (LockException e) {
				LOGGER.error("Error while releasing db lock", e);
			}
		}
	}

	private ChangeLogIterator getStandardChangelogIterator(String contexts, DatabaseChangeLog changeLog)
			throws DatabaseException {
		return new ChangeLogIterator(changeLog, new ShouldRunChangeSetFilter(database), new ContextChangeSetFilter(
				contexts), new DbmsChangeSetFilter(database));
	}
}
