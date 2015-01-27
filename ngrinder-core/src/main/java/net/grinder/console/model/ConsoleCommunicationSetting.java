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
package net.grinder.console.model;

/**
 * The communication settings of the ConsoleCommunicationImplementationEx
 * {@link net.grinder.console.communication.ConsoleCommunicationImplementationEx}.
 *
 * @author CHOE JUNG YEON
 * @see net.grinder.console.communication.ConsoleCommunicationImplementationEx
 * @since 3.3.1
 */
public class ConsoleCommunicationSetting {
	private static final long DEFAULT_INACTIVE_CLIENT_TIME_OUT = 30000;
	private static final long DEFAULT_IDLE_POLL_DELAY = 500;

	private long inactiveClientTimeOut;
	private long idlePollDelay;

	/**
	 * How long before we consider a client connection that presents no data to be
	 * inactive.
	 *
	 * @return
	 */
	public long getInactiveClientTimeOut() {
		return inactiveClientTimeOut;
	}

	public void setInactiveClientTimeOut(long inactiveClientTimeOut) {
		this.inactiveClientTimeOut = inactiveClientTimeOut;
	}

	/**
	 * Time in milliseconds that our ServerReceiver threads should sleep for if there's
	 * no incoming messages.
	 *
	 * @return
	 */
	public long getIdlePollDelay() {
		return idlePollDelay;
	}

	public void setIdlePollDelay(long idlePollDelay) {
		this.idlePollDelay = idlePollDelay;
	}

	public static ConsoleCommunicationSetting asDefault() {
		ConsoleCommunicationSetting option = new ConsoleCommunicationSetting();
		option.setInactiveClientTimeOut(DEFAULT_INACTIVE_CLIENT_TIME_OUT);
		option.setIdlePollDelay(DEFAULT_IDLE_POLL_DELAY);
		return option;
	}
}
