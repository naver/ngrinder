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
 * Cache related Constants.
 *
 * @since 3.5.0
 */
public interface CacheConstants {
	String DIST_MAP_NAME_SAMPLING = "sampling";
	String DIST_MAP_NAME_MONITORING = "monitoring";
	String DIST_MAP_NAME_AGENT = "agent";

	String CACHE_USERS = "users";
	String CACHE_FILE_ENTRIES = "file_entries";

	String CACHE_RIGHT_PANEL_ENTRIES = "right_panel_entries";
	String CACHE_LEFT_PANEL_ENTRIES = "left_panel_entries";
	String CACHE_CURRENT_PERFTEST_STATISTICS = "current_perftest_statistics";

	String REGION_ATTR_KEY = "region";
	String REGION_EXECUTOR_SERVICE_NAME = "region_executor";
	String AGENT_EXECUTOR_SERVICE_NAME = "agent_executor";

	String AGENT_TOPIC_NAME = "agent_topic";
	String AGENT_TOPIC_LISTENER_NAME = "agent_topic_listener";

	String CACHE_USER_ENTITY = "org.ngrinder.model.User";

	String CACHE_GITHUB_SCRIPTS = "github_scripts";

	int REGION_CACHE_TIME_TO_LIVE_SECONDS = 20;
}
