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

import net.grinder.communication.Message;

/**
 * Message for agent update.
 *
 * @author JunHo Yoon
 * @since 3.0
 */
public class AgentUpdateGrinderMessage implements Message {

	private final String version;
	private final byte[] binary;
	private final long next;
	private final long checksum;

	/**
	 * Constructor.
	 */
	public AgentUpdateGrinderMessage(String version, byte[] binary, long next, long checksum) {
		this.version = version;
		this.binary = binary;
		this.next = next;
		this.checksum = checksum;
	}

	public String getVersion() {
		return version;
	}

	public long getNext() {
		return next;
	}

	public long getChecksum() {
		return checksum;
	}

	public byte[] getBinary() {
		return binary;
	}
}
