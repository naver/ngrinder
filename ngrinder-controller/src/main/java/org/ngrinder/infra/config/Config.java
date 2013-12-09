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

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.core.joran.spi.JoranException;
import net.grinder.util.ListenerSupport;
import net.grinder.util.ListenerSupport.Informer;
import net.grinder.util.NetworkUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.ngrinder.common.constant.Constants;
import org.ngrinder.common.exception.ConfigurationException;
import org.ngrinder.common.model.Home;
import org.ngrinder.common.util.FileWatchdog;
import org.ngrinder.common.util.PropertiesWrapper;
import org.ngrinder.infra.AgentConfig;
import org.ngrinder.infra.logger.CoreLogger;
import org.ngrinder.infra.spring.SpringContext;
import org.ngrinder.monitor.MonitorConstants;
import org.ngrinder.service.AbstractConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.Properties;

import static org.ngrinder.common.util.Preconditions.checkNotNull;

/**
 * Spring component which is responsible to get the nGrinder configurations which is stored ${NGRINDER_HOME}.
 *
 * @author JunHo Yoon
 * @since 3.0
 */
@Component
public class Config extends AbstractConfig implements Constants {
	private static final String NGRINDER_DEFAULT_FOLDER = ".ngrinder";
	private static final String NGRINDER_EX_FOLDER = ".ngrinder_ex";
	private static final Logger LOG = LoggerFactory.getLogger(Config.class);
	private Home home = null;
	private Home exHome = null;
	private PropertiesWrapper internalProperties;
	private PropertiesWrapper systemProperties;
	private PropertiesWrapper databaseProperties;
	private String announcement;
	private Date announcementDate;
	private String versionString = "";
	private boolean verbose;
	private String currentIP;

	public static final int NGRINDER_DEFAULT_CLUSTER_LISTENER_PORT = 40003;

	public static final String NONE_REGION = "NONE";
	private boolean cluster;
	private ListenerSupport<PropertyChangeListener> systemConfListeners = new ListenerSupport<PropertyChangeListener>();
	@Autowired
	private SpringContext context;

	/**
	 * Make it singleton.
	 */
	Config() {
	}

	/**
	 * Add the system configuration change listener.
	 *
	 * @param listener listener
	 */
	public void addSystemConfListener(PropertyChangeListener listener) {
		systemConfListeners.add(listener);
	}

	/**
	 * Initialize the {@link Config} object.
	 *
	 * This method mainly resolves ${NGRINDER_HOME} and loads system properties. In addition, the logger is initialized
	 * and the default configuration files are copied into ${NGRINDER_HOME} if they do not exists.
	 */
	@PostConstruct
	public void init() {
		try {
			CoreLogger.LOGGER.info("nGrinder is starting...");
			home = resolveHome();
			exHome = resolveExHome();
			copyDefaultConfigurationFiles();
			loadInternalProperties();
			loadSystemProperties();
			initHomeMonitor();
			// Load cluster in advance. cluster mode is not dynamically
			// reloadable.
			cluster = getSystemProperties().getPropertyBoolean(Constants.NGRINDER_PROP_CLUSTER_MODE, false);
			initLogger(isTestMode());
			resolveLocalIp();
			loadAnnouncement();
			loadDatabaseProperties();
			versionString = getVersion();
		} catch (IOException e) {
			throw new ConfigurationException("Error while init nGrinder", e);
		}
	}

	protected void resolveLocalIp() {
		currentIP = getSystemProperties().getPropertyWithBackwardCompatibility("ngrinder.controller.ip",
				"ngrinder.controller.ipaddress", "");
	}

	/**
	 * Destroy bean.
	 */
	@PreDestroy
	public void destroy() {
		// Stop all the non-daemon thread.
		announcementWatchDog.interrupt();
		systemConfWatchDog.interrupt();
		policyJsWatchDog.interrupt();
	}

	/**
	 * Get if the cluster mode is enable or not.
	 *
	 * @return true if the cluster mode is enabled.
	 * @since 3.1
	 */
	public boolean isClustered() {
		return cluster;
	}

	/**
	 * Get the ngrinder instance IPs consisting of the current cluster from the configuration.
	 *
	 * @return ngrinder instance IPs
	 */
	public String[] getClusterURIs() {
		String clusterUri = getSystemProperties().getProperty(NGRINDER_PROP_CLUSTER_URIS, "");
		return StringUtils.split(clusterUri, ";");
	}

	/**
	 * Get the current region from the configuration.
	 *
	 * @return region. If it's not clustered mode, return "NONE"
	 */
	public String getRegion() {
		return isClustered() ? getSystemProperties().getProperty(NGRINDER_PROP_REGION, NONE_REGION) : NONE_REGION;
	}

