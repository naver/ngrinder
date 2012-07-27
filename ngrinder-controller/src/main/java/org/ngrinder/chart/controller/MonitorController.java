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
package org.ngrinder.chart.controller;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.ngrinder.chart.services.MonitorService;
import org.ngrinder.common.controller.NGrinderBaseController;
import org.ngrinder.common.util.DateUtil;
import org.ngrinder.common.util.JSONUtil;
import org.ngrinder.monitor.controller.model.JavaDataModel;
import org.ngrinder.monitor.controller.model.SystemDataModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * Class description.
 * 
 * @author Mavlarn
 * @since
 */
@Controller
@RequestMapping("/monitor")
public class MonitorController extends NGrinderBaseController {

	private static final Logger LOG = LoggerFactory.getLogger(MonitorController.class);

	@Autowired
	private MonitorService monitorService;

	@RequestMapping("/chart")
	public @ResponseBody
	String getChartData(ModelMap model, @RequestParam(required = false) String[] chartTypes) {

		return null;
	}

	@RequestMapping("/getMonitorData")
	public @ResponseBody
	String getMonitorData(ModelMap model, @RequestParam String ip, @RequestParam String startTime,
			@RequestParam String finishTime, @RequestParam int imgWidth) {

		long st, et;
		try {
			st = DateUtil.toSimpleDate(startTime).getTime();
			et = DateUtil.toSimpleDate(finishTime).getTime();
		} catch (ParseException e) {
			st = 0;
			et = 0;
			LOG.error("error date format: " + startTime + "," + finishTime, e);
		}
		List<JavaDataModel> javaMonitorData = monitorService.getJavaMonitorData(ip, st, et);
		List<SystemDataModel> systemMonitorData = monitorService.getSystemMonitorData(ip, st, et);

		int pointCount = imgWidth / 10;
		int lineNumber, current, interval;

		List<String> cpuData = new ArrayList<String>();
		List<String> memoryData = new ArrayList<String>();
		List<String> heapMemorData = new ArrayList<String>();
		List<String> nonHeapMemoryData = new ArrayList<String>();
		List<String> threadCountData = new ArrayList<String>();
		List<String> jvmCpuData = new ArrayList<String>();

		if (null != javaMonitorData && !javaMonitorData.isEmpty()) {
			current = 0;
			lineNumber = javaMonitorData.size();
			interval = lineNumber / pointCount;
			// TODO should get average data
			for (JavaDataModel jdm : javaMonitorData) {
				if (0 == current) {
					heapMemorData.add(String.valueOf(jdm.getHeapUsedMemory()));
					nonHeapMemoryData.add(String.valueOf(jdm.getNonHeapUsedMemory()));
					threadCountData.add(String.valueOf(jdm.getThreadCount()));
					jvmCpuData.add(String.valueOf(jdm.getCpuUsedPercentage()));
				}
				if (++current >= interval) {
					current = 0;
				}
			}
		}
		if (null != systemMonitorData && !systemMonitorData.isEmpty()) {
			current = 0;
			lineNumber = systemMonitorData.size();
			interval = lineNumber / pointCount;
			// TODO should get average data
			for (SystemDataModel sdm : systemMonitorData) {
				if (0 == current) {
					cpuData.add(String.valueOf(sdm.getCpuUsedPercentage()));
					memoryData.add(String.valueOf(sdm.getTotalMemory() - sdm.getFreeMemory()));
				}
				if (++current >= interval) {
					current = 0;
				}
			}
		}

		Map<String, Object> rtnMap = new HashMap<String, Object>(7);
		rtnMap.put(JSON_SUCCESS, true);
		rtnMap.put("cpu", cpuData);
		rtnMap.put("memory", memoryData);
		rtnMap.put("heap_memory", heapMemorData);
		rtnMap.put("non_heap_memory", nonHeapMemoryData);
		rtnMap.put("thread_count", threadCountData);
		rtnMap.put("jvm_cpu", jvmCpuData);
		return JSONUtil.toJson(rtnMap);
	}

}
