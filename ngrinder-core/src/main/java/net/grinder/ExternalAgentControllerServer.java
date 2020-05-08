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
package net.grinder;

import net.grinder.common.processidentity.AgentIdentity;
import net.grinder.communication.*;
import net.grinder.console.common.DisplayMessageConsoleException;
import net.grinder.console.common.ErrorHandler;
import net.grinder.console.common.ErrorQueue;
import net.grinder.console.common.Resources;
import net.grinder.console.communication.AgentProcessControlImplementation;
import net.grinder.console.communication.ConsoleCommunication;
import net.grinder.console.model.ConsoleCommunicationSetting;
import net.grinder.engine.communication.AgentControllerStateMessage;
import net.grinder.engine.communication.AgentInitializeMessage;
import net.grinder.engine.console.ErrorHandlerImplementation;
import net.grinder.engine.controller.AgentControllerIdentityImplementation;
import net.grinder.message.console.AgentControllerState;
import net.grinder.util.NetworkUtils;
import net.grinder.util.StandardTimeAuthority;
import net.grinder.util.thread.BooleanCondition;
import net.grinder.util.thread.Condition;
import org.ngrinder.infra.AgentConfig;
import org.picocontainer.DefaultPicoContainer;
import org.picocontainer.behaviors.Caching;
import org.slf4j.Logger;

import static org.ngrinder.common.util.ExceptionUtils.processException;
import static org.ngrinder.common.util.NoOp.noOp;

public class ExternalAgentControllerServer {
	private DefaultPicoContainer container;
	private boolean m_shutdown = false;
	private final Condition eventSyncCondition;

	public ExternalAgentControllerServer(Resources resources, Logger logger, AgentConfig agentConfig,
										 Condition eventSyncCondition, ConsoleCommunicationSetting consoleCommunicationSetting) {
		this.eventSyncCondition = eventSyncCondition;
		container = new DefaultPicoContainer(new Caching());
		container.addComponent(logger);
		container.addComponent(resources);
		container.addComponent(agentConfig);
		container.addComponent(consoleCommunicationSetting);
		container.addComponent(ConsoleCommunicationImpl.class);

		container.addComponent(ErrorQueue.class);
		ErrorQueue errorQueue = container.getComponent(ErrorQueue.class);
		errorQueue.setErrorHandler(new ErrorHandlerImplementation(logger));
	}

	/**
	 * Run agent controller in synchronized way.
	 */
	public void run() {
		if (m_shutdown) {
			throw processException("The console can not be run because it's already shutdown");
		}
		container.start();
		container.getComponent(AgentProcessControlImplementation.class);
		ConsoleCommunication communication = container.getComponent(ConsoleCommunicationImpl.class);
		synchronized (eventSyncCondition) {
			// Now ready to work
			eventSyncCondition.notifyAll();
		}
		while (communication.processOneMessage()) {
			noOp();
			// Fall through
			// Process until communication is shut down.
		}
	}

	/**
	 * Shut down the console.
	 */
	public void shutdown() {
		m_shutdown = true;
		container.getComponent(ConsoleCommunication.class).shutdown();

		if (container.getLifecycleState().isStarted()) {
			container.stop();
		}
	}

	public static class ConsoleCommunicationImpl implements ConsoleCommunication {
		private static final ConnectionType[] CONNECTION_TYPES =
			new ConnectionType[]{ConnectionType.AGENT, ConnectionType.CONSOLE_CLIENT, ConnectionType.WORKER};

		private final Resources resources;
		private final Logger log;
		private final AgentConfig agentConfig;
		private final ErrorHandler errorHandler;

		private final long idlePollDelay;
		private final long inactiveClientTimeOut;

		private final MessageDispatchSender messageDispatcher = new MessageDispatchSender();

		private final BooleanCondition processing = new BooleanCondition();
		private final BooleanCondition shutdown = new BooleanCondition();

		private Acceptor acceptor = null;
		private ServerReceiver receiver = null;
		private FanOutServerSender sender = null;
		private Thread acceptorProblemListener = null;

		private AgentControllerState state = AgentControllerState.STARTED;

