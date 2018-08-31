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
package org.ngrinder;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import net.grinder.AgentControllerServer;
import net.grinder.AgentControllerServerDaemon;
import net.grinder.AgentDaemon.AgentShutDownListener;
import net.grinder.Console;
import net.grinder.SingleConsole;
import net.grinder.common.processidentity.AgentIdentity;
import net.grinder.util.thread.Condition;

import org.hamcrest.Matchers;
import org.hyperic.jni.ArchLoaderException;
import org.hyperic.jni.ArchNotSupportedException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.ngrinder.common.util.ThreadUtils;
import org.ngrinder.infra.AgentConfig;
import org.ngrinder.infra.ArchLoaderInit;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

abstract public class AbstractMultiGrinderTestBase {
	public AgentConfig.NullAgentConfig agentConfig1;
	public AgentConfig.NullAgentConfig agentConfig2;
	public AgentConfig.NullAgentConfig agentConfig3;



	static {
		System.setProperty("ngrinder.agent.home", "./tmp/agent-home");
	}

	@Before
	public void agentInit() throws ArchNotSupportedException, ArchLoaderException {
		agentConfig1 = new AgentConfig.NullAgentConfig(1);
		agentConfig1.init();
		ArchLoaderInit archLoaderInit = new ArchLoaderInit();
		archLoaderInit.init(agentConfig1.getHome().getNativeDirectory());
		agentConfig2 = new AgentConfig.NullAgentConfig(1);
		agentConfig2.init();
		archLoaderInit.init(agentConfig2.getHome().getNativeDirectory());
		agentConfig3 = new AgentConfig.NullAgentConfig(1);
		agentConfig3.init();
		archLoaderInit.init(agentConfig3.getHome().getNativeDirectory());
	}

	/**
	 * Sleep quietly
	 *
	 * @param millisecond
	 */
	public void sleep(long millisecond) {
		ThreadUtils.sleep(millisecond);
	}

	protected AgentIdentity getAgentIdentity(Set<AgentIdentity> agentSet, int index) {
		AgentIdentity agentIdentity = null;
		Iterator<AgentIdentity> iterator = agentSet.iterator();
		for (int i = 0; i <= index; i++) {
			agentIdentity = iterator.next();
		}
		return agentIdentity;
	}

	/**
	 * Waiting condition
	 */
	public Condition condition = new Condition();

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

	public void waitOnCondition(Condition condition, int timeout) {
		synchronized (condition) {
			condition.waitNoInterrruptException(2100);
		}
	}

	/**
	 * Returns a free port numbers on localhost, or less than give count entries if unable to find a
	 * free port.
	 *
	 * @return a free port number on localhost, or less than give count entries if unable to find a
	 *         free port
	 */
	public List<Integer> getFreePorts(int count) {
		List<Integer> ports = new ArrayList<Integer>();
		for (int i = 0; i < count; i++) {
			ports.add(getFreePort());
		}
		return ports;
	}

	/**
	 * Returns a free port number on localhost, or -1 if unable to find a free port.
	 *
	 * @return a free port number on localhost, or -1 if unable to find a free port
	 */
	public int getFreePort() {
		ServerSocket socket = null;
		try {
			socket = new ServerSocket(0);
			return socket.getLocalPort();
		} catch (IOException ignored) {
		} finally {
			if (socket != null) {
				try {
					socket.close();
				} catch (IOException ignored) {
				}
			}
		}
		return -1;
	}

	protected void waitAndAssertUntilAgentAttachedTo(SingleConsole console, int expected, int retryCount) {
		int size = 0;
		retryCount++;
		for (int i = 0; i < retryCount; i++) {
			size = console.getAllAttachedAgents().size();
			if (size == expected) {
				return;
			}
			sleep(1000);
		}
		assertThat(size, equalTo(expected));
	}



	protected void waitAndAssertUntilAgentAttachedTo(AgentControllerServerDaemon server, int expected, int retryCount) {
		int size = 0;
		retryCount++;

		for (int i = 0; i < retryCount; i++) {
			size = server.getAllAttachedAgentsCount();
			if (size == expected) {
				return;
			}
			sleep(1000);
		}
		assertThat(size, equalTo(expected));
	}
}
