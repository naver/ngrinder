package org.ngrinder.infra.hazelcast.task;

import com.hazelcast.spring.context.SpringAware;
import org.ngrinder.common.exception.NGrinderRuntimeException;
import org.ngrinder.perftest.service.AgentManager;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.io.Serializable;
import java.util.concurrent.Callable;

@SpringAware
public class ExternalAgentTask implements Callable<Void>, Serializable {

	private String ip;
	private int port;

	public ExternalAgentTask(String ip, int port) {
		this.ip = ip;
		this.port = port;
	}

	@Autowired
	private AgentManager agentManager;

	@Override
	public Void call() throws IOException {
		agentManager.addExternalAgent(ip, port);
		return null;
	}
}
