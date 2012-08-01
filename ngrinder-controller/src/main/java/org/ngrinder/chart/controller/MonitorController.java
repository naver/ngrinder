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

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.math.NumberUtils;
import org.ngrinder.chart.services.MonitorService;
import org.ngrinder.common.controller.NGrinderBaseController;
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

	private static final String DATE_FORMAT = "yyyyMMddHHmmss";

	private static final String DATE_FORMAT_PAGE = "MM/dd/yy HH:mm:ss";

	@Autowired
	private MonitorService monitorService;

	@RequestMapping("/chart")
	public @ResponseBody
	String getChartData(ModelMap model, @RequestParam(required = false) String[] chartTypes) {

		return null;
	}

	@RequestMapping("/getMonitorData")
	public @ResponseBody
	String getMonitorData(ModelMap model, @RequestParam String ip, @RequestParam Date startTime,
			@RequestParam Date finishTime, @RequestParam int imgWidth) {

		DateFormat df = new SimpleDateFormat(DATE_FORMAT);
		long st = NumberUtils.toLong(df.format(startTime));
		long et = NumberUtils.toLong(df.format(finishTime));

		List<JavaDataModel> javaMonitorData = monitorService.getJavaMonitorData(ip, st, et);
		List<SystemDataModel> systemMonitorData = monitorService.getSystemMonitorData(ip, st, et);

		int pointCount = imgWidth / 10;
		int lineObject, current, interval;

		List<List<Object>> cpuData = new ArrayList<List<Object>>();
		List<List<Object>> memoryData = new ArrayList<List<Object>>();
		List<List<Object>> heapMemoryData = new ArrayList<List<Object>>();
		List<List<Object>> nonHeapMemoryData = new ArrayList<List<Object>>();
		List<List<Object>> threadCountData = new ArrayList<List<Object>>();
		List<List<Object>> jvmCpuData = new ArrayList<List<Object>>();

		DateFormat dfPage = new SimpleDateFormat(DATE_FORMAT_PAGE);
		if (null != javaMonitorData && !javaMonitorData.isEmpty()) {
			current = 0;
			lineObject = javaMonitorData.size();
			interval = lineObject / pointCount;
			// TODO should get average data
			for (JavaDataModel jdm : javaMonitorData) {
				if (0 == current) {
					Date collectTime = null;
					try {
						collectTime = df.parse(String.valueOf(jdm.getCollectTime()));
					} catch (ParseException e) {
						LOG.error("eror date: " + jdm.getCollectTime(), e);
						continue;
					}
					String ct = dfPage.format(collectTime);
					List<Object> heapMemory = new ArrayList<Object>();
					heapMemory.add(ct);
					heapMemory.add(jdm.getHeapUsedMemory());
					heapMemoryData.add(heapMemory);
					List<Object> nonHeapMemor = new ArrayList<Object>();
					nonHeapMemor.add(ct);
					nonHeapMemor.add(jdm.getNonHeapUsedMemory());
					nonHeapMemoryData.add(nonHeapMemor);
					List<Object> threadCount = new ArrayList<Object>();
					threadCount.add(ct);
					threadCount.add(jdm.getThreadCount());
					threadCountData.add(threadCount);
					List<Object> jvmCpu = new ArrayList<Object>();
					jvmCpu.add(ct);
					jvmCpu.add(jdm.getCpuUsedPercentage());
					jvmCpuData.add(jvmCpu);
				}
				if (++current >= interval) {
					current = 0;
				}
			}
		}
		if (null != systemMonitorData && !systemMonitorData.isEmpty()) {
			current = 0;
			lineObject = systemMonitorData.size();
			interval = lineObject / pointCount;
			// TODO should get average data
			for (SystemDataModel sdm : systemMonitorData) {
				if (0 == current) {
					Date collectTime = null;
					try {
						collectTime = df.parse(String.valueOf(sdm.getCollectTime()));
					} catch (ParseException e) {
						LOG.error("eror date: " + sdm.getCollectTime(), e);
						continue;
					}
					String ct = dfPage.format(collectTime);
					List<Object> cpu = new ArrayList<Object>();
					cpu.add(ct);
					cpu.add(sdm.getCpuUsedPercentage());
					cpuData.add(cpu);
					List<Object> memory = new ArrayList<Object>();
					memory.add(ct);
					memory.add(sdm.getTotalMemory() - sdm.getFreeMemory());
					memoryData.add(memory);
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
		rtnMap.put("heap_memory", heapMemoryData);
		rtnMap.put("non_heap_memory", nonHeapMemoryData);
		rtnMap.put("thread_count", threadCountData);
		rtnMap.put("jvm_cpu", jvmCpuData);
		return JSONUtil.toJson(rtnMap);
	}

}
