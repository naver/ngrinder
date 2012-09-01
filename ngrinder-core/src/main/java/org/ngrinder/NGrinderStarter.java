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
package org.ngrinder;

import static org.ngrinder.common.util.Preconditions.checkNotNull;

import java.io.File;
import java.io.FilenameFilter;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map.Entry;

import net.grinder.AgentControllerDaemon;
import net.grinder.communication.AgentControllerCommunicationDefauts;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.SystemUtils;
import org.ngrinder.common.util.ReflectionUtil;
import org.ngrinder.infra.AgentConfig;
import org.ngrinder.monitor.MonitorConstants;
import org.ngrinder.monitor.agent.AgentMonitorServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Main class to start agent or monitor.
 * 
 * @author Mavlarn
 * @author JunHo Yoon
 * @since 3.0
 */
public class NGrinderStarter {

	private static final Logger LOG = LoggerFactory.getLogger(NGrinderStarter.class);

	private boolean localAttachmentSupported;

	public static AgentConfig agentConfig;

	public NGrinderStarter() {
		try {
			addClassPath();
			Class.forName("com.sun.tools.attach.VirtualMachine");
			Class.forName("sun.management.ConnectorAddressLink");
			localAttachmentSupported = true;
		} catch (NoClassDefFoundError x) {
			LOG.error("Local attachement is not supported", x);
			localAttachmentSupported = false;
		} catch (ClassNotFoundException x) {
			LOG.error("Local attachement is not supported", x);
			localAttachmentSupported = false;
		}
	}

	public void startMonitor() {

		LOG.info("**************************");
		LOG.info("* Start nGrinder Monitor *");
		LOG.info("**************************");
		LOG.info("* Local JVM link support :{}", localAttachmentSupported);
		LOG.info("* Colllect SYSTEM data. **");
		try {
			AgentMonitorServer.getInstance().init();
			AgentMonitorServer.getInstance().start();
		} catch (Exception e) {
			LOG.error("ERROR: {}", e.getMessage());
			printHelpAndExit("Error while starting Monitor", e);
		}
	}

	private void startAgent(String region, String consoleIP, int consolePort) {
		LOG.info("*************************");
		LOG.info("Start nGrinder Agent ...");
		LOG.info("with console: {}:{}", consoleIP, consolePort);
		try {

			AgentControllerDaemon agentController = new AgentControllerDaemon();
			agentController.setRegion(region);
			agentController.setAgentConfig(agentConfig);
			agentController.run(consoleIP, consolePort);
		} catch (Exception e) {
			LOG.error("ERROR: {}", e.getMessage());
			printHelpAndExit("Error while starting Agent", e);
		}
	}

	public static int getCurrentJVMPid() {
		RuntimeMXBean runtime = ManagementFactory.getRuntimeMXBean();
		String name = runtime.getName();
		try {
			return Integer.parseInt(name.substring(0, name.indexOf('@')));
		} catch (Exception e) {
			return -1;
		}
	}

	/**
	 * Do best to find tools.jar path.
	 * 
	 * <ol>
	 * <li>First try to resolve JAVA_HOME</li>
	 * <li>If it's not defined, try to get java.home system property</li>
	 * <li>Try to find the ${java.home}/lib/tools.jar</li>
	 * <li>Try to find the ${java.home}/../lib/tools.jar</li>
	 * <li>Try to find the ${java.home}/../{any_subpath}/lib/tools.jar</li>
	 * </ol>
	 * 
	 * @return found tools.jar path.
	 */
	public URL findToolsJarPath() {
		// In OSX, tools.jar should be classes.jar
		String toolsJar = SystemUtils.IS_OS_MAC_OSX ? "Classes/classes.jar" : "lib/tools.jar";
		try {
			for (Entry<Object, Object> each : System.getProperties().entrySet()) {
				LOG.trace("{}={}", each.getKey(), each.getValue());
			}
			String javaHomePath = System.getenv().get("JAVA_HOME");
			if (StringUtils.isBlank(javaHomePath)) {
				LOG.warn("JAVA_HOME is not set. NGrinder is trying to find the JAVA_HOME programically");
				javaHomePath = System.getProperty("java.home");
			}

			File javaHome = new File(javaHomePath);
			File toolsJarPath = new File(javaHome, toolsJar);
			if (toolsJarPath.exists()) {
				return toolsJarPath.toURI().toURL();
			}
			File parentFile = javaHome.getParentFile();
			if (parentFile != null) {
				toolsJarPath = new File(parentFile, toolsJar);
				if (toolsJarPath.exists()) {
					return toolsJarPath.toURI().toURL();
				}
				File[] fileList = parentFile.listFiles();
				if (fileList != null) {
					for (File eachCandidate : fileList) {
						toolsJarPath = new File(eachCandidate, toolsJar);
						if (toolsJarPath.exists()) {
							return toolsJarPath.toURI().toURL();
						}
					}
				}
			}
		} catch (MalformedURLException e) {
			LOG.error("Error while patching tools.jar file. Please set JAVA_HOME env home correctly", e);
		}
		printHelpAndExit("tools.jar path is not found. Please set up JAVA_HOME env var to JDK(not JRE)");
		return null;
	}

