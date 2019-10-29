package org.ngrinder.agent.service;


import org.ngrinder.agent.repository.AgentManagerRepository;
import org.ngrinder.agent.store.AgentInfoStore;
import org.ngrinder.infra.config.Config;
import org.ngrinder.infra.hazelcast.HazelcastService;
import org.ngrinder.infra.schedule.ScheduledTaskService;
import org.ngrinder.perftest.service.AgentManager;

public class MockAgentManagerService extends AgentManagerService {

	public MockAgentManagerService(AgentManager agentManager, AgentManagerRepository agentManagerRepository,
								   Config config, AgentInfoStore agentInfoStore, ScheduledTaskService scheduledTaskService) {
		super(agentManager, agentManagerRepository, config, agentInfoStore, scheduledTaskService);
	}
}
