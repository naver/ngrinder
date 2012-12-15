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
	 * Reset the instance.
	 * 
	 */
	static void reset() {
		singleton = null;
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
