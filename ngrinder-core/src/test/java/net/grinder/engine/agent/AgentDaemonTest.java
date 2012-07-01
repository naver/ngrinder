package net.grinder.engine.agent;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import net.grinder.SingleConsole;
import net.grinder.AgentDaemon;
import net.grinder.AgentDaemon.AgentShutDownListener;

public class AgentDaemonTest {
	private SingleConsole console2;

	@Before
	public void before() {
		console2 = new SingleConsole(1223);
		console2.start();
		sleep(1000);
	}

	@After
	public void after() {
		console2.shutdown();

	}

	@Test
	public void testDifferentPort() throws InterruptedException {
		AgentDaemon agent = new AgentDaemon();
		agent.run(1223);
		sleep(1000);
	}

	public void sleep(int howLong) {
		try {
			Thread.sleep(howLong);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Test
	public void isDeadWell() {
		AgentDaemon agent = new AgentDaemon();
		agent.run(1223);
		agent.addListener(new AgentShutDownListener() {
			public void shutdownAgent() {
				System.out.println("WOW IT DEAD!!");
			}
		});
		sleep(1000);
		console2.shutdown();
		sleep(5000);

		console2 = new SingleConsole(1223);
		console2.start();
		System.out.println("Restart");
		sleep(1000);
	}
}
