package net.grinder.engine.agent;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import net.grinder.AgentDaemon;
import net.grinder.SingleConsole;
import net.grinder.util.thread.Condition;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.ngrinder.AbstractMuliGrinderTestBase;

public class AgentDaemonTest extends AbstractMuliGrinderTestBase {

	private SingleConsole console1;
	private Integer consolePort;

	@Before
	public void before() {
		consolePort = getFreePort();
		console1 = new SingleConsole(consolePort);
		console1.start();
		sleep(1000);
	}

	@After
	public void after() {
		console1.shutdown();
	}

	@Test
	public void testDifferentPort() throws InterruptedException {
		// Try to connect any port
		AgentDaemon agent = new AgentDaemon();
		agent.run(getFreePort());
		sleep(1000);
	}

	Condition condition = new Condition();

	@Test(timeout = 3000)
	public void testIfAgentDeadWellWhenConsoleIsShutdowned() {
		// After connecting agent daemon,
		AgentDaemon agent = new AgentDaemon();
		agent.run(console1.getConsolePort());
		agent.addListener(new AgentShutDownSynchronizeListener(condition));
		// Shutdown console
		console1.shutdown();
		waitOnCondition(condition, 2100);
	}

	@Test
	public void testAgentReconnect() {
		// After connecting agent daemon,
		AgentDaemon agent = new AgentDaemon();
		agent.run(console1.getConsolePort());
		agent.addListener(new AgentShutDownSynchronizeListener(condition));
		// Shutdown console
		sleep(4000);
		assertThat(console1.getAllAttachedAgentsCount(), is(1));

		// shutdown twice
		agent.shutdown();
		sleep(3000);
		assertThat(console1.getAllAttachedAgentsCount(), is(0));
		agent.shutdown();
		sleep(3000);
		assertThat(console1.getAllAttachedAgentsCount(), is(0));

		// Even shutdown twice, agent is well connected into console.
		agent.run(console1.getConsolePort());
		sleep(2000);

		assertThat(console1.getAllAttachedAgentsCount(), is(1));

	}
}
