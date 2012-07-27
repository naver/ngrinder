package net.grinder.console;

import java.util.Date;
import java.util.Timer;

import net.grinder.common.GrinderException;
import net.grinder.communication.MessageDispatchRegistry;
import net.grinder.communication.MessageDispatchRegistry.AbstractHandler;
import net.grinder.console.common.ErrorQueue;
import net.grinder.console.common.Resources;
import net.grinder.console.communication.ConsoleCommunication;
import net.grinder.console.communication.ConsoleCommunicationImplementation;
import net.grinder.console.communication.DistributionControlImplementation;
import net.grinder.console.communication.ProcessControlImplementation;
import net.grinder.console.communication.WireEnhancedProcessReportMessage;
import net.grinder.console.communication.server.DispatchClientCommands;
import net.grinder.console.distribution.FileDistributionImplementation;
import net.grinder.console.distribution.WireFileDistribution;
import net.grinder.console.model.ConsoleProperties;
import net.grinder.console.model.SampleListener;
import net.grinder.console.model.SampleModel;
import net.grinder.console.model.SampleModelImplementationEx;
import net.grinder.console.model.SampleModelViews;
import net.grinder.console.model.SampleModelViewsImplementation;
import net.grinder.console.synchronisation.WireDistributedBarriers;
import net.grinder.engine.console.ErrorHandlerImplementation;
import net.grinder.messages.console.RegisterExpressionViewMessage;
import net.grinder.messages.console.RegisterTestsMessage;
import net.grinder.messages.console.ReportStatisticsMessage;
import net.grinder.statistics.StatisticsServicesImplementation;
import net.grinder.statistics.StatisticsSet;
import net.grinder.util.StandardTimeAuthority;
import net.grinder.util.thread.Condition;

import org.ngrinder.common.exception.NGrinderRuntimeException;
import org.picocontainer.DefaultPicoContainer;
import org.picocontainer.MutablePicoContainer;
import org.picocontainer.Parameter;
import org.picocontainer.behaviors.Caching;
import org.picocontainer.parameters.ComponentParameter;
import org.picocontainer.parameters.ConstantParameter;
import org.slf4j.Logger;

/**
 * grinders {@link ConsoleFoundation} extension.
 * 
 * @author JunHo Yoon
 * 
 */
public class ConsoleFoundationEx {

	private final MutablePicoContainer m_container;
	private final Timer m_timer;
	private boolean m_shutdown = false;

	private final Condition m_eventSyncCondition;
	private static Date TPS_LESSTHAN_ZREO_TIME = null;

	private Logger m_logger;

	/**
	 * Constructor. Allows properties to be specified.
	 * 
	 * @param resources
	 *            Console resources
	 * @param logger
	 *            Logger.
	 * @param timer
	 *            A timer.
	 * @param properties
	 *            The properties.
	 * 
	 * @exception GrinderException
	 *                If an error occurs.
	 */
	public ConsoleFoundationEx(Resources resources, Logger logger, ConsoleProperties properties,
			Condition eventSyncCondition) throws GrinderException {
		m_logger = logger;
		m_eventSyncCondition = eventSyncCondition;
		m_container = new DefaultPicoContainer(new Caching());
		m_container.addComponent(logger);
		m_container.addComponent(resources);
		m_container.addComponent(properties);
		m_container.addComponent(StatisticsServicesImplementation.getInstance());
		m_container.addComponent(new StandardTimeAuthority());

		m_container.addComponent(SampleModelImplementationEx.class);
		m_container.addComponent(SampleModelViewsImplementation.class);
		m_container.addComponent(ConsoleCommunicationImplementation.class);
		m_container.addComponent(DistributionControlImplementation.class);
		m_container.addComponent(ProcessControlImplementation.class);
		m_timer = new Timer(true);
		m_container.addComponent(m_timer);

		m_container.addComponent(
				FileDistributionImplementation.class,
				FileDistributionImplementation.class,
				new Parameter[] { new ComponentParameter(DistributionControlImplementation.class),
						new ComponentParameter(ProcessControlImplementation.class),
						new ConstantParameter(properties.getDistributionDirectory()),
						new ConstantParameter(properties.getDistributionFileFilterPattern()), });

		m_container.addComponent(DispatchClientCommands.class);

		m_container.addComponent(WireFileDistribution.class);

		m_container.addComponent(WireMessageDispatch.class);

		m_container.addComponent(WireDistributedBarriers.class);

		m_container.addComponent(WireEnhancedProcessReportMessage.class);

		m_container.addComponent(ErrorQueue.class);

		ErrorQueue errorQueue = m_container.getComponent(ErrorQueue.class);
		errorQueue.setErrorHandler(new ErrorHandlerImplementation(logger));
	}

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
		m_timer.cancel();
		if (m_container.getLifecycleState().isStarted())
			m_container.stop();
	}

	/**
	 * Console message event loop. Dispatches communication messages appropriately. Blocks until we are
	 * {@link #shutdown()}.
	 */
	public void run() {
		if (m_shutdown) {
			throw new NGrinderRuntimeException("console can not run becaz it's shutdowned");
		}
		m_container.start();

		final SampleModel sampleModel = (SampleModel) m_container.getComponent(SampleModelImplementationEx.class);
		sampleModel.addTotalSampleListener(new SampleListener() {
			@Override
			public void update(StatisticsSet intervalStatistics, StatisticsSet cumulativeStatistics) {
				double tps = sampleModel.getTPSExpression().getDoubleValue(intervalStatistics);
				// If the tps is low that it's can be the agents or scripts goes wrong.
				if (tps < 0.001) {
					if (TPS_LESSTHAN_ZREO_TIME == null) {
						TPS_LESSTHAN_ZREO_TIME = new Date();
					} else if (new Date().getTime() - TPS_LESSTHAN_ZREO_TIME.getTime() >= 60000) {
						// FIXME : they are not really stop. What's wrong?
						m_logger.warn("Test has been forced stop because of tps is less than 0.001 and sustain more than one minitue.");
					}
				} else {
					TPS_LESSTHAN_ZREO_TIME = null;
				}
			}
		});
		ConsoleCommunication communication = m_container.getComponent(ConsoleCommunication.class);
		// Need to request components, or they won't be instantiated.
		m_container.getComponent(WireMessageDispatch.class);
		m_container.getComponent(WireFileDistribution.class);
		m_container.getComponent(WireDistributedBarriers.class);
		m_container.getComponent(WireEnhancedProcessReportMessage.class);
		m_container.getComponent(Logger.class).info("{} console has been stated", "test");
		synchronized (m_eventSyncCondition) {
			m_eventSyncCondition.notifyAll();
		}
		while (communication.processOneMessage()) {
			// Process until communication is shut down.
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
		 * @param communication
		 *            Console communication.
		 * @param model
		 *            Console sample model.
		 * @param sampleModelViews
		 *            Console sample model views
		 * @param dispatchClientCommands
		 *            Client command dispatcher.
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
