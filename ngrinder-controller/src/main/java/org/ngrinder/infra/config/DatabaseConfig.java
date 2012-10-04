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
package org.ngrinder.infra.config;

import javax.persistence.Entity;

import org.apache.commons.dbcp.BasicDataSource;
import org.ngrinder.common.constant.NGrinderConstants;
import org.ngrinder.common.util.PropertiesWrapper;
import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.persistenceunit.MutablePersistenceUnitInfo;
import org.springframework.orm.jpa.persistenceunit.PersistenceUnitPostProcessor;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;

/**
 * Dynamic datasource bean generator.
 * 
 * @author JunHo Yoon
 * @since 3.0
 */
@Configuration
public class DatabaseConfig implements NGrinderConstants {
	private static final Logger logger = LoggerFactory.getLogger(DatabaseConfig.class);

	@Autowired
	private Config config;

	/**
	 * Get the dataSource based on the configuration.
	 * 
	 * @return dataSource
	 */
	@Bean(name = "dataSource", destroyMethod = "close")
	public BasicDataSource dataSource() {
		BasicDataSource dataSource = new BasicDataSource();
		PropertiesWrapper databaseProperties = config.getDatabaseProperties();
		Database database = Database.getDatabase(databaseProperties.getProperty("database", "H2",
				"[FATAL] Database type is not sepecfied. In default, use H2."));
		database.setup(dataSource, databaseProperties);
		return dataSource;
	}

	/**
	 * Create EntityManagerFactory for Hibernate. General Hibernate doesn't
	 * support the search for the {@link Entity} classes in the Jar files. So in
	 * this method, by dropping Hibernate entity class search and directly
	 * search the {@link Entity} classes with {@link Reflections}
	 * 
	 * @return {@link LocalContainerEntityManagerFactoryBean}
	 */
	@Bean(name = "emf")
	public LocalContainerEntityManagerFactoryBean emf() {
		LocalContainerEntityManagerFactoryBean emf = new LocalContainerEntityManagerFactoryBean();
		emf.setDataSource(dataSource());
		emf.setPersistenceUnitName("ngrinder");
		HibernateJpaVendorAdapter hibernateJpaVendorAdapter = new HibernateJpaVendorAdapter();
		PropertiesWrapper databaseProperties = config.getDatabaseProperties();
		Database database = Database.getDatabase(databaseProperties.getProperty("database", "H2",
				"[FATAL] Database type is not sepecfied. In default, use H2."));

		hibernateJpaVendorAdapter.setDatabasePlatform(database.getDialect());
		hibernateJpaVendorAdapter.setShowSql(false);
		hibernateJpaVendorAdapter.setGenerateDdl(true);
		emf.setJpaVendorAdapter(hibernateJpaVendorAdapter);
		// To search entity packages from other jar files..
		emf.setPackagesToScan("empty");
		emf.setPersistenceUnitPostProcessors(new PersistenceUnitPostProcessor() {
			@Override
			public void postProcessPersistenceUnitInfo(MutablePersistenceUnitInfo pui) {
				Reflections reflections = new Reflections(NGRINDER_DEFAULT_PACKAGE);
				for (Class<?> each : reflections.getTypesAnnotatedWith(Entity.class)) {
					logger.trace("Entity class {} is detected as the SpringData entity.", each.getName());
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
