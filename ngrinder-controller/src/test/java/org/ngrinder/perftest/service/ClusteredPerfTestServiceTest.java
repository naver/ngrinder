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
package org.ngrinder.perftest.service;

import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.ngrinder.AbstractNGrinderTransactionalTest;
import org.ngrinder.infra.config.Config;
import org.ngrinder.model.PerfTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.util.ReflectionTestUtils;

/**
 * {@link ClusteredPerfTestService} test.
 * 
 */

public class ClusteredPerfTestServiceTest extends AbstractNGrinderTransactionalTest {
	@Autowired
	private Config config;

	PerfTestServiceConfig perfTestConfig;

	@Before
	public void before() {
		perfTestConfig = new PerfTestServiceConfig();
		perfTestConfig.setApplicationContext(applicationContext);
	}

	@Test
	public void testGetPerfTestCandidate() {
		Config spiedConfig = spy(config);
		when(spiedConfig.isClustered()).thenReturn(true);
		ReflectionTestUtils.setField(perfTestConfig, "config", spiedConfig);

		PerfTestService clusteredPerfTest = perfTestConfig.perfTestService();

		PerfTest test = clusteredPerfTest.getNextRunnablePerfTestPerfTestCandidate();

		assertThat(test, nullValue());
	}
}
