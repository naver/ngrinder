// Copyright (C) 2007 - 2011 Philip Aston
// All rights reserved.
//
// This file is part of The Grinder software distribution. Refer to
// the file LICENSE which is part of The Grinder distribution for
// licensing details. The Grinder distribution is available on the
// Internet at http://grinder.sourceforge.net/
//
// THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
// "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
// LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS
// FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE
// COPYRIGHT HOLDERS OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
// INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
// (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
// SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
// HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
// STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
// ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
// OF THE POSSIBILITY OF SUCH DAMAGE.

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
import net.grinder.console.model.SampleModelImplementation;
import net.grinder.message.console.AgentControllerProcessReportMessage;
import net.grinder.message.console.AgentControllerState;
import net.grinder.messages.agent.StartGrinderMessage;
import net.grinder.messages.agent.StopGrinderMessage;
import net.grinder.messages.console.AgentAddress;
import net.grinder.util.ListenerSupport;

/**
 * Implementation of {@link ProcessControl}.
 * 
 * @author Philip Aston
 */
public class AgentProcessControlImplementation implements AgentProcessControl {

	private final ConsoleCommunication m_consoleCommunication;
	private Map<AgentIdentity, AgentStatus> m_agentMap = new ConcurrentHashMap<AgentIdentity, AgentStatus>();
	private final ListenerSupport<Listener> m_listeners = new ListenerSupport<Listener>();

	/**
	 * Period at which to update the listeners.
	 */
	private static final long UPDATE_PERIOD = 500;

	/**
	 * We keep a record of processes for a few seconds after they have been
	 * terminated.
	 * 
	 * Every FLUSH_PERIOD, process statuses are checked. Those haven't reported
	 * for a while are marked and are discarded if they still haven't been
	 * updated by the next FLUSH_PERIOD.
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

		final MessageDispatchRegistry messageDispatchRegistry = consoleCommunication.getMessageDispatchRegistry();

		messageDispatchRegistry.set(AgentControllerProcessReportMessage.class,
				new AbstractHandler<AgentControllerProcessReportMessage>() {
					public void handle(AgentControllerProcessReportMessage message) {
						addAgentStatusReport(message);
					}
				});
	}

	private void addAgentStatusReport(AgentControllerProcessReportMessage message) {
		AgentStatus agentStatus = getAgentStatus(message.getAgentIdentity());
		agentStatus.setAgentProcessStatus(message);
		m_newData = true;
	}

	private AgentStatus getAgentStatus(AgentIdentity agentIdentity) {
		synchronized (m_agentMap) {
			final AgentStatus existing = m_agentMap.get(agentIdentity);
			if (existing != null) {
				return existing;
			}
			final AgentStatus created = new AgentStatus(agentIdentity);
			m_agentMap.put(agentIdentity, created);
			return created;
		}
	}

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
	 * Interface for listeners to {@link SampleModelImplementation}.
	 */
	interface Listener extends EventListener {
		public void update(Map<AgentIdentity, AgentStatus> agentMap);
	}

	/**
	 * Callers are responsible for synchronization.
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
		boolean shouldPurge();
	}

	private abstract class AbstractTimedReference implements Purgable {
		private int m_purgeDelayCount;

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

		AgentReference(AgentControllerProcessReportMessage m_agentProcessReportMessage) {
			this.m_agentProcessReportMessage = m_agentProcessReportMessage;
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

		public AgentStatus(AgentIdentity agentIdentity) {
			setAgentProcessStatus(new UnknownAgentProcessReport(new AgentAddress(agentIdentity)));
		}

		public boolean shouldPurge() {
			return m_agentReference.shouldPurge();
		}

		public AgentControllerState getAgentControllerState() {
			AgentControllerProcessReportMessage m_agentProcessReportMessage = m_agentReference.m_agentProcessReportMessage;
			return m_agentProcessReportMessage == null ? AgentControllerState.UNKNOWN : m_agentProcessReportMessage
					.getState();
		}

		public void setAgentProcessStatus(AgentControllerProcessReportMessage message) {
			m_agentReference = new AgentReference(message);
		}
	}

	public void addListener(Listener listener) {
		m_listeners.add(listener);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * net.grinder.console.communication.AgentProcessControl#startAgent(java
	 * .util.Set, net.grinder.common.GrinderProperties)
	 */
	@Override
	public void startAgent(Set<AgentIdentity> agents, GrinderProperties properties) {
		final GrinderProperties propertiesToSend = properties != null ? properties : new GrinderProperties();
		for (AgentIdentity each : agents) {
			m_consoleCommunication.sendToAddressedAgents(new AgentAddress((AgentIdentity) each),
					new StartGrinderMessage(propertiesToSend, each.getNumber()));
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * net.grinder.console.communication.AgentProcessControl#stopAgent(net.grinder
	 * .common.processidentity.AgentIdentity)
	 */
	@Override
	public void stopAgent(AgentIdentity agentIdentity) {
		m_consoleCommunication.sendToAddressedAgents(new AgentAddress(agentIdentity), new StopGrinderMessage());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * net.grinder.console.communication.AgentProcessControl#getNumberOfLiveAgents
	 * ()
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
	 * @see
	 * net.grinder.console.communication.AgentProcessControl#getAgents(net.grinder
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
		return m_agentMap.keySet();
	}

	private static class UnknownAgentProcessReport extends AgentControllerProcessReportMessage {

		/**
		 * UUID
		 */
		private static final long serialVersionUID = -2758014000696737553L;

		public UnknownAgentProcessReport(AgentAddress address) {
			super(AgentControllerState.UNKNOWN);
			try {
				setAddress(address);
			} catch (CommunicationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		public AgentControllerState getState() {
			return AgentControllerState.UNKNOWN;
		}
	}

}
