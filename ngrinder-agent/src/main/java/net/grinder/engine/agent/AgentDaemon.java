package net.grinder.engine.agent;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.concurrent.Semaphore;

import net.grinder.common.GrinderException;
import net.grinder.util.Sleeper;
import net.grinder.util.Sleeper.ShutdownException;
import net.grinder.util.SleeperImplementation;
import net.grinder.util.StandardTimeAuthority;

import org.ngrinder.manager.GrinderWrapper;
import org.ngrinder.util.HudsonPluginConfig;
import org.ngrinder.util.ReflectionUtil;
import org.slf4j.Logger;

public final class AgentDaemon implements Agent {

	private final Logger m_logger;
	private final long m_sleepTime;
	private final Agent m_delegateAgent;
	private final Sleeper m_sleeper;
	private final Thread m_shutdownHook;

	private volatile boolean shutdown;
	private Semaphore sem = new Semaphore(1);

	/**
	 * Constructor for AgentDaemon.
	 * 
	 * @param logger
	 *            A Logger.
	 * @param sleepTime
	 *            Time in milliseconds between connection attempts.
	 * @param agent
	 *            Delegate Agent that we want to run.
	 */
	public AgentDaemon(Logger logger, long sleepTime, Agent agent) {
		this(logger, sleepTime, agent, new SleeperImplementation(new StandardTimeAuthority(), logger, 1, 0));
	}

	/**
	 * Package scope for unit tests.
	 */
	AgentDaemon(Logger logger, long sleepTime, Agent agent, Sleeper sleeper) {
		m_logger = logger;
		m_delegateAgent = agent;
		m_sleepTime = sleepTime;
		m_sleeper = sleeper;
		m_shutdownHook = new Thread(new ShutdownHook());

		shutdown = false;
	}

	/**
	 * waiting for the agentImplementation thread to stop.
	 */
	public void waitForStop() {
		try {
			sem.acquire();
			sem.release();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Start the agent.
	 * 
	 * @throws GrinderException
	 *             If an error occurs.
	 */
	public void run() throws GrinderException {

		Runtime.getRuntime().addShutdownHook(m_shutdownHook);

		try {
			while (true) {
				sem.acquire();
				try {
					m_delegateAgent.run();

					// reset m_alternateFile in AgentImplementation, to avoid it
					// use previous properties file to run
					URL url = GrinderWrapper.class.getResource("/grinder.properties");
					ReflectionUtil.setFieldValue(m_delegateAgent, "m_alternateFile", new File(url.toURI()));

					// reset hudson config, otherwise, the agent will send the
					// log to hudson always
					HudsonPluginConfig.setHudsonHost(null);
					HudsonPluginConfig.setHudsonPort(0);
					HudsonPluginConfig.setNeedToHudson(false);
				} finally {
					sem.release();
				}
				m_logger.info("agent finished");

				if (shutdown) {
					break;
				}
				m_sleeper.sleepNormal(m_sleepTime);
			}
		} catch (ShutdownException e) {
			e.printStackTrace();
		} catch (URISyntaxException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Shut down the agent.
	 */
	public void shutdown() {
		shutdown = true;
		m_sleeper.shutdown();
		m_delegateAgent.shutdown();
	}

	/**
	 * For unit tests.
	 * 
	 * @return The shutdown hook.
	 */
	Thread getShutdownHook() {
		return m_shutdownHook;
	}

	private class ShutdownHook implements Runnable {
		public void run() {
			shutdown();
		}
	}
}
