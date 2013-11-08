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

import static org.ngrinder.common.util.ExceptionUtils.processException;
import static org.ngrinder.common.util.Preconditions.checkNotNull;
import net.grinder.common.GrinderException;
import net.grinder.common.GrinderProperties;
import net.grinder.communication.AgentControllerCommunicationDefauts;
import net.grinder.engine.agent.Agent;
import net.grinder.util.ListenerHelper;
import net.grinder.util.ListenerSupport;
import net.grinder.util.ListenerSupport.Informer;
import net.grinder.util.thread.Condition;

import org.ngrinder.common.util.ThreadUtil;
import org.ngrinder.infra.AgentConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is daemon wrapper for agent controller.
 * 
 * @author JunHo Yoon
 */
public class AgentControllerDaemon implements Agent {

	private static final int LOG_FREQUENCY = 5;
	private final AgentController agentController;
	private Thread thread;
	private final GrinderProperties properties;
	private final ListenerSupport<AgentControllerShutDownListener> m_listeners = ListenerHelper.create();
	private boolean forceShutdown = false;
	// event synchronization for
	private Condition m_eventSyncCondition = new Condition();

	public static final Logger LOGGER = LoggerFactory.getLogger("agent controller daemon");
	/**
	 * Region of grinder agent.
	 */
	private String region = "";
	private AgentConfig agentConfig;

	/**
	 * Constructor.
	 * 
	 * @param currentIp
	 *            current ip
	 */
	public AgentControllerDaemon(String currentIp) {
		try {
			properties = new GrinderProperties(GrinderProperties.DEFAULT_PROPERTIES);
			agentController = new AgentController(m_eventSyncCondition, currentIp);
		} catch (GrinderException e) {
			throw processException("Exception occurred while initiating the agent controller deamon", e);
		}
	}

	/**
	 * Run agent controller with the default agent controller port.
	 * 
	 */
	public void run() {
		run(null, AgentControllerCommunicationDefauts.DEFAULT_AGENT_CONTROLLER_SERVER_PORT);
	}

	/**
	 * Run agent controller with the agentControllerServerPort.
	 * 
	 * @param agentControllerServerPort
	 *            agent controller server port
	 */
	public void run(int agentControllerServerPort) {
		run(null, agentControllerServerPort);
	}

	/**
	 * Run agent controller with the given agent controller host and the agent
	 * controller server port.
	 * 
	 * @param agentControllerServerHost
	 *            agent controller server host
	 * @param agentControllerServerPort
	 *            agent controller server port
	 */
	public void run(String agentControllerServerHost, int agentControllerServerPort) {
		if (agentControllerServerHost != null) {
			properties.setProperty(AgentConfig.AGENT_CONTROLLER_SERVER_HOST, agentControllerServerHost);
		}
		if (agentControllerServerPort != 0) {
			properties.setInt(AgentConfig.AGENT_CONTROLLER_SERVER_PORT, agentControllerServerPort);
		}
		run(properties);
	}

	private long count = 0;

	/**
	 * Run agent controller with given {@link GrinderProperties}. server host
	 * and port will be gained from {@link GrinderProperties}
	 * 
	 * @param grinderProperties
	 *            {@link GrinderProperties}
	 */
	public void run(final GrinderProperties grinderProperties) {
		grinderProperties.put("grinder.region", region);
		thread = new Thread(new Runnable() {
			public void run() {
				do {
					try {
						if (count % LOG_FREQUENCY == 0) {
							LOGGER.info("The agent controller daemon is started.");
						}
						getAgentController().setAgentConfig(
								checkNotNull(agentConfig, "the agent config should be provided "
										+ "before the controller is started"));
						getAgentController().run(grinderProperties, count);

						getListeners().apply(new Informer<AgentControllerShutDownListener>() {
							public void inform(AgentControllerShutDownListener listener) {
								listener.shutdownAgentController();
							}
						});
						count++;
					} catch (Exception e) {
						LOGGER.info("Agent controller daemon is crashed. {}", e.getMessage());
						LOGGER.debug("The error detail is  ", e);
					}
					if (isForceShutdown()) {
						setForceShutdown(false);
						break;
					}
					ThreadUtil.sleep(GrinderConstants.AGENT_CONTROLLER_RETRY_INTERVAL);
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
	 * Add agent controller shutdown listener.
	 * 
	 * @param listener
	 *            listener
	 */
	public void addListener(AgentControllerShutDownListener listener) {
		m_listeners.add(listener);
	}

	/**
	 * Shutdown agent controller.
	 */
	public void shutdown() {
		try {
			setForceShutdown(true);
			agentController.shutdown();
			if (thread != null) {
				ThreadUtil.stopQuietly(thread, "Agent controller thread was not stopped. Stop by force.");
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

	public void setRegion(String region) {
		this.region = region;
	}

	public void setAgentConfig(AgentConfig agentConfig) {
		this.agentConfig = agentConfig;
	}

}
