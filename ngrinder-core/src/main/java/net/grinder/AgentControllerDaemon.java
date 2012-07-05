package net.grinder;

import net.grinder.common.GrinderException;
import net.grinder.common.GrinderProperties;
import net.grinder.communication.AgentControllerCommunicationDefauts;
import net.grinder.engine.agent.Agent;
import net.grinder.util.ListenerSupport;
import net.grinder.util.ListenerSupport.Informer;
import net.grinder.util.thread.Condition;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is daemon wrapper for agent controller;
 * 
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

	public final static Logger logger = LoggerFactory.getLogger("agent controller daemon");

	public AgentControllerDaemon() {
		try {
			properties = new GrinderProperties(GrinderProperties.DEFAULT_PROPERTIES);
			agent = new AgentController(logger, m_eventSyncCondition);
		} catch (GrinderException e) {
			throw new RuntimeException(e);
		}
	}

	public void run() throws GrinderException {
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

		thread = new Thread(new Runnable() {
			public void run() {
				do {
					try {
						logger.info("agent controller daemon : started.");
						getAgentController().run(grinderProperties);

						getListeners().apply(new Informer<AgentShutDownListener>() {
							public void inform(AgentShutDownListener listener) {
								listener.shutdownAgent();
							}
						});
					} catch (Exception e) {
						logger.info("agent controller daemon : crashed.", e);
					}
					if (isForceToshutdown()) {
						setForceToshutdown(false);
						break;
					}
					try {
						Thread.sleep(2000);
					} catch (InterruptedException e) {
						new RuntimeException(e);
					}
				} while (true);
			}
		});
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
			thread.join();
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

	private void setForceToshutdown(boolean force) {
		this.forceToshutdown = force;
	}

}
