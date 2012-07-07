package net.grinder;

import java.util.Timer;

import net.grinder.common.GrinderException;
import net.grinder.console.common.ErrorQueue;
import net.grinder.console.common.Resources;
import net.grinder.console.communication.AgentProcessControlImplementation;
import net.grinder.console.communication.ConsoleCommunication;
import net.grinder.console.communication.ConsoleCommunicationImplementation;
import net.grinder.console.model.ConsoleProperties;
import net.grinder.engine.console.ErrorHandlerImplementation;
import net.grinder.util.StandardTimeAuthority;
import net.grinder.util.thread.Condition;

import org.ngrinder.common.exception.NGrinderRuntimeException;
import org.picocontainer.DefaultPicoContainer;
import org.picocontainer.behaviors.Caching;
import org.slf4j.Logger;

public class AgentControllerServer {
	private DefaultPicoContainer m_container;
	private Timer m_timer;
	private boolean m_shutdown = false;
	private final Condition m_eventSyncCondition;

	public AgentControllerServer(Resources resources, Logger logger, ConsoleProperties properties,
			Condition eventSyncCondition) throws GrinderException {
		m_eventSyncCondition = eventSyncCondition;
		m_container = new DefaultPicoContainer(new Caching());
		m_container.addComponent(logger);
		m_container.addComponent(resources);
		m_container.addComponent(properties);
		m_container.addComponent(new StandardTimeAuthority());
		m_container.addComponent(ConsoleCommunicationImplementation.class);
		m_container.addComponent(AgentProcessControlImplementation.class);
		m_timer = new Timer(true);
		m_container.addComponent(m_timer);

		// m_container.addComponent(WireEnhancedProcessReportMessage.class);
		m_container.addComponent(ErrorQueue.class);
		ErrorQueue errorQueue = m_container.getComponent(ErrorQueue.class);
		errorQueue.setErrorHandler(new ErrorHandlerImplementation(logger));
	}

	public void run() {
		if (m_shutdown) {
			throw new NGrinderRuntimeException("console can not run becaz it's shutdowned");
		}
		m_container.start();
		m_container.getComponent(AgentProcessControlImplementation.class);
		ConsoleCommunication communication = m_container.getComponent(ConsoleCommunication.class);
		synchronized (m_eventSyncCondition) {
			m_eventSyncCondition.notifyAll();
		}
		while (communication.processOneMessage()) {
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
		m_timer.cancel();
		if (m_container.getLifecycleState().isStarted())
			m_container.stop();
	}

	public <T> T getComponent(Class<T> componentType) {
		return m_container.getComponent(componentType);
	}

}
