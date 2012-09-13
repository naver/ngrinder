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
package org.ngrinder.monitor.share.domain;

import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.RuntimeMXBean;
import java.lang.management.ThreadMXBean;

import javax.management.InstanceNotFoundException;
import javax.management.MBeanServerConnection;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.management.OperatingSystemMXBean;

@SuppressWarnings("restriction")
public class MBeanClient {
	private static final Logger LOG = LoggerFactory.getLogger(MBeanClient.class);

	private static final String JMX_URI = "/jndi/rmi://%s:%s/jmxrmi";

	public enum ConnectionState {
		CONNECTED, DISCONNECTED, CONNECTING
	}

	private ConnectionState connectionState = ConnectionState.DISCONNECTED;

	private String hostName = null;
	private int port = 0;
	private JMXServiceURL jmxUrl = null;

	private volatile boolean isDead = true;

	private MBeanServerConnection server = null;
	private JMXConnector jmxConnector = null;

	private MemoryMXBean memoryMBean = null;
	private RuntimeMXBean runtimeMBean = null;
	private ThreadMXBean threadMBean = null;

	private OperatingSystemMXBean sunOperatingSystemMXBean = null;

	/**
	 * Used to connect remote monitor JMX
	 * @param hostName
	 * @param port
	 * @throws IOException
	 */
	public MBeanClient(String hostName, int port) throws IOException {
		JMXServiceURL url = new JMXServiceURL("rmi", "", 0, String.format(JMX_URI, hostName, port));
		this.jmxUrl = url;
		this.hostName = url.getHost();
		this.port = url.getPort();
	}

	/**
	 * connect jmx
	 */
	public void connect() {
		setConnectionState(ConnectionState.CONNECTING);
		boolean bConnect = true;

		try {
			connectClient();
		} catch (IOException ex) {
			LOG.error(ex.getMessage());
			LOG.trace(ex.getMessage(), ex.getCause());
			bConnect = false;
		}

		if (!bConnect) {
			setConnectionState(ConnectionState.DISCONNECTED);
		} else {
			setConnectionState(ConnectionState.CONNECTED);
		}
	}

	public void disconnect() {
		IOUtils.closeQuietly(jmxConnector);

		memoryMBean = null;
		runtimeMBean = null;
		threadMBean = null;
		sunOperatingSystemMXBean = null;
		server = null;

		if (!isDead) {
			isDead = true;
			setConnectionState(ConnectionState.DISCONNECTED);
		}
	}

	// public void flush() {
	// if (server != null) {
	// server.flush();
	// }
	// }

	public Object getAttribute(ObjectName objName, String attrName) throws Exception {
		return server.getAttribute(objName, attrName);
	}

	public synchronized OperatingSystemMXBean getSunOperatingSystemMXBean() throws IOException,
			MalformedObjectNameException, NullPointerException, InstanceNotFoundException {

		ObjectName on = new ObjectName(ManagementFactory.OPERATING_SYSTEM_MXBEAN_NAME);
		if (sunOperatingSystemMXBean == null) {
			if (server.isInstanceOf(on, "com.sun.management.OperatingSystemMXBean")) {
				sunOperatingSystemMXBean = ManagementFactory.newPlatformMXBeanProxy(server,
								ManagementFactory.OPERATING_SYSTEM_MXBEAN_NAME,
								OperatingSystemMXBean.class);
			}
		}
		return sunOperatingSystemMXBean;
	}

	public synchronized MemoryMXBean getMemoryMXBean() throws IOException {
		if (memoryMBean == null) {
			memoryMBean = ManagementFactory.newPlatformMXBeanProxy(server,
							ManagementFactory.MEMORY_MXBEAN_NAME, MemoryMXBean.class);
		}
		return memoryMBean;
	}

	public synchronized RuntimeMXBean getRuntimeMXBean() throws IOException {
		if (runtimeMBean == null) {
			runtimeMBean = ManagementFactory.newPlatformMXBeanProxy(server,
							ManagementFactory.RUNTIME_MXBEAN_NAME, RuntimeMXBean.class);
		}
		return runtimeMBean;
	}

	public synchronized ThreadMXBean getThreadMXBean() throws IOException {
		if (threadMBean == null) {
			threadMBean = ManagementFactory.newPlatformMXBeanProxy(server,
							ManagementFactory.THREAD_MXBEAN_NAME, ThreadMXBean.class);
		}
		return threadMBean;
	}

	private void connectClient() throws IOException {
		if (jmxUrl == null && "localhost".equals(hostName) && port == 0) {
			server = ManagementFactory.getPlatformMBeanServer();
		} else {
			jmxConnector = JMXConnectorFactory.connect(jmxUrl);
			server = jmxConnector.getMBeanServerConnection();
		}
		this.isDead = false;
	}

//	public static ProxyMBeanServerConnection newProxyMBeanServerConnection(MBeanServerConnection mbsc) {
//		return (ProxyMBeanServerConnection) Proxy.newProxyInstance(MBeanClient.class.getClassLoader(),
//						new Class[] { ProxyMBeanServerConnection.class },
//						new ProxyMBeanServerInvocationHandler(mbsc));
//	}

	private void setConnectionState(ConnectionState connectionState) {
		this.connectionState = connectionState;
	}
	
	public ConnectionState getConnectionState() {
		return connectionState;
	}

	public boolean isConnected() {
		return !isDead;
	}
}
