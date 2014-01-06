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
package org.ngrinder.perftest.service.monitor;

import org.apache.commons.io.FileUtils;
import org.hamcrest.Matchers;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.ngrinder.monitor.share.domain.SystemInfo;
import org.ngrinder.perftest.service.AbstractAgentReadyTest;

import java.io.File;
import java.io.IOException;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertThat;


/**
 * Monitor Client Service Test
 */
public class MonitorClientServiceTest extends AbstractAgentReadyTest {
	private File tempReport;

	@Before
	public void before() {
		tempReport = new File(System.getProperty("java.io.tmpdir"), "tmp-report");
		FileUtils.deleteQuietly(tempReport);
		tempReport.mkdir();
	}


	@After
	public void after() {
		tempReport.deleteOnExit();
	}

	@Test
	public void testMonitorClient() throws IOException {
		MonitorClientService client = new MonitorClientService("127.0.0.1", 13243);
		client.init();
		final SystemInfo monitorData = client.getSystemInfo();
		assertThat(monitorData.isParsed(), is(false));
		sleep(3000);
		client.update();
		SystemInfo monitorData2 = client.getSystemInfo();
		assertThat(monitorData2.isParsed(), is(true));
		assertThat(monitorData2, Matchers.notNullValue());
		assertThat(monitorData, not(monitorData2));

	}


}
