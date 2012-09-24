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
package org.ngrinder.security;

import org.ngrinder.extension.OnLoginRunnable;
import org.ngrinder.model.User;
import org.ngrinder.user.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.support.MessageSourceAccessor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.encoding.PasswordEncoder;
import org.springframework.security.core.SpringSecurityMessageSource;
import org.springframework.stereotype.Service;

/**
 * This detault Plugin
 * @author nhn
 *
 */
@Service
public class DefaultLoginPlugin implements OnLoginRunnable {

	protected static final Logger LOG = LoggerFactory.getLogger(DefaultLoginPlugin.class);

	@Autowired
	private UserService userService;

	protected MessageSourceAccessor messages = SpringSecurityMessageSource.getAccessor();

	@Override
	public User loadUser(String userId) {
		return userService.getUserById(userId);
	}

	@SuppressWarnings("deprecation")
	@Override
	public boolean validateUser(String userId, String password, String encPass, Object encoder, Object salt) {
		if (!((PasswordEncoder) encoder).isPasswordValid(password, encPass, salt)) {
			LOG.debug("Authentication failed: password does not match stored value");

			throw new BadCredentialsException(messages.getMessage(
					"AbstractUserDetailsAuthenticationProvider.badCredentials", "Bad credentials"), userId);
		}
		return true;
	}

	@Override
	public void saveUser(User user) {
		// Do nothing for default plugin
	}
}
