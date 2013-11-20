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
import org.python.google.common.net.InetAddresses;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.*;
import java.util.Enumeration;

import static org.ngrinder.common.util.ExceptionUtils.processException;

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
	public static String getLocalHostAddress(String byConnecting, int port) {
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
	public static String getLocalHostName(String byConnecting, int port) {
		InetAddress addr = getLocalInetAddress(byConnecting, port);
		if (addr != null) {
			return addr.getHostName();
		} else {
			return "localhost";
		}
	}

	private static InetAddress getLocalInetAddress(String byConnecting, int port) {
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

	private static InetAddress getAddressWithSocket(String byConnecting, int port) {
		Socket s = null;
		try {
			s = new Socket();
			SocketAddress addr = new InetSocketAddress(byConnecting, port);
			s.connect(addr, 2000); // 2 seconds timeout
			return s.getLocalAddress();
		} catch (Exception e) {
			return null;
		} finally {
			IOUtils.closeQuietly(s);
		}
	}

	private static InetAddress getFirstNonLoopbackAddress(boolean preferIpv4, boolean preferIPv6)
			throws SocketException {
		Enumeration<?> en = NetworkInterface.getNetworkInterfaces();
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
	 * Download a file from the given URL string.
	 *
	 * @param urlString URL string
	 * @param toFile    file to be saved.
	 */
	public static void downloadFile(String urlString, File toFile) {
		FileOutputStream os = null;
		InputStream in = null;
		URLConnection connection = null;
		try {
			URL url = new URL(urlString);
			connection = url.openConnection();
			connection.connect();
			byte[] buffer = new byte[4 * 1024];
			int read;
			os = new FileOutputStream(toFile);
			in = connection.getInputStream();
			while ((read = in.read(buffer)) > 0) {
				os.write(buffer, 0, read);
			}
		} catch (Exception e) {
			LOGGER.error("download file from {} was failed", urlString, e);
			throw processException("Error while download " + urlString, e);
		} finally {
			((HttpURLConnection) connection).disconnect();
			IOUtils.closeQuietly(os);
			IOUtils.closeQuietly(in);
		}
		return;
	}

	public static String getIP(String ip) {
		if (InetAddresses.isInetAddress(ip)) {
			return ip;
		}
		try {
			InetAddress byName = InetAddress.getByName(ip);
			ip = byName.getHostAddress();
		} catch (UnknownHostException e) {
			ip = "127.0.0.1";
		}
		return ip;
	}
}
