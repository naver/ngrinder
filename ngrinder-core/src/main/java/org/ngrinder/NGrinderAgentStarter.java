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
package org.ngrinder;

import com.beust.jcommander.JCommander;
import net.grinder.AgentControllerDaemon;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.ngrinder.common.constants.AgentConstants;
import org.ngrinder.common.constants.CommonConstants;
import org.ngrinder.infra.AgentConfig;
import org.ngrinder.monitor.agent.MonitorServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import oshi.software.os.OSProcess;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Properties;

import static java.lang.Integer.parseInt;
import static java.lang.String.format;
import static net.grinder.util.NetworkUtils.getIP;
import static org.apache.commons.lang.StringUtils.*;
import static org.ngrinder.common.constants.InternalConstants.PROP_INTERNAL_NGRINDER_VERSION;
import static org.ngrinder.common.util.SystemInfoUtils.*;

/**
 * Main class to start agent or monitor.
 *
 * @author Mavlarn
 * @author JunHo Yoon
 * @since 3.0
 */
public class NGrinderAgentStarter implements AgentConstants, CommonConstants {

	private static final Logger LOG = LoggerFactory.getLogger("starter");

	private static final String NETWORK_ADDRESS_CACHE_TTL_SECOND = "20";

	private AgentConfig agentConfig;

	private AgentControllerDaemon agentController;


	/**
	 * Constructor.
	 */
	public NGrinderAgentStarter() {
	}

	public void init() {
		// Check agent start mode
		this.agentConfig = createAgentConfig();
	}

	protected AgentConfig createAgentConfig() {
		AgentConfig agentConfig = new AgentConfig();
		agentConfig.init();
		return agentConfig;
	}

	/*
	 * Get the start mode, "agent" or "monitor". If it is not set in configuration, it will return "agent".
	 */
	public String getStartMode() {
		return agentConfig.getCommonProperties().getProperty(PROP_COMMON_START_MODE);
	}

	/**
	 * Get agent version.
	 *
	 * @return version string
	 */
	public String getVersion() {
		return agentConfig.getInternalProperties().getProperty(PROP_INTERNAL_NGRINDER_VERSION);
	}

	/**
	 * Start the performance monitor.
	 */
	public void startMonitor() {
		printLog("***************************************************");
		printLog("* Start nGrinder Monitor... ");
		printLog("***************************************************");
		try {
			MonitorServer.getInstance().init(agentConfig);
			MonitorServer.getInstance().start();
		} catch (Exception e) {
			LOG.error("ERROR: {}", e.getMessage());
			printHelpAndExit("Error while starting Monitor", e);
		}
	}

	/**
	 * Stop monitors.
	 * Only for unit-test.
	 */
	void stopMonitor() {
		MonitorServer.getInstance().stop();
	}

	/**
	 * Start ngrinder agent.
	 */
	public void startAgent() {
		printLog("***************************************************");
		printLog("   Start nGrinder Agent ...");
		printLog("***************************************************");

		if (StringUtils.isEmpty(System.getenv("JAVA_HOME"))) {
			printLog("Hey!! JAVA_HOME env var was not provided. "
					+ "Please provide JAVA_HOME env var before running agent."
					+ "Otherwise you can not execute the agent in the security mode.");
		}

		boolean serverMode = agentConfig.isServerMode();
		if (!serverMode) {
			printLog("JVM server mode is disabled.");
		}

		if (agentConfig.isConnectionMode()) {
			LOG.info("waiting for connection on {}:{}", agentConfig.getBroadcastIP(), agentConfig.getConnectionAgentPort());
		} else {
			String controllerIP = getIP(agentConfig.getControllerHost());
			agentConfig.setControllerIP(controllerIP);
			LOG.info("connecting to controller {}:{}", controllerIP, agentConfig.getControllerPort());
		}

		try {
			agentController = new AgentControllerDaemon(agentConfig);
			agentController.run();
		} catch (Exception e) {
			LOG.error("Error while starting Agent", e);
			printHelpAndExit("Error while starting Agent", e);
		}
	}

	private void printLog(String s, Object... args) {
		if (!agentConfig.isSilentMode()) {
			LOG.info(s, args);
		}
	}

	/**
	 * Stop the ngrinder agent.
	 * Only for unit-test.
	 */
	void stopAgent() {
		LOG.info("Stop nGrinder agent!");
		agentController.shutdown();
	}

	private static NGrinderAgentStarterParam.NGrinderModeParam modeParam;

