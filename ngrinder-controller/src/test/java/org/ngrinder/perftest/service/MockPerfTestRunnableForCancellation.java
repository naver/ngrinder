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
import org.ngrinder.common.util.ThreadUtils;
import org.ngrinder.model.PerfTest;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/**
 * Mock PerfTest which disable spring task schedule.
 *
 * @author JunHo Yoon
 */
@Profile("unit-test")
@Component
public class MockPerfTestRunnableForCancellation extends PerfTestRunnable {
	private Runnable runnable;
	private int ignoreCount;

	@Override
	public void startPeriodically() {
		// no nothing
	}

	@Override
	public void finishPeriodically() {
		// no nothing
	}

	@Override
	protected boolean hasEnoughFreeAgents(PerfTest test) {
		return true;
	}

	@Override
	void startAgentsOn(PerfTest perfTest, GrinderProperties grinderProperties, SingleConsole singleConsole) {
		ThreadUtils.sleep(1000);
	}

	int calledCount = 0;

	@Override
	SingleConsole checkCancellation(SingleConsole singleConsole) {
		if (++calledCount > ignoreCount) {
			if (this.runnable != null) {
				this.runnable.run();
			}
		}
		return super.checkCancellation(singleConsole);
	}


	@Override
	protected void addSamplingListeners(PerfTest perfTest, SingleConsole singleConsole) {
		// Do nothing on each sampling...
		// To avoid exceptions due to @Transactional in the different thread.
	}


	public void setRunnable(Runnable runnable, int ignoreCount) {
		this.runnable = runnable;
		this.ignoreCount = ignoreCount;
	}
}
