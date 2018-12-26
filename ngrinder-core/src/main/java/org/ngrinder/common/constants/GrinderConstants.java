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
	public static final String GRINDER_PROP_AGENTS = "grinder.agents";
	public static final String GRINDER_PROP_PROCESSES = "grinder.processes";
	public static final String GRINDER_PROP_THREAD = "grinder.threads";
	public static final String GRINDER_PROP_RUNS = "grinder.runs";
	public static final String GRINDER_PROP_PROCESS_INCREMENT = "grinder.processIncrement";
	public static final String GRINDER_PROP_PROCESS_INCREMENT_INTERVAL = "grinder.processIncrementInterval";
	public static final String GRINDER_PROP_INITIAL_PROCESS = "grinder.initialProcesses";
	public static final String GRINDER_PROP_DURATION = "grinder.duration";
	public static final String GRINDER_PROP_SCRIPT = "grinder.script";
	public static final String GRINDER_PROP_PARAM = "grinder.param";
	public static final String GRINDER_PROP_JVM_CLASSPATH = "grinder.jvm.classpath";
	public static final String GRINDER_PROP_JVM_ARGUMENTS = "grinder.jvm.arguments";
	public static final String GRINDER_PROP_USE_CONSOLE = "grinder.useConsole";
	public static final String GRINDER_PROP_REPORT_TO_CONSOLE = "grinder.reportToConsole.interval";
	public static final String GRINDER_PROP_INITIAL_SLEEP_TIME = "grinder.initialSleepTime";
	public static final String GRINDER_PROP_INITIAL_THREAD_SLEEP_TIME = "grinder.initialThreadSleepTime";
	public static final String GRINDER_PROP_THREAD_RAMPUP ="grinder.threadRampUp";
	public static final String GRINDER_PROP_TEST_ID = "grinder.test.id";
	public static final String GRINDER_PROP_IGNORE_SAMPLE_COUNT = "grinder.ignoreSampleCount";
	public static final String GRINDER_PROP_SECURITY = "grinder.security";
	public static final String GRINDER_PROP_SECURITY_LEVEL = "grinder.security.level";
	public static final String GRINDER_PROP_USER = "grinder.user";
	public static final String GRINDER_PROP_ETC_HOSTS = "ngrinder.etc.hosts";
	public static final String GRINDER_SECURITY_LEVEL_LIGHT = "light";
	public static final String GRINDER_SECURITY_LEVEL_NORMAL = "normal";
	public static final String DEFAULT_GRINDER_PROPERTIES = "grinder.properties";

}