	/**
	 * Get the monitor listener port from the configuration.
	 *
	 * @return monitor port
	 */
	public int getMonitorPort() {
		return getSystemProperties().getPropertyInt(AgentConfig.MONITOR_LISTEN_PORT,
				MonitorConstants.DEFAULT_MONITOR_PORT);
	}

	/**
	 * Check if the periodic usage report is enabled.
	 *
	 * @return true if enabled.
	 */
	public boolean isUsageReportEnabled() {
		return getSystemProperties().getPropertyBoolean(Constants.NGRINDER_PROP_USAGE_REPORT, true);
	}

	/**
	 * Check if user self-registration is enabled.
	 *
	 * @return true if enabled.
	 */
	public boolean isSelfUserRegistration() {
		return getSystemProperties().getPropertyBoolean(Constants.NGRINDER_USER_SELF_REGISTRATION, false);
	}

	/**
	 * Initialize Logger.
	 *
	 * @param forceToVerbose true to force verbose logging.
	 */
	public synchronized void initLogger(boolean forceToVerbose) {
		setupLogger((forceToVerbose) || getSystemProperties().getPropertyBoolean("verbose", false));
	}

	/**
	 * Set up the logger.
	 *
	 * @param verbose verbose mode?
	 */
	protected void setupLogger(boolean verbose) {
		this.verbose = verbose;
		final LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();
		final JoranConfigurator configurator = new JoranConfigurator();
		configurator.setContext(context);
		context.reset();
		context.putProperty("LOG_LEVEL", verbose ? "DEBUG" : "INFO");
		File logbackConf = home.getSubFile("logback.xml");
		try {
			if (!logbackConf.exists()) {
				logbackConf = new ClassPathResource("/logback/logback-ngrinder.xml").getFile();
				if (exHome.exists() && isClustered()) {
					context.putProperty("LOG_DIRECTORY", exHome.getGlobalLogFile().getAbsolutePath());
					context.putProperty("SUFFIX", "_" + getRegion());
				} else {
					context.putProperty("SUFFIX", "");
					context.putProperty("LOG_DIRECTORY", home.getGlobalLogFile().getAbsolutePath());
				}
			}
			configurator.doConfigure(logbackConf);
		} catch (JoranException e) {
			CoreLogger.LOGGER.error(e.getMessage(), e);
		} catch (IOException e) {
			CoreLogger.LOGGER.error(e.getMessage(), e);
		}
	}

