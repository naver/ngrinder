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
	TOO_LOW_TPS,
	/** If too many error happen. */
	TOO_MANY_ERRORS,
	/** Error while test preparation. */
	ERROR_WHILE_PREPARE,
	/** Error while first execution. */
	SCRIPT_ERROR,
	/** Error too overall traffic */
	TOO_MUCH_TRAFFIC_ON_REGION,
	/** Normal Stop. */
	NORMAL,
	/** Cancel By User. */
	CANCEL_BY_USER
}