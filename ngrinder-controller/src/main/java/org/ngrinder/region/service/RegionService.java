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
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;

import net.sf.ehcache.Ehcache;

import org.ngrinder.common.constant.NGrinderConstants;
import org.ngrinder.infra.config.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
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
	
	
	/**
	 * get region list of all clustered controller.
	 * @return region list
	 */
	@Cacheable("region_list")
	public List<String> getRegionList() {
		List<String> regionList = new ArrayList<String>();
		regionList.add(config.getRegion());
		LOG.debug("Can not get Region list from cache, get from config:{}", regionList);
		return regionList;
	}
	
	@Scheduled(fixedDelay = 5000)
	public void test() {
		Cache distCache = cacheManager.getCache(NGrinderConstants.CACHE_NAME_REGION_LIST);
		List list = ((Ehcache)distCache.getNativeCache()).getKeys();
		StringBuilder sb = new StringBuilder();
		for (Object object : list) {
			sb.append(distCache.get(object).get());
		}
		LOG.debug("Region list from cache:{}", sb.toString());
		;
		LOG.debug("get region:{}", config.getRegion());

	}

}
