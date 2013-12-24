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
package net.grinder.communication;


import org.ngrinder.common.util.ReflectionUtils;

import java.net.Socket;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class AcceptorResolver {
	public final Map<Address, ResourcePool.Resource> sockets = new ConcurrentHashMap<Address, ResourcePool.Resource>();

	public void addSocketListener(Acceptor acceptor) throws Acceptor.ShutdownException {
		final ResourcePool socketSet = acceptor.getSocketSet(ConnectionType.AGENT);
		socketSet.addListener(new ResourcePool.Listener() {
			@Override
			public void resourceAdded(ResourcePool.Resource resource) {
				sockets.put((
						(SocketWrapper) resource).getAddress(), resource);
			}

			@Override
			public void resourceClosed(ResourcePool.Resource resource) {
				sockets.remove((
						(SocketWrapper) resource).getAddress());
			}
		});
	}

	public String getServerAddress(Address address) {
		ResourcePool.Resource value = sockets.get(address);
		if (!(value instanceof SocketWrapper)) {
			return "";
		}
		final Socket socket = (Socket) ReflectionUtils.getFieldValue(value, "m_socket");
		if (socket == null) {
			return "";
		}
		return socket.getLocalAddress().getHostAddress();
	}
}
