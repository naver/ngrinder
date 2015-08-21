package org.ngrinder.agent.service;

import org.fest.assertions.Assertions;
import org.junit.Test;
import org.ngrinder.agent.service.AgentAutoScaleService;
import org.ngrinder.agent.service.autoscale.AwsAgentAutoScaleAction;
import org.ngrinder.agent.service.autoscale.NullAgentAutoScaleAction;
import org.ngrinder.infra.config.Config;

import static org.fest.assertions.Assertions.assertThat;
import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Created by junoyoon on 15. 8. 4.
 */
public class AgentAutoScaleServiceTest {
	private AgentAutoScaleService agentAutoScaleService = new AgentAutoScaleService();

	@Test
	public void testAgentAutoScaleActionCreation() {
		// Given
		Config config = mock(Config.class);

		// When
		when(config.getAgentAutoScaleType()).thenReturn("aws");
		when(config.isAgentAutoScaleEnabled()).thenReturn(true);
		agentAutoScaleService.setConfig(config);

		// Then
		assertThat(agentAutoScaleService.createAgentAutoScaleAction()).isInstanceOf(AwsAgentAutoScaleAction.class);

		when(config.getAgentAutoScaleType()).thenReturn("");
		assertThat(agentAutoScaleService.createAgentAutoScaleAction()).isInstanceOf(NullAgentAutoScaleAction.class);

		when(config.getAgentAutoScaleType()).thenReturn("meanningless");
		assertThat(agentAutoScaleService.createAgentAutoScaleAction()).isInstanceOf(NullAgentAutoScaleAction.class);
	}
}