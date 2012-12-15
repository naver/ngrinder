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
package org.ngrinder.infra.init;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.ngrinder.model.PerfTest;
import org.ngrinder.model.User;
import org.ngrinder.perftest.repository.PerfTestRepository;
import org.ngrinder.perftest.repository.TagRepository;
import org.ngrinder.user.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;

public class DBInitTest extends org.ngrinder.AbstractNGrinderTransactionalTest {
	@Autowired
	private DBInit dbInit;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private PerfTestRepository perfTestRepository;

	@Autowired
	private TagRepository tagRepository;

	@Before
	public void before() {
		List<PerfTest> findAll = perfTestRepository.findAll();
		for (PerfTest perfTest : findAll) {
			perfTest.getTags().clear();
		}
		perfTestRepository.save(findAll);
		perfTestRepository.flush();
		perfTestRepository.deleteAll();
		tagRepository.deleteAll();
		userRepository.deleteAll();
	}

	@Test
	public void initUserDB() {
		dbInit.init();
		List<User> users = userRepository.findAll();

		// Two users should be exist
		assertThat(users.size(), is(4));
		assertThat(users.get(0).getUserId(), is("admin"));
		assertThat(users.get(1).getUserId(), is("user"));
	}
}
