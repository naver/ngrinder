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

import org.apache.commons.io.IOUtils;
import org.ngrinder.common.exception.NGrinderRuntimeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.management.MBeanServerConnection;
import javax.management.ObjectName;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.util.concurrent.*;

import static org.ngrinder.common.util.ExceptionUtils.processException;

/**
 * Client for the JMX monitor server.
 *
 * @author Mavlarn
 * @since 2.0
 */
public class MBeanClient {
    private static final Logger LOGGER = LoggerFactory.getLogger(MBeanClient.class);

    private static final String JMX_URI = "/jndi/rmi://%s:%s/jmxrmi";

    private JMXServiceURL jmxUrl = null;

    private volatile boolean connected = false;

    private MBeanServerConnection mbeanServerConnection = null;
    private JMXConnector jmxConnector = null;

    private int timeout = 2000;
    /**
     * Used to connect remote monitor JMX.
     *
     * @param hostName is the server name of target server
     * @param port     is the JMX server's listener port of target monitor server
     * @param timeout  the connection timeout og mbean client.
     * @throws IOException wraps JMX MalformedURLException exception
     */
    public MBeanClient(String hostName, int port, int timeout) throws IOException {
        this(hostName, port);
        this.timeout = timeout;
    }

    /**
     * Used to connect remote monitor JMX.
     *
     * @param hostName is the server name of target server
     * @param port     is the JMX server's listener port of target monitor server
     * @throws IOException wraps JMX MalformedURLException exception
     */
    public MBeanClient(String hostName, int port) throws IOException {
        this.jmxUrl = new JMXServiceURL("rmi", hostName, port, String.format(JMX_URI, hostName, port));
    }

    /**
     * connect with target JMX.
     */
    public void connect() {
        try {
            connectClient();
        } catch (Exception e) {
            LOGGER.info("Timeout while connecting to {}:{} monitor : {}", jmxUrl.getHost(), jmxUrl.getPort());
        }
    }


    @SuppressWarnings("UnusedDeclaration")
    public MBeanServerConnection getMBeanServerConnection() {
        return this.mbeanServerConnection;
    }

    /**
     * disconnect the MBeanClient. If it is remote JMX server, disconnect the connection. If it is
     * local, disconnect the binding.
     */
    public void disconnect() {
        connected = false;
        IOUtils.closeQuietly(jmxConnector);
        mbeanServerConnection = null;
    }


    /**
     * get the monitor object of the object name and attribute name. See
     * {@link org.ngrinder.monitor.domain.MonitorCollectionInfoDomain}.
     *
     * @param objName  is the object name of the object in JMX MBean server.
     * @param attrName is the attribute name
     * @return the monitor object from MBean
     * @throws Exception wraps all JMX related exception
     */
    public Object getAttribute(ObjectName objName, String attrName) throws Exception {
        return mbeanServerConnection.getAttribute(objName, attrName);
    }

    private void connectClient() throws IOException, TimeoutException {
        if (jmxUrl == null || ("localhost".equals(jmxUrl.getHost()) && jmxUrl.getPort() == 0)) {
            mbeanServerConnection = ManagementFactory.getPlatformMBeanServer();
        } else {
            jmxConnector = connectWithTimeout(jmxUrl, timeout);
            mbeanServerConnection = jmxConnector.getMBeanServerConnection();
        }
        this.connected = true;
    }

    private JMXConnector connectWithTimeout(final JMXServiceURL jmxUrl, int timeout) throws NGrinderRuntimeException, TimeoutException {
        try {
            ExecutorService executor = Executors.newSingleThreadExecutor();
            Future<JMXConnector> future = executor.submit(new Callable<JMXConnector>() {
                public JMXConnector call() throws IOException {
                    return JMXConnectorFactory.connect(jmxUrl);
                }
            });

            return future.get(timeout, TimeUnit.MILLISECONDS);
        } catch (TimeoutException e) {
            throw e;
        } catch (Exception e) {
            throw processException(e);
        }

    }

    public boolean isConnected() {
        return connected;
    }
}
