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
package net.grinder.console.communication;

import net.grinder.common.GrinderProperties;
import net.grinder.common.processidentity.AgentIdentity;
import net.grinder.common.processidentity.ProcessIdentity;
import net.grinder.communication.CommunicationException;
import net.grinder.communication.MessageDispatchRegistry;
import net.grinder.communication.MessageDispatchRegistry.AbstractHandler;
import net.grinder.engine.communication.*;
import net.grinder.message.console.AgentControllerProcessReportMessage;
import net.grinder.message.console.AgentControllerState;
import net.grinder.messages.agent.StartGrinderMessage;
import net.grinder.messages.agent.StopGrinderMessage;
import net.grinder.messages.console.AgentAddress;
import net.grinder.util.ListenerSupport;
import org.ngrinder.monitor.controller.model.SystemDataModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;

import static java.util.Collections.unmodifiableMap;
import static org.ngrinder.common.util.CollectionUtils.newLinkedHashSet;

/**
 * Implementation of {@link AgentProcessControl}.
 *
 * @author JunHo Yoon
 */
public class AgentProcessControlImplementation implements AgentProcessControl {

	private final ConsoleCommunication m_consoleCommunication;
	private final Map<AgentIdentity, AgentStatus> m_agentMap = new ConcurrentHashMap<>();
	private final ListenerSupport<AgentStatusUpdateListener> m_agentStatusUpdateListeners = new ListenerSupport<>();
	private final ListenerSupport<LogArrivedListener> m_logListeners = new ListenerSupport<>();
	private final ListenerSupport<AgentDownloadRequestListener> m_agentDownloadRequestListeners = new ListenerSupport<>();
	private final ListenerSupport<ConnectionAgentListener> m_connectionAgentListener = new ListenerSupport<>();
	private final ListenerSupport<ConnectionAgentCommunicationListener> m_connectionAgentCommunicationListener = new ListenerSupport<>();

	private static final Logger LOGGER = LoggerFactory.getLogger(AgentProcessControlImplementation.class);
	/**
	 * Period at which to update the listeners.
	 */
	private static final long UPDATE_PERIOD = 500;

	/**
	 * We keep a record of processes for a few seconds after they have been terminated.
	 * <p/>
	 * Every FLUSH_PERIOD, process statuses are checked. Those haven't reported for a while are
	 * marked and are discarded if they still haven't been updated by the next FLUSH_PERIOD.
	 */
	private static final long FLUSH_PERIOD = 2000;

	private volatile boolean m_newData = false;

	/**
	 * Constructor.
	 *
	 * @param timer                Timer that can be used to schedule housekeeping tasks.
	 * @param consoleCommunication The console communication handler.
	 */
	public AgentProcessControlImplementation(Timer timer, ConsoleCommunication consoleCommunication) {
		m_consoleCommunication = consoleCommunication;
		timer.schedule(new TimerTask() {
			public void run() {
				synchronized (m_agentMap) {
					try {
						update();
					} catch (Exception e) {
						LOGGER.error("Error occurred during update agent", e);
					}
				}
			}
		}, 0, UPDATE_PERIOD);

		timer.schedule(new TimerTask() {
			public void run() {
				synchronized (m_agentMap) {
					try {
						purge(m_agentMap);
					} catch (Exception e) {
						LOGGER.error("Error occurred during purge agent", e);
					}
				}
			}
		}, 0, FLUSH_PERIOD);
		final MessageDispatchRegistry messageDispatchRegistry = consoleCommunication.getMessageDispatchRegistry();

		messageDispatchRegistry.set(AgentControllerProcessReportMessage.class,
				new AbstractHandler<AgentControllerProcessReportMessage>() {
					public void handle(AgentControllerProcessReportMessage message) {
						updateAgentProcessReportMessage(message);
					}
				});

		messageDispatchRegistry.set(LogReportGrinderMessage.class, new AbstractHandler<LogReportGrinderMessage>() {
			public void handle(final LogReportGrinderMessage message) {
				m_logListeners.apply(listener -> {
					listener.logArrived(message.getTestId(), message.getAddress(), message.getLogs());
				});
			}
		});

		messageDispatchRegistry.set(AgentDownloadGrinderMessage.class, new AbstractHandler<AgentDownloadGrinderMessage>() {
			public void handle(final AgentDownloadGrinderMessage message) {
				m_agentDownloadRequestListeners.apply(listener -> {
					AgentUpdateGrinderMessage agentUpdateGrinderMessage = listener.onAgentDownloadRequested(message.getVersion(), message.getNext());
					if (agentUpdateGrinderMessage != null) {
						m_consoleCommunication.sendToAddressedAgents(message.getAddress(), agentUpdateGrinderMessage);
					}
				});
			}
		});

		messageDispatchRegistry.set(ConnectionAgentMessage.class, new AbstractHandler<ConnectionAgentMessage>() {
			public void handle(final ConnectionAgentMessage message) {
				m_connectionAgentListener.apply(listener -> {
					listener.onConnectionAgentMessage(message.getIp(), message.getName(), message.getSubregion(), message.getPort());
				});
			}
		});

		messageDispatchRegistry.set(ConnectionAgentCommunicationMessage.class, new AbstractHandler<ConnectionAgentCommunicationMessage>() {
			public void handle(final ConnectionAgentCommunicationMessage message) {
				m_connectionAgentCommunicationListener.apply(listener -> {
					listener.onConnectionAgentCommunication(message.getUsingPort(), message.getIp(), message.getPort());
				});
			}
		});
	}

