/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.grinder.engine.agent;

import net.grinder.AgentDaemon;
import net.grinder.SingleConsole;
import net.grinder.common.GrinderException;
import net.grinder.console.common.ConsoleException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.ngrinder.AbstractMultiGrinderTestBase;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class SingleConsoleThreadTest extends AbstractMultiGrinderTestBase {

	AgentDaemon agentThread1;
	AgentDaemon agentThread2;
	AgentDaemon agentThread3;

	SingleConsole console1;
	SingleConsole console2;
	int startThreadCount;

	@Before
	public void before() throws ConsoleException, InterruptedException {
		sleep(5000);
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
		sleep(10000);
		assertThat("After all agents and console shutdown, the active thread count should be same",
						Thread.activeCount(), is(startThreadCount));

	}

	@Test
	public void testConsole() {
		agentThread1 = new AgentDaemon(agentConfig1);
		agentThread1.run(console1.getConsolePort());
		agentThread2 = new AgentDaemon(agentConfig2);
		agentThread2.run(console1.getConsolePort());

		// Wait until all agents are started. They will connect main console.
		waitAndAssertUntilAgentAttachedTo(console1, 2, 20);

		// if we shut down one agent.
		agentThread2.addListener(new AgentShutDownSynchronizeListener(condition));
		agentThread2.shutdown();
		waitOnCondition(condition, 5000);

		waitAndAssertUntilAgentAttachedTo(console1, 1, 5);

		// If we connect console1 again
		agentThread2.run(console1.getConsolePort());

		waitAndAssertUntilAgentAttachedTo(console1, 2, 10);

		// enable one agent more.
		agentThread3 = new AgentDaemon(agentConfig3);
		agentThread3.run(console2.getConsolePort());

		// Now it should be 2 agents
		waitAndAssertUntilAgentAttachedTo(console1, 2, 10);

		// Now it should be 1 agents on console2
		waitAndAssertUntilAgentAttachedTo(console2, 1, 10);

		console1.shutdown();
		console1 = new SingleConsole(getFreePort());
		console1.start();
		console1.shutdown();
	}

}
