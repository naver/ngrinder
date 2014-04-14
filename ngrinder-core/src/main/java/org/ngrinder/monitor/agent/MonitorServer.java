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
package org.ngrinder.monitor.agent;

import net.grinder.util.NetworkUtils;
import org.apache.commons.lang.StringUtils;
import org.ngrinder.common.constants.MonitorConstants;
import org.ngrinder.infra.AgentConfig;
import org.ngrinder.monitor.MonitorContext;
import org.ngrinder.monitor.collector.DataCollectManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.management.MBeanServer;
import javax.management.remote.JMXConnectorServer;
import javax.management.remote.JMXConnectorServerFactory;
import javax.management.remote.JMXServiceURL;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashSet;
import java.util.Set;

/**
 * Monitor server, it will be used to start a JMX server.
 *
 * @author Mavlarn
 * @since 3.0
 */
public final class MonitorServer implements MonitorConstants {

	private static final Logger LOG = LoggerFactory.getLogger(MonitorServer.class);
	private JMXConnectorServer jmxServer = null;
	private Registry rmiRegistry = null;
	private boolean isRunning = false;
	private AgentConfig agentConfig;

	private static final MonitorServer INSTANCE = new MonitorServer();

	private MonitorServer() {
	}

	public static MonitorServer getInstance() {
		return INSTANCE;
	}

	/**
	 * Initialize the monitor server.
	 *
	 * @param agentConfig agentConfig
	 * @throws IOException IO error
	 */
	public void init(AgentConfig agentConfig) throws IOException {
		this.agentConfig = agentConfig;
		Set<String> systemDataCollector = new HashSet<String>();
		systemDataCollector.add(SYSTEM);
		MonitorContext.getInstance().setDataCollectors(systemDataCollector);
		int port = agentConfig.getMonitorProperties().getPropertyInt(PROP_MONITOR_BINDING_PORT);
		this.rmiRegistry = LocateRegistry.createRegistry(port);
		MBeanServer mBeanServer = ManagementFactory.getPlatformMBeanServer();
		String hostname = agentConfig.getMonitorBindingIP();
		if (StringUtils.isBlank(hostname)) {
			hostname = NetworkUtils.getAllPBindingAddress();
		}
 		final String jmxUrlString = String.format("service:jmx:rmi://%s:%d/jndi/rmi://%s:%d/jmxrmi", hostname, port, hostname, port);
		JMXServiceURL jmxUrl = new JMXServiceURL(jmxUrlString);
		this.jmxServer = JMXConnectorServerFactory.newJMXConnectorServer(jmxUrl, null, mBeanServer);
		RegisterMXBean.getInstance().addDefaultMXBean(mBeanServer);
		LOG.info("Service URL:{} is initiated.", jmxUrl);
	}

	/**
	 * check whether the monitor server is running.
	 *
	 * @return true if the monitor server is running
	 */
	public boolean isRunning() {
		return isRunning;
	}

	/**
	 * Start monitoring.
	 *
	 * @throws IOException exception
	 */
	public void start() throws IOException {
		if (!isRunning()) {
			jmxServer.start();
			DataCollectManager.getInstance().init(agentConfig);
			DataCollectManager.getInstance().start();
			isRunning = true;
		}
	}

	/**
	 * Stop monitoring.
	 */
	public void stop() {
		LOG.info("Stop monitor.");
		if (!isRunning) {
			LOG.info("Monitor was not started!");
			return;
		}
		isRunning = false;
		try {
			jmxServer.stop();
			DataCollectManager.getInstance().stop();
			UnicastRemoteObject.unexportObject(rmiRegistry, true);
		} catch (IOException e) {
			LOG.error(e.getMessage(), e);
		}
	}
}
