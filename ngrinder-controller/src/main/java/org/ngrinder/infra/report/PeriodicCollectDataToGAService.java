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
package org.ngrinder.infra.report;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang.time.DateUtils;
import org.ngrinder.analytics.GoogleAnalytic;
import org.ngrinder.infra.config.Config;
import org.ngrinder.model.PerfTest;
import org.ngrinder.perftest.service.PerfTestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Used to collect the number of executed test .<br/>
 * It executes only once each day at midnight
 *
 * @author maoyb
 * @since3.2
 */
@Service
public class PeriodicCollectDataToGAService {

	@Autowired
	private Config config;

	@Autowired
	private PerfTestService perfTestService;

	/**
	 * Collect the number of executed test
	 */
	@Scheduled(cron = "* 15 * * * ?")
	@Transactional
	public void collectExetedTest() throws UnknownHostException {
		GoogleAnalytic googleAnalytic = new GoogleAnalytic("AppName", config.getVesion(), "UA-40264348-1");

		if (config.enabledCollectDataToGA()) {
			String localhost = InetAddress.getLocalHost().getHostAddress();
			Date yesterday = DateUtils.addDays(new Date(), -1);
			Date start = DateUtils.truncate(yesterday, Calendar.DATE);
			Date end = DateUtils.addMilliseconds(DateUtils.ceiling(yesterday, Calendar.DATE), -1);
			List<PerfTest> list = perfTestService.getPerfTestList(start, end);
			googleAnalytic.sendStaticDataToUA(localhost, String.valueOf(list.size()));
		}
	}
}