	/**
	 * Set Agent status report.
	 *
	 * @param message {@link AgentControllerProcessReportMessage}
	 */
	private void updateAgentProcessReportMessage(AgentControllerProcessReportMessage message) {
		AgentIdentity agentIdentity = message.getAgentIdentity();
		AgentStatus agentStatus = getAgentStatus(agentIdentity);
		agentStatus.setAgentProcessStatus(message);
		m_agentMap.put(agentIdentity, agentStatus);
		m_newData = true;
	}

	/**
	 * Get agent status. It's for internal use.
	 *
	 * @param agentIdentity agent identity
	 * @return {@link AgentStatus}
	 */
	private AgentStatus getAgentStatus(AgentIdentity agentIdentity) {
		return m_agentMap.getOrDefault(agentIdentity, new AgentStatus(agentIdentity));
	}

	/**
	 * Update agent status.
	 */
	private void update() {
		if (!m_newData) {
			return;
		}

		m_newData = false;

		m_agentStatusUpdateListeners.apply(agentStatusUpdateListener -> {
			agentStatusUpdateListener.update(unmodifiableMap(m_agentMap));
		});
	}

	public void addAgentDownloadRequestListener(AgentDownloadRequestListener agentDownloadRequestListener) {
		m_agentDownloadRequestListeners.add(agentDownloadRequestListener);
	}

	public void addConnectionAgentListener(ConnectionAgentListener connectionAgentListener) {
		m_connectionAgentListener.add(connectionAgentListener);
	}

	public void addConnectionAgentCommunicationListener(ConnectionAgentCommunicationListener listener) {
		m_connectionAgentCommunicationListener.add(listener);
	}

	/**
	 * Interface for listeners to SampleModelImplementation.
	 */
	public interface AgentStatusUpdateListener extends EventListener {
		/**
		 * Update agent status.
		 *
		 * @param agentMap agent map
		 */
		void update(Map<AgentIdentity, AgentStatus> agentMap);
	}

