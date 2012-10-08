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
package org.ngrinder.infra.init;

import java.util.Date;

import javax.annotation.PostConstruct;

import org.ngrinder.model.Role;
import org.ngrinder.model.User;
import org.ngrinder.script.service.FileEntryService;
import org.ngrinder.security.SecuredUser;
import org.ngrinder.user.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.dao.SaltSource;
import org.springframework.security.authentication.encoding.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Database Initialization. 
 * When the first boot-up, some data(ex: user account) should be inserted into DB.
 * 
 * And... It's the perfect place to upgrade DB.
 * 
 * @author JunHo Yoon
 * @since 3.0
 */
@Service
public class DBInit {
	@Autowired
	private UserRepository userRepository;

	/**
	 * Initalize DB.
	 */
	@PostConstruct
	@Transactional
	public void init() {
		createDefaultUserIfNecessary();
	}

	@Autowired
	private SaltSource saltSource;

	@Autowired
	private PasswordEncoder passwordEncoder;

	@Autowired
	private FileEntryService fileEntryService;
	
	/**
	 * Create users.
	 * 
	 * @param userId
	 *            userId
	 * @param password
	 *            raw user password
	 * @param role
	 *            role
	 * @param userName
	 *            user name
	 * @param email
	 *            email
	 */
	private void createUser(String userId, String password, Role role, String userName, String email) {
		if (userRepository.findOneByUserId(userId) == null) {
			User user = new User();
			user.setUserId(userId);
			SecuredUser securedUser = new SecuredUser(user, null);
			Object salt = saltSource.getSalt(securedUser);
			user.setPassword(passwordEncoder.encodePassword(password, salt));
			user.setRole(role);
			user.setUserName(userName);
			user.setEmail(email);
			user.setCreatedDate(new Date());
			user = userRepository.save(user);
			fileEntryService.prepare(user);
		}

	}

	/**
	 * Create initial users.
	 */
	private void createDefaultUserIfNecessary() {
		// If there is no users.. make admin and user and U, S, A roles.
		if (userRepository.count() < 2) {
			createUser("admin", "admin", Role.ADMIN, "admin", "admin@nhn.com");
			createUser("user", "user", Role.USER, "user", "user@nhn.com");
			createUser("superuser", "superuser", Role.SUPER_USER, "superuser", "superuser@nhn.com");
			createUser("system", "system", Role.SYSTEM_USER, "system", "system@nhn.com");
		}
	}
}
