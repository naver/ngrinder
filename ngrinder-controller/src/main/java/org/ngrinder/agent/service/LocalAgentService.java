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

import org.apache.commons.collections.CollectionUtils;
import org.ngrinder.agent.repository.AgentManagerRepository;
import org.ngrinder.infra.config.Config;
import org.ngrinder.model.AgentInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import java.util.List;

import static org.ngrinder.agent.repository.AgentManagerSpecification.startWithRegion;

@Component
public class LocalAgentService {

	@Autowired
	private AgentManagerRepository agentManagerRepository;

	private static final Logger LOGGER = LoggerFactory.getLogger(LocalAgentService.class);

	@Autowired
	private Config config;

	private boolean clustered;
	private String region;

	@PostConstruct
	public void init() {
		clustered = config.isClustered();
		region = config.getRegion();
	}

	@Cacheable("local_agents")
	public List<AgentInfo> getLocalAgents() {
		LOGGER.debug("Local Cache is Updated.");
		if (clustered) {
			return agentManagerRepository.findAll(startWithRegion(region));
		} else {
			return agentManagerRepository.findAll();
		}
	}


	@Transactional
	public void updateAgents(List<AgentInfo> newAgents,
							 List<AgentInfo> updatedAgents,
							 List<AgentInfo> stateUpdatedAgents,
							 List<AgentInfo> removedAgents) {
		if (CollectionUtils.isNotEmpty(newAgents)) {
			agentManagerRepository.save(newAgents);
		}
		if (CollectionUtils.isNotEmpty(updatedAgents)) {
			agentManagerRepository.save(updatedAgents);
		}
		for (AgentInfo each : stateUpdatedAgents) {
			agentManagerRepository.updateState(each.getId(), each.getState());
		}
		if (CollectionUtils.isNotEmpty(removedAgents)) {
			agentManagerRepository.delete(removedAgents);
		}
		agentManagerRepository.flush();
	}

	@Transactional
	public void doSthInTransaction(Runnable runnable) {
		runnable.run();
	}

	@CacheEvict("local_agents")
	public void expireCache() {

	}
}
