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

import net.grinder.common.GrinderException;
import net.grinder.console.common.Resources;
import net.grinder.console.common.ResourcesImplementation;
import net.grinder.console.model.ConsoleCommunicationSetting;
import net.grinder.console.model.ConsoleProperties;
import net.grinder.engine.agent.Agent;
import net.grinder.util.ConsolePropertiesFactory;
import net.grinder.util.thread.Condition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.ngrinder.common.util.ExceptionUtils.processException;

public class ExternalAgentControllerServerDaemon implements Agent {
	public static final Resources RESOURCES =
		new ResourcesImplementation("net.grinder.console.common.resources.Console");

	public static final Logger log = LoggerFactory.getLogger(RESOURCES.getString("shortTitle"));
	private static final long AGENT_CONTROLLER_SERVER_SHUTDOWN_WAITING_TIMEOUT = 10000;
	private static final long AGENT_CONTROLLER_SERVER_EVENT_EXPIRATION_TIMEOUT = 10000;

	private final Condition eventSyncCondition = new Condition();

	private Thread thread;
	private ExternalAgentControllerServer agentControllerServer;

	public ExternalAgentControllerServerDaemon(String ip, int port) {
		this(ip, port, ConsolePropertiesFactory.createEmptyConsoleProperties(), ConsoleCommunicationSetting.asDefault());
	}

	public ExternalAgentControllerServerDaemon(String ip, int port, ConsoleProperties consoleProperties,
											   ConsoleCommunicationSetting consoleCommunicationSetting) {
		try {
			consoleProperties.setConsoleHost(ip);
			consoleProperties.setConsolePort(port);
			this.agentControllerServer = new ExternalAgentControllerServer(RESOURCES, log, consoleProperties,
				eventSyncCondition, consoleCommunicationSetting);
		} catch (GrinderException e) {
			throw processException("Exception occurred while initiating the agent controller server daemon", e);
		}
	}

	@Override
	public void run() {
		thread = new Thread(this::startSync);
		thread.setName("Agent controller server thread");
		thread.start();
		synchronized (eventSyncCondition) {
			eventSyncCondition.waitNoInterrruptException(AGENT_CONTROLLER_SERVER_EVENT_EXPIRATION_TIMEOUT);
		}
	}

	public void startSync() {
		agentControllerServer.run();
	}

	@Override
	public void shutdown() {
		try {
			agentControllerServer.shutdown();
			if (thread != null) {
				thread.join(AGENT_CONTROLLER_SERVER_SHUTDOWN_WAITING_TIMEOUT);
				thread.interrupt();
			}
		} catch (Exception e) {
			throw processException("Exception occurred during shutting down the agent controller server daemon", e);
		} finally {
			thread = null;
		}
	}

}
