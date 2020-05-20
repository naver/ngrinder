// Copyright (C) 2003 - 2012 Philip Aston
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

package net.grinder.communication;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;

import net.grinder.common.UncheckedInterruptedException;
import net.grinder.util.ListenerSupport;
import net.grinder.util.TimeAuthority;
import net.grinder.util.thread.ExecutorFactory;
import net.grinder.util.thread.InterruptibleRunnable;
import net.grinder.util.thread.InterruptibleRunnableAdapter;


/**
 * Active object that accepts connections on a ServerSocket.
 *
 * @author Philip Aston
 */
public final class Acceptor {

	private final ServerSocket m_serverSocket;
	private final ExecutorService m_executor;
	private final BlockingQueue<Exception> m_exceptionQueue =
		new ArrayBlockingQueue<Exception>(10);

	/**
	 * {@link ResourcePool}s indexed by {@link ConnectionType}.
	 * Guarded by m_socketSets.
	 */
	private final Map<ConnectionType, ResourcePool> m_socketSets =
		new HashMap<ConnectionType, ResourcePool>();

	/**
	 * {@link ListenerSupport}s indexed by {@link ConnectionType}.
	 * Guarded by m_listenerMap.
	 */
	private final Map<ConnectionType, ListenerSupport<Listener>> m_listenerMap =
		new HashMap<ConnectionType, ListenerSupport<Listener>>();

	/** Guarded by m_socketSets. */
	private boolean m_isShutdown = false;
	private final TimeAuthority m_timeAuthority;

	/**
	 * Constructor.
	 *
	 * @param addressString The TCP address to listen on. Zero-length
	 * string => listen on all interfaces.
	 * @param port The TCP port to listen to. 0 => use any free port.
	 * @param numberOfThreads Number of acceptor threads.
	 * @param timeAuthority Knows the time.
	 * @throws CommunicationException If server socket could not be
	 * bound.
	 */
	public Acceptor(String addressString,
					int port,
					int numberOfThreads,
					TimeAuthority timeAuthority)
		throws CommunicationException {

		m_timeAuthority = timeAuthority;

		if (addressString.length() > 0) {
			try {
				m_serverSocket =
					new ServerSocket(port, 50, InetAddress.getByName(addressString));
			}
			catch (IOException e) {
				UncheckedInterruptedException.ioException(e);
				throw new CommunicationException(
					"Could not bind to address '" + addressString + ':' + port + '\'', e);
			}
		}
		else {
			try {
				m_serverSocket = new ServerSocket(port, 50);
			}
			catch (IOException e) {
				UncheckedInterruptedException.ioException(e);
				throw new CommunicationException(
					"Could not bind to port '" + port + "' on local interfaces", e);
			}
		}

		m_executor = ExecutorFactory.createThreadPool("Acceptor", numberOfThreads);

		for (int i = 0; i < numberOfThreads; ++i) {
			m_executor.submit(
				new InterruptibleRunnableAdapter(new AcceptorRunnable()));
		}
	}

	/**
	 * Shut down this acceptor.
	 *
	 * @throws CommunicationException If an IO exception occurs.
	 */
	public void shutdown() throws CommunicationException {

		synchronized (m_socketSets) {
			// Prevent recursion and creation of new ResourcePools.

			if (m_isShutdown) {
				return;
			}

			m_isShutdown = true;
		}

		try {
			m_serverSocket.close();
		}
		catch (IOException e) {
			UncheckedInterruptedException.ioException(e);
			throw new CommunicationException("Error closing socket", e);
		}
		finally {
			// Interrupt the acceptor thread group.
			m_executor.shutdownNow();

			// We clone contents of m_socketSets and don't hold m_socketSets whilst
			// closing the ResourcePools to remove opportunity for dead lock with
			// events triggered by closing resources.
			final ResourcePool[] socketSets = cloneListOfSocketSets();

			for (int i = 0; i < socketSets.length; ++i) {
				socketSets[i].closeCurrentResources();
			}

			m_exceptionQueue.clear();
		}
	}

	private ResourcePool[] cloneListOfSocketSets() {
		final ResourcePool[] resourcePools;

		synchronized (m_socketSets) {
			resourcePools =
				m_socketSets.values().toArray(new ResourcePool[m_socketSets.size()]);
		}
		return resourcePools;
	}

	/**
	 * Get the port this Acceptor is listening on.
	 *
	 * @return The port.
	 */
	public int getPort() {
		return m_serverSocket.getLocalPort();
	}

	/**
	 * Asynchronous exception handling.
	 *
	 * @return The exception, or {@code null} if this Acceptor has been shut down.
	 */
	public Exception getPendingException() {
		synchronized (m_socketSets) {
			if (m_isShutdown) {
				return null;
			}
		}

		try {
			return m_exceptionQueue.take();
		}
		catch (InterruptedException e) {
			throw new UncheckedInterruptedException(e);
		}
	}

	/**
	 * For unit tests.
	 */
	Exception peekPendingException() {
		return m_exceptionQueue.peek();
	}


	/**
	 * The number of connections that have been accepted and are still active.
	 * Used by the unit tests.
	 *
	 * @return The number of accepted connections.
	 */
	public int getNumberOfConnections() {
		final ResourcePool[] socketSets = cloneListOfSocketSets();

		int result = 0;

		for (int i = 0; i < socketSets.length; ++i) {
			result += socketSets[i].countActive();
		}

		return result;
	}

