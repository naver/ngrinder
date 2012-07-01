package net.grinder;

import java.util.ArrayList;
import java.util.List;

import net.grinder.common.GrinderException;
import net.grinder.common.processidentity.AgentIdentity;
import net.grinder.console.ConsoleFoundationEx;
import net.grinder.console.common.Resources;
import net.grinder.console.common.ResourcesImplementation;
import net.grinder.console.communication.ProcessControl;
import net.grinder.console.communication.ProcessControlImplementation;
import net.grinder.console.model.ConsoleProperties;
import net.grinder.util.AllocateLowestNumber;
import net.grinder.util.ConsolePropertiesFactory;
import net.grinder.util.ReflectionUtil;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SingleConsole {
	private final ConsoleProperties consoleProperties;
	private Thread thread;
	private ConsoleFoundationEx consoleFoundation;
	public final static Resources resources = new ResourcesImplementation(
			"net.grinder.console.common.resources.Console");

	public final static Logger logger = LoggerFactory.getLogger(resources.getString("shortTitle"));

	public SingleConsole(String ip, int port) {
		this(ip, port, ConsolePropertiesFactory.createEmptyConsoleProperties());
	}

	public SingleConsole(String ip, int port, ConsoleProperties consoleProperties) {
		this.consoleProperties = consoleProperties;
		try {
			this.getConsoleProperties().setConsoleHost(ip);
			this.getConsoleProperties().setConsolePort(port);
			this.consoleFoundation = new ConsoleFoundationEx(resources, logger, consoleProperties);
		} catch (GrinderException e) {
			throw new RuntimeException(e);
		}
	}

	public SingleConsole(int port) {
		this("", port);
	}

	public void start() {
		thread = new Thread(new Runnable() {
			public void run() {
				consoleFoundation.run();
			}
		});
		thread.setDaemon(true);
		thread.start();
	}

	/**
	 * For test
	 */
	public void startSync() {
		consoleFoundation.run();
	}

	public void shutdown() {
		try {
			consoleFoundation.shutdown();
			thread.join();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public int getAllAttachedAgentsCount() {
		return ((ProcessControlImplementation) consoleFoundation.getComponent(ProcessControl.class))
				.getNumberOfLiveAgents();
	}

	public List<AgentIdentity> getAllAttachedAgents() {
		final List<AgentIdentity> agentIdentities = new ArrayList<AgentIdentity>();
		AllocateLowestNumber agentIdentity = (AllocateLowestNumber) ReflectionUtil
				.getFieldValue((ProcessControlImplementation) consoleFoundation.getComponent(ProcessControl.class),
						"m_agentNumberMap");
		agentIdentity.forEach(new AllocateLowestNumber.IteratorCallback() {
			public void objectAndNumber(Object object, int number) {
				agentIdentities.add((AgentIdentity) object);
			}
		});
		return agentIdentities;
	}

	/**
	 * @return the consoleFoundation
	 */
	public <T> T getConsoleComponent(Class<T> componentType) {
		return consoleFoundation.getComponent(componentType);
	}

	public ConsoleProperties getConsoleProperties() {
		return consoleProperties;
	}
}
