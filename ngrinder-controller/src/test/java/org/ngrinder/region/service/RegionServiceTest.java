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

import org.junit.Test;
import org.ngrinder.AbstractNGrinderTransactionalTest;
import org.ngrinder.infra.config.Config;
import org.springframework.beans.factory.annotation.Autowired;

import static org.fest.assertions.Assertions.assertThat;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;
import static org.ngrinder.common.constant.CacheConstants.REGION_ATTR_KEY;
import static org.springframework.test.util.ReflectionTestUtils.setField;

public class RegionServiceTest extends AbstractNGrinderTransactionalTest {

	@Autowired
	private Config config;

	@Autowired
	private RegionService regionService;

	@Test
	public void testGetAllRegion() {
		Config spiedConfig = spy(config);
		when(spiedConfig.isClustered()).thenReturn(true);

		setField(regionService, "config", spiedConfig);

		String currentRegion = config.getRegion();

		assertThat(regionService.getAll().keySet().contains(currentRegion)).isEqualTo(true);
		assertThat(regionService.getAllVisibleRegionNames()
			.stream()
			.anyMatch(regionNamesMap -> regionNamesMap.get(REGION_ATTR_KEY).equals(currentRegion)))
			.isEqualTo(true);
	}
}
