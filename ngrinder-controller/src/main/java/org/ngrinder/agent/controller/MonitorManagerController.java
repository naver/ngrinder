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

import org.apache.commons.lang.StringUtils;
import org.ngrinder.common.controller.BaseController;
import org.ngrinder.common.controller.RestAPI;
import org.ngrinder.monitor.controller.model.SystemDataModel;
import org.ngrinder.monitor.share.domain.SystemInfo;
import org.ngrinder.perftest.service.monitor.MonitorInfoStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeoutException;

import static java.util.concurrent.Executors.newCachedThreadPool;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.ngrinder.common.util.Preconditions.checkNotNull;

/**
 * Controller which gets the target host system information.
 *
 * @since 3.2
 */
@Controller
@RequestMapping("/monitor")
public class MonitorManagerController extends BaseController {

	@Autowired
	private MonitorInfoStore monitorInfoStore;

	/**
	 * Get the target's monitor info page for the given IP.
	 *
	 * @param model model
	 * @param ip    target host IP
	 * @return monitor/info
	 */
	@RequestMapping("/info")
	public String getMonitor(ModelMap model, @RequestParam String ip) {
		String[] addresses = StringUtils.split(ip, ":");
		if (addresses.length > 0) {
			ip = addresses[addresses.length - 1];
		}
		model.put("targetIP", ip);
		return "monitor/info";
	}

	/**
	 * Get the target's monitored data by the given IP.
	 *
	 * @param ip target host IP
	 * @return json message containing the target's monitoring data.
	 */
	@RestAPI
	@ResponseBody
	@RequestMapping("/state")
	public SystemDataModel getRealTimeMonitorData(@RequestParam final String ip) throws InterruptedException, ExecutionException, TimeoutException {
		Future<SystemInfo> submit = newCachedThreadPool().submit(() -> monitorInfoStore.getSystemInfo(ip, getConfig().getMonitorPort()));
		SystemInfo systemInfo = checkNotNull(submit.get(2, SECONDS), "Monitoring data is not available.");
		return new SystemDataModel(systemInfo, "UNKNOWN");
	}

	/**
	 * Close the monitor JXM connection to the given target.
	 *
	 * @param ip target host IP
	 * @return success if succeeded.
	 */
	@ResponseBody
	@RequestMapping("/close")
	public String closeMonitorConnection(@RequestParam String ip) {
		monitorInfoStore.close(ip);
		return returnSuccess();
	}
}
