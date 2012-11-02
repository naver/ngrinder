package net.grinder;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.io.File;
import java.lang.reflect.Field;
import java.util.Set;

import net.grinder.common.GrinderProperties;
import net.grinder.common.processidentity.AgentIdentity;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.ngrinder.AbstractMuliGrinderTestBase;

public class AgentControllerTest extends AbstractMuliGrinderTestBase {
	AgentControllerServerDaemon agentControllerServerDeamon;
	AgentControllerDaemon agentControllerDaemon;
	AgentControllerDaemon agentControllerDaemon2;
	SingleConsole console1;
	Set<AgentIdentity> allAvailableAgents;

	@Before
	public void before() {
		File file = new File(new File("."), "native_lib");
		System.setProperty("java.library.path", file.getAbsolutePath());
		// set sys_paths to null
		Field sysPathsField = null;
		try {
			sysPathsField = ClassLoader.class.getDeclaredField("sys_paths");
			sysPathsField.setAccessible(true);
			sysPathsField.set(null, null);
		} catch (SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchFieldException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		agentControllerServerDeamon = new AgentControllerServerDaemon(getFreePort());
		agentControllerServerDeamon.start();

		agentControllerDaemon = new AgentControllerDaemon("127.0.0.1");
		agentControllerDaemon.setAgentConfig(agentConfig1);
		agentControllerDaemon.run(agentControllerServerDeamon.getPort());
		agentControllerDaemon2 = new AgentControllerDaemon("127.0.0.1");
		agentControllerDaemon2.setAgentConfig(agentConfig2);
		agentControllerDaemon2.run(agentControllerServerDeamon.getPort());
		sleep(2000);
		// Validate if all agents are well-attached.

		allAvailableAgents = agentControllerServerDeamon.getAllAvailableAgents();
		assertThat(allAvailableAgents.size(), is(2));
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

		// Validate all agents are well attached even after restarting agent
		// controller server
		agentControllerServerDeamon.shutdown();
		agentControllerServerDeamon = new AgentControllerServerDaemon(agentControllerServerDeamon.getPort());
		agentControllerServerDeamon.start();
		sleep(3000);

		// all agent should be re-attached
		allAvailableAgents = agentControllerServerDeamon.getAllAvailableAgents();
		assertThat(allAvailableAgents.size(), is(2));
	}

	@Test
	public void testStartAgent() {

		// Start Console
		console1 = new SingleConsole(getFreePort());
		console1.start();

		// Check there is no agents are attached.
		assertThat(console1.getAllAttachedAgentsCount(), is(0));

		// Make one agent connect to console1
		GrinderProperties grinderProperties = new GrinderProperties();
		grinderProperties.setInt(GrinderProperties.CONSOLE_PORT, console1.getConsolePort());
		AgentIdentity next = getAgentIdentity(allAvailableAgents, 0);
		agentControllerServerDeamon.startAgent(grinderProperties, next);
		sleep(2000);
		assertThat(console1.getAllAttachedAgents().size(), is(1));

		// Shutdown agent controller and see agent is detached as well
		agentControllerServerDeamon.shutdown();
		sleep(5000);
		assertThat(console1.getAllAttachedAgentsCount(), is(0));

	}

	@Test
	public void testStopAndStartAgentRepeatly() {
		// Get one agent
		AgentIdentity agentIdentity = getAgentIdentity(allAvailableAgents, 0);

		// Start console
		SingleConsole console1 = new SingleConsole(getFreePort());
		console1.start();

		// Start one agent
		GrinderProperties grinderProperties = new GrinderProperties();
		grinderProperties.setInt(GrinderProperties.CONSOLE_PORT, console1.getConsolePort());

		agentControllerServerDeamon.startAgent(grinderProperties, agentIdentity);
		sleep(3000);
		assertThat(console1.getAllAttachedAgentsCount(), is(1));

		// Stop that agent and see it's well disconnected
		agentControllerServerDeamon.stopAgent(agentIdentity);
		sleep(5000);
		assertThat(console1.getAllAttachedAgentsCount(), is(0));

		// Stop that agent and see it's well disconnected again.
		// It should be verified
		agentControllerServerDeamon.stopAgent(agentIdentity);
		sleep(5000);
		assertThat(console1.getAllAttachedAgentsCount(), is(0));

		agentControllerServerDeamon.startAgent(grinderProperties, agentIdentity);
		sleep(5000);
		assertThat(console1.getAllAttachedAgentsCount(), is(1));
	}

	@Test
	public void testAgentControllerServerDaemonRecovery() {
		sleep(2000);
		// Connect all available agents
		Set<AgentIdentity> allAvailableAgents = agentControllerServerDeamon.getAllAvailableAgents();
		assertThat(allAvailableAgents.size(), is(2));
		SingleConsole console1 = new SingleConsole(6372);
		console1.start();

		GrinderProperties grinderProperties = new GrinderProperties();
		grinderProperties.setInt(GrinderProperties.CONSOLE_PORT, console1.getConsolePort());

		agentControllerServerDeamon.startAgent(grinderProperties, getAgentIdentity(allAvailableAgents, 0));
		agentControllerServerDeamon.startAgent(grinderProperties, getAgentIdentity(allAvailableAgents, 1));
		sleep(1000);
		assertThat(console1.getAllAttachedAgents().size(), is(2));

		// Shutdown agent controller
		agentControllerServerDeamon.shutdown();
		sleep(5000);

		assertThat(console1.getAllAttachedAgents().size(), is(0));

		// Then start again
		agentControllerServerDeamon = new AgentControllerServerDaemon(agentControllerServerDeamon.getPort());
		agentControllerServerDeamon.start();
		sleep(3000);

		// See the agent controller is attached automatically
		assertThat(agentControllerServerDeamon.getAllAttachedAgentsCount(), is(2));
		sleep(2000);
		allAvailableAgents = agentControllerServerDeamon.getAllAvailableAgents();

		// If we restart agents
		agentControllerServerDeamon.startAgent(grinderProperties, getAgentIdentity(allAvailableAgents, 0));
		agentControllerServerDeamon.startAgent(grinderProperties, getAgentIdentity(allAvailableAgents, 1));
		sleep(2000);

		// They should be successfully attached into the existing console.
		assertThat(console1.getAllAttachedAgents().size(), is(2));

	}
}
