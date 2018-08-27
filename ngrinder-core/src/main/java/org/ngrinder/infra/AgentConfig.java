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

import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.core.Context;
import ch.qos.logback.core.joran.spi.JoranException;
import net.grinder.util.NetworkUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.ngrinder.common.constants.AgentConstants;
import org.ngrinder.common.constants.CommonConstants;
import org.ngrinder.common.constants.MonitorConstants;
import org.ngrinder.common.util.PropertiesKeyMapper;
import org.ngrinder.common.util.PropertiesWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.Set;

import static net.grinder.util.NetworkUtils.DEFAULT_LOCAL_HOST_ADDRESS;
import static org.apache.commons.lang.StringUtils.trimToEmpty;
import static org.ngrinder.common.util.ExceptionUtils.processException;
import static org.ngrinder.common.util.NoOp.noOp;
import static org.ngrinder.common.util.Preconditions.checkNotNull;

/**
 * Spring component which is responsible to get the nGrinder config which is stored
 * ${NGRINDER_AGENT_HOME}.
 *
 * @author JunHo Yoon
 * @since 3.0
 */
public class AgentConfig implements AgentConstants, MonitorConstants, CommonConstants {
	private static final String NGRINDER_DEFAULT_FOLDER = ".ngrinder_agent";
	private static final Logger LOGGER = LoggerFactory.getLogger("agent config");

	protected AgentHome home = null;

	private PropertiesWrapper agentProperties;
	private PropertiesWrapper monitorProperties;
	private PropertiesWrapper commonProperties;
	private PropertiesWrapper internalProperties;

	private PropertiesKeyMapper internalPropertyMapper = PropertiesKeyMapper.create("internal-properties.map");
	private PropertiesKeyMapper agentPropertyMapper = PropertiesKeyMapper.create("agent-properties.map");
	private PropertiesKeyMapper monitorPropertyMapper = PropertiesKeyMapper.create("monitor-properties.map");
	private PropertiesKeyMapper commonPropertyMapper = PropertiesKeyMapper.create("common-properties.map");

	/**
	 * Initialize.
	 *
	 * @return initialized AgentConfig
	 */
	public AgentConfig init() {
		home = resolveHome();
		copyDefaultConfigurationFiles();
		loadProperties();
		loadInternalProperties();
		configureLogging();
		return this;
	}

	private void copyDefaultConfigurationFiles() {
		checkNotNull(home);
		final File agentConfig = home.getFile("agent.conf");
		File newAgentConfig = new File(getCurrentDirectory(), "__agent.conf");
		if (agentConfig.exists()) {
			if (System.getProperty(CommonConstants.PROP_OVERWRITE_CONFIG) != null) {
				LOGGER.info("Overwrite the existing agent.conf with __agent.conf");
			} else if (newAgentConfig.exists()) {
				LOGGER.warn("The agent configuration file '{}' already exists.", agentConfig.getAbsolutePath());
				LOGGER.warn("If you want to use the '{}' file", newAgentConfig.getAbsolutePath());
				LOGGER.warn("Please run agent with -o option");
				return;
			}
		}
		if (newAgentConfig.exists()) {
			home.copyFileTo(newAgentConfig, "agent.conf");
			agentConfig.setLastModified(newAgentConfig.lastModified());
		} else {
			try {
				home.writeFileTo(loadResource("/agent.conf"), "agent.conf");
			} catch (IOException e) {
				throw processException(e);
			}
		}
	}

	protected void loadProperties() {
		checkNotNull(home);
		Properties properties = home.getProperties("agent.conf");
		properties.put("NGRINDER_AGENT_HOME", home.getDirectory().getAbsolutePath());
		properties.putAll(System.getProperties());
		agentProperties = new PropertiesWrapper(properties, agentPropertyMapper);
		monitorProperties = new PropertiesWrapper(properties, monitorPropertyMapper);
		commonProperties = new PropertiesWrapper(properties, commonPropertyMapper);
	}

	protected void loadInternalProperties() {
		InputStream inputStream = null;
		Properties properties = new Properties();
		try {
			inputStream = AgentConfig.class.getResourceAsStream("/internal.properties");
			properties.load(inputStream);
			internalProperties = new PropertiesWrapper(properties, internalPropertyMapper);
		} catch (IOException e) {
			LOGGER.error("Error while load internal.properties", e);
			internalProperties = new PropertiesWrapper(properties, internalPropertyMapper);
		} finally {
			IOUtils.closeQuietly(inputStream);
		}
	}

