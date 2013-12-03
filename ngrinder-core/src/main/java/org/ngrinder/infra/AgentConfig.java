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

import net.grinder.communication.AgentControllerCommunicationDefaults;
import net.grinder.util.NetworkUtil;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.BooleanUtils;
import org.apache.commons.lang.StringUtils;
import org.ngrinder.common.util.PropertiesWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.Set;

import static org.apache.commons.lang.StringUtils.trimToEmpty;
import static org.ngrinder.common.util.ExceptionUtils.processException;
import static org.ngrinder.common.util.NoOp.noOp;
import static org.ngrinder.common.util.Preconditions.checkNotNull;

/**
 * Spring component which is responsible to get the nGrinder config which is stored
 * ${NGRINDER_HOME}.
 *
 * @author JunHo Yoon
 * @since 3.0
 */
public class AgentConfig {
	private static final String NGRINDER_DEFAULT_FOLDER = ".ngrinder_agent";

	// Available from 3.3
	public static final String AGENT_CONTROLLER_IP = "agent.controller.ip";
	public static final String AGENT_CONTROLLER_PORT = "agent.controller.port";

	// For backward compatibility
	public static final String AGENT_CONSOLE_IP = "agent.console.ip";
	public static final String AGENT_CONSOLE_PORT = "agent.console.port";

	public static final String AGENT_REGION = "agent.region";
	public static final String AGENT_HOST_ID = "agent.hostid";
	public static final String MONITOR_LISTEN_PORT = "monitor.listen.port";
	public static final String MONITOR_LISTEN_IP = "monitor.listen.ip";

	private static final Logger LOGGER = LoggerFactory.getLogger(AgentConfig.class);

	private AgentHome home = null;
	private PropertiesWrapper agentProperties;
	private PropertiesWrapper internalProperties;
	private String agentHostID;
	private boolean silent = false;
	private int controllerPort;

	/**
	 * Initialize.
	 *
	 * @return initialized AgentConfig
	 */
	public AgentConfig init() {
		home = resolveHome();
		copyDefaultConfigurationFiles();
		loadAgentProperties();
		loadInternalProperties();
		return this;
	}

	/**
	 * Initialize.
	 *
	 * @param silent true if no log should be printed
	 * @return initialized AgentConfig
	 */
	public AgentConfig init(boolean silent) {
		this.silent = silent;
		return init();
	}

	private void loadInternalProperties() {
		InputStream inputStream = null;
		Properties properties = new Properties();
		try {
			inputStream = AgentConfig.class.getResourceAsStream("/internal.properties");
			properties.load(inputStream);
			internalProperties = new PropertiesWrapper(properties);
		} catch (IOException e) {
			LOGGER.error("Error while load internal.properties", e);
			internalProperties = new PropertiesWrapper(properties);
		} finally {
			IOUtils.closeQuietly(inputStream);
		}
	}

	private void copyDefaultConfigurationFiles() {
		checkNotNull(home);
		final File agentConfig = home.getFile("agent.conf");
		if (agentConfig.exists()) {
			return;
		}
		File newAgentConfig = new File(getCurrentDirectory(), "__agent.conf");
		if (newAgentConfig.exists()) {
			home.copyFileTo(newAgentConfig, "agent.conf");
		} else {
			home.writeFileTo("", "agent.conf");
		}
	}

	/**
	 * Load the internal files for the given path by searching class paths.
	 *
	 * @param path path in the classpath
	 * @return {@link InputStream}
	 */
	public InputStream loadFromClassPath(String path) {
		return AgentConfig.class.getClassLoader().getResourceAsStream(path);
	}

