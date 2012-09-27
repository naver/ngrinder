/*
 * Copyright (C) 2012 - 2012 NHN Corporation
 * All rights reserved.
 *
 * This file is part of The nGrinder software distribution. Refer to
 * the file LICENSE which is part of The nGrinder distribution for
 * licensing details. The nGrinder distribution is available on the
 * Internet at http://nhnopensource.org/ngrinder
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS
 * FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE
 * COPYRIGHT HOLDERS OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package net.grinder.console.communication;

import java.util.EventListener;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;

import net.grinder.common.GrinderProperties;
import net.grinder.common.processidentity.AgentIdentity;
import net.grinder.common.processidentity.ProcessIdentity;
import net.grinder.communication.CommunicationException;
import net.grinder.communication.MessageDispatchRegistry;
import net.grinder.communication.MessageDispatchRegistry.AbstractHandler;
import net.grinder.engine.communication.LogReportGrinderMessage;
import net.grinder.message.console.AgentControllerProcessReportMessage;
import net.grinder.message.console.AgentControllerState;
import net.grinder.messages.agent.StartGrinderMessage;
import net.grinder.messages.agent.StopGrinderMessage;
import net.grinder.messages.console.AgentAddress;
import net.grinder.util.ListenerSupport;
import net.grinder.util.ListenerSupport.Informer;

import org.ngrinder.monitor.controller.model.SystemDataModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementation of {@link AgentProcessControl}.
 * 
 * @author JunHo Yoon
 */
public class AgentProcessControlImplementation implements AgentProcessControl {

	private final ConsoleCommunication m_consoleCommunication;
	private Map<AgentIdentity, AgentStatus> m_agentMap = new ConcurrentHashMap<AgentIdentity, AgentStatus>();
	private final ListenerSupport<Listener> m_listeners = new ListenerSupport<Listener>();
	private final ListenerSupport<LogArrivedListener> m_logListeners = new ListenerSupport<LogArrivedListener>();

	private final static Logger logger = LoggerFactory.getLogger(AgentProcessControlImplementation.class);
	/**
	 * Period at which to update the listeners.
	 */
	private static final long UPDATE_PERIOD = 500;

	/**
	 * We keep a record of processes for a few seconds after they have been terminated.
	 * 
	 * Every FLUSH_PERIOD, process statuses are checked. Those haven't reported for a while are
	 * marked and are discarded if they still haven't been updated by the next FLUSH_PERIOD.
	 */
	private static final long FLUSH_PERIOD = 2000;

	private volatile boolean m_newData = false;

	/**
	 * Constructor.
	 * 
	 * @param timer
	 *            Timer that can be used to schedule housekeeping tasks.
	 * @param consoleCommunication
	 *            The console communication handler.
	 */
	public AgentProcessControlImplementation(Timer timer, ConsoleCommunication consoleCommunication) {
		m_consoleCommunication = consoleCommunication;
		timer.schedule(new TimerTask() {
			public void run() {
				update();
			}
		}, 0, UPDATE_PERIOD);

		timer.schedule(new TimerTask() {
			public void run() {
				synchronized (m_agentMap) {
					purge(m_agentMap);
				}
			}
		}, 0, FLUSH_PERIOD);
		final MessageDispatchRegistry messageDispatchRegistry = consoleCommunication
						.getMessageDispatchRegistry();

		messageDispatchRegistry.set(AgentControllerProcessReportMessage.class,
						new AbstractHandler<AgentControllerProcessReportMessage>() {
							public void handle(AgentControllerProcessReportMessage message) {
								addAgentStatusReport(message);
							}
						});

		messageDispatchRegistry.set(LogReportGrinderMessage.class,
						new AbstractHandler<LogReportGrinderMessage>() {
							public void handle(final LogReportGrinderMessage message) {
								m_logListeners.apply(new Informer<LogArrivedListener>() {
									@Override
									public void inform(LogArrivedListener listener) {
										listener.logArrived(message.getTestId(), message.getAddress(),
														message.getLogs());
									}
								});
							}
						});

	}

	/**
	 * Add Agent status report.
	 * 
	 * @param message
	 *            {@link AgentControllerProcessReportMessage}
	 */
	public void addAgentStatusReport(AgentControllerProcessReportMessage message) {
		AgentStatus agentStatus = getAgentStatus(message.getAgentIdentity());
		agentStatus.setAgentProcessStatus(message);
		m_newData = true;
	}

