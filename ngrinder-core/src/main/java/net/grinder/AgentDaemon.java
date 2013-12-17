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
import net.grinder.common.GrinderProperties;
import net.grinder.communication.CommunicationDefaults;
import net.grinder.engine.agent.Agent;
import net.grinder.engine.agent.AgentImplementationEx;
import net.grinder.util.ListenerSupport;
import net.grinder.util.ListenerSupport.Informer;
import org.apache.commons.lang.StringUtils;
import org.ngrinder.common.util.ThreadUtils;
import org.ngrinder.infra.AgentConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.ngrinder.common.util.ExceptionUtils.processException;

/**
 * Agent Daemon wrapper for {@link AgentImplementationEx} in thread.
 *
 * @author JunHo Yoon
 * @since 3.0
 */
public class AgentDaemon implements Agent {
	private volatile AgentImplementationEx agent;
	private Thread thread = new Thread();
	private GrinderProperties properties;
	private final ListenerSupport<AgentShutDownListener> m_listeners = new ListenerSupport<AgentShutDownListener>();
	private boolean forceShutdown = false;
	public static final Logger LOGGER = LoggerFactory.getLogger("agent daemon");
	private final AgentConfig m_agentConfig;

	/**
	 * Constructor.
	 *
	 * @param agentConfig agent configuration
	 */

	public AgentDaemon(AgentConfig agentConfig) {
		this.m_agentConfig = agentConfig;
		try {
			properties = new GrinderProperties(GrinderProperties.DEFAULT_PROPERTIES);
		} catch (GrinderException e) {
			throw processException("Exception occurred while creating agent daemon", e);
		}
	}

	/**
	 * Set agent.
	 *
	 * @param agent agent
	 * @return set agent
	 */
	public synchronized AgentImplementationEx setAgent(AgentImplementationEx agent) {
		this.agent = agent;
		return this.agent;
	}

	/**
	 * Run agent to connect to default console in localhost.
	 */
	public void run() {
		run(null, CommunicationDefaults.CONSOLE_PORT);
	}

	/**
	 * Run agent to connect to given console in localhost.
	 *
	 * @param consolePort port to which agent connect
	 */
	public void run(int consolePort) {
		run(null, consolePort);
	}

	/**
	 * Run agent with given {@link GrinderProperties}.
	 *
	 * @param grinderProperties {@link GrinderProperties}
	 */
	public void run(GrinderProperties grinderProperties) {
		this.properties = grinderProperties;
		run(null, 0);
	}

	/**
	 * Run agent with given consoleHost and consolePort.
	 *
	 * if consoleHost is null it will use localhost or use console host set in
	 * {@link GrinderProperties}
	 *
	 * if port number is 0, it will use default consolePort or use console port
	 * set in {@link GrinderProperties}
	 *
	 * @param consoleHost host name
	 * @param consolePort port number
	 */
	public void run(String consoleHost, int consolePort) {
		if (StringUtils.isNotEmpty(consoleHost)) {
			getGrinderProperties().setProperty(GrinderProperties.CONSOLE_HOST, consoleHost);
		}
		if (consolePort > 0) {
			getGrinderProperties().setInt(GrinderProperties.CONSOLE_PORT, consolePort);
		}

		thread = new Thread(new AgentThreadRunnable(), "Agent daemon connecting to port "
				+ getGrinderProperties().getInt(GrinderProperties.CONSOLE_PORT, 0));
		thread.setDaemon(true);
		thread.start();
		LOGGER.info("{} is started.", thread.getName());
	}

	private GrinderProperties getGrinderProperties() {
		return this.properties;
	}

	class AgentThreadRunnable implements Runnable {
		public void run() {
			try {
				setAgent(new AgentImplementationEx(LOGGER, m_agentConfig)).run(getGrinderProperties());
			} catch (Exception e) {
				LOGGER.error("While running an agent thread, an error occurred", e);
			}
			getListeners().apply(new Informer<AgentShutDownListener>() {
				public void inform(AgentShutDownListener listener) {
					listener.shutdownAgent();
				}
			});
			if (isForceShutdown()) {
				setForceShutdown(false);
			}
		}
	}

	/**
	 * Interface to detect agent shutdown.
	 *
	 * @author JunHo Yoon
	 */
	public interface AgentShutDownListener {
		/**
		 * AgentShutdown listening method.
		 */
		public void shutdownAgent();
	}

	public ListenerSupport<AgentShutDownListener> getListeners() {
		return this.m_listeners;
	}

	/**
	 * Reset all shutdown listener.
	 */
	public void resetListeners() {
		final ListenerSupport<AgentShutDownListener> backup = new ListenerSupport<AgentDaemon.AgentShutDownListener>();
		getListeners().apply(new Informer<AgentShutDownListener>() {
			public void inform(AgentShutDownListener listener) {
				backup.add(listener);
			}
		});

		backup.apply(new Informer<AgentShutDownListener>() {
			public void inform(AgentShutDownListener listener) {
				getListeners().remove(listener);
			}
		});
	}

	/**
	 * Add AgentShutdownListener.
	 *
	 * @param listener listener to detect to Agent Shutdown
	 */
	public void addListener(AgentShutDownListener listener) {
		m_listeners.add(listener);
	}

	/**
	 * Shutdown.
	 */
	public void shutdown() {
		try {
			forceShutdown = true;
			if (agent != null) {
				agent.shutdown();
			}
			ThreadUtils.stopQuietly(thread, "Agent daemon is not stopped. So stop by force");
			thread = null;
		} catch (Exception e) {
			throw processException("Exception occurred while shutting down the agent daemon", e);
		}
	}

	private boolean isForceShutdown() {
		return forceShutdown;
	}

	private void setForceShutdown(boolean force) {
		this.forceShutdown = force;
	}

}