	/**
	 * Listener interface.
	 */
	public interface Listener {
		/**
		 * A connection has been accepted.
		 *
		 * @param connectionType The type of the connection.
		 * @param connection The connection identity.
		 */
		void connectionAccepted(ConnectionType connectionType,
								ConnectionIdentity connection);

		/**
		 * A connection has been closed.
		 *
		 * @param connectionType The type of the connection.
		 * @param connection The connection identity.
		 */
		void connectionClosed(ConnectionType connectionType,
							  ConnectionIdentity connection);
	}

	/**
	 * Add a new listener.
	 *
	 * @param connectionType The connection type.
	 * @param listener The listener.
	 */
	public void addListener(ConnectionType connectionType, Listener listener) {
		getListeners(connectionType).add(listener);
	}

	/**
	 * Get a set of accepted connections.
	 *
	 * @param connectionType
	 *          Identifies the set of connections to return.
	 * @return A set of sockets, each wrapped in an {@link IdleAwareSocketWrapper}
	 *         .
	 * @throws ShutdownException
	 *           If the acceptor is shutdown.
	 */
	ResourcePool getSocketSet(final ConnectionType connectionType)
		throws ShutdownException {

		synchronized (m_socketSets) {
			if (m_isShutdown) {
				throw new ShutdownException("Acceptor has been shut down");
			}

			final ResourcePool original = m_socketSets.get(connectionType);

			if (original != null) {
				return original;
			}
			else {
				final ResourcePool newSocketSet = new ResourcePoolImplementation();

				newSocketSet.addListener(
					new ResourcePool.Listener() {
						public void resourceAdded(ResourcePool.Resource resource) {
							final ConnectionIdentity connection =
								((SocketWrapper)resource).getConnectionIdentity();

							getListeners(connectionType).apply(
								new ListenerSupport.Informer<Listener>() {
									public void inform(Listener l) {
										l.connectionAccepted(connectionType, connection);
									}
								});
						}

						public void resourceClosed(ResourcePool.Resource resource) {
							final ConnectionIdentity connection =
								((SocketWrapper)resource).getConnectionIdentity();

							getListeners(connectionType).apply(
								new ListenerSupport.Informer<Listener>() {
									public void inform(Listener l) {
										l.connectionClosed(connectionType, connection);
									}
								});
						}
					});

				m_socketSets.put(connectionType, newSocketSet);
				return newSocketSet;
			}
		}
	}

	/**
	 * Get the listener list for a particular connection type.
	 */
	private ListenerSupport<Listener> getListeners(
		ConnectionType connectionType) {

		synchronized (m_listenerMap) {
			final ListenerSupport<Listener> original =
				m_listenerMap.get(connectionType);

			if (original != null) {
				return original;
			}
			else {
				final ListenerSupport<Listener> newList =
					new ListenerSupport<Listener>();

				m_listenerMap.put(connectionType, newList);
				return newList;
			}
		}
	}

	public void discriminateConnection(Socket localSocket)
		throws IOException, ShutdownException {

		boolean closeSocket = true;

		try {
			final Connector.ConnectDetails connectDetails =
				Connector.read(localSocket.getInputStream());

			final SocketWrapper socketWrapper =
				new IdleAwareSocketWrapper(localSocket, m_timeAuthority);
			socketWrapper.setAddress(connectDetails.getAddress());

			// Possible minor race if the socket is closed between here...
			final ResourcePool.Closeable closeable =
				getSocketSet(connectDetails.getConnectionType()).add(socketWrapper);

			// .. and the time a listener is registered. Will pick up such a zombie
			// the next time we try to use the resource.
			socketWrapper.addClosedListener(new SocketWrapper.ClosedListener() {
				public void socketClosed() {
					closeable.close();
				}
			});

			// We did good.
			closeSocket = false;
		}
		catch (CommunicationException e) {
			try {
				m_exceptionQueue.put(e);
			}
			catch (InterruptedException ie) {
				throw new UncheckedInterruptedException(ie);
			}
		}
		finally {
			if (closeSocket) {
				try {
					localSocket.close();
				}
				catch (IOException ioException) {
					UncheckedInterruptedException.ioException(ioException);
					// Ignore.
				}
			}
		}
	}

	private class AcceptorRunnable implements InterruptibleRunnable {
		public void interruptibleRun() {
			try {
				while (true) {
					final Socket localSocket = m_serverSocket.accept();
					discriminateConnection(localSocket);
				}
			}
			catch (IOException e) {
				// Treat accept socket errors as fatal - we've probably been
				// shutdown. This includes InterruptedIOExceptions.
			}
			catch (ShutdownException e) {
				// Acceptor has been shutdown, exit.
			}
			finally {
				// Best effort to ensure our server socket is closed.
				try {
					shutdown();
				}
				catch (CommunicationException e) {
					// Ignore.
				}
			}
		}
	}

	/**
	 * Indicates the Acceptor has been shut down.
	 */
	public static final class ShutdownException extends CommunicationException {
		private ShutdownException(String s) {
			super(s);
		}
	}
}
