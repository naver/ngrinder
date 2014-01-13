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
package net.grinder.console;

import net.grinder.common.GrinderException;
import net.grinder.communication.MessageDispatchRegistry;
import net.grinder.communication.MessageDispatchRegistry.AbstractHandler;
import net.grinder.console.common.ErrorQueue;
import net.grinder.console.common.Resources;
import net.grinder.console.communication.ConsoleCommunication;
import net.grinder.console.communication.ConsoleCommunicationImplementationEx;
import net.grinder.console.communication.DistributionControlImplementation;
import net.grinder.console.communication.ProcessControlImplementation;
import net.grinder.console.communication.server.DispatchClientCommands;
import net.grinder.console.distribution.FileDistributionImplementation;
import net.grinder.console.distribution.WireFileDistribution;
import net.grinder.console.model.*;
import net.grinder.console.synchronisation.WireDistributedBarriers;
import net.grinder.engine.console.ErrorHandlerImplementation;
import net.grinder.messages.console.RegisterExpressionViewMessage;
import net.grinder.messages.console.RegisterTestsMessage;
import net.grinder.messages.console.ReportStatisticsMessage;
import net.grinder.statistics.StatisticsServicesImplementation;
import net.grinder.util.StandardTimeAuthority;
import net.grinder.util.thread.Condition;
import org.apache.commons.lang.StringUtils;
import org.picocontainer.DefaultPicoContainer;
import org.picocontainer.MutablePicoContainer;
import org.picocontainer.Parameter;
import org.picocontainer.behaviors.Caching;
import org.picocontainer.parameters.ComponentParameter;
import org.picocontainer.parameters.ConstantParameter;
import org.slf4j.Logger;

import java.util.Timer;

import static org.ngrinder.common.util.ExceptionUtils.processException;
import static org.ngrinder.common.util.NoOp.noOp;

/**
 * Extension of {@link ConsoleFoundation} for nGrinder use.
 * 
 * @author JunHo Yoon
 * @since 3.0
 */
public class ConsoleFoundationEx {

	private final MutablePicoContainer m_container;
	private final Timer m_timer;
	private boolean m_shutdown = false;

	private final Condition m_eventSyncCondition;

	/**
	 * Constructor. Allows properties to be specified.
	 * 
	 * @param resources		Console resources
	 * @param logger		Logger.
	 * @param properties	The properties.
	 * @param eventSyncCondition	event synchronization condition.
	 * @exception GrinderException	occurs If an error occurs.
	 */
	public ConsoleFoundationEx(Resources resources, Logger logger, ConsoleProperties properties,
					Condition eventSyncCondition) throws GrinderException {
		m_eventSyncCondition = eventSyncCondition;
		m_container = new DefaultPicoContainer(new Caching());
		m_container.addComponent(logger);
		m_container.addComponent(resources);
		m_container.addComponent(properties);
		m_container.addComponent(StatisticsServicesImplementation.getInstance());
		m_container.addComponent(new StandardTimeAuthority());

		m_container.addComponent(SampleModelImplementationEx.class);
		m_container.addComponent(SampleModelViewsImplementation.class);
		m_container.addComponent(ConsoleCommunicationImplementationEx.class);
		m_container.addComponent(DistributionControlImplementation.class);
		m_container.addComponent(ProcessControlImplementation.class);
		m_timer = new Timer(true);
		m_container.addComponent(m_timer);

		//noinspection RedundantArrayCreation
		m_container.addComponent(FileDistributionImplementation.class, FileDistributionImplementation.class,
						new Parameter[] { new ComponentParameter(DistributionControlImplementation.class),
								new ComponentParameter(ProcessControlImplementation.class),
								new ConstantParameter(properties.getDistributionDirectory()),
								new ConstantParameter(properties.getDistributionFileFilterPattern()), });

		m_container.addComponent(DispatchClientCommands.class);
		m_container.addComponent(WireFileDistribution.class);
		m_container.addComponent(WireMessageDispatch.class);
		m_container.addComponent(WireDistributedBarriers.class);
		m_container.addComponent(ErrorQueue.class);

		ErrorQueue errorQueue = m_container.getComponent(ErrorQueue.class);
		errorQueue.setErrorHandler(new ErrorHandlerImplementation(logger));
	}

	/**
	 * Get the component of the given type.
	 * 
	 * @param <T>	component type
	 * @param componentType	component type class
	 * @return component
	 */
	public <T> T getComponent(Class<T> componentType) {
		return m_container.getComponent(componentType);
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
			noOp();
		}
		if (m_container.getLifecycleState().isStarted()) {
			m_container.stop();
		}
	}

	private String getConsoleInfo() {
		ConsoleProperties consoleProperties = m_container.getComponent(ConsoleProperties.class);
		return StringUtils.defaultIfBlank(consoleProperties.getConsoleHost(), "localhost") + ":"
						+ consoleProperties.getConsolePort();
	}

	/**
	 * Console message event loop.
	 * 
	 * Dispatches communication messages appropriately. Blocks until we are {@link #shutdown()}.
	 */
	public void run() {
		if (m_shutdown) {
			throw processException("console can not run because it's shutdowned");
		}
		m_container.start();
		ConsoleCommunication communication = m_container.getComponent(ConsoleCommunication.class);
		// Need to request components, or they won't be instantiated.
		m_container.getComponent(WireMessageDispatch.class);
		m_container.getComponent(WireFileDistribution.class);
		m_container.getComponent(WireDistributedBarriers.class);
		m_container.getComponent(Logger.class).info("console {} has been started", getConsoleInfo());
		synchronized (m_eventSyncCondition) {
			m_eventSyncCondition.notifyAll();
		}
		while (communication.processOneMessage()) {
			// Process until communication is shut down.
			noOp();
		}

	}

	/**
	 * @return the m_container
	 */
	public MutablePicoContainer getContainer() {
		return m_container;
	}

	/**
	 * Factory that wires up the message dispatch.
	 * 
	 * <p>
	 * Must be public for PicoContainer.
	 * </p>
	 * 
	 * @see WireFileDistribution
	 */
	public static class WireMessageDispatch {

		/**
		 * Constructor.
		 * 
		 * @param communication	Console communication.
		 * @param model			Console sample model.
		 * @param sampleModelViews	Console sample model views
		 * @param dispatchClientCommands	Client command dispatcher.
		 */
		public WireMessageDispatch(ConsoleCommunication communication, final SampleModel model,
						final SampleModelViews sampleModelViews, DispatchClientCommands dispatchClientCommands) {

			final MessageDispatchRegistry messageDispatchRegistry = communication.getMessageDispatchRegistry();

			messageDispatchRegistry.set(RegisterTestsMessage.class, new AbstractHandler<RegisterTestsMessage>() {
				public void handle(RegisterTestsMessage message) {
					model.registerTests(message.getTests());
				}
			});

			messageDispatchRegistry.set(ReportStatisticsMessage.class, new AbstractHandler<ReportStatisticsMessage>() {
				public void handle(ReportStatisticsMessage message) {
					model.addTestReport(message.getStatisticsDelta());
				}
			});

			messageDispatchRegistry.set(RegisterExpressionViewMessage.class,
							new AbstractHandler<RegisterExpressionViewMessage>() {
								public void handle(RegisterExpressionViewMessage message) {
									sampleModelViews.registerStatisticExpression(message.getExpressionView());
								}
							});

			dispatchClientCommands.registerMessageHandlers(messageDispatchRegistry);
		}
	}
}
