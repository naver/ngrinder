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
package org.ngrinder.monitor.agent;

import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.net.InetAddress;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.Set;

import javax.management.InstanceAlreadyExistsException;
import javax.management.MBeanRegistrationException;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.NotCompliantMBeanException;
import javax.management.remote.JMXConnectorServer;
import javax.management.remote.JMXConnectorServerFactory;
import javax.management.remote.JMXServiceURL;

import org.ngrinder.monitor.MonitorConstants;
import org.ngrinder.monitor.MonitorContext;
import org.ngrinder.monitor.agent.collector.AgentDataCollectManager;
import org.ngrinder.monitor.agent.mxbean.core.MXBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * This class is the agent of monitor server, it will be used to start a JMX server.
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

	private static final AgentMonitorServer instance = new AgentMonitorServer();

	private AgentMonitorServer() {}

	public static AgentMonitorServer getInstance() {
		return instance;
	}

	public void init() throws MalformedObjectNameException, InstanceAlreadyExistsException, MBeanRegistrationException,
			NotCompliantMBeanException, IOException {
		this.init(MonitorConstants.DEFAULT_MONITOR_PORT);
	}

	public void init(final int port) throws MalformedObjectNameException, InstanceAlreadyExistsException,
			MBeanRegistrationException, NotCompliantMBeanException, IOException {
		this.init(port, MonitorConstants.DEFAULT_DATA_COLLECTOR);
	}

	public void init(final int port, final Set<String> dataCollector) throws IOException,
			MalformedObjectNameException, InstanceAlreadyExistsException, MBeanRegistrationException,
			NotCompliantMBeanException {

		MonitorContext.getInstance().setDataCollectors(dataCollector);

		this.port = port;
		this.rmiRegistry = LocateRegistry.createRegistry(port);
		this.mBeanServer = ManagementFactory.getPlatformMBeanServer();

		final String hostname = InetAddress.getLocalHost().getHostName();
		final String jmxUrlString = "service:jmx:rmi://" + hostname + ":" + port + "/jndi/rmi://" + hostname + ":" + port + "/jmxrmi";
		JMXServiceURL jmxUrl = new JMXServiceURL(jmxUrlString);
		this.jmxServer = JMXConnectorServerFactory.newJMXConnectorServer(jmxUrl, null, mBeanServer);
		AgentRegisterMXBean.getInstance().addDefaultMXBean(mBeanServer);
		LOG.info("Service URL:{} is initiated.", jmxUrl);
	}

	public void addMXBean(String subDomainName, MXBean mxBean) throws MalformedObjectNameException,
			InstanceAlreadyExistsException, MBeanRegistrationException, NotCompliantMBeanException,
			NullPointerException {
		AgentRegisterMXBean.getInstance().addMXBean(mBeanServer, subDomainName, mxBean);
	}

	public boolean isRunning() {
		return isRunning;
	}

	public int getPort() {
		return port;
	}

	public void start() throws IOException {
		if (!isRunning()) {
			jmxServer.start();
			AgentDataCollectManager.getInstance().start();
			isRunning = true;
		}
	}

	public void stop() throws IOException {
		if (isRunning()) {
			isRunning = false;
			jmxServer.stop();
			AgentDataCollectManager.getInstance().stop();
			UnicastRemoteObject.unexportObject(rmiRegistry, true);
		}
	}

	public void restart() throws IOException {
		stop();
		start();
	}
}
