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
package org.ngrinder.perftest.service;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;

import lombok.Getter;
import lombok.Setter;

import static org.ngrinder.common.util.NoOp.noOp;

/**
 * Console information which containing console attributes.<br/>
 * {@link #port} is the most important attribute of this class.
 *
 * @since 3.0
 */
public class ConsoleEntry {

	private final String ip;
	/**
	 * Console port number.
	 */
	@Getter
	@Setter
	private Integer port;
	private ServerSocket socket;

	/**
	 * Constructor.
	 *
	 * @param port port
	 */
	public ConsoleEntry(String ip, Integer port) {
		this.ip = ip;
		this.port = port;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}

		ConsoleEntry other = (ConsoleEntry) obj;

		if (port == null) {
			return other.port == null;
		}
		return port.equals(other.port);
	}

	@Override
	public int hashCode() {
		int result = ip != null ? ip.hashCode() : 0;
		result = 31 * result + (port != null ? port.hashCode() : 0);
		return result;
	}

	public void occupySocket() throws IOException {
		InetAddress address = null;
		try {
			address = InetAddress.getByName(ip);

		} catch (Exception e) {
			noOp();
		}
		if (address != null) {
			socket = new ServerSocket(port, 50, address);
		} else {
			socket = new ServerSocket(port);
		}
	}

	public void releaseSocket() {
		if (socket != null) {
			try {
				socket.close();
			} catch (Exception e) {
				noOp();
			}
		}
	}
}
