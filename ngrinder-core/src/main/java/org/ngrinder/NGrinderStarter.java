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

import java.io.File;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.HashSet;
import java.util.Set;
import java.util.Map.Entry;

import net.grinder.AgentControllerDaemon;
import net.grinder.common.GrinderException;
import net.grinder.util.ReflectionUtil;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.SystemUtils;
import org.ngrinder.monitor.MonitorConstants;
import org.ngrinder.monitor.agent.AgentMonitorServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Main class to start agent or monitor.
 * 
 * @author Mavlarn
 * @since 3.0
 */
public class NGrinderStarter {

	private final Logger LOG = LoggerFactory.getLogger(NGrinderStarter.class);

	private boolean localAttachmentSupported;

	public NGrinderStarter() {
		try {
			addClassPathForToolsJar();
			Class.forName("com.sun.tools.attach.VirtualMachine");
			Class.forName("sun.management.ConnectorAddressLink");
			localAttachmentSupported = true;
		} catch (NoClassDefFoundError x) {
			LOG.error(x.getMessage(), x);
			localAttachmentSupported = false;
		} catch (ClassNotFoundException x) {
			LOG.error(x.getMessage(), x);
			localAttachmentSupported = false;
		}
	}

	private void startMonitor(boolean withAgent) {
		int port = MonitorConstants.DEFAULT_AGENT_PORT;
		Set<String> dataCollectors;
		if (withAgent) {
			dataCollectors = MonitorConstants.DEFAULT_DATA_COLLECTOR;
		} else {
			dataCollectors = MonitorConstants.TARGET_SERVER_DATA_COLLECTOR;
		}
		Set<Integer> jvmPids = new HashSet<Integer>();
		int currPID = getCurrentJVMPid();
		jvmPids.add(currPID);

		LOG.info("**************************");
		LOG.info("* Start nGrinder Monitor *");
		LOG.info("**************************");
		LOG.info("* Local JVM link support :{}", localAttachmentSupported);
		LOG.info("* Colllect SYSTEM %s data. **", withAgent ? "and JAVA" : "");
		try {
			AgentMonitorServer.getInstance().init(port, dataCollectors, jvmPids);
			AgentMonitorServer.getInstance().start();
		} catch (Exception e) {
			LOG.error("ERROR:", e);
		}
	}

	private void startAgent() {
		LOG.info("*************************");
		LOG.info("* Start nGrinder Agent **");
		LOG.info("*************************");

		AgentControllerDaemon agentController = new AgentControllerDaemon();
		try {
			agentController.run();
		} catch (GrinderException e) {
			LOG.error("Error while starting agent controller", e);
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

	public URL findToolsJarPath() {
		String toolsJar = SystemUtils.IS_OS_MAC_OSX ? "Classes/classes.jar" : "lib/tools.jar";
		try {
			for (Entry<Object, Object> each : System.getProperties().entrySet()) {
				LOG.debug("{}={}", each.getKey(), each.getValue());
			}

			String property = System.getProperty("java.home");

			File javaHome = new File(property);
			File toolsJarPath = new File(javaHome, toolsJar);
			if (toolsJarPath.exists()) {
				return toolsJarPath.toURI().toURL();
			}
			toolsJarPath = new File(javaHome.getParentFile(), toolsJar);
			if (toolsJarPath.exists()) {
				return toolsJarPath.toURI().toURL();
			}
			for (File eachCandidate : javaHome.getParentFile().listFiles()) {
				toolsJarPath = new File(eachCandidate, toolsJar);
				if (toolsJarPath.exists()) {
					return toolsJarPath.toURI().toURL();
				}
			}
		} catch (MalformedURLException e) {
		}
		LOG.error("{} path is not found. Please set up JAVA_HOME env var to JDK(not JRE).", toolsJar);
		System.exit(-1);
		return null;
	}

	private void addClassPathForToolsJar() {
		URLClassLoader urlClassLoader = (URLClassLoader) ClassLoader.getSystemClassLoader();
		ReflectionUtil.invokePrivateMethod(urlClassLoader, "addURL", new Object[] { findToolsJarPath() });
	}

	public static void main(String[] args) {
		NGrinderStarter starter = new NGrinderStarter();
		boolean withAgent = false;

		if (args != null && args.length > 0 && args[0].equals("-a")) {
			// just start monitor
			withAgent = true;
		}
		if (withAgent) {
			starter.startAgent();
		}
		starter.startMonitor(withAgent);
	}
}
