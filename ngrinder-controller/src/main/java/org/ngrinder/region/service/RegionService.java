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

import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import com.google.common.collect.Maps;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.cluster.Member;
import lombok.RequiredArgsConstructor;
import net.grinder.util.NetworkUtils;
import org.apache.commons.lang.StringUtils;
import org.ngrinder.common.constant.ClusterConstants;
import org.ngrinder.common.exception.NGrinderRuntimeException;
import org.ngrinder.infra.config.Config;
import org.ngrinder.infra.hazelcast.HazelcastService;
import org.ngrinder.infra.hazelcast.task.RegionInfoTask;
import org.ngrinder.region.model.RegionInfo;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.*;
import java.util.concurrent.TimeUnit;

import static java.util.Collections.emptyList;
import static java.util.Collections.emptySet;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.lang.StringUtils.isEmpty;
import static org.ngrinder.common.constant.CacheConstants.*;
import static org.ngrinder.common.util.ExceptionUtils.processException;
import static org.ngrinder.common.util.RegionUtils.convertSubregionsStringToSet;

/**
 * Region service class. This class responsible to keep the status of available regions.
 *
 * @since 3.1
 */
@Service
@RequiredArgsConstructor
public class RegionService {

	private final Config config;

	private final HazelcastService hazelcastService;

	private final HazelcastInstance hazelcastInstance;

	private final Supplier<Map<String, RegionInfo>> allRegions = Suppliers.memoizeWithExpiration(new Supplier<Map<String, RegionInfo>>() {
		@Override
		public Map<String, RegionInfo> get() {
			Map<String, RegionInfo> regions = Maps.newHashMap();
			if (config.isClustered()) {
				List<RegionInfo> regionInfos = hazelcastService.submitToAllRegion(REGION_EXECUTOR_SERVICE_NAME, new RegionInfoTask());
				for (RegionInfo regionInfo : regionInfos) {
					regions.put(regionInfo.getRegionName(), regionInfo);
				}
			} else {
				final String regionIP = StringUtils.defaultIfBlank(config.getCurrentIP(), NetworkUtils.DEFAULT_LOCAL_HOST_ADDRESS);
				regions.put(config.getRegion(), new RegionInfo(config.getRegion(), emptySet(), regionIP, config.getControllerPort()));
			}
			return regions;
		}
	}, REGION_CACHE_TIME_TO_LIVE_SECONDS, TimeUnit.SECONDS);

	private final Supplier<List<Map<String, String>>> allRegionNames = Suppliers.memoizeWithExpiration(new Supplier<List<Map<String, String>>>() {
		@Override
		public List<Map<String, String>> get() {
			Set<Member> members = hazelcastInstance.getCluster().getMembers();
			List<Map<String, String>> regionNames = new ArrayList<>();

			for (Member member : members) {
				if (member.getAttributes().containsKey(REGION_ATTR_KEY)) {
					Map<String, String> regionMap = new HashMap<>();
					regionMap.put(REGION_ATTR_KEY, member.getAttributes().get(REGION_ATTR_KEY));
					regionMap.put(SUBREGION_ATTR_KEY, member.getAttributes().get(SUBREGION_ATTR_KEY));
					regionNames.add(regionMap);
				}
			}

			return regionNames;
		}
	}, REGION_CACHE_TIME_TO_LIVE_SECONDS, TimeUnit.SECONDS);

	@PostConstruct
	public void initRegion() {
		if (config.isClustered()) {
			verifyDuplicatedRegion();
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
	 * @param regionName region name
	 * @return region info
	 */
	public RegionInfo getOne(String regionName) {
		RegionInfo regionInfo = getAll().get(regionName);
		if (regionInfo != null) {
			return regionInfo;
		}
		throw new NGrinderRuntimeException(regionName + " is not exist");
	}

	public RegionInfo getOne(String region, String subregion) {
		if (isEmpty(subregion)) {
			return getOne(region);
		}

		RegionInfo regionInfo = getAll().get(region);
		if (regionInfo != null) {
			if (isEmpty(subregion) || regionInfo.getSubregion().contains(subregion)) {
				return regionInfo;
			}
		}
		throw new NGrinderRuntimeException(region + "." + subregion + " is not exist");
	}

	/**
	 * Get region list of all clustered controller.
	 *
	 * @return region list
	 */
	public Map<String, RegionInfo> getAll() {
		return allRegions.get();
	}

	public List<Map<String, Object>> getAllVisibleRegionNames() {
		if (config.isClustered()) {
			return allRegionNames.get().stream().map(region -> {
					Map<String, Object> regionInfo = new HashMap<>();
					String subregionAttributes = region.get(SUBREGION_ATTR_KEY);
					regionInfo.put(REGION_ATTR_KEY, region.get(REGION_ATTR_KEY));
					regionInfo.put(SUBREGION_ATTR_KEY, convertSubregionsStringToSet(subregionAttributes));
					return regionInfo;
			}).collect(toList());
		} else {
			return emptyList();
		}
	}

	public Config getConfig() {
		return config;
	}
}
