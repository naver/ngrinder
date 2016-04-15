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
package org.ngrinder.agent.repository;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.ngrinder.agent.repository.AgentManagerSpecification.ready;

import java.util.List;

import net.grinder.message.console.AgentControllerState;

import org.junit.Before;
import org.junit.Test;
import org.ngrinder.AbstractNGrinderTransactionalTest;
import org.ngrinder.model.AgentInfo;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Test AgentRepository Class.
 * 
 * @author Mavlarn
 * @since
 */
public class AgentRepositoryTest extends AbstractNGrinderTransactionalTest {

	@Autowired
	private AgentManagerRepository agentRepository;

	AgentInfo agentInfo;

	@Before
	public void before() {
		agentRepository.deleteAll();
		addAgent("hello", "world1", AgentControllerState.BUSY);
	}

	private void addAgent(String name, String region, AgentControllerState state) {
		agentInfo = new AgentInfo();
		agentInfo.setName(name);
		agentInfo.setIp("127.0.0.1");
		agentInfo.setRegion(region);
		agentInfo.setState(state);
		agentInfo.setApproved(false);
		agentRepository.save(agentInfo);
	}

	@Test
	public void testGetByIp() {
		AgentInfo findByIp = agentRepository.findByIp("127.0.0.1");
		assertThat(findByIp.isApproved(), is(false));
		findByIp.setApproved(true);
		agentRepository.save(findByIp);
		findByIp = agentRepository.findByIp("127.0.0.1");
		assertThat(findByIp.isApproved(), is(true));
		assertThat(findByIp, notNullValue());
		assertThat(findByIp.getName(), is("hello"));
		assertThat(findByIp.getRegion(), is("world1"));
	}

	@Test
	public void testGetByOwner() {
		addAgent("hello2", "NAVER", AgentControllerState.BUSY);
		addAgent("hello3", "NAVER", AgentControllerState.BUSY);
		assertThat(agentRepository.findAll().size(), is(3));
		List<AgentInfo> findAll = agentRepository.findAll(AgentManagerSpecification.startWithRegion("NAVER"));
		assertThat(findAll.size(), is(2));
	}

	@Test
	public void testAgentStateByReady() {
		//add ready state
		addAgent("hello5", "stateTest", AgentControllerState.READY);
		addAgent("hello6", "stateTest", AgentControllerState.READY);
		//add order state
		addAgent("hello7", "stateTest", AgentControllerState.STARTED);
		addAgent("hello8", "stateTest", AgentControllerState.FINISHED);
		assertThat(agentRepository.findAll().size(), is(5));
		List<AgentInfo> readyAgentList = agentRepository.findAll(AgentManagerSpecification.ready());
		assertThat(readyAgentList.size(), is(2));
	}
	
}
