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
 * nGrinder specific constants.
 * 
 * @author JunHo Yoon
 * @since 3.0
 */
public interface Constants {

	public static final String START = "start";
	public static final String STOP = "stop";
	public static final String END = "end";

	public static final String SCRIPT_PROPERTIES = "script.properties";

	public static final String PATH_LOG = "logs";
	public static final String PATH_REPORT = "report";
	public static final String PATH_DIST = "dist";
	public static final String PATH_STAT = "stat";

	public static final String CACHE_NAME = "cache";

	public static final String COMMA = ",";

	public static final String ENCODE_UTF8 = "UTF-8";

	public static final String GRINDER_PROPERTIES = "grinder.properties";

	public static final String REPORT_CSV = "output.csv";

	public static final String JSON_SUCCESS = "success";
	public static final String JSON_MESSAGE = "message";

	public static final String NGRINDER_DEFAULT_PACKAGE = "org.ngrinder";
	public static final int PLUGIN_UPDATE_FREQUENCY = 10;

	// Google Analytics application name and Tracking ID
	public static final String GOOGLEANALYTICS_APPNAME = "ngrinder-controller";
	public static final String GOOGLEANALYTICS_TRACKINGID = "UA-36328271-5";

	/**
	 * Initial Max assignable agent size per console.
	 */
	public static final int MAX_AGENT_SIZE_PER_CONSOLE = 10;

	/**
	 * Initial Max vuser per agent.
	 */
	public static final int MAX_VUSER_PER_AGENT = 2000;

	/**
	 * Initial Max run count.
	 */
	public static final int MAX_RUN_COUNT = 100000;

	/**
	 * Maximum waiting seconds until all agents are connected.
	 */
	public static final String NGRINDER_PROP_CONSOLE_MAX_WAITING_MILLISECONDS = "ngrinder." + "max.waitingmilliseconds";

	/**
	 * Maximum waiting seconds until all agents are connected.
	 */
	public static final int NGRINDER_PROP_CONSOLE_MAX_WAITING_MILLISECONDS_VALUE = 5000;

	/**
	 * Performance test execution frequency in milliseconds.
	 */
	public static final int PERFTEST_RUN_FREQUENCY_MILLISECONDS = 1000;

	/**
	 * Performance test termination frequency in milliseconds.
	 */
	public static final int PERFTEST_TERMINATION_FREQUENCY_MILLISECONDS = 3000;

	/**
	 * Max trial count to run performance test.
	 */
	public static final int PERFTEST_MAXIMUM_TRIAL_COUNT = 3;

	// HOME_PATH
	public static final String PLUGIN_PATH = "plugins";
	public static final String SCRIPT_PATH = "script";
	public static final String USER_REPO_PATH = "repos";
	public static final String PERF_TEST_PATH = "perftest";
	public static final String DOWNLOAD_PATH = "download";
	public static final String SHARE_PATH = "share";
	public static final String CONTROLLER_PATH = "controller";
	public static final String GLOBAL_LOG_PATH = "logs";

	public static final String DEFAULT_GRINDER_PROPERTIES_PATH = "grinder.properties";

	// parameter constant, for parameter from page, and for key in map
	public static final String PARAM_USERID = "userId";
	public static final String PARAM_ROLE = "role";
	public static final String PARAM_USER_LANGUAGE = "userLanguage";
	public static final String PARAM_TIMEZONE = "timeZone";
	public static final String PARAM_MESSAGE = "message";
	public static final String PARAM_THREAD_COUNT = "threadCount";
	public static final String PARAM_PROCESS_COUNT = "processCount";
	public static final String PARAM_DATA_LIST = "dataList";
	public static final String PARAM_STATUS_UPDATE_ID = "id";
	public static final String PARAM_STATUS_UPDATE_STATUS_NAME = "name";
	public static final String PARAM_STATUS_UPDATE_STATUS_ID = "status_id";
	public static final String PARAM_STATUS_UPDATE_STATUS_TYPE = "status_type";
	public static final String PARAM_STATUS_UPDATE_STATUS_ICON = "icon";
	public static final String PARAM_STATUS_UPDATE_STATUS_MESSAGE = "message";
	public static final String PARAM_STATUS_UPDATE_DELETABLE = "deletable";
	public static final String PARAM_STATUS_UPDATE_STOPPABLE = "stoppable";
	public static final String PARAM_TPS_TOTAL = "tps_total";
	public static final String PARAM_TPS_FAILED = "tps_failed";
	public static final String PARAM_TPS = "TPS";
	public static final String PARAM_VUSER = "vuser";
	public static final String PARAM_RESPONSE_TIME = "response_time";
	public static final String PARAM_STATUS_AGENT_PORT = "port";

