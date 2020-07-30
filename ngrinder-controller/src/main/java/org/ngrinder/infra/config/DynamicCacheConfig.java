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

import com.github.benmanes.caffeine.cache.Caffeine;
import com.hazelcast.config.*;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.ITopic;
import com.hazelcast.spi.merge.LatestUpdateMergePolicy;
import com.hazelcast.spring.cache.HazelcastCacheManager;
import com.hazelcast.spring.context.SpringManagedContext;
import lombok.RequiredArgsConstructor;
import net.grinder.util.NetworkUtils;
import org.ngrinder.common.constant.ClusterConstants;
import org.ngrinder.infra.hazelcast.topic.message.TopicEvent;
import org.ngrinder.infra.hazelcast.topic.subscriber.TopicSubscriber;
import org.springframework.cache.Cache;
import org.springframework.cache.caffeine.CaffeineCache;
import org.springframework.cache.support.CompositeCacheManager;
import org.springframework.cache.support.SimpleCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.net.InetAddress;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static java.util.concurrent.TimeUnit.SECONDS;
import static net.grinder.util.NetworkUtils.DEFAULT_LOCAL_HOST_ADDRESS;
import static net.grinder.util.NetworkUtils.selectLocalIp;
import static org.ngrinder.common.constant.CacheConstants.*;
import static org.ngrinder.infra.logger.CoreLogger.LOGGER;

/**
 * Dynamic cache configuration. it creates local cache or dist cache.
 *
 * @since 3.1
 */
@Configuration
@RequiredArgsConstructor
public class DynamicCacheConfig implements ClusterConstants {

	private final Config config;

	private static final int DAY = 24 * 60 * 60;
	private static final int HOUR = 60 * 60;
	private static final int MIN = 60;

	private static final String HAZELCAST_PARTITION_COUNT = "hazelcast.partition.count";

	@Bean
	public org.springframework.cache.CacheManager cacheManager() {
		return new CompositeCacheManager(createLocalCacheManager(), createDistCacheManager());
	}

	private SimpleCacheManager createLocalCacheManager() {
		SimpleCacheManager cacheManager = new SimpleCacheManager();
		List<Cache> caches = new ArrayList<>();
		for (Map.Entry<String, Caffeine<Object, Object>> each : cacheConfigMap().getCaffeineCacheConfig().entrySet()) {
			caches.add(new CaffeineCache(each.getKey(), each.getValue().build()));
		}
		cacheManager.setCaches(caches);
		cacheManager.initializeCaches();
		return cacheManager;
	}

	private HazelcastCacheManager createDistCacheManager() {
		return new HazelcastCacheManager(hazelcastInstance());
	}

	@Bean
	public SpringManagedContext managedContext() {
		return new SpringManagedContext();
	}

	@Bean
	public HazelcastInstance hazelcastInstance() {
		com.hazelcast.config.Config hazelcastConfig = new com.hazelcast.config.Config("nGrinder");
		hazelcastConfig.setManagedContext(managedContext());
		hazelcastConfig.getMemberAttributeConfig().setAttributes(getClusterMemberAttributes());
		hazelcastConfig.setMapConfigs(cacheConfigMap().getHazelcastCacheConfigs());
		hazelcastConfig.addExecutorConfig(getExecutorConfig(REGION_EXECUTOR_SERVICE_NAME));
		hazelcastConfig.addExecutorConfig(getExecutorConfig(AGENT_EXECUTOR_SERVICE_NAME));
		hazelcastConfig.addTopicConfig(getTopicConfig());
		NetworkConfig networkConfig = hazelcastConfig.getNetworkConfig();
		networkConfig.setPort(getClusterPort()).setPortAutoIncrement(false);

		JoinConfig join = networkConfig.getJoin();
		join.getMulticastConfig().setEnabled(false);

		if (isClustered() && getClusterURIs() != null && getClusterURIs().length > 0) {
			TcpIpConfig tcpIpConfig = join.getTcpIpConfig();
			tcpIpConfig.setEnabled(true);
			tcpIpConfig.setMembers(Arrays.asList(getClusterURIs()));
			networkConfig.setPublicAddress(selectLocalIp(Arrays.asList(getClusterURIs())));
		}

		if (!isClustered()) {
			hazelcastConfig.setProperty(HAZELCAST_PARTITION_COUNT, "1");
		}

		HazelcastInstance hazelcastInstance = Hazelcast.newHazelcastInstance(hazelcastConfig);
		ITopic<TopicEvent> topic = hazelcastInstance.getTopic(AGENT_TOPIC_NAME);
		topic.addMessageListener(topicSubscriber());
		return hazelcastInstance;
	}

	private ExecutorConfig getExecutorConfig(String regionExecutorService) {
		ExecutorConfig config = new ExecutorConfig();
		config.setName(regionExecutorService);
		return config;
	}

	private TopicConfig getTopicConfig() {
		TopicConfig topicConfig = new TopicConfig();
		topicConfig.setGlobalOrderingEnabled(true);
		topicConfig.setStatisticsEnabled(true);
		topicConfig.setName(AGENT_TOPIC_NAME);
		return topicConfig;
	}

