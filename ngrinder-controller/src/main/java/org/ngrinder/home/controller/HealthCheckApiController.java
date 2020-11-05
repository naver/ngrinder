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
package org.ngrinder.home.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.ngrinder.common.constant.ControllerConstants;
import org.ngrinder.common.util.ThreadUtils;
import org.ngrinder.infra.config.Config;
import org.ngrinder.region.service.RegionService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Health check api controller.
 *
 * @since 3.5.0
 */
@Slf4j
@RestController
@RequiredArgsConstructor
public class HealthCheckApiController implements ControllerConstants {

	private final RegionService regionService;

	private final Config config;

	/**
	 * Return the health check message. If there is shutdown lock, it returns
	 * 503. Otherwise it returns region lists.
	 *
	 * @param response response
	 * @return region list
	 */
	@GetMapping("/check/healthcheck")
	public Map<String, Object> healthCheck(HttpServletResponse response) {
		if (config.hasShutdownLock()) {
			try {
				response.sendError(503, "nGrinder is about to down");
			} catch (IOException e) {
				log.error("While running healthCheck() in HomeController, the error occurs.");
				log.error("Details : ", e);
			}
		}
		Map<String, Object> map = new HashMap<>();
		map.put("current", regionService.getCurrent());
		map.put("regions", regionService.getAll());
		return map;
	}

	/**
	 * Return health check message with 1 sec delay. If there is shutdown lock,
	 * it returns 503. Otherwise, it returns region lists.
	 *
	 * @param sleep    in milliseconds.
	 * @param response response
	 * @return region list
	 */
	@GetMapping("/check/healthcheck_slow")
	public Map<String, Object> healthCheckSlowly(@RequestParam(value = "delay", defaultValue = "1000") int sleep,
												HttpServletResponse response) {
		ThreadUtils.sleep(sleep);
		return healthCheck(response);
	}
}
