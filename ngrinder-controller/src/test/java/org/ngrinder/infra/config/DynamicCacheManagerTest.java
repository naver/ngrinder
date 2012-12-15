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
package org.ngrinder.infra.config;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import net.grinder.util.NetworkUtil;
import net.sf.ehcache.CacheManager;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.ngrinder.common.util.ThreadUtil;
import org.springframework.cache.Cache;
import org.springframework.cache.ehcache.EhCacheCacheManager;

public class DynamicCacheManagerTest {
	ReloadableDynamicCacheManager dynamicCacheManagerConfig1;
	ReloadableDynamicCacheManager dynamicCacheManagerConfig2;
	private EhCacheCacheManager dynamicCacheManager1;
	private EhCacheCacheManager dynamicCacheManager2;

	class ReloadableDynamicCacheManager extends DynamicCacheConfig {
		final int port;
		final String cacheName;
		CacheManager mgr;

		ReloadableDynamicCacheManager(int port, String cacheName) {
			this.port = port;
			this.cacheName = cacheName;
		}

		@Override
		void setCacheManager(CacheManager mgr) {
			this.mgr = mgr;
		}

		@Override
		String getCacheName() {
			return this.cacheName;
		}

		@Override
		int getCacheListenerPort() {
			return this.port;
		}
	}

	@Before
	public void before() {
		dynamicCacheManagerConfig1 = new ReloadableDynamicCacheManager(40001, "cache1");
		Config config = mock(Config.class);
		dynamicCacheManagerConfig1.setConfig(config);
		String currentIp = NetworkUtil.getLocalHostAddress();
		when(config.getClusterURIs()).thenReturn(new String[] { currentIp + ":40001", currentIp + ":40002" });
		when(config.getCurrentIP()).thenReturn(currentIp);
		when(config.isCluster()).thenReturn(true);
		dynamicCacheManager1 = dynamicCacheManagerConfig1.dynamicCacheManager();
		dynamicCacheManager1.afterPropertiesSet();
		dynamicCacheManagerConfig2 = new ReloadableDynamicCacheManager(40002, "cache2");
		when(config.getClusterURIs()).thenReturn(new String[] { currentIp + ":40001", currentIp + ":40002" });
		dynamicCacheManagerConfig2.setConfig(config);
		dynamicCacheManager2 = dynamicCacheManagerConfig2.dynamicCacheManager();
		dynamicCacheManager2.afterPropertiesSet();
	}

	@Test
	public void testDynamicCache() throws InterruptedException {
		System.setProperty("net.sf.ehcache.skipUpdateCheck", "true");
		Cache cache1 = dynamicCacheManager1.getCache("agent_request");
		Cache cache2 = dynamicCacheManager2.getCache("agent_request");

		cache2.put("hello", "127.0.0.1");
		ThreadUtil.sleep(1000);
		assertThat((String) cache1.get("hello").get(), is("127.0.0.1"));
		ThreadUtil.sleep(4000);
		assertThat(cache1.get("hello"), not(nullValue()));
		assertThat(cache2.get("hello"), not(nullValue()));
		// After timeout
		ThreadUtil.sleep(8000);
		assertThat(cache1.get("hello"), nullValue());
		assertThat(cache2.get("hello"), nullValue());
	}

	@Test
	public void testDynamicCacheUpdate() throws InterruptedException {
		// Given
		System.setProperty("net.sf.ehcache.skipUpdateCheck", "true");
		String value = "127.0.0.1";
		Cache cache1 = dynamicCacheManager1.getCache("agent_request");
		Cache cache2 = dynamicCacheManager2.getCache("agent_request");
		cache2.put("hello", value);
		cache2.put("hello2", value);

		ThreadUtil.sleep(1000);
		// When
		assertThat((String) cache1.get("hello").get(), is(value));
		assertThat((String) cache1.get("hello2").get(), is(value));

		// Update Cache after 4 sec
		ThreadUtil.sleep(4000);
		assertThat(cache1.get("hello"), not(nullValue()));
		cache1.put("hello", value);

		// Then
		ThreadUtil.sleep(8000);
		assertThat(cache1.get("hello"), not(nullValue()));
		assertThat(cache2.get("hello"), not(nullValue()));
	}

	@After
	public void after() {
		dynamicCacheManagerConfig1.mgr.shutdown();
		dynamicCacheManagerConfig2.mgr.shutdown();
	}
}