	/**
	 * Get agent status. It's for internal use.
	 * 
	 * @param agentIdentity
	 *            agent identity
	 * @return {@link AgentStatus}
	 */
	private AgentStatus getAgentStatus(AgentIdentity agentIdentity) {
		synchronized (m_agentMap) {
			final AgentStatus existing = m_agentMap.get(agentIdentity);
			if (existing != null) {
				m_agentMap.put(agentIdentity, existing);
				return existing;
			}

			final AgentStatus created = new AgentStatus(agentIdentity);
			m_agentMap.put(agentIdentity, created);
			return created;
		}
	}

	/**
	 * Update agent status.
	 */
	private void update() {
		if (!m_newData) {
			return;
		}

		m_newData = false;

		m_listeners.apply(new ListenerSupport.Informer<Listener>() {
			public void inform(Listener l) {
				l.update(new ConcurrentHashMap<AgentIdentity, AgentStatus>(m_agentMap));
			}
		});
	}

	/**
	 * Interface for listeners to SampleModelImplementation.
	 */
	interface Listener extends EventListener {
		/**
		 * Update agent status.
		 * 
		 * @param agentMap
		 *            agent map
		 */
		public void update(Map<AgentIdentity, AgentStatus> agentMap);
	}

	/**
	 * Callers are for synchronization.
	 * 
	 * @param purgableMap
	 *            map for {@link ProcessIdentity}
	 */
	private void purge(Map<? extends ProcessIdentity, ? extends Purgable> purgableMap) {

		final Set<ProcessIdentity> zombies = new HashSet<ProcessIdentity>();

		for (Entry<? extends ProcessIdentity, ? extends Purgable> entry : purgableMap.entrySet()) {
			if (entry.getValue().shouldPurge()) {
				zombies.add(entry.getKey());
			}
		}

		if (zombies.size() > 0) {
			purgableMap.keySet().removeAll(zombies);
			m_newData = true;
		}
	}

	private interface Purgable {
		/**
		 * check it should be purged.
		 * 
		 * @return true if purse is necessary
		 */
		boolean shouldPurge();
	}

	private abstract class AbstractTimedReference implements Purgable {
		private int m_purgeDelayCount;

		@Override
		public boolean shouldPurge() {
			// Processes have a short time to report - see the javadoc for
			// FLUSH_PERIOD.
			if (m_purgeDelayCount > 0) {
				return true;
			}

			++m_purgeDelayCount;

			return false;
		}
	}

	private final class AgentReference extends AbstractTimedReference {
		private final AgentControllerProcessReportMessage m_agentProcessReportMessage;

		/**
		 * Constructor.
		 * 
		 * @param agentProcessReportMessage
		 *            {@link AgentControllerProcessReportMessage}
		 */
		AgentReference(AgentControllerProcessReportMessage agentProcessReportMessage) {
			this.m_agentProcessReportMessage = agentProcessReportMessage;
		}

		@Override
		public boolean shouldPurge() {
			final boolean purge = super.shouldPurge();
			if (purge) {
				// Protected against race with add since the caller holds
				// m_agentIdentityToAgentAndWorkers, and we are about to be
				// removed from m_agentIdentityToAgentAndWorkers.
				m_agentMap.remove(m_agentProcessReportMessage.getAgentIdentity());
			}
			return purge;
		}
	}

	private final class AgentStatus implements Purgable {
		private volatile AgentReference m_agentReference;

		/**
		 * Constructor.
		 * 
		 * @param agentIdentity
		 *            agent identity
		 */
		public AgentStatus(AgentIdentity agentIdentity) {
			setAgentProcessStatus(new UnknownAgentProcessReport(new AgentAddress(agentIdentity)));
		}

		@Override
		public boolean shouldPurge() {
			return m_agentReference.shouldPurge();
		}

		/**
		 * Get agent controller status.
		 * 
		 * @return {@link AgentControllerState} member
		 */
		public AgentControllerState getAgentControllerState() {
			AgentControllerProcessReportMessage agentProcessReport = m_agentReference.m_agentProcessReportMessage;
			return agentProcessReport == null ? AgentControllerState.UNKNOWN : agentProcessReport.getState();
		}