	/**
	 * Callers are for synchronization.
	 *
	 * @param purgableMap map for {@link ProcessIdentity}
	 */
	private void purge(Map<? extends ProcessIdentity, ? extends Purgable> purgableMap) {

		final Set<ProcessIdentity> zombies = new HashSet<>();

		for (Entry<? extends ProcessIdentity, ? extends Purgable> entry : purgableMap.entrySet()) {
			if (entry.getValue().shouldPurge()) {
				zombies.add(entry.getKey());
			}
		}

		if (zombies.size() > 0) {
			for (ProcessIdentity processIdentity: zombies) {
				purgableMap.remove(processIdentity);
			}
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

	private static abstract class AbstractTimedReference implements Purgable {
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

		public void initPurgeDelayCount() {
			m_purgeDelayCount = 0;
		}
	}

	private static final class AgentReference extends AbstractTimedReference {
		private final AgentControllerProcessReportMessage m_agentProcessReportMessage;

		/**
		 * Constructor.
		 *
		 * @param agentProcessReportMessage {@link AgentControllerProcessReportMessage}
		 */
		AgentReference(AgentControllerProcessReportMessage agentProcessReportMessage) {
			this.m_agentProcessReportMessage = agentProcessReportMessage;
		}
	}

	/**
	 * Agent Status.
	 *
	 * @author JunHo Yoon
	 */
	public static final class AgentStatus implements Purgable {
		private volatile AgentReference m_agentReference;

		/**
		 * Constructor.
		 *
		 * @param agentIdentity agent identity
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
			if (m_agentReference == null) {
				return AgentControllerState.UNKNOWN;
			}
			AgentControllerProcessReportMessage agentProcessReport = m_agentReference.m_agentProcessReportMessage;
			return agentProcessReport == null ? AgentControllerState.UNKNOWN : agentProcessReport.getState();
		}

		/**
		 * Set each agent process message on the agent status.
		 *
		 * @param message Message
		 */
		public void setAgentProcessStatus(AgentControllerProcessReportMessage message) {
			m_agentReference = new AgentReference(message);
		}

		public String getVersion() {
			return m_agentReference == null ? null : m_agentReference.m_agentProcessReportMessage.getVersion();
		}

		public SystemDataModel getSystemDataModel() {
			return m_agentReference == null ? null : m_agentReference.m_agentProcessReportMessage.getSystemDataModel();
		}

		public int getConnectingPort() {
			return m_agentReference == null ? 0 : m_agentReference.m_agentProcessReportMessage.getConnectingPort();
		}

		public AgentIdentity getAgentIdentity() {
			return m_agentReference == null ? null : m_agentReference.m_agentProcessReportMessage.getAgentIdentity();
		}

		public String getAgentName() {
			return m_agentReference == null ? "" : m_agentReference.m_agentProcessReportMessage.getAgentIdentity()
					.getName();
		}
	}

	/**
	 * Add process control {@link AgentStatusUpdateListener}.
	 *
	 * @param agentStatusUpdateListener agentStatusUpdateListener to be added
	 */
	public void addAgentStatusUpdateListener(AgentStatusUpdateListener agentStatusUpdateListener) {
		m_agentStatusUpdateListeners.add(agentStatusUpdateListener);
	}

	/**
	 * Add Log control {@link LogArrivedListener}.
	 *
	 * @param listener listener to be added
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
		m_consoleCommunication.sendToAddressedAgents(new AgentAddress(agentIdentity), new StopGrinderMessage());
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
			Set<AgentIdentity> agents = new HashSet<>();
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

		/**
		 * UUID.
		 */
		private static final long serialVersionUID = -2758014000696737553L;

		/**
		 * Constructor.
		 *
		 * @param address {@link AgentAddress} in which the agent process is not known.
		 */
		public UnknownAgentProcessReport(AgentAddress address) {
			super(AgentControllerState.UNKNOWN, null, 0, null);
			try {
				setAddress(address);
			} catch (CommunicationException e) {
				LOGGER.error("Error while setAdress" + address, e);
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
	public String getAgentVersion(AgentIdentity agentIdentity) {
		return getAgentStatus(agentIdentity).getVersion();
	}

	@Override
	public SystemDataModel getSystemDataModel(AgentIdentity agentIdentity) {
		return getAgentStatus(agentIdentity).getSystemDataModel();
	}

	@Override
	public int getAgentConnectingPort(AgentIdentity agentIdentity) {
		return getAgentStatus(agentIdentity).getConnectingPort();
	}

	/**
	 * Get agent identities and status map matching the given predicate.
	 *
	 * @param predicate predicate
	 * @return {@link AgentIdentity} {@link AgentStatus} map
	 * @since 3.1.2
	 */
	public Set<AgentStatus> getAgentStatusSet(Predicate<AgentStatus> predicate) {
		Set<AgentStatus> statusSet = newLinkedHashSet();
		for (Entry<AgentIdentity, AgentStatus> each : m_agentMap.entrySet()) {
			if (predicate.test(each.getValue())) {
				statusSet.add(each.getValue());
			}
		}
		return statusSet;
	}

}
