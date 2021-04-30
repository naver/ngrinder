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

import liquibase.Liquibase;
import liquibase.database.Database;
import liquibase.database.DatabaseFactory;
import liquibase.database.core.BooleanTypeEx;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.LiquibaseException;
import liquibase.resource.ClassLoaderResourceAccessor;
import liquibase.sqlgenerator.SqlGeneratorFactory;
import liquibase.sqlgenerator.core.LockDatabaseChangeLogGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.dbcp.BasicDataSource;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.context.annotation.DependsOn;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;

import java.sql.SQLException;
import java.sql.Statement;

import static liquibase.datatype.DataTypeFactory.getInstance;
import static org.apache.commons.lang.StringUtils.equalsIgnoreCase;
import static org.ngrinder.common.util.ExceptionUtils.processException;

/**
 * DB Data Updater. This class is used to update DB automatically when System restarted through log
 * file db.changelog.xml
 *
 * @since 3.0
 */
@Slf4j
@Service
@DependsOn("dataSource")
@RequiredArgsConstructor
public class DatabaseUpdater implements ResourceLoaderAware {

	private final BasicDataSource dataSource;

	private ResourceLoader resourceLoader;

	private Database getDatabase() {
		try {
			return DatabaseFactory.getInstance().findCorrectDatabaseImplementation(
					new JdbcConnection(dataSource.getConnection()));
		} catch (Exception e) {
			throw processException("Error getting database from " + dataSource.getUrl(), e);
		}
	}

	public String getChangeLog() {
		return "ngrinder_datachange_logfile/db.changelog.xml";
	}


	/**
	 * Automated updates DB after nGrinder has load with all bean properties.
	 *
	 * @throws Exception occurs when db update is failed.
	 */
	@PostConstruct
	public void init() throws Exception {
		SqlGeneratorFactory.getInstance().register(new LockDatabaseChangeLogGenerator());
		getInstance().register(BooleanTypeEx.class);
		Liquibase liquibase = new Liquibase(getChangeLog(), new ClassLoaderResourceAccessor(getResourceLoader()
				.getClassLoader()), getDatabase());

		updateLiquibaseDatabaseLockTableForBackwardCompatibility(liquibase.getDatabase());

		try {
			liquibase.update((String) null);
		} catch (LiquibaseException e) {
			throw processException("Exception occurs while Liquibase update DB", e);
		}
	}

	public ResourceLoader getResourceLoader() {
		return resourceLoader;
	}

	public void setResourceLoader(ResourceLoader resourceLoader) {
		this.resourceLoader = resourceLoader;
	}

	private void updateLiquibaseDatabaseLockTableForBackwardCompatibility(Database database) {
		if (!equalsIgnoreCase(database.getDatabaseProductName(), "H2")) {
			return;
		}

		String databaseLockTableName = database.getDatabaseChangeLogLockTableName();
		String lockColumnDataType = getInstance().fromDescription("boolean", database).toDatabaseDataType(database).getType();
		String updateQuery = "ALTER TABLE " + databaseLockTableName +  " ALTER COLUMN LOCKED " + lockColumnDataType;

		try (Statement stmt = dataSource.getConnection().createStatement()) {
			stmt.executeUpdate(updateQuery);
			log.info("Liquibase lock table has been updated successfully from '" + updateQuery + "'");
		} catch (SQLException e) {
			log.error("Failed to update liquibase lock table from '" + updateQuery + "' cause by {} " +
				"If you run the ngrinder first time, Please ignore this error.", e.getMessage());
		}
	}
}
