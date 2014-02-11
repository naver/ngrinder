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

import net.grinder.util.NetworkUtils;
import net.grinder.util.Pair;
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
import org.ngrinder.common.constant.ClusterConstants;
import org.ngrinder.common.util.Preconditions;
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

import static org.ngrinder.common.util.TypeConvertUtils.cast;

/**
 * Dynamic cache configuration. This get the control of EhCache configuration from Spring. Depending
 * on the system.conf, it creates local cache or dist cache.
 *
 * @author Mavlarn
 * @author JunHo Yoon
 * @since 3.1
 */
@SuppressWarnings("SpellCheckingInspection")
@Component
public class DynamicCacheConfig implements ClusterConstants {

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
			if (!isClustered()) {
				inputStream = new ClassPathResource("ehcache.xml").getInputStream();
				cacheManagerConfig = ConfigurationFactory.parseConfiguration(inputStream);
			} else {
				CoreLogger.LOGGER.info("In cluster mode.");
				inputStream = new ClassPathResource("ehcache-dist.xml").getInputStream();
				cacheManagerConfig = ConfigurationFactory.parseConfiguration(inputStream);
				Pair<FactoryConfiguration, NetworkUtils.IPPortPair> result =
						createRMICacheManagerPeerProviderFactory(cacheManagerConfig);
				cacheManagerConfig.addCacheManagerPeerProviderFactory(result.getFirst());
				cacheManagerConfig.addCacheManagerPeerListenerFactory(
						createPearListenerFactory(result.getSecond().getIP(), result.getSecond().getPort()));
			}
			cacheManagerConfig.setName(getCacheName());
			CacheManager mgr = CacheManager.create(cacheManagerConfig);
			cacheManager.setCacheManager(mgr);

		} catch (IOException e) {
			CoreLogger.LOGGER.error("Error while setting up cache", e);
		} finally {
			IOUtils.closeQuietly(inputStream);
		}
		return cacheManager;
	}

	protected boolean isClustered() {
		return config.isClustered();
	}

	private Pair<FactoryConfiguration, NetworkUtils.IPPortPair> createRMICacheManagerPeerProviderFactory
			(Configuration cacheManagerConfig) {
		FactoryConfiguration peerProviderConfig = new FactoryConfiguration();
		peerProviderConfig.setClass(RMICacheManagerPeerProviderFactory.class.getName());

		Pair<NetworkUtils.IPPortPair, String> properties;
		if (StringUtils.equals(getClusterMode(), "advanced")) {
			CoreLogger.LOGGER.info("In cluster - advanced mode.");
			properties = createManualDiscoveryCacheProperties(getReplicatedCacheNames(cacheManagerConfig));
		} else {
			CoreLogger.LOGGER.info("In cluster - easy mode.");
			properties = createAutoDiscoveryCacheProperties();
		}

		NetworkUtils.IPPortPair currentListener = properties.getFirst();
		System.setProperty("java.rmi.server.hostname", currentListener.getFormattedIP());

		String peers = properties.getSecond();
		peerProviderConfig.setProperties(peers);
		CoreLogger.LOGGER.info("peer provider is set as {}", peers);
		return Pair.of(peerProviderConfig, currentListener);
	}

	protected String getClusterMode() {
		return config.getClusterProperties().getProperty(ClusterConstants.PROP_CLUSTER_MODE, "advanced");
	}

	private FactoryConfiguration createPearListenerFactory(String ip, int port) {
		FactoryConfiguration peerListenerConfig = new FactoryConfiguration();
		peerListenerConfig.setClass(RMICacheManagerPeerListenerFactory.class.getName());
		peerListenerConfig.setProperties(String.format("hostName=%s, port=%d, socketTimeoutMillis=3000", ip, port));
		CoreLogger.LOGGER.info("peer listener is set as {}:{}", ip, port);
		return peerListenerConfig;
	}

	String getCacheName() {
		return "cacheManager";
	}


	public Pair<NetworkUtils.IPPortPair, String> createAutoDiscoveryCacheProperties() {
		// rmiUrls=//10.34.223.148:40003/distributed_map|//10.34.63.28:40003/distributed_map
		NetworkUtils.IPPortPair local = new NetworkUtils.IPPortPair(getClusterHostName(), getClusterPort());
		String peerProperty = "peerDiscovery=automatic, multicastGroupAddress=230.0.0.1,multicastGroupPort=4446, timeToLive=32";
		return Pair.of(Preconditions.checkNotNull(local, "localhost ip does not exists in the cluster uris"),
				peerProperty);
	}

	public Pair<NetworkUtils.IPPortPair, String> createManualDiscoveryCacheProperties(List<String> replicatedCacheNames) {
		int clusterListenerPort = getClusterPort();
		// rmiUrls=//10.34.223.148:40003/distributed_map|//10.34.63.28:40003/distributed_map
		List<String> uris = new ArrayList<String>();
		NetworkUtils.IPPortPair local = null;
		for (String ip : getClusterURIs()) {
			NetworkUtils.IPPortPair ipAndPortPair = NetworkUtils.convertIPAndPortPair(ip, clusterListenerPort);
			if (ipAndPortPair.isLocalHost() && ipAndPortPair.getPort() == clusterListenerPort) {
				local = ipAndPortPair;
				continue;
			}
			for (String cacheName : replicatedCacheNames) {
				uris.add(String.format("//%s/%s", ipAndPortPair.toString(), cacheName));
			}
		}

		String peerProperty = "peerDiscovery=manual,rmiUrls=" + StringUtils.join(uris, "|");
		return Pair.of(Preconditions.checkNotNull(local, "localhost ip does not exists in the cluster uris"),
				peerProperty);
	}

	protected String[] getClusterURIs() {
		return config.getClusterURIs();
	}


	public String getClusterHostName() {
		String hostName = config.getClusterProperties().getProperty(PROP_CLUSTER_HOST, NetworkUtils.DEFAULT_LOCAL_HOST_ADDRESS);
		try {
			//noinspection ResultOfMethodCallIgnored
			InetAddress.getByName(hostName);
		} catch (Exception e) {
			CoreLogger.LOGGER.error("The cluster host name {} is not available. Use localhost instead", hostName);
			hostName = "localhost";
		}
		return hostName;
	}

	int getClusterPort() {
		int port = config.getClusterProperties().getPropertyInt(PROP_CLUSTER_PORT);
		try {
			final InetAddress byName = InetAddress.getByName(getClusterHostName());
			port = NetworkUtils.checkPortAvailability(byName, port, 30);
		} catch (Exception e) {
			CoreLogger.LOGGER.error("The cluster port {} is failed to bind. Please check network configuration.", port);
		}
		return port;
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
