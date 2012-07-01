package net.grinder;

import net.grinder.common.GrinderException;
import net.grinder.common.GrinderProperties;
import net.grinder.engine.agent.Agent;
import net.grinder.engine.agent.AgentImplementationEx;
import net.grinder.util.ListenerSupport;
import net.grinder.util.ListenerSupport.Informer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AgentDaemon implements Agent {
	private final AgentImplementationEx agent;
	private Thread thread;
	private GrinderProperties properties;
	private final ListenerSupport<AgentShutDownListener> m_listeners = new ListenerSupport<AgentShutDownListener>();
	private boolean forceToshutdown = false;
	public final static Logger logger = LoggerFactory.getLogger("agent");

	public AgentDaemon() {
		try {
			properties = new GrinderProperties(
					GrinderProperties.DEFAULT_PROPERTIES);
			agent = new AgentImplementationEx(logger);
		} catch (GrinderException e) {
			throw new RuntimeException(e);
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
			getGrinderProperties().setProperty(GrinderProperties.CONSOLE_HOST,
					consoleHost);
		}
		if (consolePort != 0) {
			getGrinderProperties().setInt(GrinderProperties.CONSOLE_PORT,
					consolePort);
		}
		thread = new Thread(new Runnable() {
			public void run() {
				do {
					try {
						getAgentImplementationEx().run(getGrinderProperties());

						getListeners().apply(
								new Informer<AgentShutDownListener>() {
									public void inform(
											AgentShutDownListener listener) {
										listener.shutdownAgent();
									}
								});
					} catch (GrinderException e) {
						logger.error(
								"while sleeping agent thread, error occurs", e);
					}
					if (isForceToshutdown()) {
						setForceToshutdown(false);
						break;
					}
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						logger.error(
								"while sleeping agent thread, error occurs", e);
					}
				} while (true);
			}
		});
		thread.setDaemon(true);
		thread.start();
	}

	private GrinderProperties getGrinderProperties() {
		return this.properties;
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
			if (thread != null) {
				thread.join();
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
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
