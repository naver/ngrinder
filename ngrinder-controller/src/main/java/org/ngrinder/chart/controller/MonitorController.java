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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.ngrinder.chart.service.MonitorService;
import org.ngrinder.common.controller.NGrinderBaseController;
import org.ngrinder.monitor.controller.model.SystemDataModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * Monitor controller.
 * 
 * @author Mavlarn
 * @since 3.0
 */
@Controller
@RequestMapping("/monitor")
public class MonitorController extends NGrinderBaseController {

	@Autowired
	private MonitorService monitorService;

	@Autowired

	/**
	 * Get monitor data of agents.
	 * 
	 * @param model
	 *            model
	 * @param testId
	 *            test Id
	 * @param monitorIP
	 *            monitorIP
	 * @param imgWidth
	 *            image width
	 * @return json message
	 */
	@RequestMapping("/getMonitorData")
	@ResponseBody
	public String getMonitorData(ModelMap model, @RequestParam(required = true) long testId,
					@RequestParam(required = true) String monitorIP, @RequestParam int imgWidth) {
		Map<String, Object> rtnMap = new HashMap<String, Object>(7);
		rtnMap.put("SystemData", this.getMonitorDataSystem(testId, monitorIP, imgWidth));
		rtnMap.put(JSON_SUCCESS, true);

		return toJson(rtnMap);
	}

	private Map<String, Object> getMonitorDataSystem(long testId, String monitorIP, int imgWidth) {
		Map<String, Object> rtnMap = new HashMap<String, Object>();
		List<SystemDataModel> systemMonitorData = monitorService.getSystemMonitorData(testId, monitorIP);

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
