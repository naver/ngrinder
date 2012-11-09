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
package org.ngrinder.infra.init;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;

import org.ngrinder.common.constant.NGrinderConstants;
import org.ngrinder.infra.config.Config;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.Cache;
import org.springframework.cache.Cache.ValueWrapper;
import org.springframework.cache.ehcache.EhCacheCacheManager;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Service;

/**
 * Initialize cache cluster related environment.
 *
 * @author Mavlarn
 * @since
 */
@Service
@DependsOn("config")
public class CacheClusterInit {

	@Autowired
	private Config config;

	@Autowired
	@Qualifier("dynamicCacheManager")
	private EhCacheCacheManager dynamicCacheManager;
	
	@PostConstruct
	public void init() {
		initRegion();
	}
	
	@SuppressWarnings("unchecked")
	public void initRegion() {
		Cache distCache = dynamicCacheManager.getCache(NGrinderConstants.CACHE_NAME_DISTRIBUTED_MAP);
		ValueWrapper regionCacheObj = distCache.get(NGrinderConstants.CACHE_NAME_REGION_LIST);
		List<String> regionList = null;
		if (regionCacheObj == null) {
			regionList = new ArrayList<String>();
			distCache.put(NGrinderConstants.CACHE_NAME_REGION_LIST, regionList);
			regionList.add(config.getRegion());
		} else {
			regionList = (List<String>)regionCacheObj.get();
		}
		if (!regionList.contains(config.getRegion())) {
			regionList.add(config.getRegion());
		}
	}
}
