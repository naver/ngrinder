package org.ngrinder;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import net.grinder.AgentDaemon.AgentShutDownListener;
import net.grinder.common.processidentity.AgentIdentity;
import net.grinder.util.thread.Condition;

import org.junit.Before;
import org.ngrinder.common.util.ThreadUtil;
import org.ngrinder.infra.AgentConfig;

abstract public class AbstractMuliGrinderTestBase {
	public AgentConfig agentConfig;

	@Before
	public void agentInit() {
		agentConfig = new AgentConfig();
		agentConfig.init();
	}

	/**
	 * Sleep quietly
	 * 
	 * @param milisecond
	 */
	public void sleep(long milisecond) {
		ThreadUtil.sleep(milisecond);
	}

	protected AgentIdentity getAgentIdentity(Set<AgentIdentity> agentSet, int index) {
		AgentIdentity agentIdentity = null;
		Iterator<AgentIdentity> iterator = agentSet.iterator();
		for (int i = 0; i <= index; i++) {
			agentIdentity = iterator.next();
		}
		return agentIdentity;
	}

	/** Waiting condition */
	public Condition condition = new Condition();

	public final class AgentShutDownSynchronizeListener implements AgentShutDownListener {

		private final Condition condition;

		public AgentShutDownSynchronizeListener(Condition condition) {
			this.condition = condition;

		}

		public void shutdownAgent() {
			synchronized (condition) {
				condition.notifyAll();
			}
		}
	}

	public void waitOnCondition(Condition codition, int timeout) {
		synchronized (condition) {
			condition.waitNoInterrruptException(2100);
		}
	}

	/**
	 * Returns a free port numbers on localhost, or less than give count entries if unable to find a free port.
	 * 
	 * @return a free port number on localhost, or less than give count entries if unable to find a free port
	 */
	public List<Integer> getFreePorts(int count) {
		List<Integer> ports = new ArrayList<Integer>();
		for (int i = 0; i < count; i++) {
			ports.add(getFreePort());
		}
		return ports;
	}

	/**
	 * Returns a free port number on localhost, or -1 if unable to find a free port.
	 * 
	 * @return a free port number on localhost, or -1 if unable to find a free port
	 */
	public int getFreePort() {
		ServerSocket socket = null;
		try {
			socket = new ServerSocket(0);
			return socket.getLocalPort();
		} catch (IOException e) {
		} finally {
			if (socket != null) {
				try {
					socket.close();
				} catch (IOException e) {
				}
			}
		}
		return -1;
	}

}
