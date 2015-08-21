package org.ngrinder.agent.service.autoscale;

import com.google.common.collect.Sets;
import net.grinder.common.processidentity.AgentIdentity;
import net.grinder.engine.controller.AgentControllerIdentityImplementation;
import org.apache.commons.lang3.StringUtils;
import org.dasein.cloud.compute.VirtualMachine;
import org.dasein.cloud.compute.VmState;
import org.junit.Before;
import org.junit.Test;
import org.ngrinder.agent.service.AgentAutoScaleAction;
import org.ngrinder.agent.service.AgentAutoScaleService;
import org.ngrinder.infra.config.Config;
import org.ngrinder.perftest.service.AgentManager;

import java.util.List;

import static com.google.common.collect.Lists.newArrayList;
import static org.fest.assertions.Assertions.assertThat;
import static org.fest.assertions.Index.atIndex;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


/**
 * Created by junoyoon on 15. 8. 4.
 */
public class AwsAgentAutoScaleActionTest {

	private AwsAgentAutoScaleAction awsAgentAutoScaleAction = new AwsAgentAutoScaleAction();

	@Before
	public void init() {
		Config config = mock(Config.class);
		when(config.getAgentAutoScaleRegion()).thenReturn("ap-southeast-1");
		when(config.getAgentAutoScaleIdentity()).thenReturn(System.getProperty("agent.auto_scale.identity"));
		when(config.getAgentAutoScaleCredential()).thenReturn(System.getProperty("agent.auto_scale.credential"));
		when(config.getAgentAutoScaleControllerIP()).thenReturn("176.34.4.181");
		when(config.getAgentAutoScaleControllerPort()).thenReturn("8080");
		when(config.getAgentAutoScaleDockerRepo()).thenReturn("ngrinder/agent");
		when(config.getAgentAutoScaleDockerTag()).thenReturn("3.3-p1");
		if (StringUtils.isNotBlank(System.getProperty("controller.proxy_host"))) {
			when(config.getProxyHost()).thenReturn(System.getProperty("controller.proxy_host"));
			when(config.getProxyPort()).thenReturn(Integer.parseInt(System.getProperty("controller.proxy_port")));
			System.setProperty("http.proxyHost", System.getProperty("controller.proxy_host"));
			System.setProperty("http.proxyPort", System.getProperty("controller.proxy_port"));
			System.setProperty("https.proxyHost", System.getProperty("controller.proxy_host"));
			System.setProperty("https.proxyPort", System.getProperty("controller.proxy_port"));
		}
		AgentManager agentManager = mock(AgentManager.class);
		when(agentManager.getAllFreeAgents()).thenReturn(Sets.<AgentIdentity>newHashSet(new AgentControllerIdentityImplementation("ww", "10")));
		MockScheduledTaskService scheduledTaskService = new MockScheduledTaskService();
		awsAgentAutoScaleAction.init(config, agentManager, scheduledTaskService);
	}


	@Test
	public void testActivateNodes() throws AgentAutoScaleService.NotSufficientAvailableNodeException {
		awsAgentAutoScaleAction.activateNodes(2);
	}


	private VirtualMachine createVm(String name, VmState state) {
		VirtualMachine machine = new VirtualMachine();
		machine.setName(name);
		machine.setCurrentState(state);
		return machine;
	}

}