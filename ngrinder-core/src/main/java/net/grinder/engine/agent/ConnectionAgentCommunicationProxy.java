package net.grinder.engine.agent;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.BindException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;

public class ConnectionAgentCommunicationProxy extends Thread {
	public static final ConnectionAgentCommunicationProxy EMPTY = empty();
	private static final int DEFAULT_BUFFER_SIZE = 8192;

	private final int localPort;
	private final int remotePort;
	private final Logger LOGGER;
	private final CommunicationMessageSender sender;

	private ServerSocket localServerSocket;
	private ServerSocket remoteServerSocket;

	public ConnectionAgentCommunicationProxy(int localPort, int remotePort, Logger LOGGER, CommunicationMessageSender sender) {
		this.localPort = localPort;
		this.remotePort = remotePort;
		this.LOGGER = LOGGER;
		this.sender = sender;
	}

	@Override
	public void run() {
		try {
			localServerSocket = new ServerSocket(localPort);
			remoteServerSocket = new ServerSocket(remotePort);

			while (!localServerSocket.isClosed() && !remoteServerSocket.isClosed()) {
				Socket localSocket = localServerSocket.accept();

				sender.send();
				Socket remoteSocket = remoteServerSocket.accept();

				new SocketPipeline(localSocket, remoteSocket).start();
			}
		} catch (BindException e) {
			LOGGER.error("Cannot transfer agent connection", e);
			throw new RuntimeException(e);
		} catch (SocketException e) {
			LOGGER.debug("Communication proxy shutdown", e);
			// normal case. shutdown.
		} catch (Exception e) {
			LOGGER.error("Cannot transfer agent connection", e);
			throw new RuntimeException(e);
		} finally {
			shutdown();
		}
		LOGGER.info("Communication proxy shutdown");
	}

	public void shutdown() {
		IOUtils.closeQuietly(localServerSocket);
		IOUtils.closeQuietly(remoteServerSocket);
	}

	public interface CommunicationMessageSender {
		void send();
	}

	private static class SocketPipeline extends Thread {
		private final Socket one;
		private final Socket other;

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
			Thread thread = new Thread(() -> {
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
			});
			thread.start();
			return thread;
		}

		private boolean isClosed() {
			return one.isClosed() || other.isClosed();
		}
	}

	public static ConnectionAgentCommunicationProxy empty() {
		return new ConnectionAgentCommunicationProxy(0, 0, null, () -> {
			// noop
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
