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
import org.ngrinder.user.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DBInit {
	@Autowired
	private UserRepository userRepository;

	@PostConstruct
	@Transactional
	public void init() {
		createDefaultUserIfNecessary();
	}

	public void createUser(String userId, String password, Role role, String userName, String email) {
		if (userRepository.findOneByUserId(userId) == null) {
			User adminUser = new User();
			adminUser.setUserId(userId);
			adminUser.setPassword(password);
			adminUser.setRole(role);
			adminUser.setUserName(userName);
			adminUser.setEmail(email);
			adminUser.setCreatedDate(new Date());
			userRepository.save(adminUser);
		}

	}

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