	public static final String PARAM_TEST = "test";
	public static final String PARAM_LOG_LIST = "logs";

	public static final String PARAM_TEST_CHART_INTERVAL = "chartInterval";

	public static final String PARAM_TIMEZONE_OFFSET = "timezone_offset";
	public static final String PARAM_REGION_LIST = "regionList";
	public static final String PARAM_REGION_AGENT_COUNT_MAP = "regionAgentCountMap";
	public static final String PARAM_MAX_AGENT_COUNT = "maxAgentCount";
	public static final String PARAM_SCRIPT_LIST = "scriptList";
	public static final String PARAM_QUICK_SCRIPT = "quickScript";
	public static final String PARAM_QUICK_SCRIPT_REVISION = "quickScriptRevision";
	public static final String PARAM_TARGET_HOST = "targetHostString";
	public static final String PARAM_TEST_NAME = "testName";
	public static final String PARAM_PROCESSTHREAD_POLICY_SCRIPT = "processthread_policy_script";

	public static final String PARAM_CURRENT_FREE_AGENTS_COUNT = "currentFreeAgentsCount";
	public static final String PARAM_MAX_AGENT_SIZE_PER_CONSOLE = "maxAgentSizePerConsole";
	public static final String PARAM_MAX_VUSER_PER_AGENT = "maxVuserPerAgent";
	public static final String PARAM_MAX_RUN_COUNT = "maxRunCount";
	public static final String PARAM_MAX_RUN_HOUR = "maxRunHour";
	public static final String PARAM_SAFE_FILE_DISTRIBUTION = "safeFileDistribution";

	public static final String PARAM_SECURITY_MODE = "securityMode";
	public static final String PARAM_RESULT_SUB = "resultsub";
	public static final String PARAM_RESULT_AGENT_PERF = "agentStat";
	public static final String PARAM_RESULT_MONITOR_PERF = "monitorStat";

	public static final String PARAM_AGENTS = "agents";

	public static final int AGENT_SERVER_DAEMON_PORT = 1011;
	
	//NGRINDER INITIAL ADMIN USER ID
	public static final String NGRINDER_INITIAL_ADMIN_USERID = "admin";
	
	// GRINDER_PROPERTY_KEY
	public static final String GRINDER_PROP_AGENTS = "grinder.agents";
	public static final String GRINDER_PROP_PROCESSES = "grinder.processes";
	public static final String GRINDER_PROP_THREAD = "grinder.threads";
	public static final String GRINDER_PROP_RUNS = "grinder.runs";
	public static final String GRINDER_PROP_PROCESS_INCREMENT = "grinder.processIncrement";
	public static final String GRINDER_PROP_PROCESS_INCREMENT_INTERVAL = "grinder.processIncrementInterval";
	public static final String GRINDER_PROP_INITIAL_PROCESS = "grinder.initialProcesses";
	public static final String GRINDER_PROP_DURATION = "grinder.duration";
	public static final String GRINDER_PROP_SCRIPT = "grinder.script";
	public static final String GRINDER_PROP_JVM = "grinder.jvm";
	public static final String GRINDER_PROP_PARAM = "grinder.param";
	public static final String GRINDER_PROP_JVM_CLASSPATH = "grinder.jvm.classpath";
	public static final String GRINDER_PROP_JVM_ARGUMENTS = "grinder.jvm.arguments";
	public static final String GRINDER_PROP_LOG_DIRECTORY = "grinder.logDirectory";
	public static final String GRINDER_PROP_CONSOLE_HOST = "grinder.consoleHost";
	public static final String GRINDER_PROP_CONSOLE_PORT = "grinder.consolePort";
	public static final String GRINDER_PROP_USE_CONSOLE = "grinder.useConsole";
	public static final String GRINDER_PROP_REPORT_TO_CONSOLE = "grinder.reportToConsole.interval";
	public static final String GRINDER_PROP_INITIAL_SLEEP_TIME = "grinder.initialSleepTime";
	public static final String GRINDER_PROP_REPORT_TIMES_TO_CONSOLE = "grinder.reportTimesToConsole";
	public static final String GRINDER_PROP_TEST_ID = "grinder.test.id";
	public static final String GRINDER_PROP_IGNORE_SAMPLE_COUNT = "grinder.ignoreSampleCount";
	public static final String GRINDER_PROP_SECURITY = "grinder.security";
	public static final String GRINDER_PROP_USER = "grinder.user";
	// ngrinder setting.
	public static final String NGRINDER_PROP_DIST_SAFE_THRESHOLD_OLD = "ngrinder.dist.safe.threashhold";
	public static final String NGRINDER_PROP_DIST_SAFE_THRESHOLD = "ngrinder.dist.safe.threshold";

