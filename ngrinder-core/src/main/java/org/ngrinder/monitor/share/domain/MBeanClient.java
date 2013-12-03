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
	 * @param hostName
	 *            is the server name of target server
	 * @param port
	 *            is the JMX server's listener port of target monitor server
	 * @throws IOException
	 *             wraps JMX MalformedURLException exception
	 */
	public MBeanClient(String hostName, int port) throws IOException {
		JMXServiceURL url = new JMXServiceURL("rmi", hostName, port, String.format(JMX_URI, hostName, port));
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
			LOG.debug(ex.getMessage());
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
	 * disconnect the MBeanClient. If it is remote JMX server, disconnect the connection. If it is
	 * local, disconnect the binding.
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
	 * get the monitor object of the object name and attribute name. See
	 * {@link MonitorCollectionInfoDomain}.
	 * 
	 * @param objName
	 *            is the object name of the object in JMX MBean server.
	 * @param attrName
	 *            is the attribute name
	 * @return the monitor object from MBean
	 * @throws Exception
	 *             wraps all JMX related exception
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
