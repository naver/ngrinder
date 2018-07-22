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
 * Controller related Constants.
 *
 * @author JunHo Yoon
 * @since 3.0
 */
public interface ControllerConstants {

	public static final String DEFAULT_PACKAGE_NAME = "org.ngrinder";
	public static final int PLUGIN_UPDATE_FREQUENCY = 10;

	// Google Analytics application name and Tracking ID
	public static final String GOOGLE_ANALYTICS_APP_NAME = "ngrinder-controller";
	public static final String GOOGLE_ANALYTICS_TRACKING_ID = "UA-36328271-5";

	/**
	 * Performance test execution frequency in milliseconds.
	 */
	public static final int PERFTEST_RUN_FREQUENCY_MILLISECONDS = 1000;

	/**
	 * Performance test termination frequency in milliseconds.
	 */
	public static final int PERFTEST_TERMINATION_FREQUENCY_MILLISECONDS = 3000;

	//NGRINDER INITIAL ADMIN USER ID
	public static final String NGRINDER_INITIAL_ADMIN_USERID = "admin";

	public static final int MAX_STACKTRACE_STRING_SIZE = 2048;

	public static final String PROP_CONTROLLER_ALLOW_SIGN_UP = "controller.allow_sign_up";
	public static final String PROP_CONTROLLER_CONSOLE_PORT_BASE = "controller.console_port_base";
	public static final String PROP_CONTROLLER_CONTROLLER_PORT = "controller.controller_port";
	public static final String PROP_CONTROLLER_DEFAULT_LANG = "controller.default_lang";
	public static final String PROP_CONTROLLER_DEMO_MODE = "controller.demo_mode";
	public static final String PROP_CONTROLLER_DEV_MODE = "controller.dev_mode";
	public static final String PROP_CONTROLLER_DIST_LOGBACK = "controller.dist_logback";
	public static final String PROP_CONTROLLER_FRONT_PAGE_ENABLED = "controller.front_page_enabled";
	public static final String PROP_CONTROLLER_FRONT_PAGE_ASK_QUESTION_URL = "controller.front_page_ask_question_url";
	public static final String PROP_CONTROLLER_FRONT_PAGE_RESOURCES_RSS = "controller.front_page_resources_rss";
	public static final String PROP_CONTROLLER_FRONT_PAGE_QNA_MORE_URL = "controller.front_page_qna_more_url";
	public static final String PROP_CONTROLLER_FRONT_PAGE_QNA_RSS = "controller.front_page_qna_rss";
	public static final String PROP_CONTROLLER_FRONT_PAGE_RESOURCES_MORE_URL = "controller.front_page_resources_more_url";
	public static final String PROP_CONTROLLER_HELP_URL = "controller.help_url";
	public static final String PROP_CONTROLLER_IP = "controller.ip";
	public static final String PROP_CONTROLLER_MAX_AGENT_PER_TEST = "controller.max_agent_per_test";
	public static final String PROP_CONTROLLER_MAX_CONCURRENT_TEST = "controller.max_concurrent_test";
	public static final String PROP_CONTROLLER_MAX_RUN_COUNT = "controller.max_run_count";
	public static final String PROP_CONTROLLER_MAX_RUN_HOUR = "controller.max_run_hour";
	public static final String PROP_CONTROLLER_MAX_VUSER_PER_AGENT = "controller.max_vuser_per_agent";
	public static final String PROP_CONTROLLER_MONITOR_PORT = "controller.monitor_port";
	public static final String PROP_CONTROLLER_PLUGIN_SUPPORT = "controller.plugin_support";
	public static final String PROP_CONTROLLER_SAFE_DIST = "controller.safe_dist";
	public static final String PROP_CONTROLLER_SAFE_DIST_THRESHOLD = "controller.safe_dist_threshold";
	public static final String PROP_CONTROLLER_SECURITY = "controller.security";
	public static final String PROP_CONTROLLER_SECURITY_LEVEL = "controller.security.level";
	public static final String PROP_CONTROLLER_URL = "controller.url";
	public static final String PROP_CONTROLLER_VALIDATION_SYNTAX_CHECK = "controller.validation_syntax_check";
	public static final String PROP_CONTROLLER_USAGE_REPORT = "controller.usage_report";
	public static final String PROP_CONTROLLER_USER_PASSWORD_SHA256 = "controller.user_password_sha256";
	public static final String PROP_CONTROLLER_USER_SECURITY = "controller.user_security";
	public static final String PROP_CONTROLLER_VALIDATION_TIMEOUT = "controller.validation_timeout";
	public static final String PROP_CONTROLLER_VERBOSE = "controller.verbose";
	public static final String PROP_CONTROLLER_MAX_CONNECTION_WAITING_MILLISECOND = "controller.max_connection_waiting_millisecond";
	public static final String PROP_CONTROLLER_ADMIN_PASSWORD_RESET = "controller.admin_password_reset";
	public static final String PROP_CONTROLLER_ENABLE_SCRIPT_CONSOLE = "controller.enable_script_console";
	public static final String PROP_CONTROLLER_ENABLE_AGENT_AUTO_APPROVAL = "controller.enable_agent_auto_approval";
	public static final String PROP_CONTROLLER_AGENT_FORCE_UPDATE = "controller.agent_force_update";
	public static final String PROP_CONTROLLER_UPDATE_CHUNK_SIZE = "controller.update_chunk_size";
	public static final String PROP_CONTROLLER_INACTIVE_CLIENT_TIME_OUT = "controller.inactive_client_time_out";
	public static final String PROP_CONTROLLER_ENABLE_STATISTICS = "controller.enable_statistics";
	public static final String PROP_CONTROLLER_CSV_SEPARATOR = "controller.csv_separator";
}
