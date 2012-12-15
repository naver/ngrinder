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
package org.ngrinder.user.model;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.ngrinder.AbstractNGrinderTransactionalTest;
import org.ngrinder.model.PerfTest;
import org.ngrinder.model.Role;
import org.ngrinder.model.User;
import org.ngrinder.perftest.repository.PerfTestRepository;
import org.ngrinder.perftest.repository.TagRepository;
import org.ngrinder.user.repository.UserRepository;
import org.ngrinder.user.repository.UserSpecification;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specifications;

public class UserTest extends AbstractNGrinderTransactionalTest {

	@Autowired
	public UserRepository userRepository;
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
		perfTestRepository.deleteAll();
		perfTestRepository.flush();
		tagRepository.deleteAll();
		tagRepository.flush();
		userRepository.deleteAll();
		userRepository.flush();
	}
	
	@Test
	public void testShareUser() {
		List<User> sharedUsers = new ArrayList<User>();
		List<User> shareUsers = new ArrayList<User>();

		User user = new User();
		user.setUserName("MyName1");
		user.setEmail("junoyoon@gmail.com");
		user.setCreatedUser(getUser("user"));
		user.setCreatedDate(new Date());
		user.setUserId("hello");
		user.setRole(Role.USER);
		user = userRepository.save(user);
		User user2 = new User();
		user2.setUserId("hello2");
		user2.setUserName("MyName2");
		user2.setEmail("junoyoon2@paran.com");
		user2.setCreatedUser(getUser("user"));
		user2.setCreatedDate(new Date());
		user2.setRole(Role.USER);
		userRepository.save(user2);
		
		User user3 = new User();
		user3.setUserId("hello3");
		user3.setUserName("MyName3");
		user3.setEmail("junoyoon3@paran.com");
		user3.setCreatedUser(getUser("user"));
		user3.setCreatedDate(new Date());
		user3.setRole(Role.USER);
		userRepository.save(user3);
		
		User user4 = new User();
		user4.setUserId("hello4");
		user4.setUserName("MyName4");
		user4.setEmail("junoyoon4@paran.com");
		user4.setCreatedUser(getUser("user"));
		user4.setCreatedDate(new Date());
		user4.setRole(Role.USER);
		sharedUsers.add(user3);
		sharedUsers.add(user2);

		user4.setFollowers(sharedUsers);
		shareUsers.add(user);
		user4.setOwners(shareUsers);
		userRepository.save(user4);

	    User sharedUser = userRepository.findOneByUserId(user4.getUserId());
		List<User> sh = sharedUser.getFollowers();
		LOG.debug("sharedUser.getFollowers:{}", sh);
	}
	

	@Test
	public void testUser() {
		User user = new User();
		user.setUserName("MyName1");
		user.setEmail("junoyoon@gmail.com");
		user.setCreatedUser(getUser("user"));
		user.setCreatedDate(new Date());
		user.setUserId("hello");
		user.setRole(Role.USER);
		user = userRepository.save(user);
		User user2 = new User();
		user2.setUserId("hello2");
		user2.setUserName("MyName2");
		user2.setEmail("junoyoon@paran.com");
		user2.setCreatedUser(getUser("user"));
		user2.setCreatedDate(new Date());
		user2.setRole(Role.USER);
		userRepository.save(user2);

		assertThat(userRepository.count(), is(2L));

		assertThat(userRepository.findAll(UserSpecification.emailLike("gmail")).size(), is(1));

		assertThat(userRepository.findAll(
						Specifications.where(UserSpecification.emailLike("@paran")).and(
										UserSpecification.nameLike("MyName2"))).size(), is(1));

	}
}
