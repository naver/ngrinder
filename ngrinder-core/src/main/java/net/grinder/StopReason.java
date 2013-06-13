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
package net.grinder;

/**
 * Stop Reason of Test.
 * 
 * @author JunHo Yoon
 * @since 3.0
 */
public enum StopReason {
	/** If tps is too low. */
	TOO_LOW_TPS("Too low TPS"),
	/** If too many error happen. */
	TOO_MANY_ERRORS("Too many errors"),
	/** Error while test preparation. */
	ERROR_WHILE_PREPARE("Test preparation error"),
	/** Error while first execution. */
	SCRIPT_ERROR("Script error"),
	/** Error by too much overall traffic on the given region. */
	TOO_MUCH_TRAFFIC_ON_REGION("Too much traffic error"),
	/** Normal Stop. */
	NORMAL("Normal stop"),
	/** Cancel By User. */
	CANCEL_BY_USER("Cancel by user");

	private final String display;

	StopReason(String display) {
		this.display = display;
	}

	public String getDisplay() {
		return display;
	}

}