	/**
	 * Agent starter.
	 *
	 * @param args arguments
	 */
	public static void main(String[] args) {
		NGrinderAgentStarter starter = new NGrinderAgentStarter();
		final NGrinderAgentStarterParam param = new NGrinderAgentStarterParam();
		JCommander commander = new JCommander(param);
		commander.setProgramName("ngrinder-agent");
		commander.setAcceptUnknownOptions(true);
		try {
			commander.parse(args);
		} catch (Exception e) {
			LOG.error(e.getMessage());
			return;
		}
		final List<String> unknownOptions = commander.getUnknownOptions();
		modeParam = param.getModeParam();
		modeParam.parse(unknownOptions.toArray(new String[0]));

		if (modeParam.version != null) {
			LOG.info("nGrinder v" + getStaticVersion());
			return;
		}

		if (modeParam.help != null) {
			modeParam.usage();
			return;
		}

		System.getProperties().putAll(modeParam.params);
		starter.init();

		final String startMode = modeParam.name();
		if ("stop".equalsIgnoreCase(param.command)) {
			starter.stopProcess(startMode);
			LOG.info("Stop the " + startMode);
			return;
		}
		starter.checkDuplicatedRun(startMode);

		java.security.Security.setProperty("networkaddress.cache.ttl", NETWORK_ADDRESS_CACHE_TTL_SECOND);

		if (startMode.equalsIgnoreCase("agent")) {
			starter.startAgent();
		} else if (startMode.equalsIgnoreCase("monitor")) {
			starter.startMonitor();
		} else {
			staticPrintHelpAndExit("Invalid agent.conf, '--mode' must be set as 'monitor' or 'agent'.");
		}
	}

	private static String getStaticVersion() {
		InputStream inputStream = null;
		Properties properties = new Properties();
		try {
			inputStream = NGrinderAgentStarter.class.getResourceAsStream("/internal.properties");
			properties.load(inputStream);
		} catch (IOException e) {
			// Do nothing.
		} finally {
			IOUtils.closeQuietly(inputStream);
		}
		return properties.getProperty("ngrinder.version", "UNKNOWN");
	}

	/**
	 * Stop process.
	 *
	 * @param mode agent or monitor.
	 */
	protected void stopProcess(String mode) {
		String pidProperty = agentConfig.getAgentPidProperties(mode);

		if (isBlank(pidProperty)) {
			printMessageAndExit(format("Error occurred while terminating %s process. (empty pid)", mode));
		}

		int pid = parseInt(pidProperty);
		OSProcess process = getProcess(pid);
		if (process == null) {
			printMessageAndExit(format("Error occurred while terminating %s process. (pid: %s is not running)", mode, pid));
		}

		killProcess(pid);

		process = getProcess(pid);
		if (process != null) {
			printMessageAndExit(format("Error occurred while terminating %s process.\n"
					+ "It can be already stopped or you may not have the permission.\n"
					+ "If everything is OK. Please stop it manually.", mode));
		}

		agentConfig.updateAgentPidProperties(mode);
	}

	/**
	 * Check if the process is already running in this env.
	 *
	 * @param startMode monitor or agent
	 */
	public void checkDuplicatedRun(String startMode) {
		String existingPid = agentConfig.getAgentPidProperties(startMode);
		if (isNotEmpty(existingPid)) {
			OSProcess process = getProcess(parseInt(existingPid));
			if (process != null) {
				printHelpAndExit("Currently " + startMode + " is running with pid " + existingPid
					+ ". Please stop it before run");
			}
			agentConfig.updateAgentPidProperties(startMode);
		}
		agentConfig.saveAgentPidProperties(String.valueOf(getPid()), startMode);
	}

	/**
	 * Print help and exit. This is provided for mocking.
	 *
	 * @param message message
	 */
	protected void printHelpAndExit(String message) {
		staticPrintHelpAndExit(message);
	}

	/**
	 * print help and exit. This is provided for mocking.
	 *
	 * @param message message
	 * @param e       exception
	 */
	protected void printHelpAndExit(String message, Exception e) {
		staticPrintHelpAndExit(message, e);
	}


	private static void staticPrintHelpAndExit(String message) {
		staticPrintHelpAndExit(message, null);
	}

	private static void staticPrintHelpAndExit(String message, Exception e) {
		if (e == null) {
			LOG.error(message);
		} else {
			LOG.error(message, e);
		}
		if (modeParam != null) {
			modeParam.usage();
		}
		System.exit(-1);
	}

	private void printMessageAndExit(String message) {
		LOG.error(message);
		System.exit(-1);
	}
}
