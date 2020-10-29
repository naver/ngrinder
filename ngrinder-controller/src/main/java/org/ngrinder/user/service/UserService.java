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

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.ngrinder.common.constant.ControllerConstants;
import org.ngrinder.infra.config.Config;
import org.ngrinder.model.PerfTest;
import org.ngrinder.model.Role;
import org.ngrinder.model.User;
import org.ngrinder.perftest.service.PerfTestService;
import org.ngrinder.script.service.FileEntryService;
import org.ngrinder.service.AbstractUserService;
import org.ngrinder.user.repository.UserRepository;
import org.ngrinder.user.repository.UserSpecification;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.ShaPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import static java.time.Instant.now;
import static org.apache.commons.lang.StringUtils.isBlank;
import static org.hibernate.Hibernate.initialize;
import static org.ngrinder.common.constant.CacheConstants.DIST_CACHE_USERS;
import static org.ngrinder.common.constant.CacheConstants.CACHE_USER_ENTITY;
import static org.springframework.data.domain.Sort.Direction.*;
import static org.springframework.data.domain.Sort.by;

@Service
public class UserService extends AbstractUserService {

	private final UserRepository userRepository;

	private final PerfTestService perfTestService;

	private final FileEntryService scriptService;

	private final ShaPasswordEncoder passwordEncoder;

	private final Config config;

	private final CacheManager cacheManager;

	private Cache userCache;

	private Cache userModelCache;

	public UserService(UserRepository userRepository, PerfTestService perfTestService, FileEntryService scriptService,
					   @Lazy ShaPasswordEncoder passwordEncoder, Config config, CacheManager cacheManager) {
		this.userRepository = userRepository;
		this.perfTestService = perfTestService;
		this.scriptService = scriptService;
		this.passwordEncoder = passwordEncoder;
		this.config = config;
		this.cacheManager = cacheManager;
	}

	@PostConstruct
	public void init() {
		userCache = cacheManager.getCache(DIST_CACHE_USERS);
		userModelCache = cacheManager.getCache(CACHE_USER_ENTITY);
	}

	/**
	 * Get user by user id.
	 *
	 * @param userId user id
	 * @return user
	 */
	@Transactional
	@Cacheable(value = DIST_CACHE_USERS, key = "#userId")
	@Override
	public User getOne(String userId) {
		return userRepository.findOneByUserId(userId);
	}

	/**
	 * Get user by user id with eager fetch.
	 *
	 * @param userId user id
	 * @return user
	 */
	@Transactional
	public User getOneWithEagerFetch(String userId) {
		User user = userRepository.findOneByUserId(userId);
		if (user != null) {
			initialize(user.getOwners());
			initialize(user.getFollowers());
		}
		return user;
	}

	/**
	 * Encoding given user's password.
	 *
	 * @param user user
	 */
	public void encodePassword(User user) {
		if (StringUtils.isNotBlank(user.getPassword())) {
			String encodePassword = passwordEncoder.encode(user.getUserId(), user.getPassword());
			user.setPassword(encodePassword);
		}
	}

	/**
	 * Save user.
	 *
	 * @param user include id, userID, fullName, role, password.
	 * @return User
	 */
	@Transactional
	@CachePut(value = DIST_CACHE_USERS, key = "#user.userId")
	@Override
	public User save(User user) {
		encodePassword(user);
		return saveWithoutPasswordEncoding(user);
	}

	/**
	 * Save user.
	 *
	 * @param user include id, userID, fullName, role, password.
	 * @return User
	 */
	@Transactional
	@CachePut(value = DIST_CACHE_USERS, key = "#user.userId")
	@Override
	public User saveWithoutPasswordEncoding(User user) {
		final List<User> followers = getFollowers(user.getFollowersStr());
		user.setFollowers(followers);

		User savedUser = saveWithoutFollowers(user);

		// Then expires new followers so that new followers info can be loaded.
		for (User eachFollower : followers) {
			userCache.evict(eachFollower.getUserId());
			userModelCache.evict(eachFollower.getId());
		}
		prepareUserEnv(savedUser);
		return savedUser;
	}

