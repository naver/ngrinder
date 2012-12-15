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
package org.ngrinder.monitor.service;

import java.io.BufferedWriter;
import java.io.OutputStreamWriter;

import org.junit.Test;
import org.ngrinder.monitor.AbstractChartTransactionalTest;
import org.ngrinder.monitor.MonitorConstants;
import org.ngrinder.monitor.service.MonitorClientSerivce;
import org.ngrinder.monitor.share.domain.SystemInfo;

/**
 * Class description.
 *
 * @author Mavlarn
 * @since
 */
public class MonitorClientServiceTest extends AbstractChartTransactionalTest {

	@Test
	public void testGetMonitorData () {
		MonitorClientSerivce clientServ = applicationContext.getBean(MonitorClientSerivce.class);
		clientServ.init("127.0.0.1", MonitorConstants.DEFAULT_MONITOR_PORT);
		LOG.debug("sysInfo header:{}", SystemInfo.HEADER);
		
		BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(System.out));
		clientServ.recordMonitorData(bw);

	}
	
}
