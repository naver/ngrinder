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

import liquibase.database.Database;
import liquibase.database.DatabaseFactory;
import liquibase.database.core.H2ExTypeConverter;
import liquibase.database.jvm.JdbcConnection;
import liquibase.database.typeconversion.TypeConverterFactory;
import liquibase.exception.LiquibaseException;
import liquibase.resource.ClassLoaderResourceAccessor;
import liquibase.sqlgenerator.SqlGeneratorFactory;
import liquibase.sqlgenerator.core.ModifyDataTypeGenerator;
import liquibase.sqlgenerator.core.RenameColumnGenerator;
import org.apache.commons.dbcp.BasicDataSource;
import org.ngrinder.infra.config.Config;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.context.annotation.DependsOn;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;

import static org.ngrinder.common.util.ExceptionUtils.processException;

/**
 * DB Data Updater. This class is used to update DB automatically when System restarted through log
 * file db.changelog.xml
 *
 * @author Matt
 * @author JunHo Yoon
 * @since 3.0
 */
@Service
@DependsOn("dataSource")
public class DatabaseUpdater implements ResourceLoaderAware {

	@Autowired
	private BasicDataSource dataSource;

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
		SqlGeneratorFactory.getInstance().register(new LockExDatabaseChangeLogGenerator());
		TypeConverterFactory.getInstance().register(H2ExTypeConverter.class);
		LiquibaseEx liquibase = new LiquibaseEx(getChangeLog(), new ClassLoaderResourceAccessor(getResourceLoader()
				.getClassLoader()), getDatabase());
		// previous RenameColumnGenerator don't support Cubrid,so remove it and add new Generator
		SqlGeneratorFactory.getInstance().unregister(RenameColumnGenerator.class);
		SqlGeneratorFactory.getInstance().register(new RenameColumnExGenerator());
		SqlGeneratorFactory.getInstance().unregister(ModifyDataTypeGenerator.class);
		SqlGeneratorFactory.getInstance().register(new ModifyDataTypeExGenerator());
		try {
			liquibase.update(null);
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

}
