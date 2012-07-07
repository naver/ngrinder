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
package org.ngrinder.common.model;

import static org.ngrinder.common.util.Preconditions.checkNotNull;

import java.io.File;
import java.io.IOException;
import java.util.Properties;

import org.apache.commons.io.FileUtils;
import org.ngrinder.common.constant.NGrinderConstants;
import org.ngrinder.common.exception.ConfigurationException;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.support.PropertiesLoaderUtils;

/**
 * Home class which enable you to easily access resources in Home directory.
 * 
 * @author JunHo Yoon
 */
public class Home implements NGrinderConstants {
	private final File directory;

	public Home(File directory) {
		checkNotNull(directory, "directory should not be null").mkdir();
		if (!directory.canWrite()) {
			throw new ConfigurationException(String.format(" ngrinder home directory %s is not writable.", directory),
					null);
		}
		this.directory = directory;
	}
	
	/**
	 * Get home directory
	 * @return
	 */
	public File getDirectory() {
		return directory;
	}

	public void copyFrom(File from, boolean overwrite) {
		// Copy missing files
		try {
			for (File file : from.listFiles()) {
				if (!new File(directory, file.getName()).exists()) {
					FileUtils.copyFileToDirectory(file, directory);
				}
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public void makeSubPath(String subPathName) {
		File subFile = new File(directory, subPathName);
		if (!subFile.exists()) {
			subFile.mkdir();
		}
	}

	public Properties getProperties(String confFileName) {
		try {
			FileSystemResource propertyResource = new FileSystemResource(getSubFile(confFileName));
			return PropertiesLoaderUtils.loadProperties(propertyResource);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public File getSubFile(String subPathName) {
		return new File(directory, subPathName);
	}

	public File getScriptDirectory() {
		return getSubFile(SCRIPT_PATH);
	}

	public File getProjectDirectory() {
		return getSubFile(PROJECT_PATH);
	}

	public File getPluginsDirectory() {
		return getSubFile(PLUGIN_PATH);
	}

}
