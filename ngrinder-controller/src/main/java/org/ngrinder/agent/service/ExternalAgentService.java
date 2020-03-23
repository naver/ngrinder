package org.ngrinder.agent.service;

import lombok.RequiredArgsConstructor;
import org.ngrinder.perftest.service.ExternalAgentManager;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ExternalAgentService {
	private final ExternalAgentManager externalAgentManager;

	public void connectToAgent(String host, int port) {
		externalAgentManager.connectToAgent(host, port);
	}
}
