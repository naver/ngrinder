/*
 * Copyright (C) 2012 - 2012 NHN Corporation
 * All rights reserved.
 *
 * This file is part of The nGrinder software distribution. Refer to
 * the file LICENSE which is part of The nGrinder distribution for
 * licensing details. The nGrinder distribution is available on the
 * Internet at http://nhnopensource.org/ngrinder
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS
 * FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE
 * COPYRIGHT HOLDERS OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.ngrinder.common.constant;

/**
 * nGrinder specific contants.
 * 
 * @author JunHo Yoon
 * @since 3.0
 */
public interface NGrinderConstants {

	public static final String START = "start";
	public static final String STOP = "stop";
	public static final String END = "end";

	public static final String SCRIPT_PROPERTIES = "script.properties";

	public static final String PATH_LOG = "logs";

	public static final String PATH_REPORT = "report";

	public static final String PATH_HISTORY = "histories";

	public static final String CACHE_NAME = "cache";

	public static final String PATH_DIST = "dist";

	public static final String COMMA = ",";

	public static final String ENCODE_UTF8 = "UTF-8";

	public static final String PY_EXTENTION = ".py";
	public static final String JS_EXTENTION = ".js";

	public static final String GRINDER_PROPERTIES = "grinder.properties";

	public static final String REPORT_CSV = "output.csv";

	public static final String JSON_SUCCESS = "success";
	public static final String JSON_MESSAGE = "message";

	public static final String NGRINDER_DEFAULT_PACKAGE = "org.ngrinder";
	public static final int PLUGIN_UPDATE_FREQUENCY = 60;

	/**
	 * Initial Max assignable agent size per console.
	 */
	public static final int MAX_AGENT_SIZE_PER_CONSOLE = 10;

	/**
	 * Initial Max vuser per agent.
	 */
	public static final int MAX_VUSER_PER_AGENT = 100;

	/**
	 * Initial Max run count.
	 */
	public static final int MAX_RUN_COUNT = 1000;

	/**
	 * Maximum waiting seconds until all agents are connected.
	 */
	public static final String NGRINDER_PROP_CONSOLE_MAX_WAITING_MILLISECONDS = "ngrinder.consoleMaxWaitingMilliseconds";

	/**
	 * Maximum waiting seconds until all agents are connected.
	 */
	public static final int NGRINDER_PROP_CONSOLE_MAX_WAITING_MILLISECONDS_VALUE = 5000;

	/**
	 * Performance test execution frequency in milliseconds.
	 */
	public static final int PERFTEST_RUN_FREQUENCY_MILLISECONDS = 5000;

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

	public static final String PARAM_TEST = "test";;
	public static final String PARAM_SCRIPT_LIST = "scriptList";
	public static final String PARAM_PROCESSTHREAD_POLICY_SCRIPT = "processthread_policy_script";
	
	public static final String PARAM_CURRENT_FREE_AGENTS_COUNT = "currentFreeAgentsCount";
	public static final String PARAM_MAX_AGENT_SIZE_PER_CONSOLE = "maxAgentSizePerConsole";
	public static final String PARAM_MAX_VUSER_PER_AGENT = "maxVuserPerAgent";
	public static final String PARAM_MAX_RUN_COUNT = "maxRunCount";
	public static final String PARAM_MAX_RUN_HOUR = "maxRunHour";

	public static final String PARAM_RESULT_SUB = "resultsub";

	public static final String PARAM_AGENTS = "agents";

	public static final int AGENT_SERVER_DAEMON_PORT = 1011;
	// GRINDER_PROPERTY_KEY
	public static final String GRINDER_PROP_PROCESSES = "grinder.processes";
	public static final String GRINDER_PROP_THREAD = "grinder.threads";
	public static final String GRINDER_PROP_RUNS = "grinder.runs";
	public static final String GRINDER_PROP_PROCESS_INCREMENT = "grinder.processIncrement";
	public static final String GRINDER_PROP_PROCESS_INCREMENT_INTERVAL = "grinder.processIncrementInterval";
	public static final String GRINDER_PROP_INITIAL_PROCESS = "grinder.initialProcesses";
	public static final String GRINDER_PROP_DURATION = "grinder.duration";
	public static final String GRINDER_PROP_SCRIPT = "grinder.script";
	public static final String GRINDER_PROP_JVM = "grinder.jvm";
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

	// ngrinder setting.
	public static final String NGRINDER_PROP_ETC_HOSTS = "ngrinder.etc.hosts";
	public static final String NGRINDER_PROP_CONSOLE_PORT_BASE = "ngrinder.consolePortBase";
	public static final int NGRINDER_PROP_CONSOLE_PORT_BASE_VALUE = 12000;
	public static final String NGRINDER_PROP_MAX_CONCURRENT_TEST = "ngrinder.maxConcurrentTest";
	public static final int NGRINDER_PROP_MAX_CONCURRENT_TEST_VALUE = 10;
	public static final int MAX_STACKTRACE_STRING_SIZE = 2048;

	public static final String NGRINDER_NEWS_RSS_URL = "http://www.cubrid.org/wiki_ngrinder/rss";
	public static final int MAX_RUN_HOUR = 8;
}
