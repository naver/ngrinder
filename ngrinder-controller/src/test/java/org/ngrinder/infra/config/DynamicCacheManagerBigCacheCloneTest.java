package org.ngrinder.infra.config;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import net.grinder.util.NetworkUtil;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Ehcache;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.ngrinder.common.util.ThreadUtil;
import org.springframework.cache.Cache;
import org.springframework.cache.ehcache.EhCacheCacheManager;

public class DynamicCacheManagerBigCacheCloneTest {
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

	Config config;
	String currentIp;

	@Before
	public void before() {

	}

	@Test
	public void testDynamicCache() throws InterruptedException {
		dynamicCacheManagerConfig1 = new ReloadableDynamicCacheManager(40001, "cache1");
		config = mock(Config.class);
		dynamicCacheManagerConfig1.config = config;
		currentIp = NetworkUtil.getLocalHostAddress();
		when(config.getClusterURIs()).thenReturn(new String[] { currentIp + ":40001", currentIp + ":40002" });
		when(config.getCurrentIP()).thenReturn(currentIp);
		when(config.isCluster()).thenReturn(true);
		dynamicCacheManager1 = dynamicCacheManagerConfig1.dynamicCacheManager();
		dynamicCacheManager1.afterPropertiesSet();
		Cache cache = dynamicCacheManager1.getCache("region_list");
		for (int i = 0; i < 1000; i++) {
			cache.put("Hello" + i, i);
		}
		ThreadUtil.sleep(3000);
		dynamicCacheManagerConfig2 = new ReloadableDynamicCacheManager(40002, "cache2");
		when(config.getClusterURIs()).thenReturn(new String[] { currentIp + ":40001", currentIp + ":40002" });
		dynamicCacheManagerConfig2.config = config;
		dynamicCacheManager2 = dynamicCacheManagerConfig2.dynamicCacheManager();
		dynamicCacheManager2.afterPropertiesSet();
		ThreadUtil.sleep(3000);
		assertThat(((Ehcache) (dynamicCacheManager2.getCache("region_list").getNativeCache())).getKeys().size(),
						is(1000));
	}

	@After
	public void after() {
		dynamicCacheManagerConfig1.mgr.shutdown();
		dynamicCacheManagerConfig2.mgr.shutdown();
	}
}
