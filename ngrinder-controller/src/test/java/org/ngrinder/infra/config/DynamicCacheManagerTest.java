package org.ngrinder.infra.config;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import net.grinder.util.NetworkUtil;
import net.sf.ehcache.CacheManager;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
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
		dynamicCacheManagerConfig1 = new ReloadableDynamicCacheManager(4001, "cache1");
		Config config = mock(Config.class);
		dynamicCacheManagerConfig1.config = config;
		String currentIp = NetworkUtil.getLocalHostAddress();
		when(config.getClusterURIs()).thenReturn(new String[] { currentIp + ":4002" });
		when(config.getCurrentIP()).thenReturn(currentIp);
		when(config.isCluster()).thenReturn(true);
		dynamicCacheManager1 = dynamicCacheManagerConfig1.dynamicCacheManager();

		dynamicCacheManagerConfig2 = new ReloadableDynamicCacheManager(4002, "cache2");
		when(config.getClusterURIs()).thenReturn(new String[] { currentIp + ":4001" });
		dynamicCacheManagerConfig2.config = config;
		dynamicCacheManager2 = dynamicCacheManagerConfig2.dynamicCacheManager();
	}

	@Test
	public void testDynamicCache() {
		dynamicCacheManager1.getCache("region_list").put("hello", "127.0.0.1");
		assertThat((String) dynamicCacheManager2.getCache("region_list").get("hello").get(), is("127.0.0.1"));
	}

	@After
	public void after() {
		dynamicCacheManagerConfig1.mgr.shutdown();
		dynamicCacheManagerConfig2.mgr.shutdown();
	}
}
