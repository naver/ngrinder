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

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Random;

import net.grinder.util.NetworkUtil;

import org.apache.commons.lang.time.DateUtils;
import org.ngrinder.analytics.GoogleAnalytic;
import org.ngrinder.common.constant.NGrinderConstants;
import org.ngrinder.common.util.ThreadUtils;
import org.ngrinder.http.MeasureProtocolRequest;
import org.ngrinder.infra.config.Config;
import org.ngrinder.model.PerfTest;
import org.ngrinder.perftest.service.PerfTestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Send the ngrinder usage data to GA. It executes only once each day at midnight
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
	 * Send the number of executed test.
	 * 
	 */
	@Scheduled(cron = "0 1 1 * * ?")
	@Transactional
	public void reportUsage() {
		if (config.isUsageReportEnabled()) {
			doRandomDelay();
			GoogleAnalytic googleAnalytic = new GoogleAnalytic(NGrinderConstants.GOOGLEANALYTICS_APPNAME,
							config.getVersion(), NGrinderConstants.GOOGLEANALYTICS_TRACKINGID);
			MeasureProtocolRequest measureProtocolRequest = googleAnalytic.getMeasureProtocolRequest();
			measureProtocolRequest.setEventCategory("usage");
			measureProtocolRequest.setEventAction("executions");
			String currentAddress = NetworkUtil.getLocalHostAddress();
			Date yesterday = DateUtils.addDays(new Date(), -1);
			Date start = DateUtils.truncate(yesterday, Calendar.DATE);
			Date end = DateUtils.addMilliseconds(DateUtils.ceiling(yesterday, Calendar.DATE), -1);
			googleAnalytic.sendStaticDataToUA(currentAddress, String.valueOf(getUsage(start, end)));
		}
	}

	/**
	 * delay with random manner. Not to report from multiple machines at a same time.
	 */
	protected void doRandomDelay() {
		ThreadUtils.sleep(new Random().nextInt(100) * 1000);
	}

	protected int getUsage(Date start, Date end) {
		List<PerfTest> list = config.isCluster() ? //
		perfTestService.getPerfTest(start, end, config.getRegion())
						: perfTestService.getPerfTest(start, end);
		return list.size();
	}

	/**
	 * For unit test.
	 * 
	 * @param config
	 *            config
	 */
	void setConfig(Config config) {
		this.config = config;
	}
}
