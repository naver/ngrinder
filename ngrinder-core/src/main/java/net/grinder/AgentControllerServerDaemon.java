package net.grinder;

import java.util.Set;

import net.grinder.common.GrinderException;
import net.grinder.common.GrinderProperties;
import net.grinder.common.processidentity.AgentIdentity;
import net.grinder.console.common.Resources;
import net.grinder.console.common.ResourcesImplementation;
import net.grinder.console.communication.AgentProcessControl;
import net.grinder.console.communication.AgentProcessControlImplementation;
import net.grinder.console.communication.ConsoleCommunication;
import net.grinder.console.model.ConsoleProperties;
import net.grinder.message.console.AgentControllerState;
import net.grinder.messages.agent.StartGrinderMessage;
import net.grinder.messages.agent.StopGrinderMessage;
import net.grinder.messages.console.AgentAddress;
import net.grinder.util.ConsolePropertiesFactory;
import net.grinder.util.thread.Condition;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AgentControllerServerDaemon {
	private final ConsoleProperties consoleProperties;
	private Thread thread;
	private AgentControllerServer agentControllerServer;
	public final static Resources resources = new ResourcesImplementation(
			"net.grinder.console.common.resources.Console");
	public Condition m_eventSyncCondition = new Condition();

	public Condition m_alive = new Condition();
	public final static Logger logger = LoggerFactory.getLogger(resources.getString("shortTitle"));

	public AgentControllerServerDaemon(String ip, int port) {
		this(ip, port, ConsolePropertiesFactory.createEmptyConsoleProperties());
	}

	public AgentControllerServerDaemon(String ip, int port, ConsoleProperties consoleProperties) {
		this.consoleProperties = consoleProperties;
		try {
			this.consoleProperties.setConsoleHost(ip);
			this.consoleProperties.setConsolePort(port);
			this.agentControllerServer = new AgentControllerServer(resources, logger, consoleProperties,
					m_eventSyncCondition);
		} catch (GrinderException e) {
			throw new RuntimeException(e);
		}
	}

	public AgentControllerServerDaemon(int port) {
		this("", port);
	}

	public void start() {
		thread = new Thread(new Runnable() {
			public void run() {
				startSync();
			}
		});
		thread.setDaemon(true);
		thread.start();
		synchronized (m_eventSyncCondition) {
			m_eventSyncCondition.waitNoInterrruptException(10000);
		}
	}

	/**
	 * For test
	 */
	public void startSync() {
		agentControllerServer.run();
	}

	public void shutdown() {
		try {
			agentControllerServer.shutdown();
			if (thread != null && thread.isAlive()) {
				thread.join(10000);
				thread = null;
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public int getAllAttachedAgentsCount() {
		return ((AgentProcessControl) agentControllerServer.getComponent(AgentProcessControlImplementation.class))
				.getNumberOfLiveAgents();
	}

	public Set<AgentIdentity> getAllFreeAgents() {
		return agentControllerServer.getComponent(AgentProcessControlImplementation.class).getAgents(
				AgentControllerState.RUNNING, 0);
	}

	public Set<AgentIdentity> getAllAvailableAgents() {
		return agentControllerServer.getComponent(AgentProcessControlImplementation.class).getAllAgents();
	}

	/**
	 * @return the consoleFoundation
	 */
	public <T> T getComponent(Class<T> componentType) {
		return agentControllerServer.getComponent(componentType);
	}

	/**
	 * Start agent using {@link GrinderProperties}
	 * 
	 * @param grinderProperties
	 * @param agentIdentity
	 *            agent controller identity
	 */
	public void startAgent(GrinderProperties grinderProperties, AgentIdentity agentIdentity) {
		logger.info("start agent of {}", agentIdentity);
		getComponent(ConsoleCommunication.class).sendToAddressedAgents(new AgentAddress(agentIdentity),
				new StartGrinderMessage(grinderProperties, agentIdentity.getNumber()));
	}

	/**
	 * Stop agent
	 * 
	 * @param agentIdentity
	 *            agent controller identity
	 */
	public void stopAgent(AgentIdentity agentIdentity) {
		logger.info("start agent of {}", agentIdentity);
		getComponent(ConsoleCommunication.class).sendToAddressedAgents(new AgentAddress(agentIdentity),
				new StopGrinderMessage());
	}
}
