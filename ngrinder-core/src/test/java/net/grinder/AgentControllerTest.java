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
package net.grinder;

import net.grinder.common.GrinderProperties;
import net.grinder.common.processidentity.AgentIdentity;
import net.grinder.util.NetworkUtil;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.ngrinder.AbstractMultiGrinderTestBase;

import java.util.Set;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class AgentControllerTest extends AbstractMultiGrinderTestBase {
	AgentControllerServerDaemon agentControllerServerDaemon;
	AgentControllerDaemon agentControllerDaemon;
	AgentControllerDaemon agentControllerDaemon2;
	SingleConsole console1;
	Set<AgentIdentity> allAvailableAgents;

	@Before
	public void before() {
		final int freePort = getFreePort();
		agentControllerServerDaemon = new AgentControllerServerDaemon(freePort);
		agentControllerServerDaemon.start();
		agentConfig1.setControllerPort(freePort);
		agentControllerDaemon = new AgentControllerDaemon(agentConfig1);

		agentControllerDaemon.run();
		agentConfig2.setControllerPort(freePort);
		agentControllerDaemon2 = new AgentControllerDaemon(agentConfig2);
		agentControllerDaemon2.run();
		sleep(2000);
		// Validate if all agents are well-attached.

		allAvailableAgents = agentControllerServerDaemon.getAllAvailableAgents();
		assertThat(allAvailableAgents.size(), is(2));
	}

	@After
	public void shutdown() {
		agentControllerDaemon.shutdown();
		agentControllerDaemon2.shutdown();
		sleep(1000);
		agentControllerServerDaemon.shutdown();
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
		agentControllerServerDaemon.shutdown();
		agentControllerServerDaemon = new AgentControllerServerDaemon(agentControllerServerDaemon.getPort());
		agentControllerServerDaemon.start();
		sleep(3000);

		// all agent should be re-attached
		allAvailableAgents = agentControllerServerDaemon.getAllAvailableAgents();
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
		String localHostAddress = NetworkUtil.getLocalHostAddress();
		grinderProperties.setProperty(GrinderProperties.CONSOLE_HOST, localHostAddress);
		AgentIdentity next = getAgentIdentity(allAvailableAgents, 0);
		agentControllerServerDaemon.startAgent(grinderProperties, next);
		sleep(2000);
		assertThat(console1.getAllAttachedAgents().size(), is(1));

		// Shutdown agent controller and see agent is detached as well
		agentControllerServerDaemon.shutdown();
		sleep(5000);
		assertThat(console1.getAllAttachedAgentsCount(), is(0));

	}

	@Test
	public void testStopAndStartAgentRepeatedly() {
		// Get one agent
		AgentIdentity agentIdentity = getAgentIdentity(allAvailableAgents, 0);

		// Start console
		SingleConsole console1 = new SingleConsole(getFreePort());
		console1.start();

		// Start one agent
		GrinderProperties grinderProperties = new GrinderProperties();
		grinderProperties.setInt(GrinderProperties.CONSOLE_PORT, console1.getConsolePort());

		agentControllerServerDaemon.startAgent(grinderProperties, agentIdentity);
		sleep(3000);
		assertThat(console1.getAllAttachedAgentsCount(), is(1));

		// Stop that agent and see it's well disconnected
		agentControllerServerDaemon.stopAgent(agentIdentity);
		sleep(5000);
		assertThat(console1.getAllAttachedAgentsCount(), is(0));

		// Stop that agent and see it's well disconnected again.
		// It should be verified
		agentControllerServerDaemon.stopAgent(agentIdentity);
		sleep(5000);
		assertThat(console1.getAllAttachedAgentsCount(), is(0));

		agentControllerServerDaemon.startAgent(grinderProperties, agentIdentity);
		sleep(5000);
		assertThat(console1.getAllAttachedAgentsCount(), is(1));
	}

	@Test
	public void testAgentControllerServerDaemonRecovery() {
		sleep(2000);
		// Connect all available agents
		Set<AgentIdentity> allAvailableAgents = agentControllerServerDaemon.getAllAvailableAgents();
		assertThat(allAvailableAgents.size(), is(2));
		SingleConsole console1 = new SingleConsole(6372);
		console1.start();
		GrinderProperties grinderProperties = new GrinderProperties();
		grinderProperties.setInt(GrinderProperties.CONSOLE_PORT, console1.getConsolePort());

		agentControllerServerDaemon.startAgent(grinderProperties, getAgentIdentity(allAvailableAgents, 0));
		agentControllerServerDaemon.startAgent(grinderProperties, getAgentIdentity(allAvailableAgents, 1));
		sleep(1000);
		assertThat(console1.getAllAttachedAgents().size(), is(2));

		// Shutdown agent controller
		agentControllerServerDaemon.shutdown();
		sleep(5000);

		assertThat(console1.getAllAttachedAgents().size(), is(0));

		// Then start again
		agentControllerServerDaemon = new AgentControllerServerDaemon(agentControllerServerDaemon.getPort());
		agentControllerServerDaemon.start();
		sleep(3000);

		// See the agent controller is attached automatically
		assertThat(agentControllerServerDaemon.getAllAttachedAgentsCount(), is(2));
		sleep(2000);
		allAvailableAgents = agentControllerServerDaemon.getAllAvailableAgents();

		// If we restart agents
		agentControllerServerDaemon.startAgent(grinderProperties, getAgentIdentity(allAvailableAgents, 0));
		agentControllerServerDaemon.startAgent(grinderProperties, getAgentIdentity(allAvailableAgents, 1));
		sleep(2000);

		// They should be successfully attached into the existing console.
		assertThat(console1.getAllAttachedAgents().size(), is(2));

	}
}
