package com.nhncorp.ngrinder.core.model;

import static com.nhncorp.ngrinder.core.util.Preconditions.checkNotNull;

import java.io.File;
import java.io.IOException;
import java.util.Properties;

import org.apache.commons.io.FileUtils;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.support.PropertiesLoaderUtils;

import com.nhncorp.ngrinder.core.exception.ConfigurationException;

/**
 * Home class which enable you to easily access resources in Home directory.
 * 
 * @author junoyoon
 */
public class Home {
	private final File directory;

	public Home(File directory) {
		checkNotNull(directory, "directory should not be null").mkdir();
		if (!directory.canWrite()) {
			throw new ConfigurationException(String.format(" ngrinder home directory %s is not writable.", directory),
					null);
		}
		this.directory = directory;
	}

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
		return getSubFile("script");
	}

	public File getPluginsDirectory() {
		return getSubFile("plugins");
	}
}
