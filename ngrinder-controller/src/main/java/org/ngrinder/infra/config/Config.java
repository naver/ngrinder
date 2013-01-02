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
package org.ngrinder.infra.config;

import static org.ngrinder.common.util.Preconditions.checkNotNull;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import javax.annotation.PostConstruct;

import net.grinder.util.NetworkUtil;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.helpers.FileWatchdog;
import org.ngrinder.common.constant.NGrinderConstants;
import org.ngrinder.common.exception.ConfigurationException;
import org.ngrinder.common.model.Home;
import org.ngrinder.common.util.PropertiesWrapper;
import org.ngrinder.infra.logger.CoreLogger;
import org.ngrinder.service.IConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.core.joran.spi.JoranException;

/**
 * Spring component which is responsible to get the nGrinder configurations which is stored
 * ${NGRINDER_HOME}.
 * 
 * @author JunHo Yoon
 * @since 3.0
 */
@Component
public class Config implements IConfig, NGrinderConstants {
	private static final String NGRINDER_DEFAULT_FOLDER = ".ngrinder";
	private static final String NGRINDER_EX_FOLDER = ".ngrinder_ex";
	public static final String MONITOR_FILE_PREFIX = "monitor_system_";

	private static final Logger LOG = LoggerFactory.getLogger(Config.class);
	private Home home = null;
	private Home exHome = null;
	private PropertiesWrapper internalProperties;
	private PropertiesWrapper systemProperties;
	private PropertiesWrapper databaseProperties;
	private String announcement;
	private static String versionString = "";
	private boolean verbose;
	private String currentIP;

	public static final int NGRINDER_DEFAULT_CLUSTER_LISTENER_PORT = 40003;

	public static final String NONE_REGION = "NONE";
	private boolean cluster;

	/**
	 * Make it singleton.
	 */
	Config() {
	}

	/**
	 * Initialize Config. This method mainly perform NGRINDER_HOME resolution and system properties
	 * load. In addition, Logger is initialized and default configuration file is copied into
	 * NGRINDER_HOME if it's the first
	 */
	@PostConstruct
	public void init() {
		try {
			CoreLogger.LOGGER.info("NGrinder is starting...");
			home = resolveHome();
			exHome = resolveExHome();
			copyDefaultConfigurationFiles();
			loadIntrenalProperties();
			loadSystemProperties();
			initHomeMonitor();
			// Load cluster in advance. cluster mode is not dynamically
			// reloadable.
			cluster = getSystemProperties().getPropertyBoolean(NGrinderConstants.NGRINDER_PROP_CLUSTER_MODE, false);
			initLogger(isTestMode());
			resolveLocalIp();
			loadDatabaseProperties();
			// check cluster, get cluster configuration for ehcache
			verifyClusterConfig();
			versionString = getVesion();
		} catch (IOException e) {
			throw new ConfigurationException("Error while init nGrinder", e);
		}
	}

	protected void resolveLocalIp() {
		currentIP = NetworkUtil.getLocalHostAddress();
	}

	/**
	 * Verify clustering is set up well.
	 * 
	 * @since 3.1
	 */
	protected void verifyClusterConfig() {
		if (isCluster()) {
			if (getRegion().equals(NONE_REGION)) {
				LOG.error("Region is not set in cluster mode. Please set ngrinder.region properly.");
			} else {
				CoreLogger.LOGGER.info("Cache cluster URIs:{}", getClusterURIs());
				// set rmi server host for remote serving. Otherwise, maybe it
				// will use 127.0.0.1 to
				// serve.
				// then the remote client can not connect.
				CoreLogger.LOGGER.info("Set current IP:{} for RMI server.", getCurrentIP());
				System.setProperty("java.rmi.server.hostname", getCurrentIP());
			}
		}
	}

	/**
	 * Check whether the cache cluster is set.
	 * 
	 * @return true is cache cluster set
	 * @since 3.1
	 */
	public boolean isCluster() {
		return cluster;
	}