		public void setAgentProcessStatus(AgentControllerProcessReportMessage message) {
			logger.trace("agent perf status on {} is {}", message.getAgentIdentity(), message.getSystemDataModel());

			m_agentReference = new AgentReference(message);
		}

		public SystemDataModel getSystemDataModel() {
			return m_agentReference == null ? null : m_agentReference.m_agentProcessReportMessage
							.getSystemDataModel();
		}

		public int getConnectingPort() {
			return m_agentReference == null ? 0 : m_agentReference.m_agentProcessReportMessage
							.getConnectingPort();
		}
	}

	/**
	 * Add process control {@link Listener}.
	 * 
	 * @param listener
	 *            listener to be added
	 */
	public void addListener(Listener listener) {
		m_listeners.add(listener);
	}

	/**
	 * Add Log control {@link LogArrivedListener}
	 * 
	 * @param listener
	 *            listener to be added
	 */
	public void addLogArrivedListener(LogArrivedListener listener) {
		m_logListeners.add(listener);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.grinder.console.communication.AgentProcessControl#startAgent(java .util.Set,
	 * net.grinder.common.GrinderProperties)
	 */
	@Override
	public void startAgent(Set<AgentIdentity> agents, GrinderProperties properties) {
		final GrinderProperties propertiesToSend = properties != null ? properties : new GrinderProperties();
		for (AgentIdentity each : agents) {
			m_consoleCommunication.sendToAddressedAgents(new AgentAddress(each), new StartGrinderMessage(
							propertiesToSend, each.getNumber()));
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.grinder.console.communication.AgentProcessControl#stopAgent(net.grinder
	 * .common.processidentity.AgentIdentity)
	 */
	@Override
	public void stopAgent(AgentIdentity agentIdentity) {
		m_consoleCommunication.sendToAddressedAgents(new AgentAddress(agentIdentity),
						new StopGrinderMessage());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.grinder.console.communication.AgentProcessControl#getNumberOfLiveAgents ()
	 */
	@Override
	public int getNumberOfLiveAgents() {
		synchronized (m_agentMap) {
			return m_agentMap.size();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.grinder.console.communication.AgentProcessControl#getAgents(net.grinder
	 * .message.console.AgentControllerState, int)
	 */
	@Override
	public Set<AgentIdentity> getAgents(AgentControllerState state, int count) {
		count = count == 0 ? Integer.MAX_VALUE : count;
		synchronized (m_agentMap) {
			int i = 0;
			Set<AgentIdentity> agents = new HashSet<AgentIdentity>();
			for (Map.Entry<AgentIdentity, AgentStatus> each : m_agentMap.entrySet()) {
				if (each.getValue().getAgentControllerState().equals(state) && ++i <= count) {
					agents.add(each.getKey());
				}
			}
			return agents;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.grinder.console.communication.AgentProcessControl#getAllAgents()
	 */
	@Override
	public Set<AgentIdentity> getAllAgents() {
		synchronized (m_agentMap) {
			return m_agentMap.keySet();
		}
	}

	private static class UnknownAgentProcessReport extends AgentControllerProcessReportMessage {

		/** UUID. */
		private static final long serialVersionUID = -2758014000696737553L;

		/**
		 * Constructor.
		 * 
		 * @param address
		 *            {@link AgentAddress} in which the agent process is not known.
		 */
		public UnknownAgentProcessReport(AgentAddress address) {
			super(AgentControllerState.UNKNOWN, null, 0);
			try {
				setAddress(address);
			} catch (CommunicationException e) {
				logger.error("Error while setAdress" + address, e);
			}
		}

		public AgentControllerState getState() {
			return AgentControllerState.UNKNOWN;
		}
	}

	@Override
	public AgentControllerState getAgentControllerState(AgentIdentity agentIdentity) {
		return getAgentStatus(agentIdentity).getAgentControllerState();
	}

	@Override
	public SystemDataModel getSystemDataModel(AgentIdentity agentIdentity) {
		return getAgentStatus(agentIdentity).getSystemDataModel();
	}

	@Override
	public int getAgentConnectingPort(AgentIdentity agentIdentity) {
		return getAgentStatus(agentIdentity).getConnectingPort();
	}

}
