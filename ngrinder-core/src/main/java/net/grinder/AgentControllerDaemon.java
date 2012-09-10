/*
 * Copyright (C) 2012 - 2012 NHN Corporation
 * All rights reserved.
 *
 * This file is part of The nGrinder software distribution. Refer to
 * the file LICENSE which is part of The nGrinder distribution for
 * licensing details. The nGrinder distribution is available on the
 * Internet at http://nhnopensource.org/ngrinder
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS
 * FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE
 * COPYRIGHT HOLDERS OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package net.grinder;

import static org.ngrinder.common.util.Preconditions.checkNotNull;
import net.grinder.common.GrinderException;
import net.grinder.common.GrinderProperties;
import net.grinder.communication.AgentControllerCommunicationDefauts;
import net.grinder.engine.agent.Agent;
import net.grinder.util.ListenerSupport;
import net.grinder.util.ListenerSupport.Informer;
import net.grinder.util.thread.Condition;

import org.ngrinder.common.exception.NGrinderRuntimeException;
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

	private final AgentController agentController;
	private Thread thread;
	private final GrinderProperties properties;
	private final ListenerSupport<AgentControllerShutDownListener> m_listeners = new ListenerSupport<AgentControllerShutDownListener>();
	private boolean forceToshutdown = false;
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
	 */
	public AgentControllerDaemon() {
		try {
			properties = new GrinderProperties(GrinderProperties.DEFAULT_PROPERTIES);
			agentController = new AgentController(m_eventSyncCondition);
		} catch (GrinderException e) {
			throw new NGrinderRuntimeException("Exception occurs while initiating agent controller deamon", e);
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
			properties.setProperty(AgentConfig.AGENT_CONTROLER_SERVER_HOST, agentControllerServerHost);
		}
		if (agentControllerServerPort != 0) {
			properties.setInt(AgentConfig.AGENT_CONTROLER_SERVER_PORT, agentControllerServerPort);
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
						if (count % 5 == 0) {
							LOGGER.info("agent controller daemon : started.");
						}
						getAgentController().setAgentConfig(
								checkNotNull(agentConfig,
										"agent config should be provided before agent controller start"));
						getAgentController().run(grinderProperties, count);

						getListeners().apply(new Informer<AgentControllerShutDownListener>() {
							public void inform(AgentControllerShutDownListener listener) {
								listener.shutdownAgentController();
							}
						});
						count++;
					} catch (Exception e) {
						LOGGER.info("agent controller daemon : crashed.", e);
					}
					if (isForceToshutdown()) {
						setForceToshutdown(false);
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
			setForceToshutdown(true);
			agentController.shutdown();
			if (thread != null) {
				ThreadUtil.stopQuetly(thread, "Agent Controller  Thread is not stopped. Force to Stop");
				thread = null;
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public AgentController getAgentController() {
		return agentController;
	}

	private boolean isForceToshutdown() {
		return forceToshutdown;
	}

	private synchronized void setForceToshutdown(boolean force) {
		this.forceToshutdown = force;
	}

	public void setRegion(String region) {
		this.region = region;
	}

	public void setAgentConfig(AgentConfig agentConfig) {
		this.agentConfig = agentConfig;
	}

}
