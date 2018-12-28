package org.ngrinder.infra.hazelcast.task;

import com.hazelcast.spring.context.SpringAware;
import org.ngrinder.agent.service.AgentManagerService;
import org.ngrinder.monitor.controller.model.SystemDataModel;
import org.ngrinder.perftest.service.AgentManager;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.Serializable;
import java.util.concurrent.Callable;

/**
 * Task for getting agent system data
 *
 * @since 3.5.0
 */
@SpringAware
public class AgentStateTask implements Callable<SystemDataModel>, Serializable {
	private String ip;
	private String name;

	public AgentStateTask(String ip, String name) {
		this.ip = ip;
		this.name = name;
	}

	@Autowired
	private transient AgentManagerService agentManagerService;

	@Autowired
	private transient AgentManager agentManager;

	public SystemDataModel call() {
		return agentManager.getSystemDataModel(agentManagerService.getAgentIdentityByIpAndName(ip, name));
	}
}
