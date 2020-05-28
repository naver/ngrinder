// Copyright (C) 2000 - 2012 Philip Aston
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

import net.grinder.communication.*;
import net.grinder.console.common.DisplayMessageConsoleException;
import net.grinder.console.common.ErrorHandler;
import net.grinder.console.common.Resources;
import net.grinder.console.model.ConsoleCommunicationSetting;
import net.grinder.console.model.ConsoleProperties;
import net.grinder.util.TimeAuthority;
import net.grinder.util.thread.BooleanCondition;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.Thread.UncaughtExceptionHandler;

import static org.ngrinder.common.util.NoOp.noOp;

/**
 * Handles communication for the console. This is the extension of
 * {@link ConsoleCommunicationImplementation}.
 *
 * @author JunHo Yoon
 * @see ConsoleCommunicationImplementation
 * @since 3.0
 */
public final class ConsoleCommunicationImplementationEx implements ConsoleCommunication {

	private final Resources m_resources;
	private final ConsoleProperties m_properties;
	private final ErrorHandler m_errorHandler;
	private final TimeAuthority m_timeAuthority;
	private final long m_idlePollDelay;
	private long m_inactiveClientTimeOut;

	private final MessageDispatchSender m_messageDispatcher = new MessageDispatchSender();

	private final BooleanCondition m_processing = new BooleanCondition();
	private final BooleanCondition m_shutdown = new BooleanCondition();

	private Acceptor m_acceptor = null;
	private ServerReceiver m_receiver = null;
	private FanOutServerSender m_sender = null;
	private Thread m_acceptorProblemListener = null;
	private AcceptorResolver acceptorResolver = null;

