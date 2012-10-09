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

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Enumeration;
import java.util.Vector;

import javax.sql.DataSource;

import liquibase.Liquibase;
import liquibase.database.Database;
import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.LiquibaseException;
import liquibase.resource.ResourceAccessor;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.context.annotation.DependsOn;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;

/**
 * DB Data Updater This class is used to update DB automatically when System
 * restarted through log file db.changelog.xml
 * 
 * @since 3.0
 */
@Service
@DependsOn("dataSource")
public class DatabaseUpdater implements ResourceLoaderAware, InitializingBean {
	
	@Autowired
	private DataSource dataSource;

    private String changeLog="classpath:ngrinder_datachange_logfile/db.changelog.xml";

    private String contexts;

    private ResourceLoader resourceLoader;
    
    private Database getDatabase() {
        try {
            Database databaseImplementation = DatabaseFactory.getInstance().findCorrectDatabaseImplementation(new JdbcConnection(dataSource.getConnection()));
            return databaseImplementation;
        } catch (Exception e) {
            throw new RuntimeException("Error getting database", e);
        }
    }

    public String getChangeLog() {
        return changeLog;
    }

    public void setChangeLog(String changeLog) {
        this.changeLog = changeLog;
    }

	/**
	 * Automated updates DB after nGrinder has load with all bean properties
	 */
	public void afterPropertiesSet() throws Exception {
		 Liquibase liquibase = new Liquibase(changeLog, new SpringFileOpener(changeLog, getResourceLoader()), getDatabase());
	        try {
	            liquibase.update(contexts);
	        } catch (LiquibaseException e) {
	        	e.printStackTrace();
	        }
	}

	public ResourceLoader getResourceLoader() {
        return resourceLoader;
    }

	public void setResourceLoader(ResourceLoader resourceLoader) {
		this.resourceLoader = resourceLoader;
	}
	
	 class SpringFileOpener implements ResourceAccessor {
	        private String parentFile;

	        private ResourceLoader resourceLoader;

	        public SpringFileOpener(String parentFile, ResourceLoader resourceLoader) {
	            this.parentFile = parentFile;
	            this.resourceLoader = resourceLoader;
	        }

	        public InputStream getResourceAsStream(String file) throws IOException {
	            Resource resource = getResource(file);

	            return resource.getInputStream();
	        }

	        public Enumeration<URL> getResources(String packageName) throws IOException {
	            Vector<URL> tmp = new Vector<URL>();
	            tmp.add(getResource(packageName).getURL());
	            return tmp.elements();
	        }

	        public Resource getResource(String file) {
	            return getResourceLoader().getResource(adjustClasspath(file));
	        }

	        private String adjustClasspath(String file) {
	            return isClasspathPrefixPresent(parentFile) && !isClasspathPrefixPresent(file)
	                    ? ResourceLoader.CLASSPATH_URL_PREFIX + file
	                    : file;
	        }

	        public boolean isClasspathPrefixPresent(String file) {
	            return file.startsWith(ResourceLoader.CLASSPATH_URL_PREFIX);
	        }

	        public ClassLoader toClassLoader() {
	            return getResourceLoader().getClassLoader();
	        }

	        public ResourceLoader getResourceLoader() {
	            return resourceLoader;
	        }

	        public void setResourceLoader(ResourceLoader resourceLoader) {
	            this.resourceLoader = resourceLoader;
	        }
	    }
	
}
