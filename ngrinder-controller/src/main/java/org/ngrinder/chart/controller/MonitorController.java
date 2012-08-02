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

	@SuppressWarnings("unused")
	private static final Logger LOG = LoggerFactory.getLogger(MonitorController.class);

	private static final String DATE_FORMAT = "yyyyMMddHHmmss";
	private static final DateFormat df = new SimpleDateFormat(DATE_FORMAT);

	@Autowired
	private MonitorService monitorService;

	/**
	 * get chart data(like tps, vuser) of test
	 * 
	 * @param model
	 * @param chartTypes
	 *            is some chart type combined wit ',', eg. "tps,errors,vusers".
	 * @return
	 */
	@RequestMapping("/chart")
	public @ResponseBody
	String getChartData(ModelMap model, @RequestParam(required = false) String[] chartTypes) {

		return null;
	}

	/**
	 * get monitor data of agents
	 * 
	 * @param model
	 * @param ip
	 * @param startTime
	 * @param finishTime
	 * @param imgWidth
	 * @return
	 */
	@RequestMapping("/getMonitorData")
	public @ResponseBody
	String getMonitorData(ModelMap model, @RequestParam String ip, @RequestParam(required = false) Date startTime,
			@RequestParam(required = false) Date finishTime, @RequestParam int imgWidth) {

		if (null == finishTime) {
			finishTime = new Date();
		}
		if (null == startTime) {
			startTime = new Date(finishTime.getTime() - 30 * 1000);
		}
		DateFormat df = new SimpleDateFormat(DATE_FORMAT);
		long st = NumberUtils.toLong(df.format(startTime));
		long et = NumberUtils.toLong(df.format(finishTime));

		Map<String, Object> rtnMap = new HashMap<String, Object>(7);
		this.getMonitorDataJava(rtnMap, ip, st, et, imgWidth);
		this.getMonitorDataSystem(rtnMap, ip, st, et, imgWidth);
		rtnMap.put(JSON_SUCCESS, true);
		rtnMap.put("startTime", startTime);
		return JSONUtil.toJson(rtnMap);
	}

	/**
	 * get java monitor data of agents
	 * 
	 * @param model
	 * @param ip
	 * @param startTime
	 * @param finishTime
	 * @param imgWidth
	 * @return
	 */
	@RequestMapping("/getMonitorDataJava")
	public @ResponseBody
	String getMonitorDataJava(ModelMap model, @RequestParam String ip, @RequestParam(required = false) Date startTime,
			@RequestParam(required = false) Date finishTime, @RequestParam int imgWidth) {

		if (null == finishTime) {
			finishTime = new Date();
		}
		if (null == startTime) {
			startTime = new Date(finishTime.getTime() - 30 * 1000);
		}
		DateFormat df = new SimpleDateFormat(DATE_FORMAT);
		long st = NumberUtils.toLong(df.format(startTime));
		long et = NumberUtils.toLong(df.format(finishTime));

		Map<String, Object> rtnMap = new HashMap<String, Object>(5);
		this.getMonitorDataJava(rtnMap, ip, st, et, imgWidth);
		rtnMap.put(JSON_SUCCESS, true);
		return JSONUtil.toJson(rtnMap);
	}

	/**
	 * get system monitor data of agents
	 * 
	 * @param model
	 * @param ip
	 * @param startTime
	 * @param finishTime
	 * @param imgWidth
	 * @return
	 */
	@RequestMapping("/getMonitorDataSystem")
	public @ResponseBody
	String getMonitorDataSystem(ModelMap model, @RequestParam String ip,
			@RequestParam(required = false) Date startTime, @RequestParam(required = false) Date finishTime,
			@RequestParam int imgWidth) {

		if (null == finishTime) {
			finishTime = new Date();
		}
		if (null == startTime) {
			startTime = new Date(finishTime.getTime() - 30 * 1000);
		}
		long st = NumberUtils.toLong(df.format(startTime));
		long et = NumberUtils.toLong(df.format(finishTime));

		Map<String, Object> rtnMap = new HashMap<String, Object>(3);
		this.getMonitorDataSystem(rtnMap, ip, st, et, imgWidth);
		rtnMap.put(JSON_SUCCESS, true);
		return JSONUtil.toJson(rtnMap);
	}

	private void getMonitorDataJava(Map<String, Object> rtnMap, String ip, long startTime, long finishTime, int imgWidth) {

		List<JavaDataModel> javaMonitorData = monitorService.getJavaMonitorData(ip, startTime, finishTime);

		int pointCount = imgWidth / 10;
		int lineObject, current, interval = 0;

		List<Object> heapMemoryData = new ArrayList<Object>(pointCount);
		List<Object> nonHeapMemoryData = new ArrayList<Object>(pointCount);
		List<Object> threadCountData = new ArrayList<Object>(pointCount);
		List<Object> jvmCpuData = new ArrayList<Object>(pointCount);

		if (null != javaMonitorData && !javaMonitorData.isEmpty()) {
			current = 0;
			lineObject = javaMonitorData.size();
			interval = lineObject / pointCount;
			// TODO should get average data
			for (JavaDataModel jdm : javaMonitorData) {
				if (0 == current) {
					heapMemoryData.add(jdm.getHeapUsedMemory());
					nonHeapMemoryData.add(jdm.getNonHeapUsedMemory());
					threadCountData.add(jdm.getThreadCount());
					jvmCpuData.add(jdm.getCpuUsedPercentage() * 100);
				}
				if (++current >= interval) {
					current = 0;
				}
			}
		}

		rtnMap.put("heap_memory", heapMemoryData);
		rtnMap.put("non_heap_memory", nonHeapMemoryData);
		rtnMap.put("thread_count", threadCountData);
		rtnMap.put("jvm_cpu", jvmCpuData);

		rtnMap.put("interval", interval);
	}

	private void getMonitorDataSystem(Map<String, Object> rtnMap, String ip, long startTime, long finishTime,
			int imgWidth) {

		List<SystemDataModel> systemMonitorData = monitorService.getSystemMonitorData(ip, startTime, finishTime);

		int pointCount = imgWidth / 10;
		int lineObject, current, interval = 0;

		List<Object> cpuData = new ArrayList<Object>(pointCount);
		List<Object> memoryData = new ArrayList<Object>(pointCount);

		if (null != systemMonitorData && !systemMonitorData.isEmpty()) {
			current = 0;
			lineObject = systemMonitorData.size();
			interval = lineObject / pointCount;
			// TODO should get average data
			for (SystemDataModel sdm : systemMonitorData) {
				if (0 == current) {
					cpuData.add(sdm.getCpuUsedPercentage() * 100);
					memoryData.add(sdm.getTotalMemory() - sdm.getFreeMemory());
				}
				if (++current >= interval) {
					current = 0;
				}
			}
		}

		rtnMap.put("cpu", cpuData);
		rtnMap.put("memory", memoryData);

		rtnMap.put("interval", interval);
	}

}
