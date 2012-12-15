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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.ngrinder.infra.config.Config;
import org.ngrinder.monitor.controller.model.SystemDataModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Monitor Service Class.
 * 
 * @author Mavlarn
 * @since 3.0
 */
@Service
public class MonitorService {

	private static final Logger LOG = LoggerFactory.getLogger(MonitorService.class);
	
	@Autowired
	private Config config;
	
	/**
	 * Get all{@link SystemDataModel} from monitor data file of one test and target.
	 * @param testId
	 * 				test id
	 * @param monitorIP
	 * 				IP address of the monitor target
	 * @return SystemDataModel list
	 */
	public List<SystemDataModel> getSystemMonitorData(long testId, String monitorIP) {
		LOG.debug("Get SystemMonitorData of test:{} ip:{}", testId, monitorIP);
		List<SystemDataModel> rtnList = new ArrayList<SystemDataModel>();
		
		File monitorDataFile = new File(config.getHome().getPerfTestReportDirectory(String.valueOf(testId)),
				Config.MONITOR_FILE_PREFIX + monitorIP + ".data");
		BufferedReader br = null;
		try {
			br = new BufferedReader(new FileReader(monitorDataFile));
			br.readLine(); //skip the header.
			//header: "ip,system,collectTime,freeMemory,totalMemory,cpuUsedPercentage"
			String line = br.readLine();
			while (StringUtils.isNotBlank(line)) {
				SystemDataModel model = new SystemDataModel();
				String[] datalist = StringUtils.split(line, ",");
				model.setIp(datalist[0]);
				model.setSystem(datalist[1]);
				model.setCollectTime(Long.valueOf(datalist[2]));
				model.setFreeMemory(Long.valueOf(datalist[3]));
				model.setTotalMemory(Long.valueOf(datalist[4]));
				model.setCpuUsedPercentage(Float.valueOf(datalist[5]));
				rtnList.add(model);
				line = br.readLine();
			}
		} catch (FileNotFoundException e) {
			LOG.error("Monitor data file not exist:{}", monitorDataFile);
			LOG.error(e.getMessage(), e);
		} catch (IOException e) {
			LOG.error("Error while getting monitor:{} data file:{}", monitorIP, monitorDataFile);
			LOG.error(e.getMessage(), e);
		} finally {
			IOUtils.closeQuietly(br);
		}
		LOG.debug("Finish getSystemMonitorData of test:{} ip:{}", testId, monitorIP);
		return rtnList;
	}

}
