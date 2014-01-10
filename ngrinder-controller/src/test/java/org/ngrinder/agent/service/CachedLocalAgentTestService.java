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
package org.ngrinder.agent.service;

import org.junit.Test;
import org.ngrinder.AbstractNGrinderTransactionalTest;
import org.ngrinder.model.AgentInfo;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static org.fest.assertions.Assertions.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.mock;

public class CachedLocalAgentTestService extends AbstractNGrinderTransactionalTest {

	@Autowired
	LocalAgentService cachedLocalAgentService;

	@Test
	public void testCache() {
		List<AgentInfo> info = cachedLocalAgentService.getLocalAgents();
		int previousSize = info.size();
		info.add(new AgentInfo());
		assertThat(cachedLocalAgentService.getLocalAgents().size()).isEqualTo(previousSize);
	}
}
