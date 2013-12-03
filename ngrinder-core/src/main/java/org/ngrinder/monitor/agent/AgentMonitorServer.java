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

import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.Set;

import javax.management.MBeanServer;
import javax.management.remote.JMXConnectorServer;
import javax.management.remote.JMXConnectorServerFactory;
import javax.management.remote.JMXServiceURL;

import net.grinder.util.NetworkUtil;

import org.ngrinder.infra.AgentConfig;
import org.ngrinder.monitor.MonitorConstants;
import org.ngrinder.monitor.MonitorContext;
import org.ngrinder.monitor.agent.collector.AgentDataCollectManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * monitor server, it will be used to start a JMX server.
 *
 * @author Mavlarn
 * @since 3.0
 */
public final class AgentMonitorServer {

	private static final Logger LOG = LoggerFactory.getLogger(AgentMonitorServer.class);
	private JMXConnectorServer jmxServer = null;
	private MBeanServer mBeanServer = null;
	private Registry rmiRegistry = null;
	private boolean isRunning = false;
	private int port = MonitorConstants.DEFAULT_MONITOR_PORT;
	private AgentConfig agentConfig;

	private static final AgentMonitorServer INSTANCE = new AgentMonitorServer();

	private AgentMonitorServer() {
	}

	public static AgentMonitorServer getInstance() {
		return INSTANCE;
	}

	/**
	 * Initialize the monitor server with default port and collector. Default
	 * port is 12343, and default collector is system data collector.
	 *
	 * @param agentConfig agentConfig
	 * @throws IOException IO error
	 */
	public void init(AgentConfig agentConfig) throws IOException {
		this.init(MonitorConstants.DEFAULT_MONITOR_PORT, agentConfig);
		AgentDataCollectManager.getInstance().init(agentConfig);
	}

	/**
	 * Initialize the monitor server with default collector. In 3.0 version,
	 * default collector is system data collector.
	 *
	 * @param port        monitor listener port
	 * @param agentConfig agentConfig
	 * @throws IOException IO error
	 */
	public void init(final int port, AgentConfig agentConfig) throws IOException {
		this.init(port, MonitorConstants.DEFAULT_DATA_COLLECTOR, agentConfig);
	}

	/**
	 * Initialize the monitor server.
	 *
	 * @param port          monitor listener port
	 * @param dataCollector a list of collector, for java or system data
	 * @param agentConfig   agentConfig
	 * @throws IOException IO error
	 */
	public void init(final int port, final Set<String> dataCollector, AgentConfig agentConfig) throws IOException {

		MonitorContext.getInstance().setDataCollectors(dataCollector);

		this.port = port;
		this.rmiRegistry = LocateRegistry.createRegistry(port);
		this.mBeanServer = ManagementFactory.getPlatformMBeanServer();
		this.agentConfig = agentConfig;
		final String hostname = getCurrentHostName(agentConfig);
		final String jmxUrlString = "service:jmx:rmi://" + hostname + ":" + port + "/jndi/rmi://" + hostname + ":"
				+ port + "/jmxrmi";
		JMXServiceURL jmxUrl = new JMXServiceURL(jmxUrlString);
		this.jmxServer = JMXConnectorServerFactory.newJMXConnectorServer(jmxUrl, null, mBeanServer);
		AgentRegisterMXBean.getInstance().addDefaultMXBean(mBeanServer);
		LOG.info("Service URL:{} is initiated.", jmxUrl);
	}

	/**
	 * Get current host name. If the monitor.host field exists in the agent.conf
	 * file, it will return this. Otherwise, return automatically resolved host
	 * name.
	 *
	 * @param agentConfig agent config
	 * @return current host name
	 */
	protected String getCurrentHostName(AgentConfig agentConfig) {
		return agentConfig.getProperty("monitor.host", NetworkUtil.getLocalHostAddress());
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
	 * get monitor listener port of monitor JMX server.
	 *
	 * @return port listener port
	 */
	public int getPort() {
		return port;
	}

	/**
	 * Start monitoring.
	 *
	 * @throws IOException exception
	 */
	public void start() throws IOException {
		if (!isRunning()) {
			jmxServer.start();
			AgentDataCollectManager.getInstance().init(agentConfig);
			AgentDataCollectManager.getInstance().start();
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
			AgentDataCollectManager.getInstance().stop();
			UnicastRemoteObject.unexportObject(rmiRegistry, true);
		} catch (IOException e) {
			LOG.error(e.getMessage(), e);
		}
	}

}
