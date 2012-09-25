package net.grinder;

import java.util.Timer;

import net.grinder.common.GrinderException;
import net.grinder.console.common.ErrorQueue;
import net.grinder.console.common.Resources;
import net.grinder.console.communication.AgentProcessControlImplementation;
import net.grinder.console.communication.ConsoleCommunication;
import net.grinder.console.communication.ConsoleCommunicationImplementationEx;
import net.grinder.console.model.ConsoleProperties;
import net.grinder.engine.console.ErrorHandlerImplementation;
import net.grinder.util.StandardTimeAuthority;
import net.grinder.util.thread.Condition;

import org.ngrinder.common.exception.NGrinderRuntimeException;
import org.picocontainer.DefaultPicoContainer;
import org.picocontainer.behaviors.Caching;
import org.slf4j.Logger;

/**
 * Agent Controller which controls agent behavior. This class is subject to
 * synchronized. So if you want to daemon, please refer
 * {@link AgentControllerServerDaemon}
 * 
 * @author JunHo Yoon
 * @since 3.0
 */
public class AgentControllerServer {
	private DefaultPicoContainer m_container;
	private Timer m_timer;
	private boolean m_shutdown = false;
	private final Condition m_eventSyncCondition;

	/**
	 * Constructor.
	 * 
	 * @param resources
	 *            message resource
	 * @param logger
	 *            logger
	 * @param properties
	 *            {@link ConsoleProperties}
	 * @param eventSyncCondition
	 *            event synchronized condition to synchronize server stop phase.
	 * @throws GrinderException
	 *             occurs when error is
	 */
	public AgentControllerServer(Resources resources, Logger logger, ConsoleProperties properties, Condition eventSyncCondition)
			throws GrinderException {
		m_eventSyncCondition = eventSyncCondition;
		m_container = new DefaultPicoContainer(new Caching());
		m_container.addComponent(logger);
		m_container.addComponent(resources);
		m_container.addComponent(properties);
		m_container.addComponent(new StandardTimeAuthority());
		m_container.addComponent(ConsoleCommunicationImplementationEx.class);
		m_container.addComponent(AgentProcessControlImplementation.class);
		m_timer = new Timer(true);
		m_container.addComponent(m_timer);

		// m_container.addComponent(WireEnhancedProcessReportMessage.class);
		m_container.addComponent(ErrorQueue.class);
		ErrorQueue errorQueue = m_container.getComponent(ErrorQueue.class);
		errorQueue.setErrorHandler(new ErrorHandlerImplementation(logger));
	}

	/**
	 * Run agent controller in synchronized way.
	 */
	public void run() {
		if (m_shutdown) {
			throw new NGrinderRuntimeException("console can not run becaz it's shutdowned");
		}
		m_container.start();
		m_container.getComponent(AgentProcessControlImplementation.class);
		ConsoleCommunication communication = m_container.getComponent(ConsoleCommunication.class);
		synchronized (m_eventSyncCondition) {
			// Now ready to work
			m_eventSyncCondition.notifyAll();
		}
		// CHECKSTYLE:OFF
		while (communication.processOneMessage()) {
			// Fall through
			// Process until communication is shut down.
		}
	}

	/**
	 * Shut down the console.
	 * 
	 */
	public void shutdown() {
		m_shutdown = true;
		m_container.getComponent(ConsoleCommunication.class).shutdown();
		try {
			m_timer.cancel();
		} catch (Exception e) {
		}
		if (m_container.getLifecycleState().isStarted()) {
			m_container.stop();
		}
	}

	/**
	 * Get internal component.
	 * 
	 * @param componentType
	 *            component type class
	 * @param <T>
	 *            component type
	 * @return component
	 */
	public <T> T getComponent(Class<T> componentType) {
		return m_container.getComponent(componentType);
	}
}
