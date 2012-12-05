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
import java.io.InputStream;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import net.sf.ehcache.CacheManager;
import net.sf.ehcache.config.CacheConfiguration;
import net.sf.ehcache.config.CacheConfiguration.CacheEventListenerFactoryConfiguration;
import net.sf.ehcache.config.ConfigurationFactory;
import net.sf.ehcache.config.FactoryConfiguration;
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

import com.google.common.net.InetAddresses;

/**
 * Class description.
 * 
 * @author Mavlarn
 * @since 3.1
 */
@Component
public class DynamicCacheConfig {

	@Autowired
	private Config config;

	/**
	 * Create cache manager dynamically according to the configuration. Because we cann't add a
	 * cluster peer provider dynamically.
	 * 
	 * <pre>
	 * &lt;cache name=&quot;region_list&quot; maxElementsInMemory=&quot;1000&quot; eternal=&quot;true&quot; overflowToDisk=&quot;false&quot;&gt;
	 * 	&lt;cacheEventListenerFactory class=&quot;net.sf.ehcache.distribution.RMICacheReplicatorFactory&quot; /&gt;
	 * 	&lt;bootstrapCacheLoaderFactory class=&quot;net.sf.ehcache.distribution.RMIBootstrapCacheLoaderFactory&quot; /&gt;
	 * &lt;/cache&gt;
	 * &lt;cache name=&quot;running_agent_infos&quot; maxElementsInMemory=&quot;1000&quot; eternal=&quot;true&quot; overflowToDisk=&quot;false&quot;&gt;
	 * 	&lt;cacheEventListenerFactory class=&quot;net.sf.ehcache.distribution.RMICacheReplicatorFactory&quot; /&gt;
	 * &lt;/cache&gt;
	 * &lt;cache name=&quot;running_statistics&quot; maxElementsInMemory=&quot;1000&quot; eternal=&quot;true&quot; overflowToDisk=&quot;false&quot;&gt;
	 * 	&lt;cacheEventListenerFactory class=&quot;net.sf.ehcache.distribution.RMICacheReplicatorFactory&quot; /&gt;
	 * &lt;/cache&gt;
	 * &lt;cache name=&quot;users&quot; maxElementsInMemory=&quot;100&quot; eternal=&quot;false&quot;
	 * 	overflowToDisk=&quot;false&quot; timeToIdleSeconds=&quot;900&quot; timeToLiveSeconds=&quot;1800&quot;&gt;
	 * 	&lt;cacheEventListenerFactory class=&quot;net.sf.ehcache.distribution.RMICacheReplicatorFactory&quot; /&gt;
	 * &lt;/cache&gt;
	 * &lt;cache name=&quot;file_entry_search_cache&quot; maxElementsInMemory=&quot;100&quot; eternal=&quot;false&quot;
	 * 	   overflowToDisk=&quot;false&quot; timeToIdleSeconds=&quot;60&quot; timeToLiveSeconds=&quot;60&quot;&gt;
	 * 	&lt;cacheEventListenerFactory class=&quot;net.sf.ehcache.distribution.RMICacheReplicatorFactory&quot; /&gt;
	 * &lt;/cache&gt;
	 * </pre>
	 * 
	 * @return EhCacheCacheManager bean
	 * @throws IOException
	 */
	@SuppressWarnings("rawtypes")
	@Bean(name = "cacheManager")
	public EhCacheCacheManager dynamicCacheManager() {
		EhCacheCacheManager cacheManager = new EhCacheCacheManager();
		net.sf.ehcache.config.Configuration cacheManagerConfig;
		InputStream inputStream = null;
		try {
			if (!config.isCluster()) {
				CoreLogger.LOGGER.info("In no cluster mode.");
				inputStream = new ClassPathResource("ehcache.xml").getInputStream();
				cacheManagerConfig = ConfigurationFactory.parseConfiguration(inputStream);
			} else {
				CoreLogger.LOGGER.info("In cluster mode.");
				inputStream = new ClassPathResource("ehcache-dist.xml").getInputStream();
				cacheManagerConfig = ConfigurationFactory.parseConfiguration(inputStream);
				FactoryConfiguration peerProviderConfig = new FactoryConfiguration();
				peerProviderConfig.setClass(RMICacheManagerPeerProviderFactory.class.getName());
				int clusterListenerPort = config.getSystemProperties().getPropertyInt(
								NGrinderConstants.NGRINDER_PROP_CLUSTER_LISTENER_PORT,
								Config.NGRINDER_DEFAULT_CLUSTER_LISTENER_PORT);
				List<String> replicatedCacheNames = getReplicatedCacheNames(cacheManagerConfig);
				// rmiUrls=//10.34.223.148:40003/distributed_map|//10.34.63.28:40003/distributed_map
				List<String> uris = new ArrayList<String>();
				for (String ip : config.getClusterURIs()) {
					// Verify it's ip.
					if (!InetAddresses.isInetAddress(ip)) {
						try {
							ip = InetAddress.getByName(ip).getHostAddress();
						} catch (UnknownHostException e) {
							CoreLogger.LOGGER.error("{} is not valid ip or host name", ip);
							continue;
						}
					}
					if (ip.equals(config.getCurrentIP())) {
						continue;
					}
					for (String cacheName : replicatedCacheNames) {
						uris.add(String.format("%s:%d/%s", ip, clusterListenerPort, cacheName));
					}
				}
				String properties = "peerDiscovery=manual,rmiUrls=//" + StringUtils.join(uris, "|");
				peerProviderConfig.setProperties(properties);
				cacheManagerConfig.addCacheManagerPeerProviderFactory(peerProviderConfig);
				CoreLogger.LOGGER.info("clusterURLs:{}", properties);
			}
			cacheManagerConfig.setName("cacheManager");
			CacheManager mgr = new CacheManager(cacheManagerConfig);
			cacheManager.setCacheManager(mgr);
		} catch (IOException e) {
			CoreLogger.LOGGER.error("Error while setting up cache", e);
		} finally {
			IOUtils.closeQuietly(inputStream);
		}
		return cacheManager;
	}

	@SuppressWarnings("unchecked")
	private List<String> getReplicatedCacheNames(net.sf.ehcache.config.Configuration cacheManagerConfig) {
		Map<String, CacheConfiguration> cacheConfigurations = cacheManagerConfig.getCacheConfigurations();
		List<String> replicatedCacheNames = new ArrayList<String>();
		for (Map.Entry<String, CacheConfiguration> eachConfig : cacheConfigurations.entrySet()) {
			for (CacheEventListenerFactoryConfiguration each : ((List<CacheEventListenerFactoryConfiguration>) eachConfig
							.getValue().getCacheEventListenerConfigurations())) {
				if (each.getFullyQualifiedClassPath().equals("net.sf.ehcache.distribution.RMICacheReplicatorFactory")) {
					replicatedCacheNames.add(eachConfig.getKey());
				}
			}
		}
		return replicatedCacheNames;
	}
}