	/**
	 * Constructor.
	 *
	 * @param resources             Resources.
	 * @param properties            Console properties.
	 * @param errorHandler          Error handler.
	 * @param timeAuthority         Knows the time
	 * @throws DisplayMessageConsoleException If properties are invalid.
	 */
	@SuppressWarnings("UnusedDeclaration")
	public ConsoleCommunicationImplementationEx(Resources resources, ConsoleProperties properties,
	                                            ErrorHandler errorHandler, TimeAuthority timeAuthority,
												ConsoleCommunicationSetting consoleCommunicationSetting) throws DisplayMessageConsoleException {
		m_resources = resources;
		m_properties = properties;
		m_errorHandler = errorHandler;
		m_timeAuthority = timeAuthority;
		if (consoleCommunicationSetting == null) {
			consoleCommunicationSetting = ConsoleCommunicationSetting.asDefault();
		}
		m_idlePollDelay = consoleCommunicationSetting.getIdlePollDelay();
		m_inactiveClientTimeOut = consoleCommunicationSetting.getInactiveClientTimeOut();

		properties.addPropertyChangeListener(new PropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent event) {
				final String property = event.getPropertyName();

				if (property.equals(ConsoleProperties.CONSOLE_HOST_PROPERTY)
						|| property.equals(ConsoleProperties.CONSOLE_PORT_PROPERTY)) {
					reset();
				}
			}
		});

		reset();
	}

	private void reset() {
		if (m_acceptorProblemListener != null) {
			m_acceptorProblemListener.interrupt();
			m_acceptorProblemListener = null;
		}

		try {
			if (m_acceptor != null) {
				m_acceptor.shutdown();
			}
		} catch (CommunicationException e) {
			m_errorHandler.handleException(e);
			return;
		}

		if (m_sender != null) {
			m_sender.shutdown();
		}

		if (m_receiver != null) {
			m_receiver.shutdown();

			// Wait until we're deaf. This requires that some other thread
			// executes
			// processOneMessage(). We can't suck on m_receiver ourself as there
			// may
			// be valid pending messages queued up.

			m_processing.await(false);
		}

		if (m_shutdown.get()) {
			return;
		}

		try {
			m_acceptor = new Acceptor(m_properties.getConsoleHost(), m_properties.getConsolePort(), 1, m_timeAuthority);
			acceptorResolver = new AcceptorResolver();
			acceptorResolver.addSocketListener(m_acceptor);
		} catch (CommunicationException e) {
			m_errorHandler.handleException(new DisplayMessageConsoleException(m_resources, "localBindError.text", e));

			// Wake up any threads waiting in processOneMessage().
			m_processing.wakeUpAllWaiters();

			return;
		}

		m_acceptorProblemListener = new Thread("Acceptor problem listener") {
			public void run() {
				while (true) {

					Exception exception = null;
					try {
						exception = m_acceptor.getPendingException();
					} catch (Exception e) {
						// FALL THROUGH
						noOp();
					}

					if (exception == null) {
						// Acceptor is shutting down.
						break;
					}

					m_errorHandler.handleException(exception);
				}
			}
		};

		m_acceptorProblemListener.setDaemon(true);
		// Ignore any exception in acceptor problem listener
		m_acceptorProblemListener.setUncaughtExceptionHandler(new UncaughtExceptionHandler() {
			@Override
			public void uncaughtException(Thread t, Throwable e) {
				m_errorHandler.handleInformationMessage(e.getMessage());
			}
		});
		m_acceptorProblemListener.start();

		m_receiver = new ServerReceiver();

		try {
			m_receiver.receiveFrom(m_acceptor, new ConnectionType[]{ConnectionType.AGENT,
					ConnectionType.CONSOLE_CLIENT, ConnectionType.WORKER,}, 5, m_idlePollDelay,
					m_inactiveClientTimeOut);
		} catch (CommunicationException e) {
			throw new AssertionError(e);
		}

		try {
			m_sender = new FanOutServerSender(m_acceptor, ConnectionType.AGENT, 3);
		} catch (Acceptor.ShutdownException e) {
			// I am tempted to make this an assertion.
			// Currently, this condition can only happen if the accept() call
			// throws
			// an exception. I guess this might reasonably happen if a network
			// i/f
			// goes away immediately after we create the Acceptor. It's not easy
			// for
			// us to reset ourselves at this point (I certainly don't want to
			// recurse), so we notify the user. Users could get going again by
			// reseting new console address info, but most likely they'll just
			// restart
			// the console.
			m_processing.wakeUpAllWaiters();
			m_errorHandler.handleException(e);
			return;
		}

		m_processing.set(true);
	}

	/**
	 * Returns the message dispatch registry which callers can use to register new message handlers.
	 *
	 * @return The registry.
	 */
	public MessageDispatchRegistry getMessageDispatchRegistry() {
		return m_messageDispatcher;
	}

	/**
	 * Shut down communication.
	 */
	public void shutdown() {
		m_shutdown.set(true);
		m_processing.set(false);
		reset();
	}

	/**
	 * Wait to receive a message, then process it.
	 *
	 * @return <code>true</code> if we processed a message successfully; <code>false</code> if we've
	 *         been shut down.
	 * @see #shutdown()
	 */
	public boolean processOneMessage() {
		while (true) {
			if (m_shutdown.get()) {
				return false;
			}

			if (m_processing.await(true)) {
				try {
					final Message message = m_receiver.waitForMessage();

					if (message == null) {
						// Current receiver has been shut down.
						m_processing.set(false);
					} else {
						m_messageDispatcher.send(message);
						return true;
					}
				} catch (CommunicationException e) {
					// The receive or send failed. We only set m_processing to
					// false when
					// our receiver has been shut down.
					m_errorHandler.handleException(e);
				}
			}
		}
	}

	public String getLocalConnectingAddress(Address agentAddress) {
		return acceptorResolver.getServerAddress(agentAddress);
	}

	/**
	 * The number of connections that have been accepted and are still active. Used by the unit
	 * tests.
	 *
	 * @return The number of accepted connections.
	 */
	@SuppressWarnings("UnusedDeclaration")
	public int getNumberOfConnections() {
		return m_acceptor == null ? 0 : m_acceptor.getNumberOfConnections();
	}

	/**
	 * Send the given message to the agent processes (which may pass it on to their workers).
	 *
	 * <p>
	 * Any errors that occur will be handled with the error handler.
	 * </p>
	 *
	 * @param message The message to send.
	 */
	public void sendToAgents(Message message) {
		if (m_sender == null) {
			m_errorHandler.handleErrorMessage(m_resources.getString("sendError.text"));
		} else {
			try {
				m_sender.send(message);
			} catch (CommunicationException e) {
				m_errorHandler.handleException(new DisplayMessageConsoleException(m_resources, "sendError.text", e));
			}
		}
	}

	/**
	 * Send the given message to the given agent processes (which may pass it on to its workers).
	 *
	 * <p>
	 * Any errors that occur will be handled with the error handler.
	 * </p>
	 *
	 * @param address The address to which the message should be sent.
	 * @param message The message to send.
	 */
	public void sendToAddressedAgents(Address address, Message message) {
		if (m_sender == null) {
			m_errorHandler.handleErrorMessage(m_resources.getString("sendError.text"));
		} else {
			try {
				m_sender.send(address, message);
			} catch (CommunicationException e) {
				m_errorHandler.handleException(new DisplayMessageConsoleException(m_resources, "sendError.text", e));
			}
		}
	}
}