	public String loadResource(String name) throws IOException {
		InputStream inputStream = null;
		try {
			inputStream = AgentConfig.class.getResourceAsStream(name);
			if (inputStream != null) {
				return IOUtils.toString(inputStream);
			} else {
				return "";
			}
		} finally {
			IOUtils.closeQuietly(inputStream);
		}
	}

	private void configureLogging() {
		File logDirectory = getHome().getLogDirectory();
		String level = "INFO";
		if (getCommonProperties().getPropertyBoolean(PROP_COMMON_VERBOSE)) {
			level = "TRACE";
		}
		if (isSilentMode()) {
			level = "ERROR";
		}
		final Context context = (Context) LoggerFactory.getILoggerFactory();
		final JoranConfigurator configurator = new JoranConfigurator();
		configurator.setContext(context);
		context.putProperty("LOG_LEVEL", level);
		context.putProperty("LOG_DIRECTORY", logDirectory.getAbsolutePath());
		try {
			configurator.doConfigure(getClass().getResource("/logback-agent.xml"));
		} catch (JoranException e) {
			LOGGER.error("Error while configuring logger", e);
		}
	}

	/**
	 * Save the agent pid.
	 *
	 * @param agentPid  agent pid
	 * @param startMode startMode
	 */
	public void saveAgentPidProperties(String agentPid, String startMode) {
		checkNotNull(home);
		Properties properties = home.getProperties("pid");
		if ("agent".equalsIgnoreCase(startMode)) {
			properties.put("agent.pid", agentPid);
		} else {
			properties.put("monitor.pid", agentPid);
		}
		home.saveProperties("pid", properties);
	}

	/**
	 * Update agent pid file.
	 *
	 * @param startMode startMode
	 */
	public void updateAgentPidProperties(String startMode) {
		checkNotNull(home);
		Properties properties = home.getProperties("pid");
		Set<String> names = properties.stringPropertyNames();
		if (names.size() > 1) {
			properties.remove(startMode + ".pid");
			home.saveProperties("pid", properties);
		} else if (names.contains(startMode + ".pid")) {
			removeAgentPidProperties();
		}
	}

	/**
	 * Get the agent pid in the form of string.
	 *
	 * @param startMode agent or monitor
	 * @return pid
	 */
	public String getAgentPidProperties(String startMode) {
		checkNotNull(home);
		Properties properties = home.getProperties("pid");
		if ("agent".equalsIgnoreCase(startMode)) {
			return (String) properties.get("agent.pid");
		} else {
			return (String) properties.get("monitor.pid");
		}
	}

	/**
	 * Remove agent pid properties.
	 */
	public void removeAgentPidProperties() {
		checkNotNull(home);
		File file = home.getFile("pid");
		FileUtils.deleteQuietly(file);
	}

	/**
	 * Resolve NGrinder agent home path.
	 *
	 * @return resolved {@link AgentHome}
	 */
	protected AgentHome resolveHome() {
		String userHomeFromEnv = trimToEmpty(System.getenv("NGRINDER_AGENT_HOME"));
		//printLog("    System Environment:  NGRINDER_AGENT_HOME={}", userHomeFromEnv);
		String userHomeFromProperty = trimToEmpty(System.getProperty("ngrinder.agent.home"));
		//printLog("    Java System Property:  ngrinder.agent.home={}", userHomeFromEnv);
		if (StringUtils.isNotEmpty(userHomeFromEnv) && !StringUtils.equals(userHomeFromEnv, userHomeFromProperty)) {
			printLog("The path to ngrinder agent home is ambiguous:");
			printLog("    '{}' is accepted.", userHomeFromProperty);
		}

		String userHome = StringUtils.defaultIfEmpty(userHomeFromProperty, userHomeFromEnv);
		if (StringUtils.isEmpty(userHome)) {
			userHome = System.getProperty("user.home") + File.separator + NGRINDER_DEFAULT_FOLDER;
		} else if (StringUtils.startsWith(userHome, "~" + File.separator)) {
			userHome = System.getProperty("user.home") + File.separator + userHome.substring(2);
		} else if (StringUtils.startsWith(userHome, "." + File.separator)) {
			userHome = System.getProperty("user.dir") + File.separator + userHome.substring(2);
		}

		userHome = FilenameUtils.normalize(userHome);
		printLog("NGRINDER_AGENT_HOME : {}", userHome);
		File homeDirectory = new File(userHome);
		try {
			if (homeDirectory.mkdirs()) {
				noOp();
			}
			if (!homeDirectory.canWrite()) {
				throw processException("home directory " + userHome + " is not writable.");
			}
		} catch (Exception e) {
			throw processException("Error while resolve the home directory.", e);
		}
		return new AgentHome(homeDirectory);
	}

