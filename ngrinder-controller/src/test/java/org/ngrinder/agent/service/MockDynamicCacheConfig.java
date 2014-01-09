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

import net.grinder.util.NetworkUtils;
import net.grinder.util.Pair;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.config.Configuration;
import net.sf.ehcache.config.ConfigurationFactory;
import net.sf.ehcache.config.FactoryConfiguration;
import net.sf.ehcache.distribution.RMICacheManagerPeerListenerFactory;
import net.sf.ehcache.distribution.RMICacheManagerPeerProviderFactory;
import org.apache.commons.io.IOUtils;
import org.ngrinder.infra.config.DynamicCacheConfig;
import org.ngrinder.infra.logger.CoreLogger;
import org.springframework.cache.ehcache.EhCacheCacheManager;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

/**
 * Mock DynamicCacheConfig which use cluster mode.
 */
@Profile("unit-test")
@Component
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
			Pair<NetworkUtils.IPPortPair, String> properties = createManualDiscoveryCacheProperties(replicatedCacheNames);
			NetworkUtils.IPPortPair currentListener = properties.getFirst();
			String peerProperty = properties.getSecond();
			peerProviderConfig.setProperties(peerProperty);
			cacheManagerConfig.addCacheManagerPeerProviderFactory(peerProviderConfig);
			System.setProperty("java.rmi.server.hostname", currentListener.getFormattedIP());
			FactoryConfiguration peerListenerConfig = new FactoryConfiguration();
			peerListenerConfig.setClass(RMICacheManagerPeerListenerFactory.class.getName());
			String peerListenerProperty = String.format("hostName=%s, port=%d, socketTimeoutMillis=1000",
					currentListener.getFormattedIP(), currentListener.getPort());
			peerListenerConfig.setProperties(peerListenerProperty);
			cacheManagerConfig.addCacheManagerPeerListenerFactory(peerListenerConfig);
			CoreLogger.LOGGER.info("clusterURLs:{}", peerListenerProperty);
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
