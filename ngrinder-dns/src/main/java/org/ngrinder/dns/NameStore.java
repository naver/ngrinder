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
package org.ngrinder.dns;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Local Dns Name Storage.
 * 
 * @author JunHo Yoon
 * @since 3.0
 */
public class NameStore {

	private static NameStore singleton;

	private Map<String, String> globalNames;

	protected NameStore() {
		globalNames = new ConcurrentHashMap<String, String>();
	}

	/**
	 * Initialize NameStore based on the ngrinder.etc.hosts system properties.
	 */
	public void initFromSystemProperty() {
		String hostPair = System.getProperty("ngrinder.etc.hosts");
		if (!DnsUtil.isEmpty(hostPair)) {
			String[] hostPairs = hostPair.split(",");
			for (String pair : hostPairs) {
				String[] each = pair.split(":");
				if (each.length != 2 || DnsUtil.isEmpty(each[0]) || DnsUtil.isEmpty(each[1])) {
					continue;
				}
				singleton.put(each[0].trim(), each[1].trim());
			}
		}
	}

	/**
	 * Get the instance.
	 * 
	 * @return {@link NameStore} instance
	 */
	public static NameStore getInstance() {
		if (singleton == null) {
			singleton = new NameStore();
			singleton.initFromSystemProperty();
		}
		return singleton;
	}

	/**
	 * Put hostname with ipAddress.
	 * 
	 * @param hostName
	 *            host name
	 * @param ipAddress
	 *            ip address
	 */
	public void put(String hostName, String ipAddress) {
		globalNames.put(hostName, ipAddress);
	}

	/**
	 * Remove hostname from the store.
	 * 
	 * @param hostName
	 *            host name
	 */
	public void remove(String hostName) {
		globalNames.remove(hostName);
	}

	/**
	 * Get ip from hostname.
	 * 
	 * @param hostName
	 *            host name
	 * @return ip if found. null otherwise.
	 */
	public String get(String hostName) {
		return globalNames.get(hostName);
	}

	/**
	 * Get the hostname for the given ip.
	 * 
	 * @param ip
	 *            ip
	 * @return resolved host name. Null if not found.
	 */
	public String getReveredHost(String ip) {
		for (String hostName : globalNames.keySet()) {
			if (globalNames.get(hostName).equals(ip)) {
				return hostName;
			}
		}
		return null;
	}

}
