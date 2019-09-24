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
package org.ngrinder.perftest.model;

import lombok.Getter;
import lombok.Setter;
import org.ngrinder.model.PerfTest;
import org.ngrinder.model.User;

/**
 * Current running perf test info of {@link PerfTest} per user.
 * 
 * @since 3.0
 */
@Getter
@Setter
public class PerfTestStatistics {
	private User user;
	private int agentCount;
	private int testCount;

	/**
	 * Constructor.
	 * 
	 * @param user
	 *            user
	 */
	public PerfTestStatistics(User user) {
		this.user = user;
	}

	/**
	 * Add a {@link PerfTest} instance.
	 * 
	 * @param perfTest
	 *            perftest
	 */
	public void addPerfTest(PerfTest perfTest) {
		testCount++;
		agentCount += perfTest.getAgentCount();
	}
}
