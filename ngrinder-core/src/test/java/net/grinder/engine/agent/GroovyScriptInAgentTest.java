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
import net.grinder.AgentDaemon.AgentShutDownListener;
import net.grinder.SingleConsole;
import net.grinder.common.GrinderProperties;
import net.grinder.common.GrinderProperties.PersistenceException;
import net.grinder.util.GrinderClassPathInitializer;
import net.grinder.util.thread.Condition;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.ngrinder.common.util.ThreadUtils;
import org.ngrinder.infra.AgentConfig;

import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.URL;

public class GroovyScriptInAgentTest {

	private SingleConsole console1;
	private Integer consolePort;
	private AgentConfig.NullAgentConfig agentConfig;

	@Before
	public void before() {
		GrinderClassPathInitializer.initJUnitContext();
		agentConfig = new AgentConfig.NullAgentConfig(1);
		agentConfig.init();
		consolePort = getFreePort();
		agentConfig.setControllerPort(consolePort);
		console1 = new SingleConsole(consolePort);
		console1.start();
		ThreadUtils.sleep(1000);
	}

	@After
	public void after() {
		console1.shutdown();
	}

	@Test
	public void testStartAgentAndTest() throws InterruptedException, PersistenceException {
		AgentConfig agentConfig1 = Mockito.spy(agentConfig);
		AgentDaemon agent = new AgentDaemon(agentConfig1);
		URL scriptUrl = this.getClass().getResource("/grinder.properties");
		File scriptFile = new File(scriptUrl.getFile());
		GrinderProperties properties = new GrinderProperties(scriptFile);
		agent.run(properties);
		ThreadUtils.sleep(10000);
	}

	public int getFreePort() {
		ServerSocket socket = null;
		try {
			socket = new ServerSocket(0);
			return socket.getLocalPort();
		} catch (IOException e) {
		} finally {
			if (socket != null) {
				try {
					socket.close();
				} catch (IOException e) {
				}
			}
		}
		return -1;
	}

	public final class AgentShutDownSynchronizeListener implements AgentShutDownListener {

		private final Condition condition;

		public AgentShutDownSynchronizeListener(Condition condition) {
			this.condition = condition;

		}

		public void shutdownAgent() {
			synchronized (condition) {
				condition.notifyAll();
			}
		}
	}
}
