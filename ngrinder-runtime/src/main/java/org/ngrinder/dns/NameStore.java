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
package org.ngrinder.dns;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Local Dns Name Storage.
 *
 * @author JunHo Yoon
 * @since 3.0
 */
@SuppressWarnings("WeakerAccess")
public class NameStore {

	private static NameStore singleton;

	private final Map<String, Set<InetAddress>> globalNames;

	private NameStore() {
		globalNames = new ConcurrentHashMap<String, Set<InetAddress>>();
	}

	/**
	 * Initialize NameStore based on the ngrinder.etc.hosts system properties.
	 */
	public void initFromSystemProperty() {
		String hostPair = System.getProperty("ngrinder.etc.hosts");
		if (!DnsUtils.isEmpty(hostPair)) {
			String[] hostPairs = hostPair.split(",");
			for (String pair : hostPairs) {
				String[] each = pair.split(":");
				if (each.length != 2 || DnsUtils.isEmpty(each[0]) || DnsUtils.isEmpty(each[1])) {
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
			try {
				java.security.Security.setProperty("networkaddress.cache.ttl", "0");
			} catch (Exception e) {
				noOp();
			}
		}
		return singleton;
	}

	/**
	 * Reset the instance.
	 */
	void reset() {
		globalNames.clear();
		initFromSystemProperty();
	}

	private static void noOp() {
	}

	/**
	 * Put hostname with ipAddress.
	 *
	 * @param hostName  host name
	 * @param ipAddress ip address
	 */
	public void put(String hostName, String ipAddress) {
		Set<InetAddress> ipAddresses = globalNames.get(hostName);
		if (ipAddresses == null) {
			ipAddresses = new HashSet<InetAddress>();
			globalNames.put(hostName, ipAddresses);
		}

		try {
			InetAddress address = InetAddress.getByAddress(DnsUtils.textToNumericFormat(ipAddress));
			ipAddresses.add(address);
		} catch (UnknownHostException ignored) {
			noOp();
		}
	}


	/**
	 * Remove hostname from the store.
	 *
	 * @param hostName host name
	 */
	@SuppressWarnings("SameParameterValue")
	public void remove(String hostName) {
		globalNames.remove(hostName);
	}

	/**
	 * Get ip from hostname.
	 *
	 * @param hostName host name
	 * @return ip if found. null otherwise.
	 */
	public Set<InetAddress> get(String hostName) {
		return globalNames.get(hostName);
	}

	/**
	 * Get the hostname for the given ip.
	 *
	 * @param ip ip
	 * @return resolved host name. Null if not found.
	 */
	public String getReveredHost(InetAddress ip) {
		for (String hostName : globalNames.keySet()) {
			if (globalNames.get(hostName).contains(ip)) {
				return hostName;
			}
		}
		return null;
	}
}
