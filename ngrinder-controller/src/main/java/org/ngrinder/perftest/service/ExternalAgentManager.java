package org.ngrinder.perftest.service;

import lombok.Getter;
import net.grinder.ExternalAgentController;
import net.grinder.common.processidentity.AgentIdentity;
import net.grinder.communication.ConnectionType;
import net.grinder.communication.Connector;
import net.grinder.engine.controller.AgentControllerIdentityImplementation;
import net.grinder.util.NetworkUtils;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Component
public class ExternalAgentManager {
	private static final ExecutorService executorService = Executors.newCachedThreadPool();

	@Getter
	private final Map<String, ExternalAgentController.ConsoleCommunication> externalAgentsMap = new ConcurrentHashMap<>();

	public void connectToAgent(String host, int port) {
		externalAgentsMap.computeIfAbsent(createKey(host, port), key -> connect(host, port));
	}

	private ExternalAgentController.ConsoleCommunication connect(String host, int port) {
		AgentIdentity agentIdentity = new AgentControllerIdentityImplementation(host, NetworkUtils.getIP(host));
		Connector connector = new Connector(host, port, ConnectionType.AGENT);
		ExternalAgentController.ConsoleCommunication consoleCommunication = new ExternalAgentController.ConsoleCommunication(connector, agentIdentity);

		executorService.submit(consoleCommunication::start);

		return consoleCommunication;
	}

	private String createKey(String host, int port) {
		return host + "_" + port;
	}
}
