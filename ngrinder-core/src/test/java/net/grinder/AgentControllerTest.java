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
import net.grinder.console.model.ConsoleCommunicationSetting;
import net.grinder.util.ConsolePropertiesFactory;
import net.grinder.util.NetworkUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.mutable.MutableBoolean;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.ngrinder.AbstractMultiGrinderTestBase;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Set;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public class AgentControllerTest extends AbstractMultiGrinderTestBase {
	AgentControllerServerDaemon agentControllerServerDaemon;
	AgentControllerDaemon agentControllerDaemon;
	AgentControllerDaemon agentControllerDaemon2;
	SingleConsole console1;
	Set<AgentIdentity> allAvailableAgents;


	@Before
	public void before() throws IOException {
		FileUtils.copyFileToDirectory(
			new File(this.getClass().getResource("/long-time-prepare-test.py").getFile()),
			new File("./tmp/agent-home/tmp_1/file-store/_default/current")
		);
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
		console1.setReportPath(new File("./tmp/console-home"));
		// Check there is no agents are attached.
		assertThat(console1.getAllAttachedAgentsCount(), is(0));

		// Make one agent connect to console1
		GrinderProperties grinderProperties = new GrinderProperties();
		grinderProperties.setInt(GrinderProperties.CONSOLE_PORT, console1.getConsolePort());
		String localHostAddress = NetworkUtils.getLocalHostAddress();
		grinderProperties.setProperty(GrinderProperties.CONSOLE_HOST, localHostAddress);
		AgentIdentity next = getAgentIdentity(allAvailableAgents, 0);
		agentControllerServerDaemon.startAgent(grinderProperties, next);
		waitAndAssertUntilAgentAttachedTo(console1, 1, 2);

		// Shutdown agent controller and see agent is detached as well
		agentControllerServerDaemon.shutdown();
		waitAndAssertUntilAgentAttachedTo(console1, 0, 5);

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
		waitAndAssertUntilAgentAttachedTo(console1, 1, 3);

		// Stop that agent and see it's well disconnected
		agentControllerServerDaemon.stopAgent(agentIdentity);
		waitAndAssertUntilAgentAttachedTo(console1, 0, 5);

		// Stop that agent and see it's well disconnected again.
		// It should be verified
		agentControllerServerDaemon.stopAgent(agentIdentity);
		waitAndAssertUntilAgentAttachedTo(console1, 0, 5);

		agentControllerServerDaemon.startAgent(grinderProperties, agentIdentity);
		waitAndAssertUntilAgentAttachedTo(console1, 1, 5);
	}

	@Test
	public void testConsoleCommunicationSettingTimeout() throws GrinderProperties.PersistenceException {
		long timeout = 3000;

		// Get one agent
		AgentIdentity agentIdentity = getAgentIdentity(allAvailableAgents, 0);

		// In order to control timeout milliseconds
		ConsoleCommunicationSetting consoleCommunicationSetting = ConsoleCommunicationSetting.asDefault();
		consoleCommunicationSetting.setInactiveClientTimeOut(timeout);

		// Start console
		final SingleConsole console1 = new SingleConsole("", getFreePort(), consoleCommunicationSetting,
			ConsolePropertiesFactory.createEmptyConsoleProperties());
		console1.setReportPath(new File("./tmp/console-home"));
		console1.start();
		console1.startSampling();

		// Start one agent
		GrinderProperties grinderProperties = new GrinderProperties();
		grinderProperties.setInt(GrinderProperties.CONSOLE_PORT, console1.getConsolePort());
		agentControllerServerDaemon.startAgent(grinderProperties, agentIdentity);

		waitAndAssertUntilAgentAttachedTo(console1, 1, 3);

		URL scriptUrl = this.getClass().getResource("/long-time-prepare-test.properties");
		File scriptFile = new File(scriptUrl.getFile());
		GrinderProperties properties = new GrinderProperties(scriptFile);
		properties.setAssociatedFile(new File("long-time-prepare-test.properties"));
		final MutableBoolean timeouted = new MutableBoolean(false);
		console1.addListener(new SingleConsole.ConsoleShutdownListener() {
			@Override
			public void readyToStop(StopReason stopReason) {
				// Notice: it couldn't distinguish between a script error or
				// timed out of the keepalive connection.
				System.out.println("The stop signal is recieved " + stopReason);
				if (stopReason.getDisplay().equals("Script error")) {
					timeouted.setValue(true);
				}
			}
		});
		console1.startTest(properties);
		for (int i = 0; i < 20; i++) {
			if (timeouted.isTrue()) {
				break;
			}
			try {
				Thread.sleep(1000);
			} catch (InterruptedException ignored) {
			}
		}

		assertTrue(timeouted.isTrue());

		// Stop that agent and see it's well disconnected
		agentControllerServerDaemon.stopAgent(agentIdentity);
		waitAndAssertUntilAgentAttachedTo(console1, 0, 10);
	}

	@Test
	public void testAgentControllerServerDaemonRecovery() {
		sleep(2000);
		// Connect all available agents
		Set<AgentIdentity> allAvailableAgents = agentControllerServerDaemon.getAllAvailableAgents();
		assertThat(allAvailableAgents.size(), is(2));
		SingleConsole console1 = new SingleConsole(6372);
		console1.start();
		console1.setReportPath(new File("./tmp/console-home"));
		GrinderProperties grinderProperties = new GrinderProperties();
		grinderProperties.setInt(GrinderProperties.CONSOLE_PORT, console1.getConsolePort());

		agentControllerServerDaemon.startAgent(grinderProperties, getAgentIdentity(allAvailableAgents, 0));
		agentControllerServerDaemon.startAgent(grinderProperties, getAgentIdentity(allAvailableAgents, 1));

		waitAndAssertUntilAgentAttachedTo(console1, 2, 2);

		// Shutdown agent controller
		agentControllerServerDaemon.shutdown();

		waitAndAssertUntilAgentAttachedTo(console1, 0, 5);

		// Then start again
		agentControllerServerDaemon = new AgentControllerServerDaemon(agentControllerServerDaemon.getPort());
		agentControllerServerDaemon.start();

		// See the agent controller is attached automatically
		waitAndAssertUntilAgentAttachedTo(agentControllerServerDaemon, 2, 3);
		sleep(2000);
		allAvailableAgents = agentControllerServerDaemon.getAllAvailableAgents();

		// If we restart agents
		agentControllerServerDaemon.startAgent(grinderProperties, getAgentIdentity(allAvailableAgents, 0));
		agentControllerServerDaemon.startAgent(grinderProperties, getAgentIdentity(allAvailableAgents, 1));

		// They should be successfully attached into the existing console.
		waitAndAssertUntilAgentAttachedTo(console1, 2, 2);

	}
}