	public static final String NGRINDER_PROP_DIST_SAFE = "ngrinder.dist.safe";
	public static final String NGRINDER_PROP_DIST_SAFE_REGION = "ngrinder.dist.safe.region";
	public static final String NGRINDER_PROP_ETC_HOSTS = "ngrinder.etc.hosts";
	public static final String NGRINDER_PROP_CONSOLE_PORT_BASE = "ngrinder.console.portbase";
	public static final String NGRINDER_VALIDATION_TIMEOUT = "ngrinder.validation.timeout";
	public static final int NGRINDER_PROP_CONSOLE_PORT_BASE_VALUE = 12000;
	public static final String NGRINDER_PROP_AGENT_CONTROL_PORT = "ngrinder.agent.control.port";
	public static final String NGRINDER_PROP_MAX_CONCURRENT_TEST = "ngrinder.max.concurrenttest";
	public static final int NGRINDER_PROP_MAX_CONCURRENT_TEST_VALUE = 10;
	public static final int MAX_STACKTRACE_STRING_SIZE = 2048;
	public static final String NGRINDER_PROP_DEFAULT_LANGUAGE = "ngrinder.langauge.default";
	public static final String NGRINDER_PROP_FRONT_PAGE_RSS = "ngrinder.frontpage.rss";
	public static final String NGRINDER_PROP_USAGE_REPORT = "usage.report";
	public static final String NGRINDER_USER_SELF_REGISTRATION = "ngrinder.user.self.registration";

	public static final String NGRINDER_RESOURCE_RSS_URL = "http://www.cubrid.org/wiki_ngrinder/rss";
	public static final String NGRINDER_PROP_QNA_PAGE_RSS = "ngrinder.frontpage.qna.rss";
	public static final String NGRINDER_QNA_RSS_URL_KEY = "home.qa.rss";
	public static final String NGRINDER_PROP_REGION = "ngrinder.cluster.region";
	public static final String NGRINDER_PROP_REGION_HIDE = "ngrinder.cluster.region.hide";
	public static final int MAX_RUN_HOUR = 8;

	public static final String NGRINDER_PROP_CLUSTER_MODE = "ngrinder.cluster.mode";
	public static final String NGRINDER_PROP_CLUSTER_URIS = "ngrinder.cluster.uris";
	public static final String NGRINDER_PROP_CLUSTER_LISTENER_PORT = "ngrinder.cluster.listener.port";

	// perfTest default value
	public static final int SAMPLINGINTERVAL_DEFAULT_VALUE = 1;

	// key names for distributed map of EhCache
	public static final String CACHE_NAME_DISTRIBUTED_MAP = "distributed_map";
	public static final String CACHE_NAME_REGION_LIST = "region_list";
	public static final String CACHE_NAME_RUNNING_STATISTICS = "running_statistics";
	public static final String CACHE_NAME_CURRENT_PERFTEST_STATISTICS = "current_perftest_statistics";
	public static final String MONITOR_FILE_PREFIX = "monitor_system_";
}
