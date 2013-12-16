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
package org.ngrinder.monitor.agent;

import org.ngrinder.monitor.mxbean.core.MXBean;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 
 * Used to store monitor MXBean in a map, with the domain name as the key.
 *
 * @author Mavlarn
 * @since 2.0
 */
public final class MXBeanStorage {
	private Map<String, MXBean> cachedMxBeans = new ConcurrentHashMap<String, MXBean>();
	private static final MXBeanStorage INSTANCE = new MXBeanStorage();

	private MXBeanStorage() {
	}

	public int getSize() {
		return cachedMxBeans.size();
	}

	public static MXBeanStorage getInstance() {
		return INSTANCE;
	}

	/**
	 * get the monitor MXBean from the storage.
	 * @param key is the domain name of JMX
	 * @return MXBean registered with this key
	 */
	public MXBean getMXBean(String key) {
		return cachedMxBeans.get(key);
	}

	/**
	 * Add the monitor MXBean into the storage, with the domain name as the key.
	 * @param key is the domain name of JMX
	 * @param mxBean is the monitor MXBean
	 */
	public void addMXBean(String key, MXBean mxBean) {
		cachedMxBeans.put(key, mxBean);
	}

	public Collection<MXBean> getMXBeans() {
		return cachedMxBeans.values();
	}
}