	@Transactional
	@CachePut(value = DIST_CACHE_USERS, key = "#user.userId")
	public User saveWithoutFollowers(User user) {
		if (user.getPassword() != null && isBlank(user.getPassword())) {
			user.setPassword(null);
		}
		final User existing = userRepository.findOneByUserId(user.getUserId());
		if (existing != null) {
			// First expire existing followers.
			final List<User> existingFollowers = existing.getFollowers();
			if (existingFollowers != null) {
				for (User eachFollower : existingFollowers) {
					userCache.evict(eachFollower.getUserId());
					userModelCache.evict(eachFollower.getId());
				}
			}
			user = existing.merge(user);
		}
		return userRepository.save(user);
	}

	private void prepareUserEnv(User user) {
		scriptService.prepare(user);
	}


	private List<User> getFollowers(String followersStr) {
		List<User> newShareUsers = new ArrayList<>();
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
	 * Delete the given user. All corresponding perftest and directories are deleted as well.
	 *
	 * @param userId the user id string list
	 */
	@SuppressWarnings("SpringElInspection")
	@Transactional
	@CacheEvict(value = DIST_CACHE_USERS, key = "#userId")
	public void delete(String userId) {
		User user = getOne(userId);
		List<PerfTest> deletePerfTests = perfTestService.deleteAll(user);
		userRepository.delete(user);
		for (PerfTest perfTest : deletePerfTests) {
			FileUtils.deleteQuietly(config.getHome().getPerfTestDirectory(perfTest));
		}
		FileUtils.deleteQuietly(config.getHome().getScriptDirectory(user));
		FileUtils.deleteQuietly(config.getHome().getUserRepoDirectory(user));
	}

	/**
	 * Get the user list by the given role.
	 *
	 * @param role role
	 * @param sort sort
	 * @return found user list
	 */
	public List<User> getAll(Role role, Sort sort) {
		return (role == null) ? userRepository.findAll(sort) : userRepository.findAllByRole(role, sort);
	}

	/**
	 * get the user list by the given role.
	 *
	 * @param role     role
	 * @param pageable sort
	 * @return found user list
	 */
	public Page<User> getPagedAll(Role role, Pageable pageable) {
		return (role == null) ? userRepository.findAll(pageable) : userRepository.findAllByRole(role, pageable);
	}

	/**
	 * Get the users by the given role.
	 *
	 * @param role role
	 * @return found user list
	 */
	public List<User> getAll(Role role) {
		return getAll(role, by(ASC, "userName"));
	}

	/**
	 * Get the users by nameLike spec.
	 *
	 * @param name name of user
	 * @return found user list
	 */
	public List<User> getAll(String name) {
		return userRepository.findAll(UserSpecification.nameLike(name));
	}

	/**
	 * Get user page by the given keyword.
	 *
	 * @param keyword  keyword to be like search.
	 * @param pageable page
	 * @return user page
	 */
	public Page<User> getPagedAll(String keyword, Pageable pageable) {
		return userRepository.findAll(UserSpecification.nameLike(keyword), pageable);
	}

	/**
	 * Create an user avoiding ModelAspect behavior.
	 *
	 * @param user userID, fullName, role, password.
	 * @return User
	 */
	@Transactional
	@CachePut(value = DIST_CACHE_USERS, key = "#user.userId")
	@Override
	public User createUser(User user) {
		encodePassword(user);
		Instant createdAt = now();
		user.setCreatedAt(createdAt);
		user.setLastModifiedAt(createdAt);
		User createdBy = getOne(ControllerConstants.NGRINDER_INITIAL_ADMIN_USERID);
		user.setCreatedBy(createdBy);
		user.setLastModifiedBy(createdBy);
		return saveWithoutPasswordEncoding(user);
	}

	@Transactional
	public List<User> getSharedUser(User user) {
		User currUser = getOne(user.getUserId());
		return currUser.getOwners();
	}

	public void evictUserCacheById(String id) {
		userCache.evict(id);
	}
}
