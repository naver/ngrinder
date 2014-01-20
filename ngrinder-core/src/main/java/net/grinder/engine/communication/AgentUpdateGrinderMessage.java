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
import org.ngrinder.common.util.CRC32ChecksumUtils;

/**
 * Message for agent update.
 *
 * @author JunHo Yoon
 * @since 3.0
 */
public class AgentUpdateGrinderMessage implements Message {

	private final String version;
	private final byte[] binary;
	private final int offset;
	/**
	 * -1 is ending, -2 is error
	 */
	private int next;
	private final long checksum;

	/**
	 * Constructor.
	 */
	public AgentUpdateGrinderMessage(String version, byte[] binary, int offset, int next, long checksum) {
		this.version = version;
		this.binary = binary;
		this.offset = offset;
		this.next = next;
		this.checksum = checksum;
	}

	/**
	 * Get Null AgentUpdateGrinderMessage when nothing can be read.
	 *
	 * @param version version
	 * @return AgentUpdateGrinderMessage null message
	 */
	public static AgentUpdateGrinderMessage getNullAgentUpdateGrinderMessage(String version) {
		return new AgentUpdateGrinderMessage(version, new byte[0], 0, -1, -1);
	}

	/**
	 * Get Initial AgentUpdateGrinderMessage.
	 *
	 * @param version version
	 * @return AgentUpdateGrinderMessage
	 */
	public static AgentUpdateGrinderMessage getStartAgentUpdateGrinderMessage(String version) {
		return new AgentUpdateGrinderMessage(version, new byte[0], 0, 0, 0);
	}

	public String getVersion() {
		return version;
	}

	public int getOffset() {
		return offset;
	}

	public long getChecksum() {
		return checksum;
	}

	public byte[] getBinary() {
		return binary;
	}

	public int getNext() {
		return next;
	}

	public boolean isValid() {
		return next >= 0 && getChecksum() == CRC32ChecksumUtils.getCRC32Checksum(getBinary());
	}
}
