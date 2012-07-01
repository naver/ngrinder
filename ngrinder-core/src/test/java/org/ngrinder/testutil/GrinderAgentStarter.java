package org.ngrinder.testutil;

import java.io.File;

import net.grinder.common.GrinderException;
import net.grinder.engine.agent.AgentDaemon;
import net.grinder.engine.agent.AgentImplementation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.util.ReflectionTestUtils;

/**
 * This class is used to start a grinder agent at local host. 
 * @author Mavlarn
 *
 */
public class GrinderAgentStarter {

	private static AgentDaemon agent;
	
	public static void startLocalGrinderAgent() {
	   File propFile = new File("grinder.properties");
	   startLocalGrinderAgent(propFile);
	}

	public static void startLocalGrinderAgent(File propFile) {
		Logger logger = LoggerFactory.getLogger("agent");
		logger.debug("Start a agent in local for test.");

		try {
			agent = new AgentDaemon(logger, 10000, new AgentImplementation(logger, propFile, false));
			Thread hook = (Thread)ReflectionTestUtils.getField(agent, "m_shutdownHook");
			Runtime.getRuntime().removeShutdownHook(hook);
		} catch (GrinderException e1) {
			e1.printStackTrace();
			return;
		}
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				try {
					agent.run();
				} catch (GrinderException e) {
					e.printStackTrace();
				}				
			}
		}).start();
	}

	public static void stopLocalGrinderAgent() {
		if (agent != null) {
			agent.shutdown();
		}
	}
}
