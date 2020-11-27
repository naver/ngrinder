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
 * @since 3.0
 */
@SuppressWarnings("unused")
public interface ControllerConstants {

	String DEFAULT_PACKAGE_NAME = "org.ngrinder";
	int PLUGIN_UPDATE_FREQUENCY = 10;

	/**
	 * Performance test execution frequency in milliseconds.
	 */
	int PERFTEST_RUN_FREQUENCY_MILLISECONDS = 1000;

	/**
	 * Performance test termination frequency in milliseconds.
	 */
	int PERFTEST_TERMINATION_FREQUENCY_MILLISECONDS = 3000;

	//NGRINDER INITIAL ADMIN USER ID
	String NGRINDER_INITIAL_ADMIN_USERID = "admin";

	int MAX_STACKTRACE_STRING_SIZE = 2048;

	String PROP_CONTROLLER_ALLOW_SIGN_UP = "controller.allow_sign_up";
	String PROP_CONTROLLER_CONSOLE_PORT_BASE = "controller.console_port_base";
	String PROP_CONTROLLER_CONTROLLER_PORT = "controller.controller_port";
	String PROP_CONTROLLER_DEFAULT_LANG = "controller.default_lang";
	String PROP_CONTROLLER_DEMO_MODE = "controller.demo_mode";
	String PROP_CONTROLLER_DEV_MODE = "controller.dev_mode";
	String PROP_CONTROLLER_DIST_LOGBACK = "controller.dist_logback";
	String PROP_CONTROLLER_FRONT_PAGE_ENABLED = "controller.front_page_enabled";
	String PROP_CONTROLLER_FRONT_PAGE_ASK_QUESTION_URL = "controller.front_page_ask_question_url";
	String PROP_CONTROLLER_FRONT_PAGE_RESOURCES_RSS = "controller.front_page_resources_rss";
	String PROP_CONTROLLER_FRONT_PAGE_QNA_MORE_URL = "controller.front_page_qna_more_url";
	String PROP_CONTROLLER_FRONT_PAGE_QNA_RSS = "controller.front_page_qna_rss";
	String PROP_CONTROLLER_FRONT_PAGE_RESOURCES_MORE_URL = "controller.front_page_resources_more_url";
	String PROP_CONTROLLER_HELP_URL = "controller.help_url";
	String PROP_CONTROLLER_IP = "controller.ip";
	String PROP_CONTROLLER_MAX_AGENT_PER_TEST = "controller.max_agent_per_test";
	String PROP_CONTROLLER_MAX_CONCURRENT_TEST = "controller.max_concurrent_test";
	String PROP_CONTROLLER_MAX_RUN_COUNT = "controller.max_run_count";
	String PROP_CONTROLLER_MAX_RUN_HOUR = "controller.max_run_hour";
	String PROP_CONTROLLER_MAX_VUSER_PER_AGENT = "controller.max_vuser_per_agent";
	String PROP_CONTROLLER_MONITOR_PORT = "controller.monitor_port";
	String PROP_CONTROLLER_PLUGIN_SUPPORT = "controller.plugin_support";
	String PROP_CONTROLLER_SAFE_DIST = "controller.safe_dist";
	String PROP_CONTROLLER_SAFE_DIST_THRESHOLD = "controller.safe_dist_threshold";
	String PROP_CONTROLLER_SECURITY = "controller.security";
	String PROP_CONTROLLER_SECURITY_LEVEL = "controller.security.level";
	String PROP_CONTROLLER_URL = "controller.url";
	String PROP_CONTROLLER_VALIDATION_SYNTAX_CHECK = "controller.validation_syntax_check";
	String PROP_CONTROLLER_USAGE_REPORT = "controller.usage_report";
	String PROP_CONTROLLER_USER_PASSWORD_SHA256 = "controller.user_password_sha256";
	String PROP_CONTROLLER_USER_SECURITY = "controller.user_security";
	String PROP_CONTROLLER_VALIDATION_TIMEOUT = "controller.validation_timeout";
	String PROP_CONTROLLER_VERBOSE = "controller.verbose";
	String PROP_CONTROLLER_MAX_CONNECTION_WAITING_MILLISECOND = "controller.max_connection_waiting_millisecond";
	String PROP_CONTROLLER_ADMIN_PASSWORD_RESET = "controller.admin_password_reset";
	String PROP_CONTROLLER_ENABLE_SCRIPT_CONSOLE = "controller.enable_script_console";
	String PROP_CONTROLLER_ENABLE_AGENT_AUTO_APPROVAL = "controller.enable_agent_auto_approval";
	String PROP_CONTROLLER_AGENT_FORCE_UPDATE = "controller.agent_force_update";
	String PROP_CONTROLLER_UPDATE_CHUNK_SIZE = "controller.update_chunk_size";
	String PROP_CONTROLLER_INACTIVE_CLIENT_TIME_OUT = "controller.inactive_client_time_out";
	String PROP_CONTROLLER_ENABLE_STATISTICS = "controller.enable_statistics";
	String PROP_CONTROLLER_CSV_SEPARATOR = "controller.csv_separator";
	String PROP_CONTROLLER_GITHUB_BASE_URL = "controller.github_base_url";
}
