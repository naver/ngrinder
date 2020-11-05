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

import org.junit.Before;
import org.junit.Test;
import org.ngrinder.AbstractNGrinderTransactionalTest;
import org.ngrinder.model.AgentInfo;
import org.springframework.beans.factory.annotation.Autowired;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

public class AgentRepositoryTest extends AbstractNGrinderTransactionalTest {

	@Autowired
	private AgentManagerRepository agentRepository;

	AgentInfo agentInfo;

	@Before
	public void before() {
		agentRepository.deleteAll();
		addAgent("hello");
	}

	private void addAgent(String name) {
		agentInfo = new AgentInfo();
		agentInfo.setName(name);
		agentInfo.setIp("127.0.0.1");
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
	}
}
