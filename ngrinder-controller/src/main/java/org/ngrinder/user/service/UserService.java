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
package org.ngrinder.user.service;

import static org.ngrinder.common.util.Preconditions.checkNotNull;
import static org.ngrinder.common.util.Preconditions.checkNull;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.google.common.collect.Lists;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.hibernate.Hibernate;
import org.ngrinder.common.constant.NGrinderConstants;
import org.ngrinder.infra.config.Config;
import org.ngrinder.model.PerfTest;
import org.ngrinder.model.Role;
import org.ngrinder.model.User;
import org.ngrinder.perftest.service.PerfTestService;
import org.ngrinder.script.service.FileEntryService;
import org.ngrinder.security.SecuredUser;
import org.ngrinder.service.IUserService;
import org.ngrinder.user.repository.UserRepository;
import org.ngrinder.user.repository.UserSpecification;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.security.authentication.dao.SaltSource;
import org.springframework.security.authentication.encoding.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * The Class UserService.
 *
 * @author Yubin Mao
 * @author AlexQin
 */
@Service
public class UserService implements IUserService {

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private PerfTestService perfTestService;

	@Autowired
	private FileEntryService scriptService;

	@Autowired
	private SaltSource saltSource;

	@Autowired
	private PasswordEncoder passwordEncoder;

	@Autowired
	private Config config;

	/**
	 * Get user by user id.
	 *
	 * @param userId user id
	 * @return user
	 */
	@Transactional
	@Cacheable("users")
	@Override
	public User getUserById(String userId) {
		return userRepository.findOneByUserId(userId);
	}

	/**
	 * Encoding given user's password.
	 *
	 * @param user user
	 */
	public void encodePassword(User user) {
		if (StringUtils.isNotBlank(user.getPassword())) {
			SecuredUser securedUser = new SecuredUser(user, null);
			String encodePassword = passwordEncoder.encodePassword(user.getPassword(), saltSource.getSalt(securedUser));
			user.setPassword(encodePassword);
		}
	}


	/**
	 * Save user.
	 *
	 * @param user include id, userID, fullName, role, password.
	 * @return result
	 */
	@Transactional
	@CachePut(value = "users", key = "#user.userId")
	@Override
	public User saveUser(User user) {
		encodePassword(user);
		return saveUserWithoutPasswordEncoding(user);
	}

	/**
	 * Save user.
	 *
	 * @param user include id, userID, fullName, role, password.
	 * @return result
	 */
	@Transactional
	@CachePut(value = "users", key = "#user.userId")
	@Override
	public User saveUserWithoutPasswordEncoding(User user) {
		user.setFollowers(getFollowUsers(user.getFollowersStr()));
		if (user.getPassword() != null && StringUtils.isBlank(user.getPassword())) {
			user.setPassword(null);
		}
		final User existing = userRepository.findOneByUserId(user.getUserId());
		if (existing != null) {
			user = existing.merge(user);
		}
		User createdUser = userRepository.save(user);
		prepareUserEnv(user);
		return createdUser;
	}


	@Transactional
	@CachePut(value = "users", key = "#user.userId")
	@Override
	@Deprecated
	public User saveUser(User user, Role role) {
		user.setRole(role);
		return saveUser(user);
	}

	private void prepareUserEnv(User user) {
		scriptService.prepare(user);
	}


	private List<User> getFollowUsers(String followersStr) {
		List<User> newShareUsers = new ArrayList<User>();
		String[] userIds = StringUtils.split(StringUtils.trimToEmpty(followersStr), ',');
		for (String userId : userIds) {
			User shareUser = userRepository.findOneByUserId(userId.trim());
			if (shareUser != null) {
				newShareUsers.add(shareUser);
			}
		}
		return newShareUsers;
	}

	/**
	 * Delete user. All corresponding perftest and directories are deleted as well.
	 *
	 * @param userId the user id string list
	 */
	@Transactional
	@CacheEvict(value = "users", key = "#userId")
	public void deleteUser(String userId) {
		User user = getUserById(userId);
		List<PerfTest> deletePerfTests = perfTestService.deleteAllPerfTests(user);
		userRepository.delete(user);
		for (PerfTest perfTest : deletePerfTests) {
			FileUtils.deleteQuietly(config.getHome().getPerfTestDirectory(perfTest));
		}
		FileUtils.deleteQuietly(config.getHome().getScriptDirectory(user));
		FileUtils.deleteQuietly(config.getHome().getUserRepoDirectory(user));
	}

	/**
	 * get the user list by the given role.
	 *
	 * @param role role
	 * @param sort sort
	 * @return found user list
	 * @throws Exception
	 */
	public List<User> getUsersByRole(Role role, Sort sort) {
		return (role == null) ? userRepository.findAll(sort) : userRepository.findAllByRole(role, sort);
	}

	/**
	 * get the user list by the given role.
	 *
	 * @param role     role
	 * @param pageable sort
	 * @return found user list
	 * @throws Exception
	 */
	public Page<User> getUsersByRole(Role role, Pageable pageable) {
		return (role == null) ? userRepository.findAll(pageable) : userRepository.findAllByRole(role, pageable);
	}

	/**
	 * Get the users by the given role.
	 *
	 * @param role role
	 * @return found user list
	 * @throws Exception
	 */
	public List<User> getUsersByRole(Role role) {
		return getUsersByRole(role, new Sort(Direction.ASC, "userName"));
	}

	/**
	 * Get the users by nameLike spec.
	 *
	 * @param name name of user
	 * @return found user list
	 */
	public List<User> getUsersByKeyWord(String name) {
		return userRepository.findAll(UserSpecification.nameLike(name));
	}

	/**
	 * Get user page by the given keyword.
	 *
	 * @param keyword  keyword to be like search.
	 * @param pageable page
	 * @return user page
	 */
	public Page<User> getUsersByKeyWord(String keyword, Pageable pageable) {
		return userRepository.findAll(UserSpecification.nameLike(keyword), pageable);
	}

	/**
	 * Create an user avoiding ModelAspect behavior.
	 *
	 * @param user userID, fullName, role, password.
	 * @return result
	 */
	@Transactional
	@CachePut(value = "users", key = "#user.userId")
	@Override
	public User createUser(User user) {
		encodePassword(user);
		Date createdDate = new Date();
		user.setCreatedDate(createdDate);
		user.setLastModifiedDate(createdDate);
		User createdUser = getUserById(NGrinderConstants.NGRINDER_INITIAL_ADMIN_USERID);
		user.setCreatedUser(createdUser);
		user.setLastModifiedUser(createdUser);
		return saveUserWithoutPasswordEncoding(user);
	}


}
