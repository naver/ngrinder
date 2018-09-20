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
package org.ngrinder.infra.config;

import org.apache.commons.dbcp.BasicDataSource;
import org.ngrinder.common.constant.ControllerConstants;
import org.ngrinder.common.constant.DatabaseConstants;
import org.ngrinder.common.util.PropertiesWrapper;
import org.ngrinder.infra.logger.CoreLogger;
import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.persistenceunit.MutablePersistenceUnitInfo;
import org.springframework.orm.jpa.persistenceunit.PersistenceUnitPostProcessor;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;

import javax.persistence.Entity;

/**
 * Dynamic datasource bean configuration.
 *
 * @author JunHo Yoon
 * @since 3.0
 */
@Configuration
public class DatabaseConfig implements DatabaseConstants {
	private static final Logger LOGGER = LoggerFactory.getLogger(DatabaseConfig.class);

	@Autowired
	private Config config;

	/**
	 * Create the dataSource based on the database configuration.
	 *
	 * @return dataSource
	 */
	@Bean(name = "dataSource", destroyMethod = "close")
	public BasicDataSource dataSource() {
		BasicDataSource dataSource = new BasicDataSource();
		PropertiesWrapper databaseProperties = config.getDatabaseProperties();
		Database database = Database.getDatabase(databaseProperties.getProperty(PROP_DATABASE_TYPE));
		database.setup(dataSource, databaseProperties);
		return dataSource;
	}

	/**
	 * Create {@link LocalContainerEntityManagerFactoryBean} bean for Hibernate. Hibernate doesn't
	 * support the search for the {@link Entity} classes in the other Jar files. This method
	 * directly searches the {@link Entity} classes with {@link Reflections} not using Hibernate
	 * entity class search feature to overcome the limitation
	 * <p/>
	 * use annotation DependsOn to insure after databaseUpdater is
	 *
	 * @return {@link LocalContainerEntityManagerFactoryBean}
	 */
	@Bean(name = "emf")
	@DependsOn("databaseUpdater")
	public LocalContainerEntityManagerFactoryBean emf() {
		LocalContainerEntityManagerFactoryBean emf = new LocalContainerEntityManagerFactoryBean();
		emf.setDataSource(dataSource());
		emf.setPersistenceUnitName("ngrinder");
		HibernateJpaVendorAdapter hibernateJpaVendorAdapter = new HibernateJpaVendorAdapter();
		PropertiesWrapper databaseProperties = config.getDatabaseProperties();

		Database database = Database.getDatabase(databaseProperties.getProperty(PROP_DATABASE_TYPE));
		if (config.isClustered() && !database.isClusterSupport()) {
			CoreLogger.LOGGER.error("In cluster mode, H2 is not allowed to use. Please select mysql as database");
		}
		hibernateJpaVendorAdapter.setDatabasePlatform(database.getDialect());
		hibernateJpaVendorAdapter.setShowSql(false);
		emf.setJpaVendorAdapter(hibernateJpaVendorAdapter);
		// To search entity packages from other jar files..
		emf.setPackagesToScan("empty");
		emf.setPersistenceUnitPostProcessors(new PersistenceUnitPostProcessor() {
			@Override
			public void postProcessPersistenceUnitInfo(MutablePersistenceUnitInfo pui) {
				Reflections reflections = new Reflections(ControllerConstants.DEFAULT_PACKAGE_NAME);
				for (Class<?> each : reflections.getTypesAnnotatedWith(Entity.class)) {
					LOGGER.trace("Entity class {} is detected as the SpringData entity.", each.getName());
					pui.addManagedClassName(each.getName());
				}
			}
		});
		return emf;
	}

	/**
	 * Create the transactionManager.
	 *
	 * @return {@link JpaTransactionManager}
	 */
	@Bean
	public JpaTransactionManager transactionManager() {
		JpaTransactionManager transactionManager = new JpaTransactionManager();
		transactionManager.setEntityManagerFactory(emf().getObject());
		transactionManager.setDataSource(dataSource());
		return transactionManager;
	}
}
