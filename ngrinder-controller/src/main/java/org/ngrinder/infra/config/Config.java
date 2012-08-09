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

import static org.ngrinder.common.constant.NGrinderConstants.DOWNLOAD_PATH;
import static org.ngrinder.common.constant.NGrinderConstants.PERF_TEST_PATH;
import static org.ngrinder.common.constant.NGrinderConstants.PLUGIN_PATH;
import static org.ngrinder.common.util.Preconditions.checkNotNull;

import java.io.File;
import java.io.IOException;
import java.util.Properties;

import javax.annotation.PostConstruct;

import org.apache.commons.lang.BooleanUtils;
import org.apache.commons.lang.StringUtils;
import org.ngrinder.common.exception.ConfigurationException;
import org.ngrinder.common.model.Home;
import org.ngrinder.common.util.PropertiesWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

/**
 * Spring component which is responsible to get the nGrinder config which is stored ${NGRINDER_HOME}.
 * 
 * @author JunHo Yoon
 * @since 3.0
 */
@Component
public class Config {
	private static final String NGRINDER_DEFAULT_FOLDER = ".ngrinder";
	private static final Logger logger = LoggerFactory.getLogger(Config.class);
	private PropertiesWrapper databaseProperties;

	/**
	 * Make it singleton
	 */
	Config() {
	}

	private Home home = null;
	private PropertiesWrapper systemProperties;

	@PostConstruct
	public void init() {
		try {
			home = resolveHome();
			copyDefaultConfigurationFiles();
			loadDatabaseProperties();
			loadSystemProperties();
		} catch (IOException e) {
			throw new ConfigurationException("Error while loading NGRINDER_HOME", e);
		}
	}

	private void copyDefaultConfigurationFiles() throws IOException {
		checkNotNull(home);
		home.copyFrom(new ClassPathResource("ngrinder_home_template").getFile(), false);
		home.makeSubPath(PLUGIN_PATH);
		home.makeSubPath(PERF_TEST_PATH);
		home.makeSubPath(DOWNLOAD_PATH);

	}

	/**
	 * NGrinder home path
	 * 
	 * @return
	 */
	private Home resolveHome() {
		String userHomeFromEnv = System.getenv("NGRINDER_HOME");
		String userHomeFromProperty = System.getProperty("ngrinder.home");
		if (StringUtils.isNotEmpty(userHomeFromEnv) && !StringUtils.equals(userHomeFromEnv, userHomeFromProperty)) {
			logger.warn("The path to ngrinder-home is ambiguous:");
			logger.warn("    System Environment:  NGRINDER_HOME=" + userHomeFromEnv);
			logger.warn("    Java Sytem Property:  ngrinder.home=" + userHomeFromProperty);
			logger.warn("    '" + userHomeFromProperty + "' is accepted.");
		}
		String userHome = null;
		userHome = StringUtils.defaultIfEmpty(userHomeFromProperty, userHomeFromEnv);
		File homeDirectory = (StringUtils.isNotEmpty(userHome)) ? new File(userHome) : new File(
				System.getProperty("user.home"), NGRINDER_DEFAULT_FOLDER);

		return new Home(homeDirectory);
	}

	private void loadDatabaseProperties() throws IOException {
		checkNotNull(home);
		Properties properties = home.getProperties("database.conf");
		properties.put("NGRINDER_HOME", home.getDirectory().getAbsolutePath());
		databaseProperties = new PropertiesWrapper(properties);

	}

	private void loadSystemProperties() {
		checkNotNull(home);
		Properties properties = home.getProperties("system.conf");
		properties.put("NGRINDER_HOME", home.getDirectory().getAbsolutePath());
		systemProperties = new PropertiesWrapper(properties);
	}

	public PropertiesWrapper getDatabaseProperties() {
		checkNotNull(databaseProperties);
		return databaseProperties;
	}

	/**
	 * if there is testmode property in system.properties.. return true
	 * 
	 * @return
	 */
	public boolean isTestMode() {
		return BooleanUtils.toBoolean(getSystemProperty("testmode", "false"));
	}

	/**
	 * if there is testmode property in system.properties.. return true
	 * 
	 * @return
	 */
	public boolean isPluginSupported() {
		return (BooleanUtils.toBoolean(getSystemProperty("pluginsupport", "true")) || !isTestMode());
	}

	String getSystemProperty(String key, String defaultValue) {
		return getSystemProperties().getProperty(key, defaultValue);
	}

	public Home getHome() {
		return this.home;
	}

	public PropertiesWrapper getSystemProperties() {
		checkNotNull(systemProperties);
		return systemProperties;
	}

}
