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

import static org.ngrinder.common.util.Preconditions.checkNotZero;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.grinder.common.processidentity.AgentIdentity;

import org.apache.commons.lang.math.NumberUtils;
import org.ngrinder.chart.service.MonitorService;
import org.ngrinder.common.controller.NGrinderBaseController;
import org.ngrinder.common.util.JSONUtil;
import org.ngrinder.monitor.controller.model.SystemDataModel;
import org.ngrinder.perftest.service.AgentManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.google.gson.Gson;

/**
 * Monitor controller.
 * 
 * @author Mavlarn
 * @since 3.0
 */
@Controller
@RequestMapping("/monitor")
public class MonitorController extends NGrinderBaseController {

	private static final DateFormat DATE_FORMATER = new SimpleDateFormat("yyyyMMddHHmmss");

	@Autowired
	private MonitorService monitorService;
	
	@Autowired
	private AgentManager agentManager;
	
	private static final Gson GSON = new Gson();

	@RequestMapping("/getCurrentMonitorData")
	public @ResponseBody String getCurrentMonitorData(ModelMap model, @RequestParam String ip) {
		Map<String, Object> returnMap = new HashMap<String, Object>(3);
		
		AgentIdentity agentId = agentManager.getAgentIdentityByIp(ip);
		if (agentId == null) {
			return JSONUtil.returnError("Agent " + ip + " doesn't exist!");
		}
		SystemDataModel systemData = agentManager.getSystemDataModel(agentId);
		systemData = systemData != null ? systemData : new SystemDataModel();
		returnMap.put(JSON_SUCCESS, true);
		returnMap.put("systemData", systemData);
		return GSON.toJson(returnMap);
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
			startTime = new Date(finishTime.getTime() - 60 * 1000); //default getting one minute's monitor data
		}
		
		long st = NumberUtils.toLong(DATE_FORMATER.format(startTime));
		long et = NumberUtils.toLong(DATE_FORMATER.format(finishTime));
		checkNotZero(st, "Invalid start time!");
		checkNotZero(et, "Invalid end time!");

		Map<String, Object> rtnMap = new HashMap<String, Object>(7);
		rtnMap.put("SystemData", this.getMonitorDataSystem(ip, st, et, imgWidth));
		rtnMap.put("startTime", startTime);
		rtnMap.put(JSON_SUCCESS, true);
		
		return JSONUtil.toJson(rtnMap);
	}

	private Map<String, Object> getMonitorDataSystem(String ip, long startTime, long finishTime,
			int imgWidth) {
		Map<String, Object> rtnMap = new HashMap<String, Object>();
		List<SystemDataModel> systemMonitorData = monitorService.getSystemMonitorData(ip, startTime, finishTime);
		
		if (imgWidth < 100) {
			imgWidth = 100;
		}
		if (null != systemMonitorData && !systemMonitorData.isEmpty()) {
			int dataAmount = systemMonitorData.size();
			int pointCount = imgWidth;
			int interval = dataAmount / pointCount;
			if (interval == 0) {
				pointCount = dataAmount;
				interval = 1;
			}
			List<Object> cpuData = new ArrayList<Object>(pointCount);
			List<Object> memoryData = new ArrayList<Object>(pointCount);
			
			SystemDataModel sdm;
			for (int i = 0; i < dataAmount; i += interval) {
				sdm = systemMonitorData.get(i);
				cpuData.add(sdm.getCpuUsedPercentage());
				memoryData.add(sdm.getTotalMemory() - sdm.getFreeMemory());
			}
			
			rtnMap.put("cpu", cpuData);
			rtnMap.put("memory", memoryData);
			rtnMap.put("interval", interval);
		}

		return rtnMap;
	}
}
