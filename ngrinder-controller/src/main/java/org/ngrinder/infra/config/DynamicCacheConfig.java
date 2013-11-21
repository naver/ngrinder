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
package org.ngrinder.infra.config;

import net.grinder.util.NetworkUtil;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.config.CacheConfiguration;
import net.sf.ehcache.config.CacheConfiguration.CacheEventListenerFactoryConfiguration;
import net.sf.ehcache.config.Configuration;
import net.sf.ehcache.config.ConfigurationFactory;
import net.sf.ehcache.config.FactoryConfiguration;
import net.sf.ehcache.distribution.RMICacheManagerPeerListenerFactory;
import net.sf.ehcache.distribution.RMICacheManagerPeerProviderFactory;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.ngrinder.common.constant.NGrinderConstants;
import org.ngrinder.infra.logger.CoreLogger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.ehcache.EhCacheCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.ngrinder.common.util.TypeConvertUtil.cast;

/**
 * Dynamic cache configuration. This get the control of EhCache configuration from Spring. Depending
 * on the system.conf, it creates local cache or dist cache.
 *
 * @author Mavlarn
 * @author JunHo Yoon
 * @since 3.1
 */
@Component
public class DynamicCacheConfig {

	@Autowired
	private Config config;

	/**
	 * Create cache manager dynamically according to the configuration.
	 *
	 * @return EhCacheCacheManager bean
	 */
	@SuppressWarnings("rawtypes")
	@Bean(name = "cacheManager")
	public EhCacheCacheManager dynamicCacheManager() {
		EhCacheCacheManager cacheManager = new EhCacheCacheManager();
		Configuration cacheManagerConfig;
		InputStream inputStream = null;
		try {
			if (!config.isCluster()) {
				CoreLogger.LOGGER.info("In no cluster mode.");
				inputStream = new ClassPathResource("ehcache.xml").getInputStream();
				cacheManagerConfig = ConfigurationFactory.parseConfiguration(inputStream);
			} else {
				CoreLogger.LOGGER.info("In cluster mode.");
				System.setProperty("java.rmi.server.hostname", getCurrentIP());

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
				String peerListenerProperties = createPearListenerProperties();
				peerListenerConfig.setProperties(peerListenerProperties);
				cacheManagerConfig.addCacheManagerPeerListenerFactory(peerListenerConfig);
				CoreLogger.LOGGER.info("clusterURLs:{}", peerListenerProperties);
			}
			cacheManagerConfig.setName(getCacheName());
			CacheManager mgr = CacheManager.create(cacheManagerConfig);
			setCacheManager(mgr);
			cacheManager.setCacheManager(mgr);

		} catch (IOException e) {
			CoreLogger.LOGGER.error("Error while setting up cache", e);
		} finally {
			IOUtils.closeQuietly(inputStream);
		}
		return cacheManager;
	}

	protected String createPearListenerProperties() {
		return String.format("hostName=%s, port=%d, socketTimeoutMillis=200", getCurrentIP(), getCacheListenerPort());
	}

	private String getCurrentIP() {
		for (String eachIP : config.getClusterURIs()) {
			NetworkUtil.IPPortPair ipAndPortPair = NetworkUtil.convertIPAndPortPair(eachIP, 0);
			for (InetAddress each : NetworkUtil.DEFAULT_LOCAL_ADDRESSES) {
				if (ipAndPortPair.isSame(each.getHostAddress())) {
					return eachIP;
				}
			}
		}
		return NetworkUtil.DEFAULT_LOCAL_HOST_ADDRESS;
	}

	void setCacheManager(CacheManager mgr) {
		// Do nothing.
	}

	String getCacheName() {
		return "cacheManager";
	}

	protected String createCacheProperties(List<String> replicatedCacheNames) {
		int clusterListenerPort = getCacheListenerPort();
		// rmiUrls=//10.34.223.148:40003/distributed_map|//10.34.63.28:40003/distributed_map
		List<String> uris = new ArrayList<String>();
		final String currentIP = getCurrentIP();
		for (String ip : config.getClusterURIs()) {
			NetworkUtil.IPPortPair ipAndPortPair = NetworkUtil.convertIPAndPortPair(ip, clusterListenerPort);
			if (ipAndPortPair.isSame(currentIP)) {
				continue;
			}

			for (String cacheName : replicatedCacheNames) {
				uris.add(String.format("//%s:%d/%s", ipAndPortPair.getIP(), ipAndPortPair.getPort(), cacheName));
			}
		}
		return "peerDiscovery=manual,rmiUrls=" + StringUtils.join(uris, "|");
	}

	int getCacheListenerPort() {
		return config.getSystemProperties().getPropertyInt(NGrinderConstants.NGRINDER_PROP_CLUSTER_LISTENER_PORT,
				Config.NGRINDER_DEFAULT_CLUSTER_LISTENER_PORT);
	}

	protected List<String> getReplicatedCacheNames(Configuration cacheManagerConfig) {
		Map<String, CacheConfiguration> cacheConfigurations = cacheManagerConfig.getCacheConfigurations();
		List<String> replicatedCacheNames = new ArrayList<String>();
		for (Map.Entry<String, CacheConfiguration> eachConfig : cacheConfigurations.entrySet()) {
			List<CacheEventListenerFactoryConfiguration> list = cast(eachConfig.getValue()
					.getCacheEventListenerConfigurations());
			for (CacheEventListenerFactoryConfiguration each : list) {
				if (each.getFullyQualifiedClassPath().equals("net.sf.ehcache.distribution.RMICacheReplicatorFactory")) {
					replicatedCacheNames.add(eachConfig.getKey());
				}
			}
		}
		return replicatedCacheNames;
	}

	public Config getConfig() {
		return config;
	}

	public void setConfig(Config config) {
		this.config = config;
	}
}
