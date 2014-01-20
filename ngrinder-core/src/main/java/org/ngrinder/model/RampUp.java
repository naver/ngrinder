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
package org.ngrinder.model;

/**
 * RampUp model.
 *
 * @since 3.3
 */
public enum RampUp {
	/**
	 * Process Ramp Up
	 */
	PROCESS("process"),
	/**
	 * Thread Ramp Up
	 */
	THREAD("thread");
	private String messageKey;

	RampUp(String messageKey) {
		this.messageKey = messageKey;
	}

	@SuppressWarnings("UnusedDeclaration")
	public String getMessageKey() {
		return messageKey;
	}
}
