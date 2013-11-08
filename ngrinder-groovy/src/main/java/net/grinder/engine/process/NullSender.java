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
package net.grinder.engine.process;

import net.grinder.communication.CommunicationException;
import net.grinder.communication.Message;
import net.grinder.communication.Sender;

/**
 * NullSender to ignore {@link Sender} behavior.
 * 
 * @author JunHo Yoon
 * @since 3.2
 */
public class NullSender implements Sender {

	@Override
	public void send(Message message) throws CommunicationException {
		// Do nothing
	}

	@Override
	public void shutdown() {
		// Do nothing
	}

}
