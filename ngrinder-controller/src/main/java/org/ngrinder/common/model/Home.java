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
package org.ngrinder.common.model;

import static org.ngrinder.common.util.Preconditions.checkNotNull;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.util.Properties;

import org.apache.commons.io.FileUtils;
import org.ngrinder.common.constant.NGrinderConstants;
import org.ngrinder.common.exception.ConfigurationException;
import org.ngrinder.common.exception.NGrinderRuntimeException;
import org.ngrinder.common.util.EncodingUtil;
import org.ngrinder.model.PerfTest;
import org.ngrinder.model.User;

/**
 * Home class which enable you to easily access resources in Home directory.
 * 
 * @author JunHo Yoon
 * @since 3.0
 */
public class Home implements NGrinderConstants {

	private final File directory;

	/**
	 * Constructor.
	 * 
	 * @param directory
	 *            home directory
	 */
	public Home(File directory) {
		this(directory, true);
	}

	/**
	 * Constructor.
	 * 
	 * @param directory
	 *            home directory
	 * @param create
	 *            create the directory if not exists
	 */
	public Home(File directory, boolean create) {
		checkNotNull(directory, "directory should not be null");
		if (create) {
			directory.mkdir();
		}
		if (directory.exists() && !directory.canWrite()) {
			throw new ConfigurationException(String.format(" ngrinder home directory %s is not writable.", directory),
							null);
		}
		this.directory = directory;
	}

	/**
	 * Get home directory.
	 * 
	 * @return home directory
	 */
	public File getDirectory() {
		return directory;
	}

	/**
	 * Copy file from given location.
	 * 
	 * @param from
	 *            file location
	 * @param overwrite
	 *            overwrite
	 */
	public void copyFrom(File from, boolean overwrite) {
		// Copy missing files
		try {
			for (File file : from.listFiles()) {
				if (!(new File(directory, file.getName()).exists())) {
					FileUtils.copyFileToDirectory(file, directory);
				}
			}
		} catch (IOException e) {
			throw new NGrinderRuntimeException("Fail to copy files from " + from.getAbsolutePath(), e);
		}
	}

	/**
	 * Make sub directory on home directory.
	 * 
	 * @param subPathName
	 *            subpath name
	 */
	public void makeSubPath(String subPathName) {
		File subFile = new File(directory, subPathName);
		if (!subFile.exists()) {
			subFile.mkdir();
		}
	}

	/**
	 * Get the {@link Properties} named the given configuration file name.
	 * 
	 * @param confFileName
	 *            configuration file name
	 * @return loaded {@link Properties}
	 */
	public Properties getProperties(String confFileName) {
		try {
			File configFile = getSubFile(confFileName);
			if (configFile.exists()) {
				byte[] propByte = FileUtils.readFileToByteArray(configFile);
				String propString = EncodingUtil.getAutoDecodedString(propByte, "UTF-8");
				Properties prop = new Properties();
				prop.load(new StringReader(propString));
				return prop;
			} else {
				// default empty properties.
				return new Properties();
			}

		} catch (IOException e) {
			throw new NGrinderRuntimeException("Fail to load property file " + confFileName, e);
		}
	}

	/**
	 * Get sub {@link File} instance under home directory.
	 * 
	 * @param subPathName
	 *            subpath name
	 * @return {@link File}
	 */
	public File getSubFile(String subPathName) {
		return new File(directory, subPathName);
	}

	/**
	 * Get the script base directory.
	 * 
	 * @return script base directory.
	 */
	public File getScriptDirectory() {
		return getSubFile(SCRIPT_PATH);
	}

	/**
	 * Get the script directory for the given user.
	 * 
	 * @param user
	 *            user
	 * @return script directory for the given user.
	 */
	public File getScriptDirectory(User user) {
		return new File(getSubFile(SCRIPT_PATH), user.getUserId());
	}

	/**
	 * Get the plugin directory.
	 * 
	 * @return plugin directory.
	 */
	public File getPluginsDirectory() {
		return getSubFile(PLUGIN_PATH);
	}

	/**
	 * Get the repo base directory.
	 * 
	 * @return repo base directory.
	 */
	public File getRepoDirectoryRoot() {
		return getSubFile(USER_REPO_PATH);
	}

	/**
	 * Get the user repo directory for the given user.
	 * 
	 * @param user
	 *            user
	 * @return user repo directory.
	 */
	public File getUserRepoDirectory(User user) {
		return getUserRepoDirectory(user.getUserId());
	}

	/**
	 * Get the sub directory of the base user repo directory.
	 * 
	 * @param subPath
	 *            subPath
	 * @return base repo sub directory.
	 */
	public File getUserRepoDirectory(String subPath) {
		return new File(getRepoDirectoryRoot(), subPath);
	}

