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
 * @since 3.0
 */
@SuppressWarnings("unused")
public interface WebConstants {

	String JSON_SUCCESS = "success";
	String JSON_MESSAGE = "message";

	// parameter constant, for parameter from page, and for key in map

	String PARAM_TPS = "tps";
	String PARAM_STATUS_AGENT_PORT = "port";

	String PARAM_TEST = "test";
	String PARAM_LOG_LIST = "logs";

	String PARAM_TEST_CHART_INTERVAL = "chartInterval";

	String PARAM_TIMEZONE_OFFSET = "timezoneOffset";
	String PARAM_REGION_LIST = "regions";
	String PARAM_REGION_AGENT_COUNT_MAP = "regionAgentCountMap";
	String PARAM_QUICK_SCRIPT = "quickScript";
	String PARAM_QUICK_SCRIPT_REVISION = "quickScriptRevision";
	String PARAM_PROCESS_THREAD_POLICY_SCRIPT = "vuserCalcScript";

	String PARAM_AVAILABLE_RAMP_UP_TYPE = "rampUpTypes";
	String PARAM_MAX_VUSER_PER_AGENT = "maxVuserPerAgent";
	String PARAM_MAX_RUN_COUNT = "maxRunCount";
	String PARAM_MAX_RUN_HOUR = "maxRunHour";
	String PARAM_SAFE_FILE_DISTRIBUTION = "safeFileDistribution";

	String PARAM_SECURITY_LEVEL = "securityLevel";
}
