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

import net.grinder.communication.Address;
import net.grinder.communication.AddressAwareMessage;
import net.grinder.communication.CommunicationException;

/**
 * Message for agent download from agent to controller.
 *
 * @author JunHo Yoon
 * @since 3.0
 */
public class AgentDownloadGrinderMessage implements AddressAwareMessage {

	public static int MAX_RETRY_COUNT = 3;
	private Address address;
	private final String version;
	private int next;


	/**
	 * Constructor.
	 *
	 * @param version version
	 */
	public AgentDownloadGrinderMessage(String version) {
		this.version = version;
	}

	public String getVersion() {
		return version;
	}

	public int getNext() {
		return next;
	}

	public void setNext(int next) {
		this.next = next;
	}

	@Override
	public void setAddress(Address address) throws CommunicationException {
		this.address = address;
	}

	public Address getAddress() {
		return address;
	}
}
