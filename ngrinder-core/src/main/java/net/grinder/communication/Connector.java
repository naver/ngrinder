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

package net.grinder.communication;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

import net.grinder.common.UncheckedInterruptedException;


/**
 * Connection factory.
 *
 * @author Philip Aston
 */
public final class Connector {

	private final String m_hostString;
	private final int m_port;
	private final ConnectionType m_connectionType;

	private final Socket m_socket;

	/**
	 * Constructor.
	 *
	 * @param hostString TCP address to connect to.
	 * @param port TCP port to connect to.
	 * @param connectionType Connection type.
	 */
	public Connector(String hostString,
					 int port,
					 ConnectionType connectionType) {
		m_hostString = hostString;
		m_port = port;
		m_connectionType = connectionType;

		m_socket = null;
	}

	public Connector(Socket socket,
					 ConnectionType connectionType) {
		m_hostString = socket.getInetAddress().getHostName();
		m_port = socket.getPort();
		m_connectionType = connectionType;

		m_socket = socket;
	}

	/**
	 * Factory method that makes a TCP connection and returns a
	 * corresponding socket.
	 *
	 * @return A socket wired to the connection.
	 * @throws CommunicationException If connection could not be
	 * establish.
	 */
	Socket connect() throws CommunicationException {
		return connect(null);
	}

	Socket connect(Address address) throws CommunicationException {
		Socket socket;
		InetAddress inetAddress = null;

		try {
			if (m_socket != null) {
				inetAddress = m_socket.getInetAddress();
				socket = m_socket;
			} else {
				try {
					inetAddress = InetAddress.getByName(m_hostString);
					socket = new Socket(inetAddress, m_port);
				}
				catch (UnknownHostException e) {
					throw new CommunicationException(
						"Could not resolve host '" + m_hostString + '\'', e);
				}
			}

			final OutputStream outputStream = socket.getOutputStream();

			final ObjectOutputStream objectStream =
				new ObjectOutputStream(outputStream);
			objectStream.writeObject(m_connectionType);
			objectStream.writeObject(address);
			objectStream.flush();
			return socket;
		}
		catch (IOException e) {
			UncheckedInterruptedException.ioException(e);
			throw new CommunicationException(
				"Failed to connect to '" + inetAddress + ':' + m_port + '\'', e);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override public int hashCode() {
		return m_hostString.hashCode() ^ m_port ^ m_connectionType.hashCode();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override public boolean equals(Object o) {
		if (o == this) {
			return true;
		}

		if (o == null || o.getClass() != Connector.class) {
			return false;
		}

		final Connector other = (Connector)o;

		return
			m_port == other.m_port &&
				m_connectionType.equals(other.m_connectionType) &&
				m_hostString.equals(other.m_hostString);
	}

	/**
	 * Return a description of the connection address.
	 *
	 * @return The description.
	 */
	public String getEndpointAsString() {
		String host;

		try {
			host = InetAddress.getByName(m_hostString).toString();
		}
		catch (UnknownHostException e) {
			host = m_hostString;
		}

		return host + ":" + m_port;
	}

	/**
	 * Connection details read from a stream.
	 * @see Connector#read
	 */
	static final class ConnectDetails {
		private final ConnectionType m_connectionType;
		private final Address m_address;

		private ConnectDetails(ConnectionType connectionType, Address address) {
			m_connectionType = connectionType;
			m_address = address;
		}

		public ConnectionType getConnectionType() {
			return m_connectionType;
		}

		public Address getAddress() {
			return m_address;
		}
	}

	/**
	 * Read connection details from a stream.
	 *
	 * @param in The stream.
	 * @return The details.
	 * @throws CommunicationException If the details could not be read.
	 */
	static ConnectDetails read(InputStream in) throws CommunicationException {

		try {
			final ObjectInputStream objectInputStream = new ObjectInputStream(in);
			final ConnectionType type =
				(ConnectionType) objectInputStream.readObject();
			final Address address = (Address) objectInputStream.readObject();
			return new ConnectDetails(type, address);
		}
		catch (IOException e) {
			throw new CommunicationException("Could not read address details", e);
		}
		catch (ClassNotFoundException e) {
			throw new CommunicationException("Could not read address details", e);
		}
	}
}
