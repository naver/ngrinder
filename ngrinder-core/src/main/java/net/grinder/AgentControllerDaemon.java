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

import net.grinder.common.GrinderException;
import net.grinder.common.GrinderProperties;
import net.grinder.communication.AgentControllerCommunicationDefauts;
import net.grinder.engine.agent.Agent;
import net.grinder.util.ListenerSupport;
import net.grinder.util.ListenerSupport.Informer;
import net.grinder.util.thread.Condition;

import org.ngrinder.common.exception.NGrinderRuntimeException;
import org.ngrinder.common.util.ThreadUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is daemon wrapper for agent controller.
 * 
 * @author JunHo Yoon
 */
public class AgentControllerDaemon implements Agent {

	public static final String AGENT_CONTROLER_SERVER_HOST = "ngrinder.agentcontroller.host";
	public static final String AGENT_CONTROLER_SERVER_PORT = "ngrinder.agentcontroller.port";

	private final AgentController agent;
	private Thread thread;
	private final GrinderProperties properties;
	private final ListenerSupport<AgentShutDownListener> m_listeners = new ListenerSupport<AgentShutDownListener>();
	private boolean forceToshutdown = false;
	public Condition m_eventSyncCondition = new Condition();

	public static final Logger LOGGER = LoggerFactory.getLogger("agent controller daemon");
	/**
	 * Region of grinder agent.
	 */
	private String region = "";

	public AgentControllerDaemon() {
		try {
			properties = new GrinderProperties(GrinderProperties.DEFAULT_PROPERTIES);
			agent = new AgentController(LOGGER, m_eventSyncCondition);
		} catch (GrinderException e) {
			throw new NGrinderRuntimeException("Exception occurs while initiating agent controller deamon", e);
		}
	}

	public void run() {
		run(null, AgentControllerCommunicationDefauts.DEFAULT_AGENT_CONTROLLER_SERVER_PORT);
	}

	public void run(int agentControllerServerPort) {
		run(null, agentControllerServerPort);
	}

	public void run(String agentControllerServerHost, int agentControllerServerPort) {
		if (agentControllerServerHost != null) {
			properties.setProperty(AGENT_CONTROLER_SERVER_HOST, agentControllerServerHost);
		}
		if (agentControllerServerPort != 0) {
			properties.setInt(AGENT_CONTROLER_SERVER_PORT, agentControllerServerPort);
		}
		run(properties);
	}

	public void run(final GrinderProperties grinderProperties) {
		grinderProperties.put("grinder.region", region);
		thread = new Thread(new Runnable() {
			public void run() {
				do {
					try {
						LOGGER.info("agent controller daemon : started.");
						getAgentController().run(grinderProperties);

						getListeners().apply(new Informer<AgentShutDownListener>() {
							public void inform(AgentShutDownListener listener) {
								listener.shutdownAgent();
							}
						});
					} catch (Exception e) {
						LOGGER.info("agent controller daemon : crashed.", e);
					}
					if (isForceToshutdown()) {
						setForceToshutdown(false);
						break;
					}
					try {
						Thread.sleep(2000);
					} catch (InterruptedException e) {
						throw new NGrinderRuntimeException(
								"Exception occurs while sleep runin the loop of agent controller deamon run.", e);
					}
				} while (true);
			}
		}, "Agent Controller Thread");
		thread.start();
	}

	public interface AgentShutDownListener {
		public void shutdownAgent();
	}

	public ListenerSupport<AgentShutDownListener> getListeners() {
		return this.m_listeners;
	}

	public void addListener(AgentShutDownListener listener) {
		m_listeners.add(listener);
	}

	public void shutdown() {
		try {
			setForceToshutdown(true);
			agent.shutdown();
			if (thread != null) {
				ThreadUtil.stopQuetly(thread, "Agent Controller  Thread is not stopped. Force to Stop");
				thread = null;
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public AgentController getAgentController() {
		return agent;
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

}
