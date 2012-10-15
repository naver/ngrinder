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

import javax.management.MBeanServerConnection;
import javax.management.ObjectName;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * Client for the JMX monitor server.
 *
 * @author Mavlarn
 * @since 2.0
 */
public class MBeanClient {
	private static final Logger LOG = LoggerFactory.getLogger(MBeanClient.class);

	private static final String JMX_URI = "/jndi/rmi://%s:%s/jmxrmi";

	/**
	 * Enum class for connection state.
	 */
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

	/**
	 * Used to connect remote monitor JMX.
	 * 
	 * @param hostName is the server name of target server
	 * @param port is the JMX server's listener port of target monitor server
	 * @throws IOException
	 */
	public MBeanClient(String hostName, int port) throws IOException {
		JMXServiceURL url = new JMXServiceURL("rmi", "", 0, String.format(JMX_URI, hostName, port));
		this.jmxUrl = url;
		this.hostName = url.getHost();
		this.port = url.getPort();
	}

	/**
	 * connect with target JMX.
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

	/**
	 * disconnect the MBclient.
	 * If it is remote JMX server, disconnect the connection. If it is local, disconnect the
	 * binding.
	 */
	public void disconnect() {
		IOUtils.closeQuietly(jmxConnector);

		server = null;

		if (!isDead) {
			isDead = true;
			setConnectionState(ConnectionState.DISCONNECTED);
		}
	}

	/**
	 * get the monitor object of the object name and attribute name. See {@link MonitorCollectionInfoDomain}.
	 * 
	 * @param objName is the object name of the object in JMX MBean server.
	 * @param attrName is the attribute name
	 * @return the monitor object from MBean
	 * @throws Exception
	 */
	public Object getAttribute(ObjectName objName, String attrName) throws Exception {
		return server.getAttribute(objName, attrName);
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