	/**
	 * Copy the default files and create default directories to ${NGRINDER_HOME}.
	 *
	 * @throws IOException occurs when there is no such a files.
	 */
	protected void copyDefaultConfigurationFiles() throws IOException {
		checkNotNull(home);
		home.copyFrom(new ClassPathResource("ngrinder_home_template").getFile());
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
		if (StringUtils.isNotBlank(System.getProperty("unit-test"))) {
			final String tempDir = System.getProperty("java.io.tmpdir");
			final File tmpHome = new File(tempDir, ".ngrinder");
			if (tmpHome.mkdirs()) {
				LOG.info("{} is created", tmpHome.getPath());
			}
			try {
				FileUtils.forceDeleteOnExit(tmpHome);
			} catch (IOException e) {
				LOG.error("Error while setting forceDeleteOnExit on {}", tmpHome);
			}
			return new Home(tmpHome);
		}
		String userHomeFromEnv = System.getenv("NGRINDER_HOME");
		String userHomeFromProperty = System.getProperty("ngrinder.home");
		if (!StringUtils.equals(userHomeFromEnv, userHomeFromProperty)) {
			CoreLogger.LOGGER.warn("The path to ngrinder-home is ambiguous:");
			CoreLogger.LOGGER.warn("    System Environment:  NGRINDER_HOME=" + userHomeFromEnv);
			CoreLogger.LOGGER.warn("    Java System Property:  ngrinder.home=" + userHomeFromProperty);
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
			CoreLogger.LOGGER.warn("    Java System Property:  ngrinder.exhome=" + exHomeFromProperty);
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
	protected void loadInternalProperties() {
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
	 * Load the announcement content.
	 */
	public synchronized void loadAnnouncement() {
		checkNotNull(home);
		File sysFile = home.getSubFile("announcement.conf");
		try {
			announcement = FileUtils.readFileToString(sysFile, "UTF-8");
			if (sysFile.exists()) {
				announcementDate = new Date(sysFile.lastModified());
			} else {
				announcementDate = null;
			}
		} catch (IOException e) {
			CoreLogger.LOGGER.error("Error while reading announcement file.", e);
			announcement = "";
		}
	}

	/**
	 * watch docs.
	 */
	private FileWatchdog announcementWatchDog;
	private FileWatchdog systemConfWatchDog;
	private FileWatchdog policyJsWatchDog;

	protected void initHomeMonitor() {
		checkNotNull(home);
		this.announcementWatchDog = new FileWatchdog(getHome().getSubFile("announcement.conf").getAbsolutePath()) {
			@Override
			protected void doOnChange() {
				CoreLogger.LOGGER.info("Announcement file is changed.");
				loadAnnouncement();
			}
		};
		announcementWatchDog.setName("WatchDog - announcement.conf");
		announcementWatchDog.setDelay(2000);
		announcementWatchDog.start();
		this.systemConfWatchDog = new FileWatchdog(getHome().getSubFile("system.conf").getAbsolutePath()) {
			@Override
			protected void doOnChange() {
				try {
					CoreLogger.LOGGER.info("System configuration(system.conf) is changed.");
					loadSystemProperties();
					resolveLocalIp();
					systemConfListeners.apply(new Informer<PropertyChangeListener>() {
						@Override
						public void inform(PropertyChangeListener listener) {
							listener.propertyChange(null);
						}
					});
					CoreLogger.LOGGER.info("New system configuration is applied.");
				} catch (Exception e) {
					CoreLogger.LOGGER.error("Error occurs while applying new system configuration", e);
				}

			}
		};
		systemConfWatchDog.setName("WatchDoc - system.conf");
		systemConfWatchDog.setDelay(2000);
		systemConfWatchDog.start();

		String processThreadPolicyPath = getHome().getSubFile("process_and_thread_policy.js").getAbsolutePath();
		this.policyJsWatchDog = new FileWatchdog(processThreadPolicyPath) {
			@Override
			protected void doOnChange() {
				CoreLogger.LOGGER.info("process_and_thread_policy file is changed.");
				policyScript = "";
			}
		};
		policyJsWatchDog.setName("WatchDoc - process_and_thread_policy.js");
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
		if (context.isUnitTestContext()) {
			databaseProperties.addProperty("unit-test", "true");
		}
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
	 * Check if the user security is enabled.
	 *
	 * @return true if user security is enabled.
	 */
	public boolean isUserSecurityEnabled() {
		return getSystemProperties().getPropertyBoolean("user.security", true);
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
	 * Check if it is the demo mode.
	 *
	 * @return true if demo mode is enabled.
	 */
	public boolean isDemo() {
		return getSystemProperties().getPropertyBoolean("demo", false);
	}

	/**
	 * Check if the plugin support is enabled.
	 *
	 * The reason why we need this configuration is that it takes time to initialize plugin system in unit test context.
	 *
	 * @return true if the plugin is supported.
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
	 * @return home
	 * @since 3.1
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
	 * Get the announcement content.
	 *
	 * @return loaded from announcement.conf.
	 */
	public String getAnnouncement() {
		return announcement;
	}

	/**
	 * Get the nGrinder version number.
	 *
	 * @return nGrinder version number. If not set, return "0.0.1"
	 */
	public String getVersion() {
		return getInternalProperties().getProperty("ngrinder.version", "0.0.1");
	}

	/**
	 * Policy file which is used to determine the count of processes and threads.
	 */
	private String policyScript = "";

	/**
	 * Get the content of "process_and_thread_policy.js" file.
	 *
	 * @return loaded file content.
	 */
	public String getProcessAndThreadPolicyScript() {
		if (StringUtils.isEmpty(policyScript)) {
			try {
				policyScript = FileUtils.readFileToString(getHome().getSubFile("process_and_thread_policy.js"));
				return policyScript;
			} catch (IOException e) {
				LOG.error("Error while load process_and_thread_policy.js", e);
			}
		}
		return policyScript;
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
	 * Check if it's verbose logging mode.
	 *
	 * @return true if verbose
	 */
	public boolean isVerbose() {
		return verbose;
	}

	/**
	 * Get the currently configured controller IP.
	 *
	 * @return current IP.
	 */
	public String getCurrentIP() {
		return currentIP;
	}


	/**
	 * Check if the current ngrinder instance is hidden instance from the cluster.
	 *
	 * @return true if hidden.
	 */
	public boolean isInvisibleRegion() {
		return getSystemProperties().getPropertyBoolean(NGRINDER_PROP_REGION_HIDE, false);
	}

	/**
	 * Check if no_more_test.lock to block further test executions exists.
	 *
	 * @return true if it exists
	 */
	public boolean hasNoMoreTestLock() {
		return exHome.exists() && exHome.getSubFile("no_more_test.lock").exists();
	}

	/**
	 * Check if shutdown.lock exists.
	 *
	 * @return true if it exists
	 */
	public boolean hasShutdownLock() {
		return exHome.exists() && exHome.getSubFile("shutdown.lock").exists();
	}

	/**
	 * Get the date of the recent announcement modification.
	 *
	 * @return the date of the recent announcement modification.
	 */
	public Date getAnnouncementDate() {
		return announcementDate;
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

	/**
	 * Get the current controller public IP.
	 *
	 * @return public IP.
	 */
	public String getCurrentPublicIP() {
		return NetworkUtils.DEFAULT_LOCAL_HOST_ADDRESS;
	}
}