	/**
	 * Get the base perftest directory.
	 * 
	 * @return base perftest directory.
	 */
	public File getPerfTestDirectory() {
		return getSubFile(PERF_TEST_PATH);
	}

	/**
	 * Get the sub directory for given perftest.
	 * 
	 * @param perfTest
	 *            perfTest
	 * @param subPath
	 *            subPath
	 * @return {@link PerfTest} sub directory.
	 */
	private File getPerfTestSubDirectory(PerfTest perfTest, String subPath) {
		return new File(getPerfTestDirectory(perfTest), subPath);
	}

	/**
	 * Get the sub directory for given perftest id.
	 * 
	 * @param id
	 *            perfTest id
	 * @param subPath
	 *            subPath
	 * @return {@link PerfTest} sub directory.
	 */
	public File getPerfTestSubDirectory(String id, String subPath) {
		File file = new File(getPerfTestDirectory(id), subPath);
		file.mkdirs();
		return file;
	}

	/**
	 * Get the sub directory directory for base perftest directory.
	 * 
	 * @param subPath
	 *            subPath
	 * @return {@link PerfTest} sub directory.
	 */
	public File getPerfTestDirectory(String subPath) {
		return new File(getPerfTestDirectory(), subPath);
	}

	/**
	 * Get the root directory for given {@link PerfTest} id.
	 * 
	 * @param perfTest
	 *            perftest
	 * @return {@link PerfTest} log directory
	 */
	public File getPerfTestDirectory(PerfTest perfTest) {
		return getPerfTestDirectory(String.valueOf(perfTest.getId()));
	}

	/**
	 * Get the log directory for given {@link PerfTest} id.
	 * 
	 * @param id
	 *            perftest id
	 * @return {@link PerfTest} log directory
	 */
	public File getPerfTestLogDirectory(String id) {
		return getPerfTestSubDirectory(id, PATH_LOG);
	}

	/**
	 * Get the log directory for given {@link PerfTest}.
	 * 
	 * @param perfTest
	 *            perftest
	 * @return {@link PerfTest} log directory
	 */
	public File getPerfTestLogDirectory(PerfTest perfTest) {
		return getPerfTestSubDirectory(perfTest, PATH_LOG);
	}

	/**
	 * Get the distribution directory for given {@link PerfTest}.
	 * 
	 * @param perfTest
	 *            perftest
	 * @return {@link PerfTest} distribution directory
	 */
	public File getPerfTestDistDirectory(PerfTest perfTest) {
		return getPerfTestSubDirectory(perfTest, PATH_DIST);
	}
	
	/**
	 * Get the statistics directory for given {@link PerfTest}.
	 * 
	 * @param perfTest
	 *            perftest
	 * @return {@link PerfTest} statistics directory
	 */
	public File getPerfTestStatisticPath(PerfTest perfTest) {
		return getPerfTestSubDirectory(perfTest, PATH_STAT);
	}

	/**
	 * Get the report directory for given {@link PerfTest} id.
	 * 
	 * @param id
	 *            perftest id
	 * @return {@link PerfTest} report directory
	 */
	public File getPerfTestReportDirectory(String id) {
		return getPerfTestSubDirectory(id, PATH_REPORT);
	}

	/**
	 * Get the report directory for given {@link PerfTest}.
	 * 
	 * @param perfTest
	 *            perftest
	 * @return {@link PerfTest} report directory
	 */
	public File getPerfTestReportDirectory(PerfTest perfTest) {
		return getPerfTestSubDirectory(perfTest, PATH_REPORT);
	}

	/**
	 * Get default grinder properties file.
	 * 
	 * @return grinder properties file
	 */
	public File getDefaultGrinderProperties() {
		return getSubFile(DEFAULT_GRINDER_PROPERTIES_PATH);
	}

	/**
	 * Get download directory.
	 * 
	 * @return download directory
	 */
	public File getDownloadDirectory() {
		return getSubFile(DOWNLOAD_PATH);
	}

	/**
	 * Get download directory.
	 * 
	 * @return download directory
	 */
	public File getControllerShareDirectory() {
		File subFile = getSubFile(SHARE_PATH);
		File controller = new File(subFile, CONTROLLER_PATH);
		controller.mkdirs();
		return controller;
	}
	/**
	 * Get global log file.
	 * 
	 * @return log file
	 */
	public File getGlobalLogFile() {
		File subFile = getSubFile(GLOBAL_LOG_PATH);
		return subFile;
	}

	/**
	 * Check the this home exists.
	 * 
	 * @return true if exists.
	 */
	public boolean exists() {
		return directory.exists();
	}

}
