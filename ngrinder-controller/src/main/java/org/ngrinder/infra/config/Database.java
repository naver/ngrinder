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

import cubrid.jdbc.driver.CUBRIDDriver;
import org.apache.commons.dbcp.BasicDataSource;
import org.apache.commons.lang.StringUtils;
import org.hibernate.dialect.CUBRIDExDialect;
import org.hibernate.dialect.Dialect;
import org.hibernate.dialect.H2ExDialect;
import org.ngrinder.common.util.PropertiesWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Driver;

/**
 * Database handler for supported databases.
 * <p/>
 * You can easily add the more database type into {@link Database} enum.
 *
 * @author JunHo Yoon
 * @since 3.0
 */
public enum Database {

	/**
	 * CUBRID.
	 */
	cubrid(CUBRIDDriver.class, CUBRIDExDialect.class, "jdbc:CUBRID:%s:::?charset=utf-8%s") {
		@Override
		protected void setupVariants(BasicDataSource dataSource, PropertiesWrapper databaseProperties) {
			dataSource.setUrl(String.format(getUrlTemplate(),
					databaseProperties.getProperty(DatabaseConfig.PROP_DATABASE_URL),
					StringUtils.trimToEmpty(databaseProperties.getProperty(DatabaseConfig.PROP_DATABASE_URL_OPTION))));
			dataSource.setUsername(databaseProperties.getProperty(DatabaseConfig.PROP_DATABASE_USERNAME));
			dataSource.setPassword(databaseProperties.getProperty(DatabaseConfig.PROP_DATABASE_PASSWORD));
		}
	},

	/**
	 * H2.
	 */
	H2(org.h2.Driver.class, H2ExDialect.class, "jdbc:h2:%s/db/h2", false) {
		@Override
		protected void setupVariants(BasicDataSource dataSource, PropertiesWrapper databaseProperties) {
			String format = String.format(getUrlTemplate(), databaseProperties.getProperty("NGRINDER_HOME", "."));

			final String databaseURL = databaseProperties.getProperty(DatabaseConfig.PROP_DATABASE_URL);
			if (databaseURL.startsWith("tcp://")) {
				format = "jdbc:h2:" + databaseURL;
			}
			if (databaseProperties.exist("unit-test")) {
				format = format + ";DB_CLOSE_ON_EXIT=FALSE";
			}
			dataSource.setUrl(format);
			dataSource.setUsername(databaseProperties.getProperty(DatabaseConfig.PROP_DATABASE_USERNAME));
			dataSource.setPassword(databaseProperties.getProperty(DatabaseConfig.PROP_DATABASE_PASSWORD));
		}
	};

	/*
	 * Default db constants
	 */
	private static final int DB_MAX_OPEN_PREPARED_STATEMENTS = 50;
	private static final int DB_MAX_WAIT = 3000;
	private static final int DB_MIN_IDLE = 5;
	private static final int DB_MAX_ACTIVE = 20;
	private static final int DB_INITIAL_SIZE = 5;
	private static final Logger LOG = LoggerFactory.getLogger(Database.class);
	private final String urlTemplate;
	private final String jdbcDriverName;
	private final String dialect;
	private final boolean clusterSupport;

	/**
	 * Constructor with cluster mode true.
	 *
	 * @param jdbcDriver  JDBC Driver class
	 * @param dialect     the dialect to be used
	 * @param urlTemplate database url template. This will be used to be combined with database_url property in
	 *                    database.conf
	 */
	Database(Class<? extends Driver> jdbcDriver, Class<? extends Dialect> dialect, String urlTemplate) {
		this(jdbcDriver, dialect, urlTemplate, true);
	}

	/**
	 * Constructor.
	 *
	 * @param jdbcDriver     JDBC Driver class
	 * @param dialect        the dialect to be used
	 * @param urlTemplate    database url template. This will be used to be combined with database_url property in
	 *                       database.conf
	 * @param clusterSupport true if cluster mode is supported.
	 * @since 3.1
	 */
	Database(Class<? extends Driver> jdbcDriver, Class<? extends Dialect> dialect, String urlTemplate,
	         boolean clusterSupport) {
		this.clusterSupport = clusterSupport;
		this.dialect = dialect.getCanonicalName();
		this.jdbcDriverName = jdbcDriver.getCanonicalName();
		this.urlTemplate = urlTemplate;
	}

	/**
	 * Get the Jdbc driver name.
	 *
	 * @return driver name
	 */
	public String getJdbcDriverName() {
		return jdbcDriverName;
	}

	/**
	 * Get the database URL template.
	 *
	 * @return database URL template
	 */
	public String getUrlTemplate() {
		return urlTemplate;
	}

	/**
	 * Get the {@link Database} enum value for the given type.
	 *
	 * @param type db type name. For example... H2, Cubrid..
	 * @return found {@link Database}. {@link Database#H2} if not found.
	 */
	public static Database getDatabase(String type) {
		for (Database database : values()) {
			if (database.name().equalsIgnoreCase(type)) {
				return database;
			}
		}
		LOG.error("[FATAL] Database type {} is not supported.\n" +
				"Please check the ${NGRINDER_HOME}/database.conf.\n "
				+ "Use H2 instead.", type);
		return H2;
	}

	/**
	 * Set up database. this method consists of two parts.<br/>
	 * The first part is variant setup b/w databases. The second is common setup among databases.
	 *
	 * @param dataSource        dataSource
	 * @param propertiesWrapper {@link PropertiesWrapper} which contains db access info
	 */
	public void setup(BasicDataSource dataSource, PropertiesWrapper propertiesWrapper) {
		setupVariants(dataSource, propertiesWrapper);
		setupCommon(dataSource);
	}

	/**
	 * Setup the database specific features. Each {@link Database} enums should inherits this method.
	 *
	 * @param dataSource   dataSource
	 * @param databaseConf "database.conf" properties.
	 */
	protected abstract void setupVariants(BasicDataSource dataSource, PropertiesWrapper databaseConf);

	/**
	 * Setup the database common features.
	 *
	 * @param dataSource datasource
	 */
	protected void setupCommon(BasicDataSource dataSource) {
		dataSource.setDriverClassName(getJdbcDriverName());
		dataSource.setInitialSize(DB_INITIAL_SIZE);
		dataSource.setMaxActive(DB_MAX_ACTIVE);
		dataSource.setMinIdle(DB_MIN_IDLE);
		dataSource.setMaxWait(DB_MAX_WAIT);
		dataSource.setPoolPreparedStatements(true);
		dataSource.setMaxOpenPreparedStatements(DB_MAX_OPEN_PREPARED_STATEMENTS);
		dataSource.setTestWhileIdle(true);
		dataSource.setTestOnBorrow(true);
		dataSource.setTestOnReturn(true);
		dataSource.setValidationQuery("SELECT 1");
	}

	/**
	 * Get the current used dialect.
	 *
	 * @return dialect name
	 */
	public String getDialect() {
		return dialect;
	}

	/**
	 * Check if it's cluster mode.
	 *
	 * @return true if support
	 */
	public boolean isClusterSupport() {
		return clusterSupport;
	}
}
