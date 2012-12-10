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
package net.grinder.util;

import java.io.IOException;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Enumeration;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Common utility for network env.
 * 
 * @author JunHo Yoon
 * @since 3.0
 * 
 */
public abstract class NetworkUtil {
	private static final Logger LOGGER = LoggerFactory.getLogger(NetworkUtil.class);

	public static void main(String[] args) {
		System.out.println("host addr:" + NetworkUtil.getLocalHostAddress());
		System.out.println("host name:" + NetworkUtil.getLocalHostName());
	}
	/**
	 * Get the local host address, try to get actual IP.
	 * 
	 * @return ip form of host address
	 */
	public static String getLocalHostAddress() {
		String addr = null;
		try {
			addr = InetAddress.getLocalHost().getHostAddress();
		} catch (UnknownHostException e) {
			LOGGER.error("Error while get localhost address", e);
		}
		if (addr == null && !"127.0.0.1".equals(addr)) {
			return addr;
		}
		return getLocalHostAddress("www.baidu.com", 80);
	}

	/**
	 * Get local address by connecting to a server.
	 * @param byConnecting
	 * 				the server address to conenct.
	 * @param port
	 * 				the port to connect
	 * @return IP address
	 * 				local IP address
	 */
	public static String getLocalHostAddress(String byConnecting, int port) {
		InetAddress addr = getLocalInetAddress(byConnecting, port);
		if (addr != null) {
			return addr.getHostAddress();
		} else {
			return "127.0.0.1";
		}
	}

	/**
	 * Get local host name by connecting to a server.
	 * @param byConnecting
	 * 				the server address to conenct.
	 * @param port
	 * 				the port to connect
	 * @return host name
	 * 				local host name
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
		Socket s = null;
		Socket s2 = null;

		try {
			s = new Socket();
			SocketAddress addr = new InetSocketAddress(byConnecting, port);
			s.connect(addr, 1000); // 1 seconds timeout
			return s.getLocalAddress();
		} catch (IOException e) {
			// For safety.
			try {
				s2 = new Socket();
				SocketAddress addr = new InetSocketAddress("www.google.com", 80);
				s2.connect(addr, 1000); // 1 seconds timeout
				return s2.getLocalAddress();
			} catch (Exception e1) {
				try {
					return getFirstNonLoopbackAddress(true, false);
				} catch (SocketException e2) {
					return null;
				}
			}
		} finally {
			IOUtils.closeQuietly(s);
			IOUtils.closeQuietly(s2);

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
			for (Enumeration<?> en2 = i.getInetAddresses(); en2.hasMoreElements();) {
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
	 * Get local host name.
	 * 
	 * @return local host name
	 */
	public static String getLocalHostName() {
		try {
			String hostName = InetAddress.getLocalHost().getHostName();
			return hostName;
		} catch (UnknownHostException e) {
			LOGGER.error("Error while get localhost name", e);
			//get by connecting to server
			return getLocalHostName("www.nhnopensource.org", 80);
		}
	}

	/**
	 * Get the IP addresses from host name.
	 * 
	 * @param host
	 *            host
	 * @return {@link InetAddress} array
	 */
	public static InetAddress[] getIpsFromHost(String host) {
		try {
			return InetAddress.getAllByName(host);
		} catch (UnknownHostException e) {
			return new InetAddress[] {};
		}
	}

}
