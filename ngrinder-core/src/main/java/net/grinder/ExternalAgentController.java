package net.grinder;

import lombok.Getter;
import net.grinder.common.processidentity.AgentIdentity;
import net.grinder.communication.*;
import net.grinder.engine.communication.AgentControllerServerListener;
import net.grinder.engine.communication.AgentControllerStateMessage;
import net.grinder.message.console.AgentControllerState;
import net.grinder.messages.console.AgentAddress;
import net.grinder.util.thread.Condition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import static net.grinder.util.NoOp.noOp;

public class ExternalAgentController {

	private static final Logger log = LoggerFactory.getLogger(ExternalAgentController.class);

	public static final class ConsoleCommunication {
		@Getter
		private final AgentIdentity agentIdentity;
		private final ClientSender sender;
		private final MessagePump messagePump;
		private AtomicBoolean running = new AtomicBoolean(true);

		@Getter
		private AgentControllerState state;

		private final ScheduledExecutorService stateRequestExecutor = Executors.newSingleThreadScheduledExecutor();

		private AgentControllerServerListener agentControllerServerListener = new AgentControllerServerListener(new Condition(), log);

		public ConsoleCommunication(Connector connector, AgentIdentity agentIdentity) {
			this.agentIdentity = agentIdentity;

			try {
				final ClientReceiver receiver = ClientReceiver.connect(connector, new AgentAddress(agentIdentity));
				sender = ClientSender.connect(receiver);

				final MessageDispatchSender messageDispatcher = new MessageDispatchSender();
				agentControllerServerListener.registerMessageHandlers(messageDispatcher);
				messagePump = new MessagePump(receiver, messageDispatcher, 1);
			} catch (CommunicationException e) {
				log.error("", e);
				throw new RuntimeException(e);
			}
		}

		public void start() {
			messagePump.start();
			stateRequestExecutor.scheduleAtFixedRate(() -> {
				try {
					sender.send(AgentControllerStateMessage.EMPTY);
				} catch (CommunicationException e) {
					log.error("{}. This error is not critical if it doesn't occur much.", e.getMessage());
				}
			}, 0, 990, TimeUnit.MILLISECONDS);

			while (running.get()) {
				if (!agentControllerServerListener.received(AgentControllerServerListener.ANY)) {
					log.debug("Agent is started. Waiting for agent controller signal");
					agentControllerServerListener.waitForMessage();
				}

				if (agentControllerServerListener.received(AgentControllerServerListener.AGENT_STATE)) {
					final AgentControllerStateMessage stateMessage = agentControllerServerListener.getLastAgentStateGrinderMessage();
					agentControllerServerListener.discardMessages(AgentControllerServerListener.AGENT_STATE);
					state = stateMessage.getState();
				}

			}

			this.shutdown();
		}

		public void shutdown() {
			running.set(false);

			stateRequestExecutor.shutdownNow();
			agentControllerServerListener.shutdown();
			try {
				sender.send(new AgentControllerStateMessage(AgentControllerState.FINISHED));
			} catch (CommunicationException e) {
				// Fall through
				// Ignore - peer has probably shut down.
				noOp();
			} finally {
				messagePump.shutdown();
			}
		}
	}
}