	/**
	 * Add tools.jar classpath. This contains hack
	 */
	private void addClassPath() {
		URLClassLoader urlClassLoader = (URLClassLoader) ClassLoader.getSystemClassLoader();
		URL toolsJarPath = findToolsJarPath();
		LOG.info("tools.jar is found in {}", checkNotNull(toolsJarPath).toString());

		ReflectionUtil.invokePrivateMethod(urlClassLoader, "addURL", new Object[] { toolsJarPath });

		List<String> libString = new ArrayList<String>();
		File libFolder = new File(".", "lib").getAbsoluteFile();
		if (!libFolder.exists()) {
			printHelpAndExit("lib path (" + libFolder.getAbsolutePath() + ") does not exist");
		}
		File[] libList = libFolder.listFiles();
		if (libList == null) {
			printHelpAndExit("lib path (" + libFolder.getAbsolutePath() + ") has no content");
		}

		for (File each : libList) {
			if (each.getName().toLowerCase(Locale.getDefault()).endsWith(".jar")) {
				try {
					URL jarFileUrl = checkNotNull(each.toURI().toURL());
					ReflectionUtil.invokePrivateMethod(urlClassLoader, "addURL", new Object[] { jarFileUrl });
					libString.add(each.getPath());
				} catch (MalformedURLException e) {
					LOG.error(e.getMessage(), e);
				}
			}
		}
		if (!libString.isEmpty()) {
			String base = System.getProperties().getProperty("java.class.path");
			String classpath = base + File.pathSeparator + StringUtils.join(libString, File.pathSeparator);
			System.getProperties().setProperty("java.class.path", classpath);
		}
	}

	/**
	 * Agent starter
	 * 
	 * @param args
	 */
	public static void main(String[] args) {

		if (!idValidCurrentDirectory()) {
			printHelpAndExit("nGrinder agent should start in the folder which nGrinder agent exists.");
		}
		NGrinderStarter starter = new NGrinderStarter();
		agentConfig = new AgentConfig();
		agentConfig.init();

		String startMode = agentConfig.getAgentProperties().getProperty("start.mode", "agent");
		if (startMode.equalsIgnoreCase("agent")) {
			String consoleIP = agentConfig.getAgentProperties().getProperty("agent.console.ip", "127.0.0.1");
			int consolePort = agentConfig.getAgentProperties().getPropertyInt("agent.console.port",
							AgentControllerCommunicationDefauts.DEFAULT_AGENT_CONTROLLER_SERVER_PORT);

			String region = agentConfig.getAgentProperties().getProperty("agent.region", "");

			starter.startAgent(region, consoleIP, consolePort);
		} else if (startMode.equalsIgnoreCase("monitor")) {
			MonitorConstants.init(agentConfig);
			starter.startMonitor();
		} else {
			printHelpAndExit("Invalid agent.conf, 'start.mode' must be set as 'monitor' or 'agent'.");
		}
	}

	/**
	 * Check the current directory is valid or not.<br/>
	 * ngrinder agent should run in the folder agent exists.
	 * 
	 * @return true if it's valid
	 */
	private static boolean idValidCurrentDirectory() {
		File currentFolder = new File(System.getProperty("user.dir"));
		String[] list = currentFolder.list(new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
				return (name.startsWith("ngrinder-core") && name.endsWith(".jar"));
			}
		});
		return (list != null && list.length != 0);
	}

	private static void printHelpAndExit(String message) {
		printHelpAndExit(message, null);
	}

	private static void printHelpAndExit(String message, Exception e) {
		LOG.error(message);
		System.exit(-1);
	}
}
