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

import net.grinder.engine.controller.AgentControllerIdentityImplementation;

import org.junit.Test;
import org.ngrinder.AbstractNGrinderTransactionalTest;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Class description.
 *
 * @author Mavlarn
 * @since
 */
public class AgentManagerTest extends AbstractNGrinderTransactionalTest {
	
	@Autowired
	private AgentManager agentManager;

	@Test
	public void testGetMaxAgentSizePerConsole() {
		agentManager.getMaxAgentSizePerConsole();
	}

	@Test
	public void testConvert() {
		agentManager.convert(new AgentControllerIdentityImplementation("test", "127.0.0.1"));
	}

	@Test
	public void testGetAllFreeApprovedAgentsForUser() {
		agentManager.getAllFreeApprovedAgentsForUser(getTestUser());
	}

	@Test
	public void testGetAllSharedAgents() {
		agentManager.getAllSharedAgents();
	}

	@Test
	public void testFilterSharedAgents() {
		agentManager.filterSharedAgents(agentManager.getAllAttachedAgents());
	}

}
