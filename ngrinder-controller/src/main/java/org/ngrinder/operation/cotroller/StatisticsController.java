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

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.commons.dbcp.BasicDataSource;
import org.ngrinder.common.constant.ControllerConstants;
import org.ngrinder.common.controller.BaseController;
import org.ngrinder.infra.config.Config;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.http.HttpEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import net.sf.ehcache.Statistics;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * Statistics API Controller
 * 
 * @author Gisoo Gwon
 * @since 3.3.1
 */
@Controller
@RequestMapping("/stat")
public class StatisticsController extends BaseController implements ControllerConstants {

	// disable escaping ('<', '>', '=' ...)
	private static Gson gson = new GsonBuilder().disableHtmlEscaping().create();

	@Autowired
	private Config config;

	@Autowired
	private CacheManager cacheManager;

	@Autowired
	BasicDataSource dataSource;

	/**
	 * Get collect current statistics.
	 * 
	 * @return json string, for jvm/ehcache/dbcp statistics
	 */
	@ResponseBody
	@RequestMapping(value = {"", "/"}, method = RequestMethod.GET)
	public HttpEntity<String> getStatistics() {
		Map<String, Object> result = new HashMap<String, Object>();

		if (!config.isEnableStatistics()) {
			result.put("success", false);
			result.put("data", "Disable statistics, You can set controller.enable_statistics=true");
			return toJsonHttpEntity(result, gson);
		}

		Map<String, Object> data = new HashMap<String, Object>();
		data.put("jvm", getJVMStat());
		data.put("ehcache", getEhcacheStat());
		data.put("dbcp", getDbcpStat());

		result.put("success", true);
		result.put("data", data);
		return toJsonHttpEntity(result, gson);
	}
	
	/**
	 * Get current jvm stat
	 * 
	 * @return map for jvm used/free/total/max
	 */
	private Map<String, Long> getJVMStat() {
		Runtime runtime = Runtime.getRuntime();
		
		Map<String, Long> stat = new HashMap<String, Long>();
		stat.put("used", runtime.totalMemory() - runtime.freeMemory());
		stat.put("free", runtime.freeMemory());
		stat.put("total", runtime.totalMemory());
		stat.put("max", runtime.maxMemory());
		
		return stat;
	}
	
	/**
	 * Get all cache statistics.<br>
	 * size is object count in memory & disk<br>
	 * heap is object count in memory<br>
	 * hit & miss is cache hit & miss count
	 * 
	 * @return Ehcache statistics list, group by cacheName
	 */
	private List<Map<String, Object>> getEhcacheStat() {
		List<Map<String, Object>> stats = new LinkedList<Map<String, Object>>();
		for (String cacheName : cacheManager.getCacheNames()) {
			Cache cache = cacheManager.getCache(cacheName);
			net.sf.ehcache.Cache ehcache = (net.sf.ehcache.Cache) cache.getNativeCache();
			Statistics statistics = ehcache.getStatistics();
			
			Map<String, Object> stat = new HashMap<String, Object>();
			stat.put("cacheName", cacheName);
			stat.put("size", statistics.getObjectCount());
			stat.put("heap", statistics.getMemoryStoreObjectCount());
			stat.put("hit", statistics.getCacheHits());
			stat.put("miss", statistics.getCacheMisses());

			stats.add(stat);
		}
		return stats;
	}

	/**
	 * Get current DBCP stat(connection maxidle/idle/active)
	 * 
	 * @return 
	 */
	private Map<String, Integer> getDbcpStat() {
		Map<String, Integer> stat = new HashMap<String, Integer>();
		stat.put("maxIdle", dataSource.getMaxIdle());
		stat.put("idle", dataSource.getNumIdle());
		stat.put("active", dataSource.getNumActive());
		
		return stat;
	}

}
