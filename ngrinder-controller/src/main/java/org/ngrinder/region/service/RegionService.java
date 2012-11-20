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
import net.sf.ehcache.Element;

import org.ngrinder.common.constant.NGrinderConstants;
import org.ngrinder.infra.config.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.ehcache.EhCacheCacheManager;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

/**
 * Class description.
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
	
	//@PostConstruct
	public void initRegion() {
		//return config.getRegion();
		Cache distCache = cacheManager.getCache(NGrinderConstants.CACHE_NAME_REGION_LIST);
		Element obj = ((Ehcache)distCache.getNativeCache()).get("");
		List keys = ((Ehcache)distCache.getNativeCache()).getKeys();
		for (Object objkey : keys) {
			obj = ((Ehcache)distCache.getNativeCache()).get(objkey);
			LOG.debug("key:{} value:{}", objkey, obj);
		}
//		List<String> regionList = (List<String>)(obj.getValue());
//		if (!regionList.contains(config.getRegion())) {
//			regionList.add(config.getRegion());
//		}
	}

	@Cacheable(value = "region_list", key = "#region")
	public String getCurrentRegion(String region) {
		LOG.debug("Should put region:{} into cache.", config.getRegion());
		return config.getRegion();
	}
	
	/**
	 * get region list of all clustered controller.
	 * @return region list
	 */
	public List<String> getRegionList() {
		Cache distCache = cacheManager.getCache(NGrinderConstants.CACHE_NAME_REGION_LIST);
		List list = ((Ehcache)distCache.getNativeCache()).getKeys();
		List<String> regionList = new ArrayList<String>();
		for (Object object : list) {
			regionList.add((String)distCache.get(object).get());
		}
		LOG.debug("Region list from cache:{}", regionList);
		return regionList;
	}
	
	@Scheduled(fixedDelay = 5000)
	public void test() {
		Cache distCache = cacheManager.getCache(NGrinderConstants.CACHE_NAME_REGION_LIST);
		List list = ((Ehcache)distCache.getNativeCache()).getKeys();
		StringBuilder sb = new StringBuilder();
		for (Object object : list) {
			sb.append(distCache.get(object).get()).append(", ");
		}
		LOG.debug("Region list from cache:{}", sb.toString());

	}

}
