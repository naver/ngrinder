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
package org.ngrinder.common.constant;

/**
 * Web related Constants.
 *
 * @author JunHo Yoon
 * @since 3.0
 */
public interface WebConstants {

	public static final String JSON_SUCCESS = "success";
	public static final String JSON_MESSAGE = "message";

	// parameter constant, for parameter from page, and for key in map

	public static final String PARAM_TPS = "TPS";
	public static final String PARAM_STATUS_AGENT_PORT = "port";

	public static final String PARAM_TEST = "test";
	public static final String PARAM_LOG_LIST = "logs";

	public static final String PARAM_TEST_CHART_INTERVAL = "chartInterval";

	public static final String PARAM_TIMEZONE_OFFSET = "timezone_offset";
	public static final String PARAM_REGION_LIST = "regions";
	public static final String PARAM_REGION_AGENT_COUNT_MAP = "regionAgentCountMap";
	public static final String PARAM_QUICK_SCRIPT = "quickScript";
	public static final String PARAM_QUICK_SCRIPT_REVISION = "quickScriptRevision";
	public static final String PARAM_PROCESS_THREAD_POLICY_SCRIPT = "vuserCalcScript";

	public static final String PARAM_AVAILABLE_RAMP_UP_TYPE = "availRampUpType";
	public static final String PARAM_MAX_VUSER_PER_AGENT = "maxVuserPerAgent";
	public static final String PARAM_MAX_RUN_COUNT = "maxRunCount";
	public static final String PARAM_MAX_RUN_HOUR = "maxRunHour";
	public static final String PARAM_SAFE_FILE_DISTRIBUTION = "safeFileDistribution";

	public static final String PARAM_SECURITY_LEVEL = "securityLevel";

}
