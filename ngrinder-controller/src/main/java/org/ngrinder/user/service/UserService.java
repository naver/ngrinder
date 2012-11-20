/*
 * Copyright (C) 2012 - 2012 NHN Corporation
 * All rights reserved.
 *
 * This file is part of The nGrinder software distribution. Refer to
 * the file LICENSE which is part of The nGrinder distribution for
 * licensing details. The nGrinder distribution is available on the
 * Internet at http://nhnopensource.org/ngrinder
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS
 * FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE
 * COPYRIGHT HOLDERS OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.ngrinder.user.service;

import static org.ngrinder.common.util.Preconditions.checkNotNull;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;
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
	 * get user by user id.
	 * 
	 * @param userId
	 *            user id
	 * @return user
	 */
	public User getUserById(String userId) {
		return userRepository.findOneByUserId(userId);
	}

	/**
	 * get user by userName.
	 * 
	 * @param userName userName
	 * @return user
	 */
	public User getUserByUserName(String userName) {
		return userRepository.findOneByUserName(userName);
	}

	/**
	 * Encoding given user's password.
	 * @param user user
	 */
	public void encodePassword(User user) {
		if (StringUtils.isNotBlank(user.getPassword())) {
			SecuredUser securedUser = new SecuredUser(user, null);
			String encodePassword = passwordEncoder.encodePassword(user.getPassword(),
							saltSource.getSalt(securedUser));
			user.setPassword(encodePassword);
		}
	}
	
	/**
	 * get all users by role.
	 * 
	 * @param roleName role name
	 * @return found user list
	 */
	public List<User> getAllUserByRole(String roleName) {
		if (StringUtils.isBlank(roleName)) {
			return userRepository.findAll();
		} else {
			return userRepository.findAllByRole(getRole(roleName));
		}
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
	public User saveUser(User user) {
		encodePassword(user);
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
	 * @param user user
	 * @param role role
	 */
	public void saveUser(User user, Role role) {
		encodePassword(user);
		user.setRole(role);
		userRepository.save(user);
	}

	/**
	 * modify user information.
	 * 
	 * @param user user
	 * @return user id
	 */
	@Transactional
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
	 * @param role role
	 * @return found user list
	 * @throws Exception
	 */
	public List<User> getUserListByRole(Role role) {
		return userRepository.findAllByRole(role);
	}

	/**
	 * get Role object based on role name.
	 * 
	 * @param roleName role name
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
	 * @param name name of user
	 * @return found user list
	 */
	public List<User> getUserListByKeyWord(String name) {
		return userRepository.findAll(UserSpecification.nameLike(name));
	}
	
}
