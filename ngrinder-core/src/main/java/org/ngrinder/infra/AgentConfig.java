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

import static org.ngrinder.common.util.Preconditions.checkNotNull;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.BooleanUtils;
import org.apache.commons.lang.StringUtils;
import org.ngrinder.common.exception.NGrinderRuntimeException;
import org.ngrinder.common.util.PropertiesWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Spring component which is responsible to get the nGrinder config which is stored
 * ${NGRINDER_HOME}.
 * 
 * @author JunHo Yoon
 * @since 3.0
 */
public class AgentConfig {
	private static final String NGRINDER_DEFAULT_FOLDER = ".ngrinder_agent";
	public static final String AGENT_CONTROLER_SERVER_HOST = "agent.controller.server.host";
	public static final String AGENT_CONTROLER_SERVER_PORT = "agent.controller.server.port";
	public static final String AGENT_REGION = "agent.region";
	public static final String AGENT_HOSTID = "agent.hostid";

	private static final Logger LOGGER = LoggerFactory.getLogger(AgentConfig.class);

	private AgentHome home = null;
	private PropertiesWrapper agentProperties;
	private PropertiesWrapper internalProperties;

	/**
	 * Initialize.
	 * 
	 * @return initialized AgentCongig
	 */
	public AgentConfig init() {
		home = resolveHome();
		copyDefaultConfigurationFiles();
		loadAgentProperties();
		loadInternalProperties();
		return this;
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
		InputStream agentConfIO = loadFromClassPath("agent.conf");
		if (agentConfIO == null) {
			throw new NGrinderRuntimeException("Error while loading agent.conf file");
		}
		home.copyFileTo(agentConfIO, new File("agent.conf"), false);
		IOUtils.closeQuietly(agentConfIO);
	}

	/**
	 * Load path file from class path.
	 * 
	 * @param path
	 *            path in the classpath
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
	 * Save agent pid.
	 * 
	 * @param agentPid
	 *            agent pid
	 * @param startMode
	 *            startMode
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
	 * Get agent pid properties.
	 * 
	 * @param startMode
	 *            agent or monitor
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
	 * resolve NGrinder agent home path.
	 * 
	 * @return resolved {@link AgentHome}
	 */
	protected AgentHome resolveHome() {
		String userHomeFromEnv = System.getenv("NGRINDER_AGENT_HOME");
		LOGGER.info("    System Environment:  NGRINDER_AGENT_HOME={}", StringUtils.trimToEmpty(userHomeFromEnv));

		String userHomeFromProperty = System.getProperty("ngrinder.agent.home");
		LOGGER.info("    Java Sytem Property:  ngrinder.agent.home={}", StringUtils.trimToEmpty(userHomeFromProperty));

		if (StringUtils.isNotEmpty(userHomeFromEnv) && !StringUtils.equals(userHomeFromEnv, userHomeFromProperty)) {
			LOGGER.warn("The path to ngrinder agent home is ambiguous:");
			LOGGER.warn("    '{}' is accepted.", userHomeFromProperty);
		}

		String userHome = StringUtils.defaultIfEmpty(userHomeFromProperty, userHomeFromEnv);
		if (StringUtils.isEmpty(userHome)) {
			userHome = System.getProperty("user.home") + File.separator + NGRINDER_DEFAULT_FOLDER;
		}

		File homeDirectory = new File(userHome);
		return new AgentHome(homeDirectory);
	}

	/**
	 * if there is testmode property in system.properties.. return true
	 * 
	 * @return true is test mode
	 */
	public boolean isTestMode() {
		return BooleanUtils.toBoolean(getAgentProperty("testmode", "false"));
	}

	String getAgentProperty(String key, String defaultValue) {
		return getAgentProperties().getProperty(key, defaultValue);
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
	 * Get property string.
	 * 
	 * @param key
	 *            property key
	 * @param defaultValue
	 *            default value
	 * 
	 * @return string value for given key. If not available, return default value.
	 */
	public String getProperty(String key, String defaultValue) {
		return getAgentProperties().getProperty(key, defaultValue);
	}

	/**
	 * Get property int.
	 * 
	 * @param key
	 *            property key
	 * @param defaultValue
	 *            default value
	 * 
	 * @return int value for given key. If not available, return default value.
	 */
	public int getPropertyInt(String key, int defaultValue) {
		return getAgentProperties().getPropertyInt(key, defaultValue);
	}

	/**
	 * Get nGrinder internal properties.
	 * 
	 * @param key
	 *            key
	 * @param defaultValue
	 *            default value
	 * @return value
	 */
	public String getInternalProperty(String key, String defaultValue) {
		return internalProperties.getProperty(key, defaultValue);
	}

}
