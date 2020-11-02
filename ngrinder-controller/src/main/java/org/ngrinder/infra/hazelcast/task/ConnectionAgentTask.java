package org.ngrinder.infra.hazelcast.task;

import com.hazelcast.spring.context.SpringAware;
import org.ngrinder.perftest.service.AgentManager;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.io.Serializable;
import java.util.concurrent.Callable;

@SpringAware
public class ConnectionAgentTask implements Callable<Void>, Serializable {

	private final String ip;
	private final int port;

	public ConnectionAgentTask(String ip, int port) {
		this.ip = ip;
		this.port = port;
	}

	@Autowired
	private AgentManager agentManager;

	@Override
	public Void call() throws IOException {
		agentManager.addConnectionAgent(ip, port);
		return null;
	}
}
