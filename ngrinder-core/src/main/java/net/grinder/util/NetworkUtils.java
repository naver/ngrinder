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
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.python.google.common.net.InetAddresses;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.naming.Context;
import javax.naming.NamingException;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;
import java.io.IOException;
import java.net.*;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;

import static java.net.NetworkInterface.getNetworkInterfaces;
import static org.ngrinder.common.util.ExceptionUtils.processException;
import static org.ngrinder.common.util.NoOp.noOp;

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
public abstract class NetworkUtils {
	private static final Logger LOGGER = LoggerFactory.getLogger(NetworkUtils.class);
	public static String DEFAULT_LOCAL_HOST_ADDRESS = getLocalHostAddress();
	public static String DEFAULT_LOCAL_HOST_NAME = getLocalHostName();
	public static List<InetAddress> DEFAULT_LOCAL_ADDRESSES = getAllLocalNonLoopbackAddresses(false);

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


	public static InetAddress getAddressWithSocket(String byConnecting, int port) {
		Socket s = new Socket();
		try {
			if (tryConnection(byConnecting, port, s)) {
				return s.getLocalAddress();
			}
		} finally {
			IOUtils.closeQuietly(s);
		}
		return null;
	}

	public static boolean tryConnection(String byConnecting, int port, Socket socket) {
		try {
			socket.connect(new InetSocketAddress(byConnecting, port), 2000); // 2 seconds timeout
		} catch (Exception e) {
			return false;
		}
		return true;
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

	/**
	 * Get the available ports.
	 *
	 * @param size port size
	 * @param from port number starting from
	 * @return port list
	 */
	public static List<Integer> getAvailablePorts(String ip, int size, int from, int limit) {
		List<Integer> ports = new ArrayList<Integer>(size);
		int freeSocket;
		InetAddress inetAddress = null;
		if (StringUtils.isNotBlank(ip)) {
			try {

				inetAddress = InetAddress.getByName(ip);
			} catch (Exception e) {
				noOp();
			}
		}
		for (int i = 0; i < size; i++) {
			freeSocket = checkPortAvailability(inetAddress, from, limit);
			ports.add(freeSocket);
			from = freeSocket + 1;
		}
		return ports;
	}

	/**
	 * Get a available port greater than the given port.
	 *
	 * @param scanStartPort port scan from
	 * @return min port available from scanStartPort
	 */
	public static int checkPortAvailability(InetAddress inetAddress, int scanStartPort, int limit) {
		while (true) {
			if (checkExactPortAvailability(inetAddress, scanStartPort)) {
				return scanStartPort;
			}
			if (scanStartPort++ > limit) {
				throw processException("no port is available");
			}
		}
	}

	/**
	 * Check if the given port is available.
	 *
	 * @param inetAddress address to be bound
	 * @param port        port to be checked
	 * @return true if available
	 */
	private static boolean checkExactPortAvailability(InetAddress inetAddress, int port) {
		ServerSocket socket = null;
		try {
			if (inetAddress == null) {
				socket = new ServerSocket(port);
			} else {
				socket = new ServerSocket(port, 1, inetAddress);
			}
			return true;
		} catch (IOException e) {
			return false;
		} finally {
			if (socket != null) {
				try {
					socket.close();
				} catch (IOException e) {
					// FALL THROUGH
					noOp();
				}
			}
		}
	}

	/**
	 * Check if the current machine support IP6
	 *
	 * @return true if the IP6 is supported.
	 */
	public static boolean isIP6Supported() {
		final Enumeration<NetworkInterface> networkInterfaces;
		try {
			networkInterfaces = getNetworkInterfaces();
			while (networkInterfaces.hasMoreElements()) {
				final NetworkInterface networkInterface = networkInterfaces.nextElement();
				if (networkInterface.isUp() && !networkInterface.isLoopback() && !networkInterface.isPointToPoint()) {
					final Enumeration<InetAddress> inetAddresses = networkInterface.getInetAddresses();
					while (inetAddresses.hasMoreElements()) {
						final InetAddress inetAddress = inetAddresses.nextElement();
						if (inetAddress instanceof Inet6Address) {
							return true;
						}
					}
				}
			}
		} catch (SocketException e) {
			LOGGER.error("Error while resolving non look back local addresses.", e);
		}
		return false;

	}

	/**
	 * Get the all IP binding address.
	 *
	 * @return [::] if IP6 is supported, "0.0.0.0" otherwise.
	 */
	public static String getAllPBindingAddress() {
		return isIP6Supported() ? "[::]" : "0.0.0.0";
	}

	public static class IPPortPair {
		private InetAddress ip;
		private final int port;

		public IPPortPair(String ip, int port) {
			try {
				this.ip = InetAddress.getByName(ip);
			} catch (UnknownHostException e) {
				LOGGER.error("{} is not accessible ip");
			}
			this.port = port;
		}

		public boolean isValid() {
			return ip != null;
		}

		public int getPort() {
			return port;
		}

		public String getIP() {
			return ip.getHostAddress();
		}

		public String getFormattedIP() {
			if (isIP6()) {
				return "[" + ip.getHostAddress() + "]";
			} else {
				return ip.getHostAddress();
			}
		}

		@Override
		public String toString() {
			return getFormattedIP() + ":" + this.port;
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (o == null || getClass() != o.getClass()) {
				return false;
			}

			IPPortPair that = (IPPortPair) o;
			return port == that.port && !(ip != null ? !ip.equals(that.ip) : that.ip != null);

		}

		@Override
		public int hashCode() {
			int result = ip != null ? ip.hashCode() : 0;
			result = 31 * result + port;
			return result;
		}

		public boolean isLocalHost() {
			if (ip == null) {
				return false;
			}
			if (ip.isAnyLocalAddress() || ip.isLoopbackAddress() || ip.isLinkLocalAddress()) {
				return true;
			}
			try {
				return NetworkInterface.getByInetAddress(ip) != null;
			} catch (SocketException e) {
				return false;
			}
		}

		public boolean isIP6() {
			return ip instanceof Inet6Address;
		}
	}

	/**
	 * Convert the given string to ip and port pair.
	 * <p/>
	 * This supports IP6 and IP4.
	 * <p/>
	 * <ul>
	 * <li>127.0.0.1:30  ==> 127.0.0.1 and 30</li>
	 * <li>2001:0:9d38:90d7:469:1f94:f5bf:cf5d:30  ==> 2001:0:9d38:90d7:469:1f94:f5bf:cf5d and 30</li>
	 * <li>[2001:0:9d38:90d7:469:1f94:f5bf:cf5d]:30  ==> 2001:0:9d38:90d7:469:1f94:f5bf:cf5d and 30</li>
	 * </ul>
	 *
	 * @param ipPortString textual representation of ip and port pair
	 * @param defaultPort  default port used when port is invisible.
	 * @return ip and port pair
	 */
	public static IPPortPair convertIPAndPortPair(String ipPortString, int defaultPort) {
		// If it's the scoped IP6 address
		ipPortString = removeScopedMarkerFromIP(ipPortString);
		if (InetAddresses.isInetAddress(ipPortString)) {
			return new IPPortPair(ipPortString, defaultPort);
		}
		final int i = ipPortString.lastIndexOf(":");
		String ipPart = ipPortString;
		int portPart = defaultPort;
		if (i != -1) {
			portPart = NumberUtils.toInt(ipPortString.substring(i + 1));
			ipPart = ipPortString.substring(0, i);
		}
		return new IPPortPair(getIP(ipPart), portPart);
	}

	public static String removeScopedMarkerFromIP(String ip) {
		if (StringUtils.isNotEmpty(ip) && ip.contains("%")) {
			ip = ip.substring(0, ip.lastIndexOf("%"));
		}
		return ip;
	}

	/**
	 * Get IP form the given string.
	 * <p/>
	 * If the given ipOrHost is host name, it tries to turn it into IP.
	 * If the host name is not available, it returns 127.0.0.1 instead.
	 * ff
	 *
	 * @param ipOrHost textual representation of ip or host name
	 * @return ip
	 */
	public static String getIP(String ipOrHost) {
		String ip = ipOrHost;
		if (InetAddresses.isInetAddress(ip)) {
			return ip;
		}
		try {
			ip = InetAddress.getByName(ipOrHost).getHostAddress();
		} catch (UnknownHostException e) {
			ip = "127.0.0.1";
			LOGGER.error("Error while resolving {} to IP. Use {} instead.", ipOrHost, ip);
			LOGGER.debug("Details : ", e);
		}
		return ip;
	}


	private static List<InetAddress> getAllLocalNonLoopbackAddresses(boolean onlyIPv4) {
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
						if (onlyIPv4 && inetAddress instanceof Inet6Address) {
							continue;
						}
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

	public static List<String> getDnsServers() throws NamingException {
		Hashtable<String, String> env = new Hashtable<String, String>();
		env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.dns.DnsContextFactory");
		DirContext ctx = null;
		List<String> dnsServers = new ArrayList<String>();
		try {
			ctx = new InitialDirContext(env);
			String dnsString = (String) ctx.getEnvironment().get("java.naming.provider.url");
			for (String each : dnsString.split(" ")) {
				dnsServers.add(each.replace("dns://", ""));
			}
		} catch (Exception e) {
			NoOp.noOp();
		} finally {
			if (ctx != null) {
				ctx.close();
			}
		}
		return dnsServers;
	}
}
