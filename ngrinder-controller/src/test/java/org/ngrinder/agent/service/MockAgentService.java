package org.ngrinder.agent.service;

import net.grinder.SingleConsole;
import net.grinder.common.GrinderProperties;
import org.ngrinder.agent.repository.AgentManagerRepository;
import org.ngrinder.agent.repository.ConnectionRepository;
import org.ngrinder.agent.store.AgentInfoStore;
import org.ngrinder.infra.config.Config;
import org.ngrinder.infra.hazelcast.HazelcastService;
import org.ngrinder.infra.hazelcast.topic.subscriber.TopicSubscriber;
import org.ngrinder.infra.schedule.ScheduledTaskService;
import org.ngrinder.model.AgentInfo;
import org.ngrinder.model.User;
import org.ngrinder.perftest.service.AgentManager;
import org.ngrinder.region.service.RegionService;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.util.Set;

import static org.ngrinder.common.constant.CacheConstants.CACHE_RECENTLY_USED_AGENTS;

@Profile("unit-test")
@Service
public class MockAgentService extends AgentService {

	private HazelcastService hazelcastService;

	public MockAgentService(AgentManager agentManager, AgentManagerRepository agentManagerRepository, Config config, RegionService regionService, HazelcastService hazelcastService, TopicSubscriber topicSubscriber, AgentInfoStore agentInfoStore, ScheduledTaskService scheduledTaskService, ConnectionRepository connectionRepository) {
		super(agentManager, agentManagerRepository, config, regionService, hazelcastService, topicSubscriber, agentInfoStore, scheduledTaskService, connectionRepository);
		this.hazelcastService = hazelcastService;
	}

	/**
	 * Assign the agents on the given console.
	 *
	 * @param user              user
	 * @param singleConsole     {@link SingleConsole} to which agents will be assigned
	 * @param grinderProperties {@link GrinderProperties} to be distributed.
	 * @param agentCount        the count of agents.
	 */
	public synchronized void runAgent(User user, final SingleConsole singleConsole,
									  final GrinderProperties grinderProperties, final Integer agentCount) {
		final Set<AgentInfo> allFreeAgents = getAllAttachedFreeApprovedAgentsForUser(user.getUserId());
		final Set<AgentInfo> necessaryAgents = selectAgent(user, allFreeAgents, agentCount);

		hazelcastService.put(CACHE_RECENTLY_USED_AGENTS, user.getUserId(), necessaryAgents);

		LOGGER.info("{} agents are starting for user {}", agentCount, user.getUserId());
		for (AgentInfo agentInfo : necessaryAgents) {
			LOGGER.info("- Agent {}", agentInfo.getName());
		}
		agentManager.runAgent(singleConsole, grinderProperties, necessaryAgents);
	}
}
