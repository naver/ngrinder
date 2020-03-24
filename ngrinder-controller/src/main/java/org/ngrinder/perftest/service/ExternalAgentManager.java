package org.ngrinder.perftest.service;

import lombok.Getter;
import net.grinder.ExternalAgentController;
import net.grinder.common.processidentity.AgentIdentity;
import net.grinder.communication.CommunicationException;
import net.grinder.communication.ConnectionType;
import net.grinder.communication.Connector;
import net.grinder.engine.controller.AgentControllerIdentityImplementation;
import net.grinder.util.NetworkUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Component
public class ExternalAgentManager {
	private static final Logger LOGGER = LoggerFactory.getLogger(ExternalAgentManager.class);
	private static final ExecutorService executorService = Executors.newCachedThreadPool();

	@Getter
	private final Map<String, ExternalAgentController.ConsoleCommunication> externalAgentsMap = new ConcurrentHashMap<>();

	public void connectToAgent(String host, int port) {
		externalAgentsMap.computeIfAbsent(createKey(host, port), key -> connect(host, port));
	}

	private ExternalAgentController.ConsoleCommunication connect(String host, int port) {
		ExternalAgentController.ConsoleCommunication consoleCommunication = null;
		AgentIdentity agentIdentity = new AgentControllerIdentityImplementation(host, NetworkUtils.getIP(host));
		try {
			Connector connector = new Connector(host, port, ConnectionType.AGENT);
			consoleCommunication = new ExternalAgentController.ConsoleCommunication(connector, agentIdentity);
			executorService.submit(consoleCommunication::start);
		} catch (CommunicationException e) {
			LOGGER.error("", e);
		}
		return consoleCommunication;
	}

	private String createKey(String host, int port) {
		return host + "_" + port;
	}
}
