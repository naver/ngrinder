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

import java.util.List;

import org.ngrinder.model.PerfTest;
import org.ngrinder.model.Status;
import org.springframework.transaction.annotation.Transactional;

/**
 * {@link PerfTest} Service Class for cluster mode.
 * 
 * @author JunHo Yoon
 * @author Mavlarn
 * @since 3.0
 */
public class ClusteredPerfTestService extends PerfTestService {
	/**
	 * Get next runnable {@link PerfTest}.
	 * 
	 * @return found {@link PerfTest} which is ready to run, null otherwise
	 */

	@Override
	@Transactional
	public PerfTest getNextRunnablePerfTestPerfTestCandidate() {
		List<PerfTest> readyPerfTests = getPerfTestRepository().findAllByStatusAndRegionOrderByScheduledTimeAsc(
				Status.READY, getConfig().getRegion());
		List<PerfTest> usersFirstPerfTests = filterCurrentlyRunningTestUsersTest(readyPerfTests);
		return usersFirstPerfTests.isEmpty() ? null : readyPerfTests.get(0);
	}
}
