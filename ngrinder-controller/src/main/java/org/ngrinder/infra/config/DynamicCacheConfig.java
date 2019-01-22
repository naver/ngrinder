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
import com.hazelcast.spring.cache.HazelcastCacheManager;
import com.hazelcast.spring.context.SpringManagedContext;
import net.grinder.util.NetworkUtils;
import org.ngrinder.common.constant.ClusterConstants;
import org.ngrinder.infra.hazelcast.topic.message.TopicEvent;
import org.ngrinder.infra.hazelcast.topic.subscriber.TopicSubscriber;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.cache.caffeine.CaffeineCache;
import org.springframework.cache.support.CompositeCacheManager;
import org.springframework.cache.support.SimpleCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.net.InetAddress;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import static com.hazelcast.config.MaxSizeConfig.MaxSizePolicy.PER_NODE;
import static net.grinder.util.NetworkUtils.DEFAULT_LOCAL_HOST_ADDRESS;
import static net.grinder.util.NetworkUtils.selectLocalIp;
import static org.ngrinder.common.constant.CacheConstants.*;
import static org.ngrinder.infra.config.DynamicCacheConfig.CacheConfigHolder.Mode.DIST;
import static org.ngrinder.infra.config.DynamicCacheConfig.CacheConfigHolder.Mode.DIST_AND_LOCAL;
import static org.ngrinder.infra.logger.CoreLogger.LOGGER;

/**
 * Dynamic cache configuration. it creates local cache or dist cache.
 *
 * @author Mavlarn
 * @author JunHo Yoon
 * @since 3.1
 */
@Configuration
public class DynamicCacheConfig implements ClusterConstants {

	@Autowired
	private Config config;

	private static final int DAY = 24 * 60 * 60;
	private static final int HOUR = 60 * 60;
	private static final int MIN = 60;

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
		return new HazelcastCacheManager(embeddedHazelcast());
	}

	@Bean
	public SpringManagedContext managedContext() {
		return new SpringManagedContext();
	}

	@Bean
	public HazelcastInstance embeddedHazelcast() {
		com.hazelcast.config.Config hazelcastConfig = new com.hazelcast.config.Config();
		hazelcastConfig.setManagedContext(managedContext());
		hazelcastConfig.getMemberAttributeConfig().setAttributes(getClusterMemberAttributes());
		hazelcastConfig.setMapConfigs(cacheConfigMap().getHazelcastCacheConfigs());
		hazelcastConfig.addExecutorConfig(getExecutorConfig(REGION_EXECUTOR_SERVICE_NAME));
		hazelcastConfig.addExecutorConfig(getExecutorConfig(AGENT_EXECUTOR_SERVICE_NAME));
		hazelcastConfig.addTopicConfig(getTopicConfig());
		NetworkConfig networkConfig = hazelcastConfig.getNetworkConfig();
		networkConfig.setPort(getClusterPort());

		if (isClustered() && getClusterURIs() != null && getClusterURIs().length > 0) {
			JoinConfig join = networkConfig.getJoin();
			join.getAwsConfig().setEnabled(false);
			join.getMulticastConfig().setEnabled(false);
			TcpIpConfig tcpIpConfig = join.getTcpIpConfig();
			tcpIpConfig.setEnabled(true);
			tcpIpConfig.setMembers(Arrays.asList(getClusterURIs()));
			networkConfig.setPublicAddress(selectLocalIp(Arrays.asList(getClusterURIs())));
		} else {
			JoinConfig join = networkConfig.getJoin();
			join.getAwsConfig().setEnabled(false);
			join.getTcpIpConfig().setEnabled(false);
			join.getMulticastConfig().setEnabled(true);
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
		cm.addDistCache(CACHE_SAMPLING, 15, 15, DIST, 0, 0);
		cm.addDistCache(CACHE_MONITORING, 15, 15, DIST, 0, 0);

		cm.addDistCache(CACHE_USERS, 30, 100, DIST_AND_LOCAL, 30, 100);
		cm.addDistCache(CACHE_HIBERNATE_2ND_LEVEL, 30, 100, DIST_AND_LOCAL, 30, 100);
		cm.addDistCache(CACHE_FILE_ENTRIES, 1 * HOUR + 40 * MIN, 100, DIST_AND_LOCAL, 1 * HOUR + 40 * MIN, 100);

		cm.addLocalCache(CACHE_RIGHT_PANEL_ENTRIES, 1 * DAY, 2);
		cm.addLocalCache(CACHE_LEFT_PANEL_ENTRIES, 1 * DAY, 2);
		cm.addLocalCache(CACHE_CURRENT_PERFTEST_STATISTICS, 5, 1);
		cm.addLocalCache(CACHE_LOCAL_AGENTS, 1 * HOUR, 1);
		return cm;
	}

	static class CacheConfigHolder {
		enum Mode {
			DIST,
			DIST_AND_LOCAL,
		}

		private final Map<String, MapConfig> hazelcastCacheConfigs = new ConcurrentHashMap<>();
		private final Map<String, Caffeine<Object, Object>> caffeineCacheConfig = new ConcurrentHashMap<>();

		void addLocalCache(String cacheName, int timeout, int count) {
			Caffeine<Object, Object> cacheBuilder = Caffeine.newBuilder()
				.maximumSize(count).expireAfterWrite(timeout, TimeUnit.SECONDS);
			caffeineCacheConfig.put(cacheName, cacheBuilder);
		}

		void addDistCache(String cacheName, int timeout, int count, Mode mode, int nearCacheTimeout, int nearCacheCount) {
			MapConfig mapConfig = new MapConfig(cacheName);
			mapConfig.setTimeToLiveSeconds(timeout);
			mapConfig.setMaxSizeConfig(new MaxSizeConfig(count, PER_NODE));
			if (DIST_AND_LOCAL.equals(mode)) {
				NearCacheConfig nearCacheConfig = new NearCacheConfig(cacheName);
				nearCacheConfig.setTimeToLiveSeconds(nearCacheTimeout);
				nearCacheConfig.setMaxSize(nearCacheCount);
				mapConfig.setNearCacheConfig(nearCacheConfig);
			}
			hazelcastCacheConfigs.put(cacheName, mapConfig);
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

	public Config getConfig() {
		return config;
	}

	public void setConfig(Config config) {
		this.config = config;
	}
}
