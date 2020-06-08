package net.grinder.engine.agent;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;

public class ConnectionAgentCommunicationDelegator extends Thread {
	public static final ConnectionAgentCommunicationDelegator EMPTY = empty();
	private static final int DEFAULT_BUFFER_SIZE = 8192;

	private final int consolePort;
	private final int connectionAgentPort;
	private final Logger LOGGER;
	private final CommunicationMessageSender sender;

	private ServerSocket localServerSocket;

	public ConnectionAgentCommunicationDelegator(int consolePort, int connectionAgentPort, Logger LOGGER, CommunicationMessageSender sender) {
		this.consolePort = consolePort;
		this.connectionAgentPort = connectionAgentPort;
		this.LOGGER = LOGGER;
		this.sender = sender;
	}

	@Override
	public void run() {
		try (ServerSocket localServerSocket = new ServerSocket(consolePort)) {
			this.localServerSocket = localServerSocket;
			while (true) {
				Socket localSocket = localServerSocket.accept();

				sender.send();
				Socket remoteSocket;
				try (ServerSocket serverSocket = new ServerSocket(connectionAgentPort)) {
					remoteSocket = serverSocket.accept();
				}

				new SocketPipeline(localSocket, remoteSocket).start();
			}
		} catch (SocketException e) {
			LOGGER.debug("Shutdown communication delegator", e);
			// normal case. shutdown.
		} catch (Exception e) {
			LOGGER.error("Cannot transfer agent connection", e);
		}
		LOGGER.info("Communication delegator shutdown");
	}

	public void shutdown() {
		IOUtils.closeQuietly(localServerSocket);
	}

	public interface CommunicationMessageSender {
		void send();
	}

	private static class SocketPipeline extends Thread {
		private Socket one;
		private Socket other;

		public SocketPipeline(Socket one, Socket other) {
			this.one = one;
			this.other = other;
		}

		@Override
		public void run() {
			try {
				Thread t1 = transfer(one.getInputStream(), other.getOutputStream());
				Thread t2 = transfer(other.getInputStream(), one.getOutputStream());

				t1.join();
				t2.join();
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}

		private Thread transfer(InputStream in, OutputStream out) {
			Thread thread = new Thread(new Runnable() {
				@Override
				public void run() {
					try {
						byte[] buffer = new byte[DEFAULT_BUFFER_SIZE];
						int len = in.read(buffer);
						while (len > -1 && !isClosed()) {
							out.write(buffer, 0, len);
							len = in.read(buffer);
						}
					} catch (SocketException e) {
						// normal case. shutdown.
					} catch (Exception e) {
						throw new RuntimeException(e);
					}
				}
			});
			thread.start();
			return thread;
		}

		private boolean isClosed() {
			return one.isClosed() || other.isClosed();
		}
	}

	public static ConnectionAgentCommunicationDelegator empty() {
		return new ConnectionAgentCommunicationDelegator(0, 0, null, new ConnectionAgentCommunicationDelegator.CommunicationMessageSender() {
			@Override
			public void send() {
				// noop
			}
		}) {
			@Override
			public void shutdown() {
				// noop
			}

			@Override
			public void run() {
				// noop
			}
		};
	}
}
