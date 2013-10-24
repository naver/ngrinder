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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.ObjectUtils;
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
	 * @param userId
	 *            user id
	 * @return user
	 */
	@Transactional
	@Cacheable("users")
	@Override
	public User getUserById(String userId) {
		return userRepository.findOneByUserId(userId);
	}

	/**
	 * Get user by user id without using Cache. The user in cache has no followers and owners
	 * initialized.
	 * 
	 * @param userId
	 *            user id
	 * @return user
	 */
	@Transactional
	public User getUserByIdWithoutCache(String userId) {
		User user = userRepository.findOneByUserId(userId);
		if (user != null) {
			Hibernate.initialize(user.getOwners());
			Hibernate.initialize(user.getFollowers());
		}
		return user;
	}

	/**
	 * Encoding given user's password.
	 * 
	 * @param user
	 *            user
	 */
	public void encodePassword(User user) {
		if (StringUtils.isNotBlank(user.getPassword())) {
			SecuredUser securedUser = new SecuredUser(user, null);
			String encodePassword = passwordEncoder.encodePassword(user.getPassword(), saltSource.getSalt(securedUser));
			user.setPassword(encodePassword);
		}
	}

	/**
	 * Get all user by the given role.
	 * 
	 * @param roleName
	 *            role name
	 * @param pageable
	 *            pageable
	 * @return page of user
	 */
	public Page<User> getAllUsersByRole(String roleName, Pageable pageable) {
		if (StringUtils.isBlank(roleName)) {
			return userRepository.findAll(pageable);
		} else {
			return getUsersByRole(getRole(roleName), pageable);
		}
	}

	/**
	 * get all users by role.
	 * 
	 * @param roleName
	 *            role name
	 * @return found user list
	 */
	public List<User> getAllUsersByRole(String roleName) {
		return getAllUsersByRole(roleName, new Sort(Direction.ASC, "userName"));
	}

	/**
	 * Get all users by role.
	 * 
	 * @param roleName
	 *            role name
	 * @param sort
	 *            sort method
	 * @return found user list
	 */
	public List<User> getAllUsersByRole(String roleName, Sort sort) {
		if (StringUtils.isBlank(roleName)) {
			return userRepository.findAll(sort);
		} else {
			return getUsersByRole(getRole(roleName), sort);
		}
	}

	/**
	 * Save user.
	 * 
	 * @param user
	 *            include id, userID, fullName, role, password.
	 * 
	 * @return result
	 */
	@Transactional
	@CacheEvict(value = "users", key = "#user.userId")
	@Override
	public User saveUser(User user) {
		encodePassword(user);
		return saveUserWithoutPasswordEncoding(user);
	}

	/**
	 * create user.
	 * 
	 * @param user
	 *            include id, userID, fullName, role, password.
	 * 
	 * @return result
	 */
	@Transactional
	@CacheEvict(value = "users", key = "#user.userId")
	@Override
	public User saveUserWithoutPasswordEncoding(User user) {
		User createdUser = userRepository.save(user);
		prepareUserEnv(user);
		return createdUser;
	}
	
	private void prepareUserEnv(User user) {
		scriptService.prepare(user);
	}

	/**
	 * Add user.
	 * 
	 * @param user
	 *            user
	 * @param role
	 *            role
	 */
	@CacheEvict(value = "users", key = "#user.userId")
	public void saveUser(User user, Role role) {
		encodePassword(user);
		user.setRole(role);
		userRepository.save(user);
	}

	/**
	 * modify user information.
	 * 
	 * @param user
	 *            user
	 * @param shareUserIds
	 *            It is a list of user IDs to share the permission of user
	 * @return user id
	 */
	@Transactional
	@CacheEvict(value = "users", key = "#user.userId")
	public String modifyUser(User user, String shareUserIds) {
		checkNotNull(user, "user should be not null, when modifying user");
		checkNotNull(user.getId(), "user id should be provided when modifying user");

		shareUserIds = (String) ObjectUtils.defaultIfNull(shareUserIds, "");
		List<User> newShareUsers = new ArrayList<User>();
		String[] userIds = shareUserIds.split(",");
		for (String userId : userIds) {
			User shareUser = userRepository.findOneByUserId(userId.trim());
			newShareUsers.add(shareUser);
		}
		user.setFollowers(newShareUsers);

		encodePassword(user);
		User targetUser = userRepository.findOne(user.getId());
		targetUser.merge(user);
		userRepository.save(targetUser);
		return user.getUserId();
	}

	/**
	 * Delete user. All corresponding perftest and directories are deleted as well.
	 * 
	 * @param userIds
	 *            the user id string list
	 */
	@Transactional
	@CacheEvict(value = "users", allEntries = true)
	public void deleteUsers(List<String> userIds) {
		for (String userId : userIds) {
			User user = getUserById(userId);
			List<PerfTest> deletePerfTests = perfTestService.deleteAllPerfTests(user);
			userRepository.delete(user);
			for (PerfTest perfTest : deletePerfTests) {
				FileUtils.deleteQuietly(config.getHome().getPerfTestDirectory(perfTest));
			}
			FileUtils.deleteQuietly(config.getHome().getScriptDirectory(user));
			FileUtils.deleteQuietly(config.getHome().getUserRepoDirectory(user));
		}
	}

	/**
	 * get the user list by the given role.
	 * 
	 * @param role
	 *            role
	 * @param sort
	 *            sort
	 * @return found user list
	 * @throws Exception
	 */
	public List<User> getUsersByRole(Role role, Sort sort) {
		return userRepository.findAllByRole(role, sort);
	}

	/**
	 * get the user list by the given role.
	 * 
	 * @param role
	 *            role
	 * @param pageable
	 *            sort
	 * @return found user list
	 * @throws Exception
	 */
	public Page<User> getUsersByRole(Role role, Pageable pageable) {
		return userRepository.findAllByRole(role, pageable);
	}

	/**
	 * get the user list by the given role.
	 * 
	 * @param role
	 *            role
	 * @return found user list
	 * @throws Exception
	 */
	public List<User> getUsersByRole(Role role) {
		return getUsersByRole(role, new Sort(Direction.ASC, "userName"));
	}

	/**
	 * get Role object based on role name.
	 * 
	 * @param roleName
	 *            role name
	 * @return found Role
	 */
	public Role getRole(String roleName) {
		if (Role.ADMIN.getFullName().equals(roleName)) {
			return Role.ADMIN;
		} else if (Role.USER.getFullName().equals(roleName)) {
			return Role.USER;
		} else if (Role.SUPER_USER.getFullName().equals(roleName)) {
			return Role.SUPER_USER;
		} else if (Role.SYSTEM_USER.getFullName().equals(roleName)) {
			return Role.SYSTEM_USER;
		} else {
			return null;
		}
	}

	/**
	 * Get the user list by nameLike spec.
	 * 
	 * @param name
	 *            name of user
	 * @return found user list
	 */
	public List<User> getUsersByKeyWord(String name) {
		return userRepository.findAll(UserSpecification.nameLike(name));
	}

	/**
	 * Get user page by the given keyword.
	 * 
	 * @param namelike
	 *            keyword to be like search.
	 * @param pageable
	 *            page
	 * @return user page
	 */
	public Page<User> getUsersByKeyWord(String namelike, Pageable pageable) {
		return userRepository.findAll(UserSpecification.nameLike(namelike), pageable);
	}
	
	/**
	 * Create user.
	 * 
	 * @param user
	 *            include id, userID, fullName, role, password.
	 * 
	 * @return result
	 */
	@Transactional
	@CacheEvict(value = "users", key = "#user.userId")
	@Override
	public User createUser(User user) {
		encodePassword(user);
		Date createdDate = new Date();
		user.setCreatedDate(createdDate);
		user.setLastModifiedDate(createdDate);
		User createdUser = userRepository.findOneByUserId(NGrinderConstants.NGRINDER_INITIAL_ADMIN_USERID);
		user.setCreatedUser(createdUser);
		user.setLastModifiedUser(createdUser);
		return saveUserWithoutPasswordEncoding(user);
	}

}