	private Map<String, Object> getClusterMemberAttributes() {
		Map<String, Object> attributes = new HashMap<>();
		attributes.put(REGION_ATTR_KEY, config.getRegion());
		return attributes;
	}

	@Bean
	public TopicSubscriber topicSubscriber() {
		return new TopicSubscriber();
	}

	@SuppressWarnings("PointlessArithmeticExpression")
	@Bean
	public CacheConfigHolder cacheConfigMap() {
		CacheConfigHolder cm = new CacheConfigHolder();
		cm.addDistMap(DIST_MAP_NAME_SAMPLING, 15);
		cm.addDistMap(DIST_MAP_NAME_MONITORING, 15);
		cm.addDistMap(DIST_MAP_NAME_AGENT, 10);

		cm.addDistCache(CACHE_USERS, 30, 300);
		cm.addDistCache(CACHE_FILE_ENTRIES, 1 * HOUR + 40 * MIN, 300);

		cm.addLocalCache(CACHE_GITHUB_SCRIPTS, 5 * MIN, 300);
		cm.addLocalCache(CACHE_RIGHT_PANEL_ENTRIES, 1 * DAY, 1);
		cm.addLocalCache(CACHE_LEFT_PANEL_ENTRIES, 1 * DAY, 1);
		cm.addLocalCache(CACHE_CURRENT_PERFTEST_STATISTICS, 5, 1);
		cm.addLocalCache(CACHE_GITHUB_IS_MAVEN_GROOVY, 5 * MIN, 300);
		cm.addLocalCache(CACHE_RECENTLY_USED_AGENTS, 1 * DAY, 100);
		return cm;
	}

	static class CacheConfigHolder {
		private final Map<String, MapConfig> hazelcastCacheConfigs = new ConcurrentHashMap<>();
		private final Map<String, Caffeine<Object, Object>> caffeineCacheConfig = new ConcurrentHashMap<>();

		void addLocalCache(String cacheName, int timeout, int count) {
			Caffeine<Object, Object> cacheBuilder = Caffeine.newBuilder()
				.maximumSize(count).expireAfterWrite(timeout, SECONDS);
			caffeineCacheConfig.put(cacheName, cacheBuilder);
		}

		void addDistMap(String cacheName, int timeout) {
			MapConfig mapConfig = createDistMapConfig(cacheName, timeout);
			hazelcastCacheConfigs.put(cacheName, mapConfig);
		}

		void addDistCache(String cacheName, int timeout, int count) {
			MapConfig mapConfig = createDistMapConfig(cacheName, timeout);

			NearCacheConfig nearCacheConfig = new NearCacheConfig(cacheName);
			nearCacheConfig.setTimeToLiveSeconds(timeout);

			if (count > 0) {
				mapConfig.setEvictionPolicy(EvictionPolicy.LRU)
					.getMaxSizeConfig()
					.setSize(count)
					.setMaxSizePolicy(MaxSizeConfig.MaxSizePolicy.PER_NODE);

				nearCacheConfig.getEvictionConfig()
					.setSize(count)
					.setMaximumSizePolicy(EvictionConfig.MaxSizePolicy.ENTRY_COUNT)
					.setEvictionPolicy(EvictionPolicy.LRU);
			}

			mapConfig.setNearCacheConfig(nearCacheConfig);
			hazelcastCacheConfigs.put(cacheName, mapConfig);
		}

		private MapConfig createDistMapConfig(String cacheName, int timeout) {
			MapConfig mapConfig = new MapConfig(cacheName);
			mapConfig.getMergePolicyConfig().setPolicy(LatestUpdateMergePolicy.class.getName());
			mapConfig.setTimeToLiveSeconds(timeout);
			return mapConfig;
		}

		Map<String, MapConfig> getHazelcastCacheConfigs() {
			return hazelcastCacheConfigs;
		}

		Map<String, Caffeine<Object, Object>> getCaffeineCacheConfig() {
			return caffeineCacheConfig;
		}
	}

	protected boolean isClustered() {
		return config.isClustered();
	}

	protected String[] getClusterURIs() {
		return config.getClusterURIs();
	}

	private String getClusterHostName() {
		String hostName = config.getClusterProperties().getProperty(PROP_CLUSTER_HOST, DEFAULT_LOCAL_HOST_ADDRESS);
		try {
			//noinspection ResultOfMethodCallIgnored
			InetAddress.getByName(hostName);
		} catch (Exception e) {
			LOGGER.error("The cluster host name {} is not available. Use localhost instead", hostName);
			hostName = "localhost";
		}
		return hostName;
	}

	private int getClusterPort() {
		int port = config.getClusterProperties().getPropertyInt(PROP_CLUSTER_PORT);
		try {
			final InetAddress byName = InetAddress.getByName(getClusterHostName());
			port = NetworkUtils.checkPortAvailability(byName, port, 30);
		} catch (Exception e) {
			LOGGER.error("The cluster port {} is failed to bind. Please check network configuration.", port);
		}
		return port;
	}

}
