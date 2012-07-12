package net.grinder.engine.agent;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.junit.Assert.assertThat;

import java.util.Set;

import net.grinder.AgentDaemon;
import net.grinder.SingleConsole;
import net.grinder.common.GrinderException;
import net.grinder.console.common.ConsoleException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.ngrinder.AbstractMuliGrinderTestBase;

public class ConsoleExTest extends AbstractMuliGrinderTestBase {

	AgentDaemon agentThread1;
	AgentDaemon agentThread2;
	AgentDaemon agentThread3;

	SingleConsole console1;
	SingleConsole console2;
	int startThreadCount;

	@Before
	public void before() throws ConsoleException, InterruptedException {
		startThreadCount = Thread.activeCount();
		console1 = new SingleConsole(getFreePort());
		console2 = new SingleConsole(getFreePort());
		console1.start();
		console2.start();
	}

	@After
	public void after() throws InterruptedException {
		console1.shutdown();
		console2.shutdown();
		agentThread1.shutdown();
		agentThread2.shutdown();
		agentThread3.shutdown();
		sleep(2000);
		Set<Thread> threadSet = Thread.getAllStackTraces().keySet();
		for (Thread each : threadSet) {
			System.out.println(each.getName());
		}
		sleep(2000);
		assertThat("Aftre shutdowning all agents and console, the active thread count should be same",
				Thread.activeCount(), is(startThreadCount));
		
	}

	@Test
	public void testConsole() throws GrinderException, InterruptedException {
		agentThread1 = new AgentDaemon();
		agentThread1.run(console1.getConsolePort());
		agentThread2 = new AgentDaemon();
		agentThread2.run(console1.getConsolePort());
		// Wait until all agents are started. They will connect main console.
		sleep(4000);

		assertThat("There must be 2 agents connecting console1", console1.getAllAttachedAgents(), hasSize(2));

		// if we shut down one agent.
		agentThread2.addListener(new AgentShutDownSynchronizeListener(condition));
		agentThread2.shutdown();
		sleep(5000);
		waitOnCondition(condition, 5000);

		assertThat("There must be 1 agents connecting console1", console1.getAllAttachedAgents(), hasSize(1));

		// If we connect console1 again
		agentThread2.run(console1.getConsolePort());
		sleep(4000);

		assertThat("There must be 2 agents connecting console1", console1.getAllAttachedAgents(), hasSize(2));

		// enable one agent more.
		agentThread3 = new AgentDaemon();
		agentThread3.run(console2.getConsolePort());

		Thread.sleep(4000);

		// Now it should be 2 agents
		assertThat("There must be 2 agents connecting console1", console1.getAllAttachedAgents(), hasSize(2));

		// Now it should be 1 agents on console2
		assertThat("There must be 1 agents connecting console2", console2.getAllAttachedAgents(), hasSize(1));

		console1.shutdown();
		console1 = new SingleConsole(getFreePort());
		console1.start();
		console1.shutdown();

	}
}
