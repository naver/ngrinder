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

import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.ngrinder.model.Role;
import org.ngrinder.model.User;
import org.ngrinder.script.service.FileEntryService;
import org.ngrinder.user.repository.UserRepository;
import org.ngrinder.user.repository.UserSpecification;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * The Class UserService.
 * 
 * @author Yubin Mao
 * @author AlexQin
 */
@Service
public class UserService {

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private FileEntryService scriptService;
	
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
	 * @param userName
	 * @return user
	 */
	public User getUserByUserName(String userName) {
		return userRepository.findOneByUserName(userName);
	}

	/**
	 * get all users by role.
	 * 
	 * @return user map.
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
	 * 			include id, userID, fullName, role, password.
	 * 
	 * @return result
	 */
	@Transactional
	public User saveUser(User user) {
		User createdUser = userRepository.save(user);
		prepareUserEnv(user);
		return createdUser;
	}

	private void prepareUserEnv(User user) {
		scriptService.prepare(user);
	}

	/**
	 * Add normal user
	 * 
	 * @param user
	 */
	public void saveUser(User user, Role role) {
		user.setRole(role);
		userRepository.save(user);
	}

	/**
	 * modify user information.
	 * 
	 * @param user
	 * @return result.
	 */
	public String modifyUser(User user) {
		checkNotNull(user, "user should be not null, when modifying user");
		checkNotNull(user.getId(), "user id should be provided when modifying user");
		User targetUser = userRepository.findOne(user.getId());
		targetUser.merge(user);
		userRepository.save(targetUser);
		return user.getUserId();
	}

	/**
	 * Delete user.
	 * 
	 * @param userIds
	 *            the user id string
	 * @return true, if successful
	 */
	public void deleteUsers(List<String> userIds) {
		// TODO: delete user, how about the projects created by user
		for (String userId : userIds) {
			userRepository.deleteByUserId(userId);
		}
	}

	/**
	 * get user list by role.
	 * 
	 * @param paramMap
	 * @return user list
	 * @throws Exception
	 */
	public List<User> getUserListByRole(Role role) {
		return userRepository.findAllByRole(role);
	}

	/**
	 * get Role object based on role name
	 * 
	 * @param roleName
	 * @return
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
	
	public List<User> getUserListByKeyWord(String keyword) {
		return userRepository.findAll(UserSpecification.nameLike(keyword));
	}
}
