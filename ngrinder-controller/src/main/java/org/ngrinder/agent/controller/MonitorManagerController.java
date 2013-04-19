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
package org.ngrinder.agent.controller;

import java.util.Map;
import static org.ngrinder.common.util.Preconditions.checkNotNull;

import org.apache.commons.lang.StringUtils;
import org.ngrinder.common.controller.NGrinderBaseController;
import org.ngrinder.monitor.controller.model.SystemDataModel;
import org.ngrinder.perftest.service.monitor.MonitorInfoStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.google.common.collect.Maps;

/**
 * Controller which get target host system information.
 * 
 * @since 3.2
 */
@Controller
@RequestMapping("/monitor")
public class MonitorManagerController extends NGrinderBaseController {

	@Autowired
	private MonitorInfoStore monitorInfoStore;

	/**
	 * Get a monitor  info for the given ip.
	 * 
	 * @param model
	 *            model
	 * @param ip
	 *            target host IP
	 * @return agent/systeminfo
	 */
	@RequestMapping("/info")
	public String getMonitor(ModelMap model, @RequestParam String ip) {
		String[] addresses = StringUtils.split(ip, ":");
		if (addresses.length > 0) {
			ip = addresses[addresses.length - 1];
		}
		model.put("monitorIp", ip);
		return "agent/systeminfo";
	}

	/**
	 * Get the current system performance info by the given ip.
	 * 
	 * @param model
	 *            model
	 * @param ip
	 *            target host IP
	 * @return json message
	 */
	@RequestMapping("/status")
	@ResponseBody
	public String getRealTimeMonitorData(ModelMap model, @RequestParam String ip) {
		Map<String, Object> systemInfoMap = Maps.newHashMap();
		systemInfoMap.put(JSON_SUCCESS, true);
		systemInfoMap.put(
				"systemData",
				new SystemDataModel(checkNotNull(monitorInfoStore.getSystemInfo(ip, getConfig().getMonitorPort()),
						"Get systemInfo error from [%s]", ip), "UNKNOWN"));
		String jsonStr = toJson(systemInfoMap);

		return jsonStr;
	}
	
	/**
	 * Used to  close monitor JXM connect and remove from monitorInfoStore
	 * 
	 * @param model
	 *            model
	 * @param ip
	 *            target host IP
	 */
	@RequestMapping("/close")
	@ResponseBody
	public String closeMonitorConnection(ModelMap model, @RequestParam String ip) {
		monitorInfoStore.remove(ip);
		return returnSuccess();
	}
	
}
