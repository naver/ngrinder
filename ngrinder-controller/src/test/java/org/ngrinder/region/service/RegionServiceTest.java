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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.test.util.ReflectionTestUtils;

/**
 * Region Service Test class.
 * 
 * @author mavlarn
 * @Since 3.1
 */
public class RegionServiceTest extends AbstractNGrinderTransactionalTest {

	@Autowired
	private Config config;

	@Autowired
	private RegionService regionService;

	@Autowired
	private CacheManager cacheManager;
	
	@Before
	public void before() {
		FileUtils.deleteQuietly(config.getHome().getControllerShareDirectory());
	}

	@Test
	public void testGetRegionsInCluster() throws IOException {
		Config spiedConfig = spy(config);
		when(spiedConfig.isCluster()).thenReturn(true);
		when(spiedConfig.getRegion()).thenReturn("TEST_REGION");

		RegionService spiedRegionService = spy(regionService);
		Cache cache = cacheManager.getCache("default"); //use default cache for test
		ReflectionTestUtils.setField(spiedRegionService, "cache", cache);
		
		spiedRegionService.setConfig(spiedConfig);
		spiedRegionService.checkRegionUpdate();
		Collection<String> regions = regionService.getRegions().keySet();
		LOG.debug("list:{}", regions);
		assertThat(regions.contains("TEST_REGION"), is(true));
		
	}

	@Test
	public void testGetRegions() {
		Config spiedConfig = spy(config);
		when(spiedConfig.isCluster()).thenReturn(false);
		regionService.setConfig(spiedConfig);
		regionService.checkRegionUpdate();
		assertThat(regionService.getRegions().isEmpty(), is(true));
	}
}
