package net.grinder.engine.agent;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import junit.framework.Assert;
import net.grinder.SingleConsole;
import net.grinder.AgentDaemon;
import net.grinder.common.GrinderException;
import net.grinder.common.processidentity.AgentIdentity;
import net.grinder.console.common.ConsoleException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class ConsoleExTest {

	AgentDaemon agentThread1;
	AgentDaemon agentThread2;
	AgentDaemon agentThread3;

	SingleConsole console1;
	SingleConsole console2;

	@Before
	public void before() throws ConsoleException, InterruptedException {
		System.out.println(Thread.activeCount());
		console1 = new SingleConsole(6372);
		System.out.println(Thread.activeCount());
		console2 = new SingleConsole(1223);
		console1.start();
		Thread.sleep(1000);
		System.out.println(Thread.activeCount());
		console2.start();
		Thread.sleep(1000);
	}

	@After
	public void after() throws InterruptedException {
		console1.shutdown();
		console2.shutdown();
		agentThread1.shutdown();
		agentThread2.shutdown();
		agentThread3.shutdown();
		Thread.sleep(3000);
		System.out.println(Thread.activeCount());
		console1.start();
		Thread.sleep(3000);
		System.out.println(Thread.activeCount());
		console1.shutdown();
	}

	@Test
	public void testConsole() throws GrinderException, InterruptedException {
		System.out.println(Thread.activeCount());
		agentThread1 = new AgentDaemon();
		agentThread1.run(6372);
		agentThread2 = new AgentDaemon();
		agentThread2.run(6372);
		// Wait until all agents start. They will connect main console.
		Thread.sleep(4000);
		System.out.println(Thread.activeCount());

		Assert.assertEquals("There must be 2 agents connecting console1", 2,
				console1.getAllAttachedAgents().size());

		// if we shut down one agent.

		agentThread2.shutdown();
		Thread.sleep(4500);
		Assert.assertEquals("There must be 1 agents connecting console1", 1,
				console1.getAllAttachedAgents().size());

		agentThread2.run(6372);
		Thread.sleep(4000);
		Assert.assertEquals("There must be 2 agents connecting console1", 2,
				console1.getAllAttachedAgents().size());

		// enable one agent more.
		agentThread3 = new AgentDaemon();
		agentThread3.run(1223);

		Thread.sleep(4000);

		// Now it should be 2 agents
		Assert.assertEquals("There must be 2 agents connecting console1", 2,
				console1.getAllAttachedAgents().size());

		// Now it should be 1 agents on console2
		Assert.assertEquals("There must be 1 agents connecting console2", 1,
				console2.getAllAttachedAgents().size());

		// if we make one agent to connect child console.
		List<AgentIdentity> agents = console1.getAllAttachedAgents();
		Set<AgentIdentity> identitySet = new HashSet<AgentIdentity>();
		identitySet.add(agents.get(0));
		System.out.println(agents);
		System.out.println(Thread.activeCount());

		console1.shutdown();
		console1 = new SingleConsole(6372);
		console1.start();
		System.out.println(Thread.activeCount());
		Thread.sleep(2000);
		console1.shutdown();
		Thread.sleep(1000);
		System.out.println(Thread.activeCount());

	}
}
