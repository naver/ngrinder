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
package org.ngrinder.perftest.service;

import net.grinder.SingleConsole;
import net.grinder.common.GrinderProperties;
import org.ngrinder.model.PerfTest;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import static org.ngrinder.model.Status.START_AGENTS;
import static org.ngrinder.model.Status.START_AGENTS_FINISHED;

/**
 * Mock PerfTest which disable spring task schedule.
 *
 * @author JunHo Yoon
 */
@Profile("unit-test")
@Component
public class MockPerfTestRunnable extends PerfTestRunnable {

	@Override
	void startAgentsOn(PerfTest perfTest, GrinderProperties grinderProperties, SingleConsole singleConsole) {
		getPerfTestService().markStatusAndProgress(perfTest, START_AGENTS,
				perfTest.getAgentCount() + " agents are starting.");
		getAgentManager().runAgent(perfTest.getLastModifiedUser(), singleConsole, grinderProperties,
				perfTest.getAgentCount());
		getPerfTestService().markStatusAndProgress(perfTest, START_AGENTS_FINISHED,
				perfTest.getAgentCount() + " agents are started.");
	}

	@Override
	protected void addSamplingListeners(PerfTest perfTest, SingleConsole singleConsole) {
		// Do nothing on each sampling...
		// To avoid exceptions due to @Transactional in the different thread.
	}

	@Override
	protected long getSafeTransmissionThreshold() {
		return 1;
	}

	@Override
	public void startPeriodically() {
		// do nothing.
	}

	@Override
	public void finishPeriodically() {
		// No nothing on the periodic finish
	}
}
