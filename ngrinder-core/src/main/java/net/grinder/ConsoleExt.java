package net.grinder;

import java.util.Date;

import net.grinder.common.GrinderException;
import net.grinder.console.ConsoleFoundation;
import net.grinder.console.common.Resources;
import net.grinder.console.common.ResourcesImplementation;
import net.grinder.console.communication.ProcessControl;
import net.grinder.console.model.NGrinderSampleModelImplementation;
import net.grinder.console.model.SampleListener;
import net.grinder.console.model.SampleModel;
import net.grinder.console.model.SampleModelImplementation;
import net.grinder.console.textui.TextUI;
import net.grinder.statistics.StatisticsSet;

import org.picocontainer.MutablePicoContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.util.ReflectionTestUtils;

public class ConsoleExt {
	
	private static final Logger LOG = LoggerFactory.getLogger(ConsoleExt.class);
	
	private static final Resources resources = new ResourcesImplementation(
			"net.grinder.console.common.resources.Console");
	private static Date TPS_LESSTHAN_ZREO_TIME = null;

	/**
	 * Grinder console instance.
	 */
	//private Console console = null;
	
	private MutablePicoContainer consoleContainer;
	
	//private Object obj = new Object();
	
	/**
	 * Grinder console wrapper.
	 */
	private ConsoleFoundation m_consoleFoundation;
	
	private ProcessControl processControl;

    /**
	 * Grinder status.
	 */
	private boolean consoleStarted = false;
	
	public ConsoleExt() {
        final Logger logger = LoggerFactory.getLogger(resources
                .getString("shortTitle"));
        try {
            createInstance(resources, logger);
        } catch (GrinderException e) {
            LOG.error("Could not initialise grinder console.");
        }
	}
	
	public ConsoleExt(int port, String consoleName) {
		System.setProperty("grinder.console.consolePort", String.valueOf(port));
		consoleName = consoleName != null ? consoleName : resources.getString("shortTitle");
		final Logger logger = LoggerFactory.getLogger(consoleName);
		try {
			createInstance(resources, logger);
		} catch (GrinderException e) {
			LOG.error("Could not initialise grinder console.");
		}
	}

	/**
	 * @param args args
	 * @param resources resources
	 * @param logger logger
	 * @throws GrinderException GrinderException
	 */
	private void createInstance(Resources resources, Logger logger) throws GrinderException {
		m_consoleFoundation = new ConsoleFoundation(resources, logger);
		m_consoleFoundation.createUI(TextUI.class);
		// For debug
		// m_consoleFoundation.createUI(ConsoleUI.class);
		consoleContainer = (MutablePicoContainer)ReflectionTestUtils.getField(m_consoleFoundation, "m_container");

		consoleContainer.removeComponent(SampleModelImplementation.class);
		consoleContainer.addComponent(NGrinderSampleModelImplementation.class);
		
		processControl = (ProcessControl)consoleContainer.getComponent(ProcessControl.class);
		processControl.addProcessStatusListener(new ProcessControlListener());
		
		final SampleModel sampleModel = (SampleModel)consoleContainer.getComponent(NGrinderSampleModelImplementation.class);
		sampleModel.addTotalSampleListener(new SampleListener() {
			@Override
			public void update(StatisticsSet intervalStatistics,
				StatisticsSet cumulativeStatistics) {
				//GrinderAPI.GRAPH.setMaximum(sampleModel.getPeakTPSExpression().getDoubleValue(
				//	cumulativeStatistics));
				double tps = sampleModel.getTPSExpression().getDoubleValue(
					intervalStatistics);
				//GrinderAPI.GRAPH.add(tps);

				if (tps < 0.001) {
					//&& TPS_LESSTHAN_ZREO_TIME != null
					//&& (new Date().getTime() - TPS_LESSTHAN_ZREO_TIME.getTime()) >= 60000) {
					if (TPS_LESSTHAN_ZREO_TIME == null) {
						TPS_LESSTHAN_ZREO_TIME = new Date();
					} else if (new Date().getTime()
							- TPS_LESSTHAN_ZREO_TIME.getTime() >= 60000) {
						//GrinderAPI.stopTest(TestStopReasonEnum.LOW_TPS_ERR);
						LOG.warn("Test has been forced stop because of tps is less than 0.001 and sustain more than one minitue.");
					}
				} else {
					TPS_LESSTHAN_ZREO_TIME = null;
				}
			}
		});
	}

	/**
	 * Run console.
	 */
	private synchronized void run() {
		if (!consoleStarted) {
			consoleStarted = true;
			m_consoleFoundation.run();
		}
	}

	/**
	 * Create grinder console instance.
	 */
//	private void createInstance() {
//		synchronized (obj) {
//			if (console == null) {
//				final Resources resources = new ResourcesImplementation(
//						"net.grinder.console.common.resources.Console");
//
//				final Logger logger = LoggerFactory.getLogger(resources
//						.getString("shortTitle"));
//
//				try {
//					console = new Console(resources, logger);
//				} catch (GrinderException e) {
//					logger.error("Could not initialise grinder console.");
//				}
//			}
//		}
//	}

	/**
	 * Public api for bloc to start grinder console.
	 */
	public void startConsole() {
        run();
	}

	/**
	 * Public api for bloc to stop grinder console.
	 */
	public void shutdownConsole() {
		if (consoleStarted) {
			m_consoleFoundation.shutdown();
		}
	}

	/**
	 * @return ConsoleFoundation
	 */
	public ConsoleFoundation getConsoleFoundation() {
		return m_consoleFoundation;
	}
	
	public MutablePicoContainer getConsoleContainer () {
		return consoleContainer;
	}
	
	public ProcessControl getProcessControl() {
		return processControl;
	}
}