		public ConsoleCommunicationImpl(Resources resources, Logger log, AgentConfig agentConfig,
										ErrorHandler errorHandler, ConsoleCommunicationSetting consoleCommunicationSetting) {
			this.resources = resources;
			this.log = log;
			this.agentConfig = agentConfig;
			this.errorHandler = errorHandler;
			if (consoleCommunicationSetting == null) {
				consoleCommunicationSetting = ConsoleCommunicationSetting.asDefault();
			}
			this.idlePollDelay = consoleCommunicationSetting.getIdlePollDelay();
			this.inactiveClientTimeOut = consoleCommunicationSetting.getInactiveClientTimeOut();

			final AgentIdentity agentIdentity = new AgentControllerIdentityImplementation(agentConfig.getAgentHostID(), NetworkUtils.getLocalHostAddress());

			this.messageDispatcher.set(AgentControllerStateMessage.class, new MessageDispatchRegistry.AbstractHandler<AgentControllerStateMessage>() {
				@Override
				public void handle(AgentControllerStateMessage message) throws CommunicationException {
					if (message.getState() == null) {
						log.debug("AgentControllerState is requested. Send current state.");
						sendToAddressedAgents(message.getAddress(), new AgentControllerStateMessage(state));
					}
				}
			});
			this.messageDispatcher.set(AgentInitializeMessage.class, new MessageDispatchRegistry.AbstractHandler<AgentInitializeMessage>() {
				@Override
				public void handle(AgentInitializeMessage message) throws CommunicationException {
					sendToAddressedAgents(message.getAddress(), new AgentInitializeMessage(agentIdentity));
				}
			});

			reset();
		}

		@Override
		public MessageDispatchRegistry getMessageDispatchRegistry() {
			return messageDispatcher;
		}

		public void reset() {
			resetAcceptorProblemListener();

			try {
				if (acceptor != null) {
					acceptor.shutdown();
				}
			} catch (CommunicationException e) {
				errorHandler.handleException(e);
				return;
			}

			if (sender != null) {
				sender.shutdown();
			}

			if (receiver != null) {
				receiver.shutdown();
				processing.await(false);
			}

			if (shutdown.get()) {
				return;
			}

			try {
				log.info("Listening on " + NetworkUtils.getLocalHostAddress() + ":" + agentConfig.getControllerPort());
				acceptor = new Acceptor(NetworkUtils.getLocalHostAddress(), agentConfig.getControllerPort(), 1, new StandardTimeAuthority());
			} catch (CommunicationException e) {
				errorHandler.handleException(new DisplayMessageConsoleException(resources, "localBindError.text", e));

				// Wake up any threads waiting in processOneMessage().
				processing.wakeUpAllWaiters();
				return;
			}

			receiver = new ServerReceiver();

			try {
				receiver.receiveFrom(acceptor, CONNECTION_TYPES, 5, idlePollDelay,
					inactiveClientTimeOut);
			} catch (CommunicationException e) {
				throw new AssertionError(e);
			}

			try {
				sender = new FanOutServerSender(acceptor, ConnectionType.AGENT, 3);
			} catch (Acceptor.ShutdownException e) {
				processing.wakeUpAllWaiters();
				errorHandler.handleException(e);
				return;
			}

			processing.set(true);
		}

		public void resetAcceptorProblemListener() {
			if (acceptorProblemListener != null) {
				acceptorProblemListener.interrupt();
				acceptorProblemListener = null;
			}

			acceptorProblemListener = new Thread(() -> {
				while (true) {
					Exception exception = null;
					try {
						exception = acceptor.getPendingException();
					} catch (Exception e) {
						// FALL THROUGH
						noOp();
					}

					if (exception == null) {
						// Acceptor is shutting down.
						break;
					}

					errorHandler.handleException(exception);
				}
			});

			acceptorProblemListener.setDaemon(true);
			// Ignore any exception in acceptor problem listener
			acceptorProblemListener.setUncaughtExceptionHandler((t, e) -> errorHandler.handleInformationMessage(e.getMessage()));
			acceptorProblemListener.start();
		}

		@Override
		public void shutdown() {
			shutdown.set(true);
			processing.set(false);
			reset();
		}

		@Override
		public boolean processOneMessage() {
			while (true) {
				if (shutdown.get()) {
					return false;
				}

				if (processing.await(true)) {
					try {
						final Message message = receiver.waitForMessage();
						if (message == null) {
							processing.set(false);
						} else {
							messageDispatcher.send(message);
							return true;
						}
					} catch (CommunicationException e) {
						errorHandler.handleException(e);
					}
				}
			}
		}

		@Override
		public void sendToAgents(Message message) {
			throw new AbstractMethodError("This method is not implemented");
		}

		@Override
		public void sendToAddressedAgents(Address address, Message message) {
			if (sender == null) {
				errorHandler.handleErrorMessage(resources.getString("sendError.text"));
			} else {
				try {
					sender.send(address, message);
				} catch (CommunicationException e) {
					errorHandler.handleException(new DisplayMessageConsoleException(resources, "sendError.text", e));
				}
			}
		}
	}
}
