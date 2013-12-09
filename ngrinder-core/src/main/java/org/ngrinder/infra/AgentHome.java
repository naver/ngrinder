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
package org.ngrinder.infra;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.Properties;

import static org.ngrinder.common.util.ExceptionUtils.processException;
import static org.ngrinder.common.util.NoOp.noOp;
import static org.ngrinder.common.util.Preconditions.checkNotNull;

/**
 * Class which represents AgentHome.
 *
 * @author JunHo Yoon
 * @since 3.0
 */
public class AgentHome {

	private final File directory;
	private static final Logger LOGGER = LoggerFactory.getLogger(AgentHome.class);

	/**
	 * Constructor.
	 *
	 * @param directory agent home directory
	 */
	public AgentHome(File directory) {
		checkNotNull(directory, "The directory should not be null.");
		if (StringUtils.contains(directory.getAbsolutePath().trim(), " ")) {
			throw processException(String.format(
					"nGrinder agent home directory \"%s\" should not contain space."
							+ "Please set NGRINDER_AGENT_HOME env var in the different location",
					directory.getAbsolutePath()));
		}

		if (!directory.exists() && !directory.mkdirs()) {
			throw processException(String.format(
					"nGrinder agent home directory %s is not created. Please check the permission",
					directory.getAbsolutePath()));
		}

		if (!directory.isDirectory()) {
			throw processException(String.format(
					"nGrinder home directory %s is not directory. Please delete this sourceFile in advance",
					directory.getAbsolutePath()));
		}

		if (!directory.canWrite()) {
			throw processException(String.format(
					"nGrinder home directory %s is not writable. Please adjust permission on this folder", directory));
		}

		this.directory = directory;
	}

	/**
	 * Get the agent home directory.
	 *
	 * @return agent home directory
	 */
	public File getDirectory() {
		return directory;
	}

	/**
	 * Get agent native directory.
	 *
	 * @return agent native directory
	 */
	public File getNativeDirectory() {
		return mkDir(getFile("native"));
	}

	public File mkDir(File file) {
		if (!file.exists()) {
			if (file.mkdirs()) {
				LOGGER.info("{} is created.", file.getPath());
			}
		}
		return file;
	}

	/**
	 * Get temp directory.
	 *
	 * @return temp directory
	 */
	public File getTempDirectory() {
		return mkDir(getFile("temp"));
	}

	/**
	 * Copy the {@link File} to path in the home.
	 *
	 * @param sourceFile {@link File}
	 * @param target     target path. only sourceFile name will be used.
	 */
	public void copyFileTo(File sourceFile, String target) {
		// Copy missing files
		File targetFile = new File(directory, target);
		try {
			FileUtils.copyFile(sourceFile, targetFile);
		} catch (IOException e) {
			throw processException("Failed to write a sourceFile to " + target, e);
		}
	}

	/**
	 * Write the content to path in the home.
	 *
	 * @param content {@link File}
	 * @param target  target path. only sourceFile name will be used.
	 */
	public void writeFileTo(String content, String target) {
		File targetFile = new File(directory, target);
		try {
			FileUtils.write(targetFile, content);
		} catch (IOException e) {
			throw processException("Failed to write a sourceFile to " + target, e);
		}
	}

	/**
	 * Get the properties from path.
	 *
	 * @param path property sourceFile path
	 * @return {@link Properties} instance. return empty property if it has
	 *         problem.
	 */
	public Properties getProperties(String path) {
		Properties properties = new Properties();
		try {
			File propertiesFile = new File(directory, path);
			String config = FileUtils.readFileToString(propertiesFile, "UTF-8");
			properties.load(new StringReader(config));
		} catch (IOException e) {
			noOp();
		}
		return properties;

	}

	/**
	 * Get the sourceFile from the given path.
	 *
	 * @param path path
	 * @return {@link File} instance.
	 */
	public File getFile(String path) {
		return new File(getDirectory(), path);
	}

	/**
	 * Save properties.
	 *
	 * @param path       path to save
	 * @param properties properties.
	 */
	public void saveProperties(String path, Properties properties) {
		OutputStream out = null;
		try {
			File propertiesFile = new File(getDirectory(), path);
			out = FileUtils.openOutputStream(propertiesFile);
			properties.store(out, null);
		} catch (IOException e) {
			LOGGER.error("Could not save property  sourceFile on " + path, e);
		} finally {
			IOUtils.closeQuietly(out);
		}
	}

	public File getLogDirectory() {
		return new File(getDirectory(), "log");
	}
}
