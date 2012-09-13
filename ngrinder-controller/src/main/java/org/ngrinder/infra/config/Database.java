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

import java.sql.Driver;

import org.apache.commons.dbcp.BasicDataSource;
import org.hibernate.dialect.CUBRIDDialect;
import org.hibernate.dialect.Dialect;
import org.hibernate.dialect.H2Dialect;
import org.hibernate.dialect.SQLiteDialect;
import org.ngrinder.common.util.PropertiesWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sqlite.JDBC;

import cubrid.jdbc.driver.CUBRIDDriver;

/**
 * Various Database handler for supported databases. You can easily add the more
 * databases in Enumerator.
 * 
 * @author JunHo Yoon
 * @since 3.0
 */
public enum Database {

	// SQLite
	sqlite(JDBC.class, SQLiteDialect.class, "jdbc:sqlite:%s/ngrinder.sqlite3") {
		@Override
		protected void setupVariants(BasicDataSource dataSource, PropertiesWrapper databaseProperties) {

			dataSource.setUrl(String.format(getUrlTemplate(), databaseProperties.getProperty("NGRINDER_HOME", "."), " is not defined"));
			dataSource.setUsername(databaseProperties.getProperty("database_username", "ngrinder"));
			dataSource.setPassword(databaseProperties.getProperty("database_password", "ngrinder"));
		}
	},

	// CUBRID
	cubrid(CUBRIDDriver.class, CUBRIDDialect.class, "jdbc:CUBRID:%s:::?charset=utf-8") {
		@Override
		protected void setupVariants(BasicDataSource dataSource, PropertiesWrapper databaseProperties) {
			dataSource.setUrl(String.format(getUrlTemplate(),
					databaseProperties.getProperty("database_url", "localhost:33000:ngrinder", " is not defined")));
			dataSource.setUsername(databaseProperties.getProperty("database_username", "ngrinder"));
			dataSource.setPassword(databaseProperties.getProperty("database_password", "ngrinder"));
		}
	},

	// CUBRID
	H2(org.h2.Driver.class, H2Dialect.class, "jdbc:h2:%s/db/h2") {
		@Override
		protected void setupVariants(BasicDataSource dataSource, PropertiesWrapper databaseProperties) {
			String format = String.format(getUrlTemplate(), databaseProperties.getProperty("NGRINDER_HOME", "."), " is not defined");
			dataSource.setUrl(format);
			dataSource.setUsername(databaseProperties.getProperty("database_username", "ngrinder"));
			dataSource.setPassword(databaseProperties.getProperty("database_password", "ngrinder"));
		}
	};

	private static final Logger logger = LoggerFactory.getLogger(Database.class);
	private final String urlTemplate;
	private final String jdbcDriverName;
	private final String dialect;

	/**
	 * Constructor.
	 * 
	 * @param jdbcDriver
	 *            JDBC Driver class
	 * @param dialect
	 *            the dialect to be used
	 * @param urlTemplate
	 *            database url template. This will be used to combine it with
	 *            database_url property in database.conf
	 */
	Database(Class<? extends Driver> jdbcDriver, Class<? extends Dialect> dialect, String urlTemplate) {
		this.dialect = dialect.getCanonicalName();
		this.jdbcDriverName = jdbcDriver.getCanonicalName();
		this.urlTemplate = urlTemplate;
	}

	/**
	 * Get the jdbc driver name.
	 * 
	 * @return driver name
	 */
	public String getJdbcDriverName() {
		return jdbcDriverName;
	}

	/**
	 * Get the database url template
	 * 
	 * @return databsae url template
	 */
	public String getUrlTemplate() {
		return urlTemplate;
	}

	/**
	 * Get the {@link Database} enum for the given type.
	 * 
	 * @param type
	 *            type of db. For example... H2, Cubrid..
	 * @return {@link Database} enum
	 */
	public static Database getDatabase(String type) {
		for (Database database : values()) {
			if (database.name().equalsIgnoreCase(type)) {
				return database;
			}
		}
		logger.error("[FATAL] Database type {} is not supported. Please check the ${NFORGE_HOME}/database.conf. This time, Use H2 istead.",
				type);
		return H2;
	}

	/**
	 * Set up database.
	 * 
	 * @param dataSource
	 * @param propertiesWrapper
	 */
	public void setup(BasicDataSource dataSource, PropertiesWrapper propertiesWrapper) {
		setupVariants(dataSource, propertiesWrapper);
		setupCommon(dataSource);
	}

	/**
	 * Each database needs custom setup. Specify these setup in inherited method
	 * of this.
	 * 
	 * @param dataSource dataSource
	 * @param propertiesWrapper database.conf's properties. 
	 */
	abstract protected void setupVariants(BasicDataSource dataSource, PropertiesWrapper propertiesWrapper);

	/**
	 * Common setup among databases.
	 * 
	 * @param dataSource
	 *            datasource
	 */
	protected void setupCommon(BasicDataSource dataSource) {
		dataSource.setDriverClassName(getJdbcDriverName());
		dataSource.setInitialSize(5);
		dataSource.setMaxActive(20);
		dataSource.setMinIdle(5);
		dataSource.setMaxWait(3000);
		dataSource.setPoolPreparedStatements(true);
		dataSource.setMaxOpenPreparedStatements(50);
	}

	public String getDialect() {
		return dialect;
	}
}
