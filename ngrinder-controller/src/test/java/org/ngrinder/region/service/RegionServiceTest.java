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
package org.ngrinder.region.service;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.Collection;

import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.Test;
import org.ngrinder.AbstractNGrinderTransactionalTest;
import org.ngrinder.infra.config.Config;
import org.ngrinder.infra.config.DynamicCacheConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.ehcache.EhCacheCacheManager;
import org.springframework.test.util.ReflectionTestUtils;

/**
 * Region Service Test class.
 * 
 * @author mavlarn
 * @since 3.1
 */
public class RegionServiceTest extends AbstractNGrinderTransactionalTest {

	@Autowired
	private Config config;

	@Autowired
	private RegionService regionService;

	private EhCacheCacheManager cacheManager;
	
	@Before
	public void before() {
	}

	@Test
	public void testGetRegionsInCluster() throws IOException {
		Config spiedConfig = spy(config);
		when(spiedConfig.isCluster()).thenReturn(true);
		when(spiedConfig.getRegion()).thenReturn("TEST_REGION");

		RegionService spiedRegionService = spy(regionService);
		spiedRegionService.setConfig(spiedConfig);
		
		DynamicCacheConfig cacheConfig = new DynamicCacheConfig();
		ReflectionTestUtils.setField(cacheConfig, "config", spiedConfig);
		cacheManager = cacheConfig.dynamicCacheManager();
		cacheManager.afterPropertiesSet();
		ReflectionTestUtils.setField(spiedRegionService, "cacheManager", cacheManager);
		
		net.sf.ehcache.CacheManager ehCacheManager = (net.sf.ehcache.CacheManager)ReflectionTestUtils.getField(cacheManager, "cacheManager");
		ehCacheManager.addCache("regions");
		
		spiedRegionService.initRegion();
		spiedRegionService.checkRegionUpdate();
		Collection<String> regions = spiedRegionService.getRegions().keySet();
		LOG.debug("list:{}", regions);
		assertThat(regions.contains("TEST_REGION"), is(true));
		
	}

}
