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
package org.ngrinder.agent.service;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import net.sf.ehcache.CacheManager;
import net.sf.ehcache.config.Configuration;
import net.sf.ehcache.config.ConfigurationFactory;
import net.sf.ehcache.config.FactoryConfiguration;
import net.sf.ehcache.distribution.RMICacheManagerPeerListenerFactory;
import net.sf.ehcache.distribution.RMICacheManagerPeerProviderFactory;

import org.apache.commons.io.IOUtils;
import org.ngrinder.infra.annotation.TestOnlyComponent;
import org.ngrinder.infra.config.DynamicCacheConfig;
import org.ngrinder.infra.logger.CoreLogger;
import org.springframework.cache.ehcache.EhCacheCacheManager;
import org.springframework.core.io.ClassPathResource;

/**
  * Mock DynamicCacheConfig which use cluster mode.
 */
@TestOnlyComponent
public class MockDynamicCacheConfig extends DynamicCacheConfig {
	
	@SuppressWarnings("rawtypes")
	@Override
	public EhCacheCacheManager dynamicCacheManager() {
		EhCacheCacheManager cacheManager = new EhCacheCacheManager();
		Configuration cacheManagerConfig;
		InputStream inputStream = null;
		try {
		
			CoreLogger.LOGGER.info("In cluster mode.");
			inputStream = new ClassPathResource("ehcache-dist.xml").getInputStream();
			cacheManagerConfig = ConfigurationFactory.parseConfiguration(inputStream);

			FactoryConfiguration peerProviderConfig = new FactoryConfiguration();
			peerProviderConfig.setClass(RMICacheManagerPeerProviderFactory.class.getName());
			List<String> replicatedCacheNames = getReplicatedCacheNames(cacheManagerConfig);
			String properties = createCacheProperties(replicatedCacheNames);
			peerProviderConfig.setProperties(properties);
			cacheManagerConfig.addCacheManagerPeerProviderFactory(peerProviderConfig);

			FactoryConfiguration peerListenerConfig = new FactoryConfiguration();
			peerListenerConfig.setClass(RMICacheManagerPeerListenerFactory.class.getName());
			String peerListenerProperties = createPearListenerProperties(replicatedCacheNames);
			peerListenerConfig.setProperties(peerListenerProperties);
			cacheManagerConfig.addCacheManagerPeerListenerFactory(peerListenerConfig);
			CoreLogger.LOGGER.info("clusterURLs:{}", peerListenerProperties);
	
			cacheManagerConfig.setName("TestCluster");
			CacheManager mgr = CacheManager.create(cacheManagerConfig);
			cacheManager.setCacheManager(mgr);
		} catch (IOException e) {
			CoreLogger.LOGGER.error("Error while setting up cache", e);
		} finally {
			IOUtils.closeQuietly(inputStream);
		}
		return cacheManager;
	}

}
