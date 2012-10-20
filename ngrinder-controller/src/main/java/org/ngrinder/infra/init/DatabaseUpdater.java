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
package org.ngrinder.infra.init;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;

import liquibase.database.Database;
import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.LiquibaseException;
import liquibase.resource.ClassLoaderResourceAccessor;

import org.ngrinder.common.exception.NGrinderRuntimeException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.context.annotation.DependsOn;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;

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
	private DataSource dataSource;

	private String changeLog = "ngrinder_datachange_logfile/db.changelog.xml";

	private String contexts;

	private ResourceLoader resourceLoader;

	private Database getDatabase() {
		try {
			Database databaseImplementation = DatabaseFactory.getInstance().findCorrectDatabaseImplementation(
							new JdbcConnection(dataSource.getConnection()));
			return databaseImplementation;
		} catch (Exception e) {
			throw new NGrinderRuntimeException("Error getting database", e);
		}
	}

	public String getChangeLog() {
		return changeLog;
	}

	public void setChangeLog(String changeLog) {
		this.changeLog = changeLog;
	}

	/**
	 * Automated updates DB after nGrinder has load with all bean properties.
	 * 
	 * @throws Exception
	 *             occurs when db update is failed.
	 */
	@PostConstruct
	public void init() throws Exception {
		LiquibaseEx liquibase = new LiquibaseEx(getChangeLog(), new ClassLoaderResourceAccessor(getResourceLoader()
						.getClassLoader()), getDatabase());
		try {
			liquibase.update(contexts);
		} catch (LiquibaseException e) {
			throw new NGrinderRuntimeException("Exception occurs while Liquibase update DB", e);
		}
	}

	public ResourceLoader getResourceLoader() {
		return resourceLoader;
	}

	public void setResourceLoader(ResourceLoader resourceLoader) {
		this.resourceLoader = resourceLoader;
	}

}
