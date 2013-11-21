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
package net.grinder.util;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.python.google.common.net.InetAddresses;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.*;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import static java.net.NetworkInterface.getNetworkInterfaces;

/**
 * Common network utility. This contains very careful implementation to detect current machine's ip.
 * There are the following cases which block to get the appropriate ip.
 * <p/>
 * <ul>
 * <li>If there are VM in the same machine</li>
 * <li>If /etc/hosts are not very well specified</li>
 * </ul>
 *
 * @author JunHo Yoon
 * @author Mavlarn
 * @since 3.0
 */
public abstract class NetworkUtil {
	private static final Logger LOGGER = LoggerFactory.getLogger(NetworkUtil.class);
	public static String DEFAULT_LOCAL_HOST_ADDRESS = getLocalHostAddress();
	public static String DEFAULT_LOCAL_HOST_NAME = getLocalHostName();
	public static List<InetAddress> DEFAULT_LOCAL_ADDRESSES = getAllLocalNonLoopbackAddresses();

	/**
	 * Get the local host address, try to get actual IP.
	 *
	 * @return ip form of host address
	 */
	public static String getLocalHostAddress() {
		InetAddress localHost = null;
		try {
			localHost = InetAddress.getLocalHost();
		} catch (Exception e) {
			LOGGER.error("Error while get localhost address", e);
		}
		if (localHost != null && !localHost.isLoopbackAddress()) {
			return localHost.getHostAddress();
		}
		return getLocalHostAddress("www.google.com", 80);
	}

	/**
	 * Get local address by connecting to a server.
	 *
	 * @param byConnecting the server address to connect.
	 * @param port         the port to connect
	 * @return IP address local IP address
	 */
	static String getLocalHostAddress(String byConnecting, int port) {
		InetAddress addr = getLocalInetAddress(byConnecting, port);
		if (addr != null) {
			return addr.getHostAddress();
		} else {
			// It's final...
			return "127.0.0.1";
		}
	}

	/**
	 * Get local host name by connecting to a server.
	 *
	 * @param byConnecting the server address to connect.
	 * @param port         the port to connect
	 * @return localhost name. if fails, return "localhost"
	 */
	static String getLocalHostName(String byConnecting, int port) {
		InetAddress addr = getLocalInetAddress(byConnecting, port);
		if (addr != null) {
			return addr.getHostName();
		} else {
			return "localhost";
		}
	}

	static InetAddress getLocalInetAddress(String byConnecting, int port) {
		InetAddress addr = getAddressWithSocket(byConnecting, port);
		if (addr == null) {
			addr = getAddressWithSocket("www.baidu.com", 80);
		}
		if (addr == null) {
			try {
				addr = getFirstNonLoopbackAddress(true, false);
			} catch (SocketException e2) {
				addr = null;
			}
		}
		return addr;
	}

	static InetAddress getAddressWithSocket(String byConnecting, int port) {
		Socket s = null;
		try {
			s = new Socket();
			SocketAddress addr = new InetSocketAddress(byConnecting, port);
			s.connect(addr, 1000); // 2 seconds timeout
			return s.getLocalAddress();
		} catch (Exception e) {
			return null;
		} finally {
			IOUtils.closeQuietly(s);
		}
	}

	static InetAddress getFirstNonLoopbackAddress(boolean preferIpv4, boolean preferIPv6)
			throws SocketException {
		Enumeration<?> en = getNetworkInterfaces();
		while (en.hasMoreElements()) {
			NetworkInterface i = (NetworkInterface) en.nextElement();
			if (!i.isUp()) {
				continue;
			}
			for (Enumeration<?> en2 = i.getInetAddresses(); en2.hasMoreElements(); ) {
				InetAddress addr = (InetAddress) en2.nextElement();
				if (!addr.isLoopbackAddress()) {
					if (addr instanceof Inet4Address) {
						if (preferIPv6) {
							continue;
						}
						return addr;
					}
					if (addr instanceof Inet6Address) {
						if (preferIpv4) {
							continue;
						}
						return addr;
					}
				}
			}
		}
		return null;
	}

	/**
	 * Get local host name. On some platform, InetAddress.getLocalHost().getHostName() will return
	 * "localhost". If the /etc/hosts file is not set properly, it will return "localhost" or throw
	 * exception. So, at this circumstance, we will get the address by connecting a network address.
	 *
	 * @return local host name
	 */
	public static String getLocalHostName() {
		String hostName = null;
		try {
			hostName = InetAddress.getLocalHost().getHostName();
		} catch (Exception e) {
			LOGGER.error("Error while get localhost name", e);
		}
		if (hostName != null && !"localhost".equals(hostName)) {
			return hostName;
		}
		return getLocalHostName("www.google.com", 80);

	}

	/**
	 * Get the IP addresses from host name.
	 *
	 * @param host host
	 * @return {@link InetAddress} array
	 */
	public static InetAddress[] getIpsFromHost(String host) {
		try {
			return InetAddress.getAllByName(host);
		} catch (UnknownHostException e) {
			LOGGER.error("Error while get localhost name for {}", host, e);
			return new InetAddress[]{};
		}
	}

	public static class IPPortPair {
		private final String ip;
		private final int port;

		public IPPortPair(String ip, int port) {
			this.ip = ip;
			this.port = port;
		}

		public boolean isSame(String ip) {
			return this.ip.equals(ip);
		}

		public int getPort() {
			return port;
		}

		public String getIP() {
			return ip;
		}

		@Override
		public String toString() {
			return this.ip + " " + this.port;
		}
	}

	public static IPPortPair convertIPAndPortPair(String ipAndPortPair, int defaultPort) {
		if (InetAddresses.isInetAddress(ipAndPortPair)) {
			return new IPPortPair(ipAndPortPair, defaultPort);
		}
		final int i = ipAndPortPair.lastIndexOf(":");
		String ipPart = ipAndPortPair;
		int portPart = defaultPort;
		if (i != -1) {
			portPart = NumberUtils.toInt(ipAndPortPair.substring(i + 1));
			ipPart = ipAndPortPair.substring(0, i);
		}
		return new IPPortPair(getIP(ipPart), portPart);
	}

	public static String getIP(String ipOrHost) {
		String ip = ipOrHost;
		if (InetAddresses.isInetAddress(ip)) {
			return ip;
		}
		try {
			ip = InetAddress.getByName(ipOrHost).getHostAddress();
		} catch (UnknownHostException e) {
			ip = "127.0.0.1";
			LOGGER.error("Error while resolving {} to IP. Use {} instead.", new Object[]{ipOrHost, ip}, e);
		}
		return ip;
	}

	private static List<InetAddress> getAllLocalNonLoopbackAddresses() {
		List<InetAddress> addresses = new ArrayList<InetAddress>();
		final Enumeration<NetworkInterface> networkInterfaces;
		try {
			networkInterfaces = getNetworkInterfaces();
			while (networkInterfaces.hasMoreElements()) {
				final NetworkInterface networkInterface = networkInterfaces.nextElement();
				if (networkInterface.isUp()) {
					final Enumeration<InetAddress> inetAddresses = networkInterface.getInetAddresses();
					while (inetAddresses.hasMoreElements()) {
						final InetAddress inetAddress = inetAddresses.nextElement();
						if (!inetAddress.isLoopbackAddress()) {
							addresses.add(inetAddress);
						}
					}
				}
			}
		} catch (SocketException e) {
			LOGGER.error("Error while resolving non look back local addresses.", e);
		}
		return addresses;
	}
}
