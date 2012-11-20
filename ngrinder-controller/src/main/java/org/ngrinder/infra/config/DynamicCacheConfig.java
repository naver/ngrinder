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
package org.ngrinder.infra.config;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import net.grinder.util.NetworkUtil;
import net.sf.ehcache.CacheException;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.config.ConfigurationFactory;
import net.sf.ehcache.config.FactoryConfiguration;
import net.sf.ehcache.distribution.RMICacheManagerPeerProviderFactory;

import org.ngrinder.common.constant.NGrinderConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.ehcache.EhCacheCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

/**
 * Class description.
 *
 * @author Mavlarn
 * @since 3.1
 */
@Component
public class DynamicCacheConfig {

	private static final Logger LOGGER = LoggerFactory.getLogger(DynamicCacheConfig.class);
	
	@Autowired
	private Config config;
	
	private static final List<String> cacheNameList;
	static {
		cacheNameList = new ArrayList<String>();
		cacheNameList.add("region_list");
		cacheNameList.add("running_agent_infos");
		cacheNameList.add("running_statistics");
		cacheNameList.add("users");
		cacheNameList.add("file_entry_search_cache");
	}
	
	/**
	 * Create cache manager dynamically according to the configuration.
	 * Because we cann't add a cluster peer provider dynamically.
	 * 
	 * distributed caches
			<cache name="region_list" maxElementsInMemory="1000" eternal="true" overflowToDisk="false">
				<cacheEventListenerFactory class="net.sf.ehcache.distribution.RMICacheReplicatorFactory" />
				<bootstrapCacheLoaderFactory class="net.sf.ehcache.distribution.RMIBootstrapCacheLoaderFactory" />
			</cache>
			<cache name="running_agent_infos" maxElementsInMemory="1000" eternal="true" overflowToDisk="false">
				<cacheEventListenerFactory class="net.sf.ehcache.distribution.RMICacheReplicatorFactory" />
			</cache>
			<cache name="running_statistics" maxElementsInMemory="1000" eternal="true" overflowToDisk="false">
				<cacheEventListenerFactory class="net.sf.ehcache.distribution.RMICacheReplicatorFactory" />
			</cache>
			<cache name="users" maxElementsInMemory="100" eternal="false"
				overflowToDisk="false" timeToIdleSeconds="900" timeToLiveSeconds="1800">
				<cacheEventListenerFactory class="net.sf.ehcache.distribution.RMICacheReplicatorFactory" />
			</cache>
			<cache name="file_entry_search_cache" maxElementsInMemory="100" eternal="false"
				   overflowToDisk="false" timeToIdleSeconds="60" timeToLiveSeconds="60">
				<cacheEventListenerFactory class="net.sf.ehcache.distribution.RMICacheReplicatorFactory" />
			</cache>
		
	 * @return
	 * @throws IOException 
	 * @throws CacheException 
	 */
	@SuppressWarnings("rawtypes")
	@Bean(name = "cacheManager")
	public EhCacheCacheManager dynamicCacheManager() throws CacheException, IOException {
		EhCacheCacheManager cacheManager = new EhCacheCacheManager();
		net.sf.ehcache.config.Configuration cacheManagerConfig;
		if (!config.isCluster()) {
			LOGGER.info("In no cluster mode.");
			cacheManagerConfig = ConfigurationFactory
					.parseConfiguration(new ClassPathResource("ehcache.xml").getFile());
		} else {
			LOGGER.info("In cluster mode.");
			cacheManagerConfig = ConfigurationFactory.parseConfiguration(new ClassPathResource("ehcache-dist.xml")
					.getFile());

			FactoryConfiguration peerProviderConfig = new FactoryConfiguration();
			peerProviderConfig.setClass(RMICacheManagerPeerProviderFactory.class.getName());
			StringBuilder peerPropSB = new StringBuilder("peerDiscovery=manual,rmiUrls=");
			String[] clusterURLVec = config.getClusterURIs();
			int clusterListenerPort = config.getSystemProperties().getPropertyInt(
							NGrinderConstants.NGRINDER_PROP_CLUSTER_LISTENER_PORT,
							Config.NGRINDER_DEFAULT_CLUSTER_LISTENER_PORT);

			// rmiUrls=//10.34.223.148:40003/distributed_map|//10.34.63.28:40003/distributed_map
			StringBuilder sb = new StringBuilder();
			StringBuilder prefixSB = new StringBuilder("//");
			for (String url : clusterURLVec) {
				if (url.equals(config.getCurrentIP())) {
					continue;
				}
				String prefix = prefixSB.append(url).append(":").append(clusterListenerPort).append("/").toString();
				for (String cacheName : cacheNameList) {
					sb.append(prefix).append(cacheName);
					sb.append("|");
				}
			}
			String clusterURLs = sb.substring(0, sb.length() - 2); //remove last |
			
			peerPropSB.append(clusterURLs);
			peerProviderConfig.setProperties(peerPropSB.toString());
			cacheManagerConfig.addCacheManagerPeerProviderFactory(peerProviderConfig);
			LOGGER.info("clusterURLs:{}", clusterURLs);
		}
		cacheManagerConfig.setName("cacheManager");
		CacheManager mgr = new CacheManager(cacheManagerConfig);
		cacheManager.setCacheManager(mgr);
		return cacheManager;
	}
	
}
