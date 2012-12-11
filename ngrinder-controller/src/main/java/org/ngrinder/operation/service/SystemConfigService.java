package org.ngrinder.operation.service;

import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.ngrinder.infra.config.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * System configuration file operating service.
 * 
 * @author Alex Qin
 * @since 3.1
 */
@Service
public class SystemConfigService {

	private static final Logger LOG = LoggerFactory.getLogger(SystemConfigService.class);

	@Autowired
	private Config config;

	/**
	 * Get system configuration file content.
	 * 
	 * @return file content.
	 */
	public String getSystemConfigFile() {
		try {
			return FileUtils.readFileToString(config.getHome().getSubFile("system.conf"), "UTF-8");
		} catch (Exception e) {
			LOG.error("Error while reading system configuration file.");
			return null;
		}
	}

	/**
	 * Save content to system configuration file.
	 * 
	 * @param content
	 *            file content.
	 * @return save successfully or not.
	 */
	public boolean saveSystemConfigFile(String content) {
		try {
			FileUtils.writeStringToFile(config.getHome().getSubFile("system.conf"), content, "UTF-8");
		} catch (IOException e) {
			LOG.error("Error while writing system configuration file.");
			return false;
		}
		return true;
	}
}
