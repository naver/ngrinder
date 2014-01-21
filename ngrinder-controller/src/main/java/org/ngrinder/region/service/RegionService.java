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
package org.ngrinder.region.service;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import net.grinder.common.processidentity.AgentIdentity;
import net.grinder.util.NetworkUtils;
import net.sf.ehcache.Ehcache;
import org.apache.commons.lang.StringUtils;
import org.ngrinder.common.constant.ClusterConstants;
import org.ngrinder.common.util.TypeConvertUtils;
import org.ngrinder.infra.config.Config;
import org.ngrinder.infra.schedule.ScheduledTaskService;
import org.ngrinder.perftest.service.AgentManager;
import org.ngrinder.region.model.RegionInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.cache.Cache.ValueWrapper;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;

import static org.ngrinder.common.util.ExceptionUtils.processException;

/**
 * Region service class. This class responsible to keep the status of available regions.
 *
 * @author Mavlarn
 * @author JunHo Yoon
 * @since 3.1
 */
@Service
public class RegionService {

	@SuppressWarnings("UnusedDeclaration")
	private static final Logger LOGGER = LoggerFactory.getLogger(RegionService.class);

	@Autowired
	private Config config;

	@Autowired
	private ScheduledTaskService scheduledTaskService;

	@Autowired
	private CacheManager cacheManager;
	private Cache cache;


	/**
	 * Set current region into cache, using the IP as key and region name as value.
	 */
	@PostConstruct
	public void initRegion() {
		if (config.isClustered()) {
			cache = cacheManager.getCache("regions");
			verifyDuplicatedRegion();
			scheduledTaskService.addFixedDelayedScheduledTask(new Runnable() {
				@Override
				public void run() {
					checkRegionUpdate();
				}
			}, 3000);
		}
	}

	/**
	 * Verify duplicate region when starting with cluster mode.
	 *
	 * @since 3.2
	 */
	private void verifyDuplicatedRegion() {
		Map<String, RegionInfo> regions = getAll();
		String localRegion = getCurrent();
		RegionInfo regionInfo = regions.get(localRegion);
		if (regionInfo != null && !StringUtils.equals(regionInfo.getIp(), config.getClusterProperties().getProperty
				(ClusterConstants.PROP_CLUSTER_HOST, NetworkUtils.DEFAULT_LOCAL_HOST_ADDRESS))) {
			throw processException("The region name, " + localRegion
					+ ", is already used by other controller " + regionInfo.getIp()
					+ ". Please set the different region name in this controller.");
		}
	}

	@Autowired
	private AgentManager agentManager;

	/**
	 * check Region and Update its value.
	 */
	public void checkRegionUpdate() {
		if (!config.isInvisibleRegion()) {
			try {
				HashSet<AgentIdentity> newHashSet = Sets.newHashSet(agentManager.getAllAttachedAgents());
				final String regionIP = StringUtils.defaultIfBlank(config.getCurrentIP(), NetworkUtils.DEFAULT_LOCAL_HOST_ADDRESS);
				cache.put(getCurrent(), new RegionInfo(regionIP, config.getControllerPort(), newHashSet));
			} catch (Exception e) {
				LOGGER.error("Error while updating regions. {}", e.getMessage());
			}
		}
	}


	/**
	 * Get current region. This method returns where this service is running.
	 *
	 * @return current region.
	 */
	public String getCurrent() {
		return config.getRegion();
	}

	/**
	 * Get region by region name
	 *
	 * @param regionName
	 * @return region info
	 */
	public RegionInfo getOne(String regionName) {
		return (RegionInfo) cache.get(regionName).get();
	}

	/**
	 * Get region list of all clustered controller.
	 *
	 * @return region list
	 */
	public Map<String, RegionInfo> getAll() {
		Map<String, RegionInfo> regions = Maps.newHashMap();
		if (config.isClustered()) {
			for (Object eachKey : ((Ehcache) (cache.getNativeCache())).getKeysWithExpiryCheck()) {
				ValueWrapper valueWrapper = cache.get(eachKey);
				if (valueWrapper != null && valueWrapper.get() != null) {
					regions.put((String) eachKey, (RegionInfo) valueWrapper.get());
				}
			}
		}
		return regions;
	}

	public ArrayList<String> getAllVisibleRegionNames() {
		final ArrayList<String> regions = new ArrayList<String>();
		if (config.isClustered()) {
			for (Object eachKey : ((Ehcache) (cache.getNativeCache())).getKeysWithExpiryCheck()) {
				ValueWrapper valueWrapper = cache.get(eachKey);
				if (valueWrapper != null && valueWrapper.get() != null) {
					final RegionInfo region = TypeConvertUtils.cast(valueWrapper.get());
					if (region.isVisible()) {
						regions.add((String) eachKey);
					}
				}
			}
		}
		Collections.sort(regions);
		return regions;
	}

	Config getConfig() {
		return config;
	}

	/**
	 * For unit test
	 */
	public void setConfig(Config config) {
		this.config = config;
	}

	/**
	 * For unit test
	 */
	public void setCache(Cache cache) {
		this.cache = cache;
	}
}
