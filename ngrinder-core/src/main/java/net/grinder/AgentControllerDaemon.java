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
import net.grinder.engine.agent.Agent;
import net.grinder.util.ListenerHelper;
import net.grinder.util.ListenerSupport;
import net.grinder.util.ListenerSupport.Informer;
import net.grinder.util.thread.Condition;
import org.ngrinder.common.util.ThreadUtils;
import org.ngrinder.infra.AgentConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.ngrinder.common.util.ExceptionUtils.processException;

/**
 * This is daemon wrapper for agent controller.
 *
 * @author JunHo Yoon
 */
public class AgentControllerDaemon implements Agent {

	private static final int LOG_FREQUENCY = 5;
	private final AgentController agentController;
	private Thread thread;
	private final ListenerSupport<AgentControllerShutDownListener> m_listeners = ListenerHelper.create();
	private boolean forceShutdown = false;
	// event synchronization for
	@SuppressWarnings("FieldCanBeLocal")
	private Condition m_eventSyncCondition = new Condition();

	public static final Logger LOGGER = LoggerFactory.getLogger("agent controller daemon");


	/**
	 * Constructor.
	 *
	 * @param agentConfig agent config
	 */
	public AgentControllerDaemon(AgentConfig agentConfig) {
		try {
			agentController = new AgentController(m_eventSyncCondition, agentConfig);
		} catch (GrinderException e) {
			throw processException("Exception occurred while initiating the agent controller daemon", e);
		}
	}


	private long count = 0;

	public void run() {
		thread = new Thread(new Runnable() {
			public void run() {
				do {
					try {
						if (count++ % LOG_FREQUENCY == 0) {
							LOGGER.info("The agent controller daemon is started.");
						}
						getAgentController().run();
						getListeners().apply(new Informer<AgentControllerShutDownListener>() {
							public void inform(AgentControllerShutDownListener listener) {
								listener.shutdownAgentController();
							}
						});
					} catch (Exception e) {
						LOGGER.info("Agent controller daemon is crashed. {}", e.getMessage());
						LOGGER.debug("The error detail is  ", e);
					}
					if (isForceShutdown()) {
						setForceShutdown(false);
						break;
					}
					ThreadUtils.sleep(GrinderConstants.AGENT_CONTROLLER_RETRY_INTERVAL);
				} while (true);
			}
		}, "Agent Controller Thread");
		thread.start();
	}

	/**
	 * Agent controller shutdown listener class.
	 *
	 * @author JunHo Yoon
	 */
	public interface AgentControllerShutDownListener {
		/**
		 * Method which will be called when agent controller.
		 */
		public void shutdownAgentController();
	}

	public ListenerSupport<AgentControllerShutDownListener> getListeners() {
		return this.m_listeners;
	}


	/**
	 * Shutdown agent controller.
	 */
	public void shutdown() {
		try {
			setForceShutdown(true);
			agentController.shutdown();
			if (thread != null) {
				ThreadUtils.stopQuietly(thread, "Agent controller thread was not stopped. Stop by force.");
				thread = null;
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public AgentController getAgentController() {
		return agentController;
	}

	private boolean isForceShutdown() {
		return forceShutdown;
	}

	private synchronized void setForceShutdown(boolean force) {
		this.forceShutdown = force;
	}
}
