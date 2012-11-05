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

import java.util.Date;
import java.util.HashMap;

import org.ngrinder.model.Role;
import org.ngrinder.model.User;
import org.ngrinder.user.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationProvider;
import org.springframework.transaction.annotation.Transactional;

/**
 * nGrinder authentication provide. This class is for the plugin system of user authentication.
 * 
 * @author JunHo Yoon
 * @since 3.0
 * 
 */
public class NGrinderAuthenticationPreAuthProvider extends PreAuthenticatedAuthenticationProvider {

	protected static final Logger LOG = LoggerFactory.getLogger(NGrinderAuthenticationPreAuthProvider.class);

	private UserRepository userRepository;

	// ~ Methods
	// ========================================================================================================

	/**
	 * Authenticate the given PreAuthenticatedAuthenticationToken.
	 * <p>
	 * If the principal contained in the authentication object is null, the request will be ignored
	 * to allow other providers to authenticate it.
	 */
	@SuppressWarnings("unchecked")
	public Authentication authenticate(Authentication authentication) throws AuthenticationException {
		Object details = authentication.getDetails();

		Authentication authenticate = super.authenticate(authentication);
		SecuredUser securedUser = (SecuredUser) authenticate.getPrincipal();
		if (details instanceof HashMap) {
			securedUser.getUser().setTimeZone(((HashMap<String, String>) details).get("user_timezone"));
			securedUser.getUser().setUserLanguage(((HashMap<String, String>) details).get("user_language"));
		} else if (details instanceof LanguageAndTimezone) {
			LanguageAndTimezone languageAndTimeZone = ((LanguageAndTimezone) details);
			securedUser.getUser().setTimeZone(languageAndTimeZone.getTimezone());
			securedUser.getUser().setUserLanguage(languageAndTimeZone.getLanguage());
		}
		// If It's the first time to login
		// means.. If the user info provider is not defaultLoginPlugin..
		if (securedUser.getUserInfoProviderClass() != null
						&& !isClassEqual(DefaultLoginPlugin.class, securedUser.getUserInfoProviderClass())) {

			addNewUserIntoLocal(securedUser);
		}
		return authenticate;
	}

	/**
	 * Check if given clazz has the given clazzName.
	 * 
	 * @param clazz
	 *            class
	 * @param clazzName
	 *            classname which is checked aginst
	 * @return true if same
	 */
	private boolean isClassEqual(Class<?> clazz, String clazzName) {
		return clazz.getName().equals(clazzName);
	}

	/**
	 * Add new user into local db.
	 * 
	 * @param securedUser
	 *            user
	 */
	@Transactional
	public void addNewUserIntoLocal(SecuredUser securedUser) {
		User user = securedUser.getUser();
		user.setAuthProviderClass(securedUser.getUserInfoProviderClass());
		user.setCreatedDate(new Date());
		User findOneByUserId = getUserRepository().findOneByUserId(user.getUserId());
		if (findOneByUserId != null) {
			user = findOneByUserId.merge(user);
		}
		if (user.getRole() == null) {
			user.setRole(Role.USER);
		}
		User savedUser = getUserRepository().save(user);
		securedUser.setUser(savedUser);
	}

	public UserRepository getUserRepository() {
		return userRepository;
	}

	public void setUserRepository(UserRepository userRepository) {
		this.userRepository = userRepository;
	}

}
