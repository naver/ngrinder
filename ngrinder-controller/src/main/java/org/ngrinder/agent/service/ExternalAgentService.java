package org.ngrinder.agent.service;

import lombok.RequiredArgsConstructor;
import net.grinder.ExternalAgentController;
import net.grinder.engine.controller.AgentControllerIdentityImplementation;
import org.ngrinder.agent.repository.AgentManagerRepository;
import org.ngrinder.agent.store.AgentInfoStore;
import org.ngrinder.infra.config.Config;
import org.ngrinder.model.AgentInfo;
import org.ngrinder.perftest.service.ExternalAgentManager;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static org.ngrinder.common.constant.ControllerConstants.PROP_CONTROLLER_ENABLE_AGENT_AUTO_APPROVAL;
import static org.ngrinder.common.util.TypeConvertUtils.cast;

@Service
@RequiredArgsConstructor
public class ExternalAgentService {

	private static final ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();

	private final ExternalAgentManager externalAgentManager;

	private final AgentManagerRepository agentManagerRepository;

	private final AgentInfoStore agentInfoStore;

	private final Config config;

	@PostConstruct
	public void init() {
		executorService.scheduleAtFixedRate(() ->
				externalAgentManager.getExternalAgentsMap().values().forEach(this::update),
			0, 1, TimeUnit.SECONDS);
	}

	private void update(ExternalAgentController.ConsoleCommunication consoleCommunication) {
		AgentControllerIdentityImplementation identity = cast(consoleCommunication.getAgentIdentity());

		String ip = identity.getIp();
		String name = identity.getName();

		// get approve from DB or get default approve
		boolean approved = config.getControllerProperties().getPropertyBoolean(PROP_CONTROLLER_ENABLE_AGENT_AUTO_APPROVAL);
		AgentInfo foundOne = agentManagerRepository.findByIpAndName(ip, name);
		if (foundOne != null) {
			approved = foundOne.isApproved();
		}

		AgentInfo agentInfo = new AgentInfo();
		agentInfo.setAgentIdentity(identity);
		agentInfo.setRegion(config.getRegion());
		agentInfo.setIp(ip);
		agentInfo.setName(name);
		agentInfo.setState(consoleCommunication.getState());
		agentInfo.setApproved(approved);
		agentInfo.setExternal(true);

		agentInfoStore.updateAgentInfo(createKey(identity), agentInfo);
	}

	private String createKey(AgentControllerIdentityImplementation agentIdentity) {
		return agentIdentity.getIp() + "_" + agentIdentity.getName();
	}

	public void connectToAgent(String host, int port) {
		externalAgentManager.connectToAgent(host, port);
	}
}
