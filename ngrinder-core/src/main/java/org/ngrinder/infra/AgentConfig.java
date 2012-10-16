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

import static org.ngrinder.common.util.Preconditions.checkNotNull;

import java.io.File;
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
 * Spring component which is responsible to get the nGrinder config which is stored ${NGRINDER_HOME}.
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

	/**
	 * Initialize.
	 * @return initialized AgentCongig
	 */
	public AgentConfig init() {
		home = resolveHome();
		copyDefaultConfigurationFiles();
		loadAgentProperties();
		return this;
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
	
	public void saveAgentPidProperties(String agentPid) {
		checkNotNull(home);
		Properties properties = new Properties();
		properties.put("agent.pid", agentPid);
		properties.put("agent.home", home.getDirectory().getAbsolutePath());
		home.saveProperties("agent_pid.conf", properties);
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
	 * @return agent properties
	 */
	public PropertiesWrapper getAgentProperties() {
		checkNotNull(agentProperties);
		return agentProperties;
	}

	/**
	 * Get property string.
	 * 
	 * @param key property key
	 * @param defaultValue default value
	 * 
	 * @return string value for given key. If not available, return default value.
	 */
	public String getProperty(String key, String defaultValue) {
		return getAgentProperties().getProperty(key, defaultValue);
	}

	/**
	 * Get property int.
	 * 
	 * @param key property key
	 * @param defaultValue default value
	 * 
	 * @return int value for given key. If not available, return default value.
	 */
	public int getPropertyInt(String key, int defaultValue) {
		return getAgentProperties().getPropertyInt(key, defaultValue);
	}
}