	private void loadAgentProperties() {
		checkNotNull(home);
		Properties properties = home.getProperties("agent.conf");
		properties.put("NGRINDER_AGENT_HOME", home.getDirectory().getAbsolutePath());
		agentProperties = new PropertiesWrapper(properties);
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
		printLog("    System Environment:  NGRINDER_AGENT_HOME={}", userHomeFromEnv);
		String userHomeFromProperty = trimToEmpty(System.getProperty("ngrinder.agent.home"));
		printLog("    Java System Property:  ngrinder.agent.home={}", userHomeFromEnv);
		if (StringUtils.isNotEmpty(userHomeFromEnv) && !StringUtils.equals(userHomeFromEnv, userHomeFromProperty)) {
			printLog("The path to ngrinder agent home is ambiguous:");
			printLog("    '{}' is accepted.", userHomeFromProperty);
		}

		String userHome = StringUtils.defaultIfEmpty(userHomeFromProperty, userHomeFromEnv);
		if (StringUtils.isEmpty(userHome)) {
			userHome = System.getProperty("user.home") + File.separator + NGRINDER_DEFAULT_FOLDER;
		}
		printLog("Finally NGRINDER_AGENT_HOME is resolved as {}", userHome);
		File homeDirectory = new File(userHome);
		try {
			homeDirectory.mkdirs();
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
	 * if there is testmode property in system.properties.. return true
	 *
	 * @return true is test mode
	 */

	public boolean isTestMode() {
		return BooleanUtils.toBoolean(getProperty("testmode", "false"));
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
		checkNotNull(agentProperties);
		return agentProperties;
	}

	/**
	 * Get the string value from property for the given key.
	 *
	 * @param key          property key
	 * @param defaultValue default value
	 * @return string value for given key. If not available, return default value.
	 */
	public String getProperty(String key, String defaultValue) {
		return getAgentProperties().getProperty(key, defaultValue);
	}

	/**
	 * Get the int value from property for the given key.
	 *
	 * @param key          property key
	 * @param defaultValue default value
	 * @return int value for given key. If not available, return default value.
	 */
	public int getPropertyInt(String key, int defaultValue) {
		return getAgentProperties().getPropertyInt(key, defaultValue);
	}

	/**
	 * Get the nGrinder internal property for the given key.
	 *
	 * @param key          key
	 * @param defaultValue default value
	 * @return value
	 */
	public String getInternalProperty(String key, String defaultValue) {
		return internalProperties.getProperty(key, defaultValue);
	}

	public File getCurrentDirectory() {
		return new File(System.getProperty("user.dir"));
	}

	/**
	 * Get the boolean value from the properties.
	 *
	 * @param key          property key
	 * @param defaultValue default value
	 * @return boolean value for given key. If not available, return default value.
	 */
	public boolean getPropertyBoolean(String key, boolean defaultValue) {
		return getAgentProperties().getPropertyBoolean(key, defaultValue);
	}

	public String getControllerIP() {
		final String property = getProperty(AGENT_CONSOLE_IP, AgentControllerCommunicationDefaults.DEFAULT_AGENT_CONTROLLER_SERVER_HOST);
		return getProperty(AGENT_CONTROLLER_IP, property);
	}

	public void setControllerIP(String ip) {
		getAgentProperties().setProperty(AGENT_CONTROLLER_IP, ip);
	}

	public int getControllerPort() {
		return getPropertyInt(AGENT_CONTROLLER_PORT, getPropertyInt(AGENT_CONSOLE_PORT, AgentControllerCommunicationDefaults.DEFAULT_AGENT_CONTROLLER_SERVER_PORT));
	}

	public String getRegion() {
		return getProperty(AGENT_REGION, "");
	}

	public String getAgentHostID() {
		return getProperty(AGENT_HOST_ID, NetworkUtil.DEFAULT_LOCAL_HOST_NAME);
	}

	public boolean isServerMode() {
		return getPropertyBoolean("agent.servermode", false);
	}

	public String getLocalIP() {
		return getProperty(MONITOR_LISTEN_IP, NetworkUtil.DEFAULT_LOCAL_HOST_ADDRESS);
	}

	public boolean isSilentMode() {
		return silent;
	}


	public static class NullAgentConfig extends AgentConfig {
		public int counter = 0;
		private int controllerPort = 0;

		public NullAgentConfig(int i) {
			counter = i;
		}


		public int getControllerPort() {
			if (controllerPort == 0) {
				return getPropertyInt(AGENT_CONTROLLER_PORT, getPropertyInt(AGENT_CONSOLE_PORT, AgentControllerCommunicationDefaults.DEFAULT_AGENT_CONTROLLER_SERVER_PORT));
			}
			return controllerPort;
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
