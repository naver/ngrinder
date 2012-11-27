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
package org.ngrinder.region.service;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;

import net.sf.ehcache.Ehcache;

import org.ngrinder.common.constant.NGrinderConstants;
import org.ngrinder.infra.config.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.cache.ehcache.EhCacheCacheManager;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

/**
 * Region service class.
 *
 * @author Mavlarn
 * @since 3.1
 */
@Service
public class RegionService {
	
	private static final Logger LOG = LoggerFactory.getLogger(RegionService.class);

	@Autowired
	private Config config;
	
	@Autowired
	private EhCacheCacheManager cacheManager;
	
	/**
	 * Set current region into cache, using the IP as key and region name as value.
	 * 
	 */
	@PostConstruct
	public void initRegion() {
		String currentRegion = config.getRegion();
		String currentIP = config.getCurrentIP();
		Cache distCache = cacheManager.getCache(NGrinderConstants.CACHE_NAME_REGION_LIST);

		@SuppressWarnings("rawtypes")
		List list = ((Ehcache) distCache.getNativeCache()).getKeys();
		for (Object object : list) {
			if (!(object instanceof String)) {
				LOG.info("Evict invalid cache: {}:{} from cache.", object, distCache.get(object));
				distCache.evict(object);
			} else {
				String ip = (String) object;
				String region = (String) distCache.get(object).get();
				if (ip.equals(currentIP) && !region.equals(currentRegion)) {
					// ip is same, region is different, means the region name is changed
					LOG.info("Evict invalid Region: {}:{} from cache.", region, currentIP);
					distCache.evict(region); // remove previous region name.
				}
			}
		}
		distCache.put(currentIP, currentRegion);
		LOG.info("Add Region: {}:{} into cache.", currentRegion, currentIP);
	}
	
	/**
	 * get region list of all clustered controller.
	 * @return region list
	 */
	public List<String> getRegionList() {
		Cache distCache = cacheManager.getCache(NGrinderConstants.CACHE_NAME_REGION_LIST);
		@SuppressWarnings("rawtypes")
		List list = ((Ehcache) distCache.getNativeCache()).getKeys();
		List<String> regionList = new ArrayList<String>();
		List<String> ipList = new ArrayList<String>();
		for (Object object : list) {
			ipList.add((String) object);
			regionList.add((String) distCache.get(object).get());
		}
		LOG.debug("Region list from cache:{}, ip:{}", regionList, ipList);
		return regionList;
	}
	
	/**
	 * just for test and debug.
	 */
	@Scheduled(fixedDelay = 5000)
	public void test() {
		testDistCache(NGrinderConstants.CACHE_NAME_REGION_LIST);
		//testDistCache(NGrinderConstants.CACHE_NAME_RUNNING_STATISTICS);
	}
	
	private void testDistCache(String cacheName) {
		Cache distCache = cacheManager.getCache(cacheName);
		@SuppressWarnings("rawtypes")
		List list = ((Ehcache) distCache.getNativeCache()).getKeys();
		StringBuilder valueSB = new StringBuilder();
		StringBuilder keySB = new StringBuilder();
		for (Object object : list) {
			keySB.append(object).append(", ");
			valueSB.append(distCache.get(object).get()).append(", ");
		}
		LOG.debug("Cache name:{}.", cacheName);
		LOG.debug("           key:{}", keySB.toString());
		LOG.debug("           value:{}", valueSB.toString());
	}

}
