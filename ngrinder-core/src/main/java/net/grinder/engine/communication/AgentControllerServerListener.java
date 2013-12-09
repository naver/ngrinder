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
package net.grinder.engine.communication;

import net.grinder.communication.Message;
import net.grinder.communication.MessageDispatchRegistry;
import net.grinder.communication.MessageDispatchRegistry.Handler;
import net.grinder.messages.agent.StartGrinderMessage;
import net.grinder.messages.agent.StopGrinderMessage;
import net.grinder.util.thread.Condition;
import org.slf4j.Logger;

/**
 * Agent control messages and allows them to be asynchronously queried.
 *
 * @author JunHo Yoon
 * @since 3.0
 */
public final class AgentControllerServerListener {

	/**
	 * Constant that represents start message.
	 *
	 * @see #received
	 */
	public static final int START = 1;

	/**
	 * Constant that represents a a reset message.
	 *
	 * @see #received
	 */
	public static final int RESET = 1 << 1;

	/**
	 * Constant that represents a stop message.
	 *
	 * @see #received
	 */
	public static final int STOP = 1 << 2;

	/**
	 * Constant that represents a communication shutdown.
	 *
	 * @see #received
	 */
	public static final int SHUTDOWN = 1 << 3;

	/**
	 * Constant that represents a agent update.
	 *
	 * @see #received
	 */
	public static final int AGENT_UPDATE = 1 << 4;



	/**
	 * Constant that represent any message.
	 *
	 * @see #received
	 */
	public static final int ANY = START | RESET | STOP | SHUTDOWN | AGENT_UPDATE;

	private final Condition m_notifyOnMessage;
	private final Logger m_logger;
	private int m_messagesReceived = 0;
	private int m_lastMessagesReceived = 0;
	private StartGrinderMessage m_lastStartGrinderMessage;
	private AgentUpdateGrinderMessage m_lastAgentUpdateGrinderMessage;

	/**
	 * Constructor.
	 *
	 * @param notifyOnMessage An <code>Object</code> to notify when a message arrives.
	 * @param logger          logger to log received event messages to.
	 */
	public AgentControllerServerListener(Condition notifyOnMessage, Logger logger) {
		m_notifyOnMessage = notifyOnMessage;
		m_logger = logger;
	}

	/**
	 * Shut down.
	 */
	public void shutdown() {
		setReceived(SHUTDOWN);
	}

	/**
	 * Wait until any message is received.
	 * <p/>
	 * <p>
	 * After calling this method, the actual messages can be determined using {@link #received}.
	 * </p>
	 */
	public void waitForMessage() {
		while (!checkForMessage(AgentControllerServerListener.ANY)) {
			synchronized (m_notifyOnMessage) {
				m_notifyOnMessage.waitNoInterrruptException();
			}
		}
	}

	/**
	 * Check for messages matching the given mask.
	 * <p/>
	 * <p>
	 * After calling this method, the actual messages can be determined using {@link #received}.
	 * </p>
	 *
	 * @param mask The messages to check for.
	 * @return <code>true</code> if at least one message matches the <code>mask</code> parameter has
	 *         been received since the last time the message was checked for, or if communications
	 *         have been shutdown. <code>false</code> otherwise.
	 */
	public boolean checkForMessage(int mask) {
		synchronized (this) {
			final int intersection = m_messagesReceived & mask;

			try {
				m_lastMessagesReceived = intersection;
			} finally {
				m_messagesReceived ^= intersection;
			}
		}

		return received(mask | SHUTDOWN);
	}

	/**
	 * Discard pending messages that match the given mask.
	 *
	 * @param mask The messages to discard.
	 */
	public void discardMessages(int mask) {
		synchronized (this) {
			m_lastMessagesReceived &= ~mask;
			m_messagesReceived &= ~mask;
		}
	}

	/**
	 * Query the messages set up by the last {@link #checkForMessage} or {@link #waitForMessage}
	 * call.
	 *
	 * @param mask The messages to check for.
	 * @return <code>true</code> if one or more of the received messages matches <code>mask</code>.
	 */
	public synchronized boolean received(int mask) {
		return (m_lastMessagesReceived & mask) != 0;
	}

	private void setReceived(int message) {
		synchronized (this) {
			m_messagesReceived |= message;
		}

		synchronized (m_notifyOnMessage) {
			m_notifyOnMessage.notifyAll();
		}
	}

	/**
	 * Registers message handlers with a dispatcher.
	 *
	 * @param messageDispatcher The dispatcher.
	 */
	public void registerMessageHandlers(MessageDispatchRegistry messageDispatcher) {

		messageDispatcher.set(StartGrinderMessage.class, new AbstractMessageHandler<StartGrinderMessage>() {
			public void handle(StartGrinderMessage message) {
				m_logger.info("received a start agent message");
				m_lastStartGrinderMessage = message;
				setReceived(START);
			}
		});

		messageDispatcher.set(StopGrinderMessage.class, new AbstractMessageHandler<StopGrinderMessage>() {
			public void handle(StopGrinderMessage message) {
				m_logger.info("received a stop agent message");
				setReceived(STOP);
			}
		});

		messageDispatcher.set(AgentUpdateGrinderMessage.class, new AbstractMessageHandler<AgentUpdateGrinderMessage>() {
			public void handle(AgentUpdateGrinderMessage message) {
				m_logger.info("received a agent update message {}", message.getNext());
				m_lastAgentUpdateGrinderMessage = message;
				setReceived(AGENT_UPDATE);
			}
		});
	}

	/**
	 * Return the last {@link StartGrinderMessage} received.
	 *
	 * @return The message.
	 */
	public StartGrinderMessage getLastStartGrinderMessage() {
		return m_lastStartGrinderMessage;
	}



	private abstract class AbstractMessageHandler<T extends Message> implements Handler<T> {

		public void shutdown() {
			final boolean shutdown;

			synchronized (AgentControllerServerListener.this) {
				shutdown = (m_messagesReceived & SHUTDOWN) == 0;
			}

			if (shutdown) {
				m_logger.info("agent controller communication is shutdown");
				setReceived(SHUTDOWN);
			}
		}
	}

	public AgentUpdateGrinderMessage getLastAgentUpdateGrinderMessage() {
		return m_lastAgentUpdateGrinderMessage;
	}

}
