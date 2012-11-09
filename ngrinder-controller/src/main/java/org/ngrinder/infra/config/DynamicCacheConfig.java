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

import net.sf.ehcache.CacheManager;
import net.sf.ehcache.config.CacheConfiguration;
import net.sf.ehcache.config.CacheConfiguration.BootstrapCacheLoaderFactoryConfiguration;
import net.sf.ehcache.config.CacheConfiguration.CacheEventListenerFactoryConfiguration;
import net.sf.ehcache.config.FactoryConfiguration;
import net.sf.ehcache.distribution.RMIBootstrapCacheLoaderFactory;
import net.sf.ehcache.distribution.RMICacheManagerPeerListenerFactory;
import net.sf.ehcache.distribution.RMICacheManagerPeerProviderFactory;
import net.sf.ehcache.distribution.RMICacheReplicatorFactory;

import org.ngrinder.common.constant.NGrinderConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowire;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.ehcache.EhCacheCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Class description.
 *
 * @author Mavlarn
 * @since
 */
@Configuration
public class DynamicCacheConfig {

	private static final Logger LOGGER = LoggerFactory.getLogger(DynamicCacheConfig.class);
	
	@Autowired
	private Config config;
	
	/**
	 * Create cache manager dynamically according to the configuration.
	 * Because we cann't add a cluster peer provider dynamically, another cache manager will be created
	 * for distributed cache.
	 * In cache cluster mode, we need to create cache "distributed_map" in program with related configuration.
	 * 
	 * distributed cache configuration
	 *  <cacheManagerPeerProviderFactory  
        	class="net.sf.ehcache.distribution.RMICacheManagerPeerProviderFactory"  
        	properties="peerDiscovery=manual,
        			rmiUrls=//10.34.223.148:40003/distributed_map|//10.34.63.28:40003/distributed_map"/>  
     
     	<cacheManagerPeerListenerFactory  
        	class="net.sf.ehcache.distribution.RMICacheManagerPeerListenerFactory"  
        	properties="port=40003"/>
        	
	    <cache name="distributed_map" maxElementsInMemory="1000"
			eternal="true" overflowToDisk="false">
			<cacheEventListenerFactory class="net.sf.ehcache.distribution.RMICacheReplicatorFactory" />
			<bootstrapCacheLoaderFactory class="net.sf.ehcache.distribution.RMIBootstrapCacheLoaderFactory"/>
		</cache>
		
	 * @return
	 */
	@SuppressWarnings("rawtypes")
	@Bean(name = "dynamicCacheManager", autowire=Autowire.NO)
	public EhCacheCacheManager dynamicCacheManager() {
		net.sf.ehcache.config.Configuration distCacheManagerConfig = new net.sf.ehcache.config.Configuration();
		distCacheManagerConfig.setName("dynamicCacheManager");
		EhCacheCacheManager distCacheManager = new EhCacheCacheManager();
		CacheConfiguration distCacheConfig = new CacheConfiguration(
				NGrinderConstants.CACHE_NAME_DISTRIBUTED_MAP, 10000).eternal(true).overflowToDisk(false);
		
		if (!config.isCluster()) {
			LOGGER.info("In no cluster mode.");
			distCacheManagerConfig.addCache(distCacheConfig);
			CacheManager mgr = new CacheManager(distCacheManagerConfig);
			distCacheManager.setCacheManager(mgr);
			return distCacheManager;
		}
		LOGGER.info("In cluster mode.");
		int clusterListenerPort = config.getSystemProperties().getPropertyInt(
				NGrinderConstants.NGRINDER_PROP_CLUSTER_LISTENER_PORT, Config.NGRINDER_DEFAULT_CLUSTER_LISTENER_PORT);

		FactoryConfiguration peerProviderConfig = new FactoryConfiguration();
		peerProviderConfig.setClass(RMICacheManagerPeerProviderFactory.class.getName());
		StringBuilder peerPropSB = new StringBuilder("peerDiscovery=manual,rmiUrls=");
		peerPropSB.append(config.getClusterURIs());
		peerProviderConfig.setProperties(peerPropSB.toString());
		distCacheManagerConfig.addCacheManagerPeerProviderFactory(peerProviderConfig);

		FactoryConfiguration peerListenerConfig = new FactoryConfiguration();
		peerListenerConfig.setClass(RMICacheManagerPeerListenerFactory.class.getName());
		String listenerPropStr = "port=" + clusterListenerPort;
		peerListenerConfig.setProperties(listenerPropStr);
		distCacheManagerConfig.addCacheManagerPeerListenerFactory(peerListenerConfig);		

		// <bootstrapCacheLoaderFactory class="net.sf.ehcache.distribution.RMIBootstrapCacheLoaderFactory"/>
		// when the cache is initialized, it will synchronize the cache with peer.
		BootstrapCacheLoaderFactoryConfiguration bootConfig = new BootstrapCacheLoaderFactoryConfiguration();
		bootConfig.setClass(RMIBootstrapCacheLoaderFactory.class.getName());
		distCacheConfig.addBootstrapCacheLoaderFactory(bootConfig);

		// <cacheEventListenerFactory class="net.sf.ehcache.distribution.RMICacheReplicatorFactory" />
		// this configuration makes sure the update on this cache will be
		// replicated to other peer
		CacheEventListenerFactoryConfiguration updateConfig = new CacheEventListenerFactoryConfiguration();
		updateConfig.setClass(RMICacheReplicatorFactory.class.getName());
		distCacheConfig.addCacheEventListenerFactory(updateConfig);
		
		distCacheManagerConfig.addCache(distCacheConfig);
		CacheManager mgr = new CacheManager(distCacheManagerConfig);
		distCacheManager.setCacheManager(mgr);
		return distCacheManager;
	}
	
}
