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
		dynamicCacheManager2.getCache("region_list").put("hello", "127.0.0.1");
		ThreadUtil.sleep(1000);
		Cache cache = dynamicCacheManager1.getCache("region_list");
		assertThat((String) cache.get("hello").get(), is("127.0.0.1"));
		ThreadUtil.sleep(4000);
		assertThat(dynamicCacheManager1.getCache("region_list").get("hello"), not(nullValue()));
		assertThat(dynamicCacheManager2.getCache("region_list").get("hello"), not(nullValue()));
		ThreadUtil.sleep(4000);
		assertThat(dynamicCacheManager1.getCache("region_list").get("hello"), nullValue());
		assertThat(dynamicCacheManager2.getCache("region_list").get("hello"), nullValue());
	}

	@Test
	public void testDynamicCacheUpdate() throws InterruptedException {
		System.setProperty("net.sf.ehcache.skipUpdateCheck", "true");
		String value = "127.0.0.1";
		dynamicCacheManager2.getCache("region_list").put("hello", value);
		ThreadUtil.sleep(1000);
		Cache cache = dynamicCacheManager1.getCache("region_list");
		assertThat((String) cache.get("hello").get(), is(value));
		ThreadUtil.sleep(4000);
		assertThat(dynamicCacheManager1.getCache("region_list").get("hello"), not(nullValue()));
		dynamicCacheManager1.getCache("region_list").put("hello", value);
		assertThat(dynamicCacheManager2.getCache("region_list").get("hello"), not(nullValue()));
		ThreadUtil.sleep(4000);
		assertThat(dynamicCacheManager1.getCache("region_list").get("hello"), not(nullValue()));
		assertThat(dynamicCacheManager2.getCache("region_list").get("hello"), not(nullValue()));
	}

	@After
	public void after() {
		dynamicCacheManagerConfig1.mgr.shutdown();
		dynamicCacheManagerConfig2.mgr.shutdown();
	}
}
