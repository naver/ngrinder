/*
 * Copyright (C) 2012 - 2012 NHN Corporation
 * All rights reserved.
 *
 * This file is part of The nGrinder software distribution. Refer to
 * the file LICENSE which is part of The nGrinder distribution for
 * licensing details. The nGrinder distribution is available on the
 * Internet at http://nhnopensource.org/ngrinder
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS
 * FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE
 * COPYRIGHT HOLDERS OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.ngrinder.chart.service;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

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
		try {
			BufferedReader br = new BufferedReader(new FileReader(monitorDataFile));
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
		}
		LOG.debug("Finish getSystemMonitorData of test:{} ip:{}", testId, monitorIP);
		return rtnList;
	}

}
