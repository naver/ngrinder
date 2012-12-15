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
package org.ngrinder.monitor.share;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

import org.ngrinder.monitor.share.domain.MBeanClient;

/**
 * 
 * Used to save the {@link MBeanClient} of monitor target.
 * 
 * @author Mavlarn
 * @since 2.0
 */
public final class CachedMBeanClient {
	private static ConcurrentHashMap<String, MBeanClient> cache = new ConcurrentHashMap<String, MBeanClient>();

	private CachedMBeanClient() {
	}

	/**
	 * Get {@link MBeanClient} of one target from the cache, if it doesn'r exist in cache, create a
	 * new one and put into cache.
	 * 
	 * @param hostName
	 *            is server name of monitor target
	 * @param port
	 *            is the monitor listener of JMX on target
	 * @return MBeanClient of the target server
	 * @throws IOException
	 *             IO exception of JMX
	 */
	public static MBeanClient getMBeanClient(String hostName, int port) throws IOException {
		final String key = getCacheKey(hostName, port);
		MBeanClient mc = cache.get(key);
		if (mc == null) {
			mc = new MBeanClient(hostName, port);
			cache.putIfAbsent(key, mc);
		}
		return mc;
	}

	private static String getCacheKey(String hostName, int port) {
		return (hostName == null ? "" : hostName) + "_" + port;
	}

}
