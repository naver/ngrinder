package org.ngrinder.infra.hazelcast.task;

import com.hazelcast.spring.context.SpringAware;
import net.grinder.engine.controller.AgentControllerIdentityImplementation;
import org.ngrinder.agent.service.AgentService;
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
	private final String ip;
	private final String name;

	public AgentStateTask(String ip, String name) {
		this.ip = ip;
		this.name = name;
	}

	@Autowired
	private transient AgentService agentService;

	@Autowired
	private transient AgentManager agentManager;

	@Override
	public SystemDataModel call() {
		AgentControllerIdentityImplementation identity = agentService.getAgentIdentityByIpAndName(ip, name);
		return identity != null ? agentManager.getSystemDataModel(identity) : new SystemDataModel();
	}
}