	/**
	 * Get the cluster URIs in configuration.
	 * 
	 * @return cluster uri strings
	 */
	public String[] getClusterURIs() {
		String clusterUri = getSystemProperties().getProperty(NGRINDER_PROP_CLUSTER_URIS, "");
		return StringUtils.split(clusterUri, ";");
	}

	/**
	 * Get the region in configuration.
	 * 
	 * @return region
	 */
	public String getRegion() {
		return isCluster() ? getSystemProperties().getProperty(NGRINDER_PROP_REGION, NONE_REGION) : NONE_REGION;
	}

	/**
	 * Initialize Logger.
	 * 
	 * @param forceToVerbose
	 *            force to verbose logging.
	 */
	public synchronized void initLogger(boolean forceToVerbose) {
		setupLogger((forceToVerbose) ? true : getSystemProperties().getPropertyBoolean("verbose", false));
	}

	/**
	 * Set up logger.
	 * 
	 * @param verbose
	 *            verbose mode?
	 */
	protected void setupLogger(boolean verbose) {
		this.verbose = verbose;
		final LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();
		final JoranConfigurator configurator = new JoranConfigurator();
		configurator.setContext(context);
		context.reset();
		context.putProperty("LOG_LEVEL", verbose ? "DEBUG" : "INFO");
		if (exHome.exists()) {
			context.putProperty("LOG_DIRECTORY", exHome.getGlobalLogFile().getAbsolutePath());
		} else {
			context.putProperty("LOG_DIRECTORY", home.getGlobalLogFile().getAbsolutePath());
		}
		if (!exHome.exists() && isCluster()) {
			context.putProperty("SUFFIX", "_" + getRegion());
		} else {
			context.putProperty("SUFFIX", "");
		}
		try {
			configurator.doConfigure(new ClassPathResource("/logback/logback-ngrinder.xml").getFile());
		} catch (JoranException e) {
			CoreLogger.LOGGER.error(e.getMessage(), e);
		} catch (IOException e) {
			CoreLogger.LOGGER.error(e.getMessage(), e);
		}
	}

	/**
	 * Copy default files.
	 * 
	 * @throws IOException
	 *             occurs when there is no such a files.
	 */
	protected void copyDefaultConfigurationFiles() throws IOException {
		checkNotNull(home);
		home.copyFrom(new ClassPathResource("ngrinder_home_template").getFile(), false);
		home.makeSubPath(PLUGIN_PATH);
		home.makeSubPath(PERF_TEST_PATH);
		home.makeSubPath(DOWNLOAD_PATH);
	}

	/**
	 * Resolve nGrinder home path.
	 * 
	 * @return resolved home
	 */
	protected Home resolveHome() {
		String userHomeFromEnv = System.getenv("NGRINDER_HOME");
		String userHomeFromProperty = System.getProperty("ngrinder.home");
		if (!StringUtils.equals(userHomeFromEnv, userHomeFromProperty)) {
			CoreLogger.LOGGER.warn("The path to ngrinder-home is ambiguous:");
			CoreLogger.LOGGER.warn("    System Environment:  NGRINDER_HOME=" + userHomeFromEnv);
			CoreLogger.LOGGER.warn("    Java Sytem Property:  ngrinder.home=" + userHomeFromProperty);
			CoreLogger.LOGGER.warn("    '" + userHomeFromProperty + "' is accepted.");
		}
		String userHome = StringUtils.defaultIfEmpty(userHomeFromProperty, userHomeFromEnv);
		File homeDirectory = (StringUtils.isNotEmpty(userHome)) ? new File(userHome) : new File(
						System.getProperty("user.home"), NGRINDER_DEFAULT_FOLDER);
		CoreLogger.LOGGER.info("nGrinder home directory:{}.", userHome);

		return new Home(homeDirectory);
	}

