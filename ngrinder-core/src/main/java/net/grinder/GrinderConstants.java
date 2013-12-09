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
 * The Class GrinderConstants.
 */
public abstract class GrinderConstants {

	/**
	 * The Constant P_PROCESS.
	 */
	public static final String P_PROCESS = "process";

	/**
	 * The Constant P_THREAD.
	 */
	public static final String P_THREAD = "thread";


	/**
	 * Agent controller HeartBeat interval.
	 */
	public static final long AGENT_CONTROLLER_HEARTBEAT_INTERVAL = 990;

	/**
	 * Agent controller daemon retry interval.
	 */
	protected static final long AGENT_CONTROLLER_RETRY_INTERVAL = 2000;

	public static final int AGENT_CONTROLLER_FANOUT_STREAM_THREAD_COUNT = 3;
	public static final int AGENT_FANOUT_STREAM_THREAD_COUNT = 3;
	public static final int AGENT_HEARTBEAT_INTERVAL = 900;
	public static final int AGENT_HEARTBEAT_DELAY = 900;

}
