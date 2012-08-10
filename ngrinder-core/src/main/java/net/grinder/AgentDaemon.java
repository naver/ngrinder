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
import net.grinder.engine.agent.Agent;
import net.grinder.engine.agent.AgentImplementationEx;
import net.grinder.util.ListenerSupport;
import net.grinder.util.ListenerSupport.Informer;

import org.ngrinder.common.exception.NGrinderRuntimeException;
import org.ngrinder.common.util.ThreadUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AgentDaemon implements Agent {
	private final AgentImplementationEx agent;
	private Thread thread = new Thread();
	private GrinderProperties properties;
	private final ListenerSupport<AgentShutDownListener> m_listeners = new ListenerSupport<AgentShutDownListener>();
	private boolean forceToshutdown = false;
	public static final Logger LOGGER = LoggerFactory.getLogger("agent");

	public AgentDaemon() {
		try {
			properties = new GrinderProperties(GrinderProperties.DEFAULT_PROPERTIES);
			agent = new AgentImplementationEx(LOGGER);
		} catch (GrinderException e) {
			throw new NGrinderRuntimeException("Exception occurs while creating AgentDaemon", e);
		}
	}

	public void run() throws GrinderException {
		run(null, 6372);
	}

	public void run(int consolePort) {
		run(null, consolePort);
	}

	public void run(GrinderProperties grinderProperties) {
		this.properties = grinderProperties;
		run(null, 0);
	}

	public void run(String consoleHost, int consolePort) {
		if (consoleHost != null) {
			getGrinderProperties().setProperty(GrinderProperties.CONSOLE_HOST, consoleHost);
		}
		if (consolePort != 0) {
			getGrinderProperties().setInt(GrinderProperties.CONSOLE_PORT, consolePort);
		}
		thread = new Thread(new AgentThreadRunnable(), "Agent conntected to port : "
				+ getGrinderProperties().getInt(GrinderProperties.CONSOLE_PORT, 0));
		thread.setDaemon(true);
		thread.start();
		LOGGER.info("Agent Daemon {} is started.", thread.getName());
	}

	private GrinderProperties getGrinderProperties() {
		return this.properties;
	}

	class AgentThreadRunnable implements Runnable {
		public void run() {
			do {
				try {
					getAgentImplementationEx().run(getGrinderProperties());

					getListeners().apply(new Informer<AgentShutDownListener>() {
						public void inform(AgentShutDownListener listener) {
							listener.shutdownAgent();
						}
					});
				} catch (GrinderException e) {
					LOGGER.error("while sleeping agent thread, error occurs", e);
				}
				if (isForceToshutdown()) {
					setForceToshutdown(false);
					break;
				}
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					LOGGER.error("while sleeping agent thread, error occurs", e);
				}
			} while (true);
		}
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
			forceToshutdown = true;
			agent.shutdown();
			ThreadUtil.stopQuetly(thread, "Agent Daemon is not stopped. So force to stop");
			thread = null;
		} catch (Exception e) {
			throw new NGrinderRuntimeException("Exception occurs while shutting down AgentDaemon", e);
		}
	}

	public AgentImplementationEx getAgentImplementationEx() {
		return agent;
	}

	private boolean isForceToshutdown() {
		return forceToshutdown;
	}

	private void setForceToshutdown(boolean force) {
		this.forceToshutdown = force;
	}

}