	/**
	 * Resolve nGrinder extended home path.
	 * 
	 * @return resolved home
	 */
	protected Home resolveExHome() {
		String exHomeFromEnv = System.getenv("NGRINDER_EX_HOME");
		String exHomeFromProperty = System.getProperty("ngrinder.exhome");
		if (!StringUtils.equals(exHomeFromEnv, exHomeFromProperty)) {
			CoreLogger.LOGGER.warn("The path to ngrinder-exhome is ambiguous:");
			CoreLogger.LOGGER.warn("    System Environment:  NGRINDER_EX_HOME=" + exHomeFromEnv);
			CoreLogger.LOGGER.warn("    Java Sytem Property:  ngrinder.exhome=" + exHomeFromProperty);
			CoreLogger.LOGGER.warn("    '" + exHomeFromProperty + "' is accepted.");
		}
		String userHome = StringUtils.defaultIfEmpty(exHomeFromProperty, exHomeFromEnv);
		File exHomeDirectory = (StringUtils.isNotEmpty(userHome)) ? new File(userHome) : new File(
						System.getProperty("user.home"), NGRINDER_EX_FOLDER);
		CoreLogger.LOGGER.info("nGrinder ex home directory:{}.", exHomeDirectory);

		return new Home(exHomeDirectory, false);
	}

	/**
	 * Load internal properties which is not modifiable by user.
	 */
	protected void loadIntrenalProperties() {
		InputStream inputStream = null;
		Properties properties = new Properties();
		try {
			inputStream = new ClassPathResource("/internal.properties").getInputStream();
			properties.load(inputStream);
			internalProperties = new PropertiesWrapper(properties);
		} catch (IOException e) {
			CoreLogger.LOGGER.error("Error while load internal.properties", e);
			internalProperties = new PropertiesWrapper(properties);
		} finally {
			IOUtils.closeQuietly(inputStream);
		}
	}

	/**
	 * Load database related properties. (database.conf)
	 * 
	 */
	protected void loadDatabaseProperties() {
		checkNotNull(home);
		Properties properties = home.getProperties("database.conf");
		properties.put("NGRINDER_HOME", home.getDirectory().getAbsolutePath());
		databaseProperties = new PropertiesWrapper(properties);
	}

	/**
	 * Load system related properties. (system.conf)
	 */
	public synchronized void loadSystemProperties() {
		checkNotNull(home);
		Properties properties = home.getProperties("system.conf");
		properties.put("NGRINDER_HOME", home.getDirectory().getAbsolutePath());
		// Override if exists
		if (exHome.exists()) {
			Properties exProperties = exHome.getProperties("system-ex.conf");
			properties.putAll(exProperties);
		}
		systemProperties = new PropertiesWrapper(properties);
	}

	/**
	 * Load announcement content.
	 */
	public synchronized void loadAnnouncement() {
		checkNotNull(home);
		File sysFile = home.getSubFile("announcement.conf");
		try {
			announcement = FileUtils.readFileToString(sysFile, "UTF-8");
			return;
		} catch (IOException e) {
			CoreLogger.LOGGER.error("Error while reading announcement file.", e);
			announcement = "";
		}
	}

	/** Configuration watch docs. */
	private FileWatchdog announcementWatchDog;
	private FileWatchdog systemConfWatchDog;
	private FileWatchdog policyJsWatchDog;

	private void initHomeMonitor() {
		checkNotNull(home);
		this.announcementWatchDog = new FileWatchdog(getHome().getSubFile("announcement.conf").getAbsolutePath()) {
			@Override
			protected void doOnChange() {
				CoreLogger.LOGGER.info("Announcement file changed.");
				loadAnnouncement();
			}
		};
		announcementWatchDog.setDelay(2000);
		announcementWatchDog.start();
		this.systemConfWatchDog = new FileWatchdog(getHome().getSubFile("system.conf").getAbsolutePath()) {
			@Override
			protected void doOnChange() {
				CoreLogger.LOGGER.info("System conf file changed.");
				loadSystemProperties();
			}
		};
		systemConfWatchDog.setDelay(2000);
		systemConfWatchDog.start();
		String absolutePath = getHome().getSubFile("process_and_thread_policy.js").getAbsolutePath();
		this.policyJsWatchDog = new FileWatchdog(absolutePath) {
			@Override
			protected void doOnChange() {
				CoreLogger.LOGGER.info("process_and_thread_policy file changed.");
				policyScript = "";
			}
		};
		policyJsWatchDog.setDelay(2000);
		policyJsWatchDog.start();
	}

