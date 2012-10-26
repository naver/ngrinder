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
package org.ngrinder.infra;

import static org.ngrinder.common.util.NoOp.noOp;
import static org.ngrinder.common.util.Preconditions.checkNotNull;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Properties;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.ngrinder.common.exception.ConfigurationException;
import org.ngrinder.common.exception.NGrinderRuntimeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class which represents AgentHome.
 * 
 * @author JunHo Yoon
 * @since 3.0
 * 
 */
public class AgentHome {

	private final File directory;
	private static final Logger LOGGER = LoggerFactory.getLogger(AgentHome.class);

	/**
	 * Constructor.
	 * 
	 * @param directory
	 *            agent home directory
	 */
	public AgentHome(File directory) {
		checkNotNull(directory, "The directory should not be null.").mkdirs();
		if (!directory.canWrite()) {
			String message = String.format("nGrinder home directory %s is not writable.", directory);
			throw new ConfigurationException(message, null);
		}
		this.directory = directory;
	}

	/**
	 * Get agent home directory.
	 * 
	 * @return agent home directory
	 */
	public File getDirectory() {
		return directory;
	}

	/**
	 * Copy {@link InputStream} to path in the target.
	 * 
	 * @param io
	 *            {@link InputStream}
	 * @param target
	 *            target path. only file name will be used.
	 * @param overwrite
	 *            true if overwrite
	 */
	public void copyFileTo(InputStream io, File target, boolean overwrite) {
		// Copy missing files
		try {
			target = new File(directory, target.getName());
			if (!(target.exists())) {
				FileUtils.writeByteArrayToFile(target, IOUtils.toByteArray(io));
			}
		} catch (IOException e) {
			String message = "Failed to write a file to " + target.getAbsolutePath();
			throw new NGrinderRuntimeException(message, e);
		}
	}

	/**
	 * Create sub path on the agent home directory.
	 * 
	 * @param subPathName
	 *            sub path name
	 */
	// not used
	// public void makeSubPath(String subPathName) {
	// File subFile = new File(directory, subPathName);
	// if (!subFile.exists()) {
	// subFile.mkdir();
	// }
	// }

	/**
	 * Get properties from path.
	 * 
	 * @param path
	 *            property file
	 * @return {@link Properties} instance. empy property if it has problem.
	 */
	public Properties getProperties(String path) {
		Properties properties = new Properties();
		InputStream is = null;
		try {
			File propertiesFile = new File(directory, path);
			is = FileUtils.openInputStream(propertiesFile);
			properties.load(is);
		} catch (IOException e) {
			noOp();
		} finally {
			IOUtils.closeQuietly(is);
		}
		return properties;

	}

	/**
	 * Save properties.
	 * 
	 * @param path
	 *            path to save
	 * @param properties
	 *            properties.
	 */
	public void saveProperties(String path, Properties properties) {
		OutputStream out = null;
		try {
			File propertiesFile = new File(getDirectory(), path);
			out = FileUtils.openOutputStream(propertiesFile);
			properties.store(out, null);
		} catch (IOException e) {
			LOGGER.error("Could not save property  file on " + path, e);
		} finally {
			IOUtils.closeQuietly(out);
		}
	}

	public File getLogDirectory() {
		return new File(getDirectory(), "log");
	}
}
