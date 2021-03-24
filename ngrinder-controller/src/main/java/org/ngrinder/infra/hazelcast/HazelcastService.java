package org.ngrinder.infra.hazelcast;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IExecutorService;
import com.hazelcast.map.IMap;
import com.hazelcast.cluster.Member;
import lombok.extern.slf4j.Slf4j;
import org.ngrinder.common.exception.NGrinderRuntimeException;
import org.ngrinder.infra.hazelcast.topic.message.TopicEvent;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import lombok.RequiredArgsConstructor;

import static org.ngrinder.common.constant.CacheConstants.REGION_ATTR_KEY;
import static org.ngrinder.common.util.Preconditions.checkNotNull;

/**
 * For support data clustering using hazelcast instance.
 *
 * @since 3.5.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class HazelcastService {

	private final HazelcastInstance hazelcastInstance;

	protected Member findClusterMember(String region) {
		Set<Member> clusterMember = hazelcastInstance.getCluster().getMembers();
		for (Member member: clusterMember) {
			if (isSupportedRegion(member, region)) {
				return member;
			}
		}
		throw new IllegalArgumentException(region + " is not clustered region.");
	}

	private boolean isSupportedRegion(Member member, String region) {
		return member.getAttributes().containsKey(REGION_ATTR_KEY) && region.equals(member.getAttributes().get(REGION_ATTR_KEY));
	}

	public <T> T submitToRegion(String executorName, Callable<T> task, String region) {
		Member member = findClusterMember(region);
		IExecutorService executorService = hazelcastInstance.getExecutorService(executorName);
		Future<T> future = executorService.submitToMember(task, member);
		try {
			return future.get();
		} catch (InterruptedException | ExecutionException e) {
			log.error("Error while running task in region [{}] {}", region, e.getMessage());
			throw new NGrinderRuntimeException(e);
		}
	}

	public <T> List<T> submitToAllRegion(String executorName, Callable<T> task) {
		List<T> results = new ArrayList<>();
		IExecutorService executorService = hazelcastInstance.getExecutorService(executorName);
		Map<Member, Future<T>> futures = executorService.submitToAllMembers(task);
		for (Future<T> future : futures.values()) {
			try {
				T result = future.get();
				results.add(result);
			} catch (InterruptedException | ExecutionException e) {
				log.error("Error while inquire region info. {}", e.getMessage());
			}
		}
		return results;
	}

	public void publish(String topic, TopicEvent event) {
		hazelcastInstance.getTopic(topic).publish(event);
	}

	public void put(String map, Object key, Object value) {
		hazelcastInstance.getMap(map).put(key, value);
	}

	public void delete(String map, Object key) {
		hazelcastInstance.getMap(map).delete(key);
	}

	public List getValuesAsList(String map) {
		return new ArrayList<>(hazelcastInstance.getMap(map).values());
	}

	public <K, V> V get(String map, K key) {
		IMap<K, V> distMap = hazelcastInstance.getMap(map);
		checkNotNull(distMap, "Cache(" + map +") is not exist");
		return distMap.get(key);
	}

	public <K, V> V getOrDefault(String map, K key, V defaultValue) {
		V value = get(map, key);
		return value == null ? defaultValue : value;
	}
}
