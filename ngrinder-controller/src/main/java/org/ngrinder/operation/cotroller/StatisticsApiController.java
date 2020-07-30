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
package org.ngrinder.operation.cotroller;

import org.apache.commons.dbcp.BasicDataSource;
import org.ngrinder.infra.config.Config;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

import lombok.RequiredArgsConstructor;

/**
 * Statistics API Controller
 *
 * @since 3.3.1
 */
@RestController
@RequestMapping("/stat")
@RequiredArgsConstructor
public class StatisticsApiController {

	private final Config config;

	private final BasicDataSource dataSource;

	/**
	 * Get collect current statistics.
	 *
	 * @return json string, for jvm/dbcp statistics
	 */
	@GetMapping({"", "/"})
	public Map<String, Object> getStatistics() {
		Map<String, Object> result = new HashMap<>();

		if (!config.isEnableStatistics()) {
			result.put("success", false);
			result.put("data", "Disable statistics, You can set controller.enable_statistics=true");
			return result;
		}

		Map<String, Object> data = new HashMap<>();
		data.put("jvm", getJVMStat());
		data.put("dbcp", getDbcpStat());

		result.put("success", true);
		result.put("data", data);
		return result;
	}

	/**
	 * Get current jvm stat
	 *
	 * @return map for jvm used/free/total/max
	 */
	private Map<String, Long> getJVMStat() {
		Runtime runtime = Runtime.getRuntime();

		Map<String, Long> stat = new HashMap<>();
		stat.put("used", runtime.totalMemory() - runtime.freeMemory());
		stat.put("free", runtime.freeMemory());
		stat.put("total", runtime.totalMemory());
		stat.put("max", runtime.maxMemory());

		return stat;
	}

	/**
	 * Get current DBCP stat(connection maxidle/idle/active)
	 *
	 * @return map for dbcp connection maxidle/idle/active
	 */
	private Map<String, Integer> getDbcpStat() {
		Map<String, Integer> stat = new HashMap<>();
		stat.put("maxIdle", dataSource.getMaxIdle());
		stat.put("idle", dataSource.getNumIdle());
		stat.put("active", dataSource.getNumActive());

		return stat;
	}

}
