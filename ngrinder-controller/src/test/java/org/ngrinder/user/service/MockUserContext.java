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

import javax.annotation.PostConstruct;

import org.ngrinder.infra.annotation.TestOnlyComponent;
import org.ngrinder.model.Role;
import org.ngrinder.model.User;
import org.ngrinder.user.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * user util
 * 
 * @author Tobi
 * @since
 * @date 2012-6-28
 */
@TestOnlyComponent
public class MockUserContext extends UserContext {
	public static final String TEST_USER_ID = "TEST_USER";
	public static final String TEST_USER_TIMEZONE_US = "America/New_York";
	public static final String TEST_USER_TIMEZONE_ZH = "Asia/Shanghai";

	@Autowired
	protected UserRepository userRepository;

	@PostConstruct
	public void init() {
		User user = userRepository.findOneByUserId(TEST_USER_ID);
		if (user == null) {
			user = new User();
			user.setUserId(TEST_USER_ID);
			user.setUserName("TEST_USER");
			user.setPassword("123");
			user.setRole(Role.USER);
			userRepository.save(user);
		}
	}

	public User getCurrentUser() {
		User user = userRepository.findOneByUserId(TEST_USER_ID);
		user.setUserLanguage("en");
		user.setTimeZone(TEST_USER_TIMEZONE_US);
		return user;
	}
}
