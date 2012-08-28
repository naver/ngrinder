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
import org.hibernate.dialect.Dialect;
import org.hibernate.dialect.H2Dialect;
import org.hibernate.dialect.SQLiteDialect;
import org.hibernate.dialect.cubrid.CUBRIDDialect;
import org.ngrinder.common.util.PropertiesWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sqlite.JDBC;

import cubrid.jdbc.driver.CUBRIDDriver;

/**
 * Various Database handler for supported databases. You can easily add the more databases in
 * Enumerator.
 * 
 * @author JunHo Yoon
 * @since 3.0
 */
public enum Database {

	// SQLite
	sqlite(JDBC.class, SQLiteDialect.class, "jdbc:sqlite:%s/ngrinder.sqlite3") {
		@Override
		protected void setupVariants(BasicDataSource dataSource, PropertiesWrapper databaseProperties) {

			dataSource.setUrl(String.format(getUrlTemplate(),
							databaseProperties.getProperty("NGRINDER_HOME", "."), " is not defined"));
			dataSource.setUsername(databaseProperties.getProperty("database_username", "ngrinder"));
			dataSource.setPassword(databaseProperties.getProperty("database_password", "ngrinder"));
		}
	},

	// CUBRID
	cubrid(CUBRIDDriver.class, CUBRIDDialect.class, "jdbc:CUBRID:%s:::?charset=utf-8") {
		@Override
		protected void setupVariants(BasicDataSource dataSource, PropertiesWrapper databaseProperties) {
			dataSource.setUrl(String.format(getUrlTemplate(), databaseProperties.getProperty("database_url",
							"localhost:33000:ngrinder", " is not defined")));
			dataSource.setUsername(databaseProperties.getProperty("database_username", "ngrinder"));
			dataSource.setPassword(databaseProperties.getProperty("database_password", "ngrinder"));
		}
	},

	// CUBRID
	H2(org.h2.Driver.class, H2Dialect.class, "jdbc:h2:%s/db/h2") {
		@Override
		protected void setupVariants(BasicDataSource dataSource, PropertiesWrapper databaseProperties) {
			String format = String.format(getUrlTemplate(),
							databaseProperties.getProperty("NGRINDER_HOME", "."), " is not defined");
			dataSource.setUrl(format);
			dataSource.setUsername(databaseProperties.getProperty("database_username", "ngrinder"));
			dataSource.setPassword(databaseProperties.getProperty("database_password", "ngrinder"));
		}
	};

	private static final Logger logger = LoggerFactory.getLogger(Database.class);
	private final String urlTemplate;
	private final String jdbcDriverName;
	private final String dialect;

	Database(Class<? extends Driver> jdbcDriver, Class<? extends Dialect> dialect, String urlTemplate) {
		this.dialect = dialect.getCanonicalName();
		this.jdbcDriverName = jdbcDriver.getCanonicalName();
		this.urlTemplate = urlTemplate;
	}

	public String getJdbcDriverName() {
		return jdbcDriverName;
	}

	public String getUrlTemplate() {
		return urlTemplate;
	}

	public static Database getDatabase(String type) {
		for (Database database : values()) {
			if (database.name().equalsIgnoreCase(type)) {
				return database;
			}
		}
		logger.error("[FATAL] Database type %s is not supported. Please check the ${NFORGE_HOME}/database.properties. Use H2 istead.",
						type);
		return H2;
	}

	public void setup(BasicDataSource dataSource, PropertiesWrapper propertiesWrapper) {
		setupVariants(dataSource, propertiesWrapper);
		setupCommon(dataSource);
	}

	abstract protected void setupVariants(BasicDataSource dataSource, PropertiesWrapper propertiesWrapper);

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