	private void printLog(String template, Object... var) {
		if (!isSilentMode()) {
			LOGGER.info(template, var);
		}
	}

	/**
	 * If there is test mode property in system.properties.. return true
	 *
	 * @return true is test mode
	 */

	public boolean isDevMode() {
		return getCommonProperties().getPropertyBoolean(PROP_COMMON_DEV_MODE);
	}

	public AgentHome getHome() {
		return this.home;
	}

	/**
	 * Get agent properties.
	 *
	 * @return agent properties
	 */
	public PropertiesWrapper getAgentProperties() {
		return checkNotNull(agentProperties);
	}

	/**
	 * Get monitor properties.
	 *
	 * @return monitor properties
	 */
	public PropertiesWrapper getMonitorProperties() {
		return checkNotNull(monitorProperties);
	}

	/**
	 * Get internal properties.
	 *
	 * @return internalProperties
	 */
	public PropertiesWrapper getInternalProperties() {
		return internalProperties;
	}

	public File getCurrentDirectory() {
		return new File(System.getProperty("user.dir"));
	}

	public String getMonitorBindingIP() {
		return getMonitorProperties().getProperty(PROP_MONITOR_BINDING_IP);
	}

	public String getControllerIP() {
		return getAgentProperties().getProperty(PROP_AGENT_CONTROLLER_HOST, DEFAULT_LOCAL_HOST_ADDRESS);
	}

	public void setControllerHost(String host) {
		getAgentProperties().addProperty(PROP_AGENT_CONTROLLER_HOST, host);
	}

	public int getControllerPort() {
		return getAgentProperties().getPropertyInt(PROP_AGENT_CONTROLLER_PORT);
	}

	public String getRegion() {
		return getAgentProperties().getProperty(PROP_AGENT_REGION);
	}

	public String getAgentHostID() {
		return getAgentProperties().getProperty(PROP_AGENT_HOST_ID, NetworkUtils.DEFAULT_LOCAL_HOST_NAME);
	}

	public boolean isSecurityEnabled() {
		return getAgentProperties().getPropertyBoolean(PROP_AGENT_ENABLE_SECURITY);
	}

	public boolean isServerMode() {
		return getAgentProperties().getPropertyBoolean(PROP_AGENT_SERVER_MODE);
	}

	public boolean isSilentMode() {
		final PropertiesWrapper properties = getCommonProperties();
		if (properties == null) {
			return Boolean.getBoolean(System.getProperty(PROP_COMMON_SILENT_MODE, "false"));
		} else {
			return properties.getPropertyBoolean(PROP_COMMON_SILENT_MODE);
		}
	}

	public PropertiesWrapper getCommonProperties() {
		return commonProperties;
	}

	public static class NullAgentConfig extends AgentConfig {
		public int counter = 0;
		private int controllerPort = 0;

		public NullAgentConfig(int i) {
			counter = i;
			home = resolveHome();
			loadProperties();
			loadInternalProperties();
		}

		public int getControllerPort() {
			return (this.controllerPort == 0) ? super.getControllerPort() : this.controllerPort;
		}

		public void setControllerPort(int controllerPort) {
			this.controllerPort = controllerPort;
		}

		public boolean isSilentMode() {
			return true;
		}

		@Override
		protected AgentHome resolveHome() {
			AgentHome resolveHome = super.resolveHome();
			File directory = new File(resolveHome.getDirectory(), "tmp_" + String.valueOf(counter));
			resolveHome = new AgentHome(directory);
			try {
				FileUtils.forceDeleteOnExit(directory);
			} catch (IOException e) {
				noOp();
			}
			return resolveHome;
		}
	}
}