	/**
	 * Get the database properties.
	 * 
	 * @return database properties
	 */
	public PropertiesWrapper getDatabaseProperties() {
		checkNotNull(databaseProperties);
		return databaseProperties;
	}

	/**
	 * Check if it's test mode.
	 * 
	 * @return true if test mode
	 */
	public boolean isTestMode() {
		return getSystemProperties().getPropertyBoolean("testmode", false);
	}

	/**
	 * Check if it's the security enabled mode.
	 * 
	 * @return true if security is enabled.
	 */
	public boolean isSecurityEnabled() {
		return !isTestMode() && getSystemProperties().getPropertyBoolean("security", false);
	}

	/**
	 * Check if plugin support is enabled. The reason why we need this configuration is that it
	 * takes time to initialize plugin system in unit test context.
	 * 
	 * @return true if plugin is supported.
	 */
	public boolean isPluginSupported() {
		return !isTestMode() && (getSystemProperties().getPropertyBoolean("pluginsupport", true));
	}

	/**
	 * Get the resolved home folder.
	 * 
	 * @return home
	 */
	public Home getHome() {
		return this.home;
	}

	/**
	 * Get the resolved extended home folder.
	 * 
	 * @since 3.1
	 * @return home
	 */
	public Home getExHome() {
		return this.exHome;
	}

	/**
	 * Get the system properties.
	 * 
	 * @return {@link PropertiesWrapper} which is loaded from system.conf.
	 */
	public PropertiesWrapper getSystemProperties() {
		return checkNotNull(systemProperties);
	}

	/**
	 * Get announcement content.
	 * 
	 * @return loaded from announcement.conf.
	 */
	public String getAnnouncement() {
		return announcement;
	}

	/**
	 * Get nGrinder version number.
	 * 
	 * @return nGrinder version number. If not set, return "UNKNOWN"
	 */
	public String getVesion() {
		return getInternalProperties().getProperty("ngrinder.version", "UNKNOWN");
	}

	/**
	 * Policy file which determine the process and thread.
	 */
	private String policyScript = "";

	/**
	 * Get the content of "process_and_thread_policy.js" file.
	 * 
	 * @return file content.
	 */
	public String getProcessAndThreadPolicyScript() {
		if (StringUtils.isEmpty(policyScript)) {
			try {
				policyScript = FileUtils.readFileToString(getHome().getSubFile("process_and_thread_policy.js"));
				return policyScript;
			} catch (IOException e) {
				LOG.error("Error while load process_and_thread_policy.js", e);
				return "";
			}
		} else {
			return policyScript;
		}
	}

	/**
	 * Get the internal properties.
	 * 
	 * @return internal properties
	 */
	public PropertiesWrapper getInternalProperties() {
		return internalProperties;
	}

	/**
	 * Get nGrinder version in static way.
	 * 
	 * @return nGrinder version.
	 */
	public static String getVerionString() {
		return versionString;
	}

	/**
	 * Check if it's verbose logging mode.
	 * 
	 * @return true if verbose
	 */
	public boolean isVerbose() {
		return verbose;
	}

	public String getCurrentIP() {
		return currentIP;
	}

	public boolean isInvisibleRegion() {
		return getSystemProperties().getPropertyBoolean(NGRINDER_PROP_REGION_HIDE, false);
	}

	/**
	 * Check the no more test lock to block further test execution.
	 * 
	 * @return true if it exists
	 */
	public boolean hasNoMoreTestLock() {
		if (exHome.exists()) {
			return exHome.getSubFile("no_more_test.lock").exists();
		}
		return false;
	}

	/**
	 * Check the shutdown lock to exclude this machine from somewhere(maybe L4).
	 * 
	 * @return true if it exists
	 */
	public boolean hasShutdownLock() {
		if (exHome.exists()) {
			return exHome.getSubFile("shutdown.lock").exists();
		}
		return false;
	}

	/**
	 * Get ngrinder help URL.
	 * 
	 * @return help URL
	 */
	public String getHelpUrl() {
		return getSystemProperties().getProperty("ngrinder.help.url",
						"http://www.cubrid.org/wiki_ngrinder/entry/user-guide");
	}

}
