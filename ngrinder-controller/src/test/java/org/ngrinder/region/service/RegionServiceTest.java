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

import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import org.assertj.core.api.Assertions;
import org.junit.Before;
import org.junit.Test;
import org.ngrinder.AbstractNGrinderTransactionalTest;
import org.ngrinder.infra.config.Config;
import org.ngrinder.infra.config.DynamicCacheConfig;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Collection;

import static net.grinder.util.NetworkUtils.DEFAULT_LOCAL_ADDRESSES;
import static net.grinder.util.NetworkUtils.removeScopedMarkerFromIP;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;
import static org.springframework.test.util.ReflectionTestUtils.setField;

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

	@Test
	public void testGetRegionsInCluster() {
		Config spiedConfig = spy(config);
		when(spiedConfig.isClustered()).thenReturn(true);
		when(spiedConfig.getRegion()).thenReturn("TEST_REGION");

		setField(regionService, "config", spiedConfig);

		Collection<String> regions = regionService.getAll().keySet();
		LOG.debug("list:{}", regions);
		//assertThat(regions.contains("TEST_REGION"), is(true));
	}
}
