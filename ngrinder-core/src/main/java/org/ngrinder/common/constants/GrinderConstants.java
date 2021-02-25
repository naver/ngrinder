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
package org.ngrinder.common.constants;

/**
 * Grinder related Constants.
 *
 * @since 3.3
 */
public interface GrinderConstants {

	// GRINDER_PROPERTY_KEY
	String GRINDER_PROP_AGENTS = "grinder.agents";
	String GRINDER_PROP_PROCESSES = "grinder.processes";
	String GRINDER_PROP_THREAD = "grinder.threads";
	String GRINDER_PROP_RUNS = "grinder.runs";
	String GRINDER_PROP_PROCESS_INCREMENT = "grinder.processIncrement";
	String GRINDER_PROP_PROCESS_INCREMENT_INTERVAL = "grinder.processIncrementInterval";
	String GRINDER_PROP_INITIAL_PROCESS = "grinder.initialProcesses";
	String GRINDER_PROP_DURATION = "grinder.duration";
	String GRINDER_PROP_SCRIPT = "grinder.script";
	String GRINDER_PROP_PARAM = "grinder.param";
	String GRINDER_PROP_JVM_CLASSPATH = "grinder.jvm.classpath";
	String GRINDER_PROP_JVM_USER_LIBRARY_CLASSPATH = "grinder.jvm.user.library.classpath";
	String GRINDER_PROP_JVM_ARGUMENTS = "grinder.jvm.arguments";
	String GRINDER_PROP_USE_CONSOLE = "grinder.useConsole";
	String GRINDER_PROP_REPORT_TO_CONSOLE = "grinder.reportToConsole.interval";
	String GRINDER_PROP_INITIAL_SLEEP_TIME = "grinder.initialSleepTime";
	String GRINDER_PROP_INITIAL_THREAD_SLEEP_TIME = "grinder.initialThreadSleepTime";
	String GRINDER_PROP_THREAD_RAMPUP ="grinder.threadRampUp";
	String GRINDER_PROP_TEST_ID = "grinder.test.id";
	String GRINDER_PROP_IGNORE_SAMPLE_COUNT = "grinder.ignoreSampleCount";
	String GRINDER_PROP_CONNECTION_RESET = "grinder.connectionReset";
	String GRINDER_PROP_SECURITY = "grinder.security";
	String GRINDER_PROP_SECURITY_LEVEL = "grinder.security.level";
	String GRINDER_PROP_USER = "grinder.user";
	String GRINDER_PROP_ETC_HOSTS = "ngrinder.etc.hosts";
	String GRINDER_SECURITY_LEVEL_LIGHT = "light";
	String GRINDER_SECURITY_LEVEL_NORMAL = "normal";
	String DEFAULT_GRINDER_PROPERTIES = "grinder.properties";

}
