package net.grinder;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.util.List;

import net.grinder.common.GrinderProperties;
import net.grinder.common.processidentity.AgentIdentity;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class AgentControllerTest {
	AgentControllerServerDaemon agentControllerServerDeamon;
	AgentControllerDaemon agentControllerDaemon;
	AgentControllerDaemon agentControllerDaemon2;
	SingleConsole console1;

	public void sleep(long time) {
		try {
			Thread.sleep(time);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Before
	public void before() {
		agentControllerServerDeamon = new AgentControllerServerDaemon(2022);
		agentControllerServerDeamon.start();
		sleep(1000);
		agentControllerDaemon = new AgentControllerDaemon();
		agentControllerDaemon.run(2022);

		agentControllerDaemon2 = new AgentControllerDaemon();
		agentControllerDaemon2.run(2022);
		sleep(1000);
	}

	@After
	public void shutdown() {
		agentControllerDaemon.shutdown();
		agentControllerDaemon2.shutdown();
		sleep(1000);
		agentControllerServerDeamon.shutdown();
		sleep(1000);

		if (console1 != null) {
			console1.shutdown();
			sleep(1000);
		}
	}

	@Test
	public void testAgentStatus() throws InterruptedException {
		List<AgentIdentity> allAvailableAgents = agentControllerServerDeamon.getAllAvailableAgents();
		System.out.println(allAvailableAgents);
		assertThat(allAvailableAgents.size(), is(2));
		agentControllerServerDeamon.shutdown();
		sleep(1000);

		allAvailableAgents = agentControllerServerDeamon.getAllAvailableAgents();
		assertThat(allAvailableAgents.size(), is(2));

		agentControllerServerDeamon = new AgentControllerServerDaemon(2022);
		agentControllerServerDeamon.start();
		sleep(3000);

		allAvailableAgents = agentControllerServerDeamon.getAllAvailableAgents();
		assertThat(allAvailableAgents.size(), is(2));
	}

	@Test
	public void testStartAgent() {
		List<AgentIdentity> allAvailableAgents = agentControllerServerDeamon.getAllAvailableAgents();
		assertThat(allAvailableAgents.size(), is(2));

		// Start Console
		console1 = new SingleConsole(6372);
		console1.start();
		assertThat(console1.getAllAttachedAgentsCount(), is(0));

		sleep(1000);
		GrinderProperties grinderProperties = new GrinderProperties();
		grinderProperties.setInt(GrinderProperties.CONSOLE_PORT, 6372);
		agentControllerServerDeamon.startAgent(grinderProperties, allAvailableAgents.get(0));

		sleep(3000);
		assertThat(console1.getAllAttachedAgents().size(), is(1));

		agentControllerServerDeamon.shutdown();
		sleep(3000);
		assertThat(console1.getAllAttachedAgentsCount(), is(0));

	}

	@Test
	public void testStopAgent() {
		List<AgentIdentity> allAvailableAgents = agentControllerServerDeamon.getAllAvailableAgents();
		AgentIdentity agentIdentity = allAvailableAgents.get(0);

		SingleConsole console1 = new SingleConsole(6372);
		console1.start();
		sleep(1000);
		GrinderProperties grinderProperties = new GrinderProperties();
		grinderProperties.setInt(GrinderProperties.CONSOLE_PORT, 6372);
		agentControllerServerDeamon.startAgent(grinderProperties, agentIdentity);
		sleep(2000);
		agentControllerServerDeamon.stopAgent(agentIdentity);
		sleep(2000);
		agentControllerServerDeamon.stopAgent(agentIdentity);
		sleep(2000);

	}

	@Test
	public void testAgentControllerServerDaemonRecovery() {
		// Connect all available agents
		List<AgentIdentity> allAvailableAgents = agentControllerServerDeamon.getAllAvailableAgents();
		assertThat(allAvailableAgents.size(), is(2));
		SingleConsole console1 = new SingleConsole(6372);
		console1.start();
		sleep(1000);
		GrinderProperties grinderProperties = new GrinderProperties();
		grinderProperties.setInt(GrinderProperties.CONSOLE_PORT, 6372);
		agentControllerServerDeamon.startAgent(grinderProperties, allAvailableAgents.get(0));
		agentControllerServerDeamon.startAgent(grinderProperties, allAvailableAgents.get(1));
		sleep(1000);
		assertThat(console1.getAllAttachedAgents().size(), is(2));

		// Shutdown agent controller
		agentControllerServerDeamon.shutdown();
		sleep(3000);

		assertThat(console1.getAllAttachedAgents().size(), is(0));

		// Then start again
		agentControllerServerDeamon = new AgentControllerServerDaemon(2022);
		agentControllerServerDeamon.start();
		sleep(2000);

		// See the agent controller is attached automatically
		assertThat(agentControllerServerDeamon.getAllAttachedAgentsCount(), is(2));
		sleep(2000);
		allAvailableAgents = agentControllerServerDeamon.getAllAvailableAgents();

		// If we restart agents
		agentControllerServerDeamon.startAgent(grinderProperties, allAvailableAgents.get(0));
		agentControllerServerDeamon.startAgent(grinderProperties, allAvailableAgents.get(1));
		sleep(2000);

		// They should be successfully attached into the existing console.
		assertThat(console1.getAllAttachedAgents().size(), is(2));

	}
}
