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
package org.ngrinder.security;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.ngrinder.model.User;
import org.ngrinder.user.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * nGrinder customized login authentication filter. This checks not only auth but also timezone and
 * locale.
 *
 * @author JunHo Yoon
 * @since 3.0
 */
public class NgrinderUsernamePasswordAuthenticationFilter extends UsernamePasswordAuthenticationFilter {

	private UserRepository userRepository;

	/**
	 * Constructor.
	 */
	public NgrinderUsernamePasswordAuthenticationFilter() {
		super();
	}

	public NgrinderUsernamePasswordAuthenticationFilter(UserRepository userRepository) {
		this.userRepository = userRepository;
	}

	@Override
	public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) {
		Authentication auth = getAuthentication(request, response);
		String timezone = request.getParameter("user_timezone");
		String language = request.getParameter("native_language");
		SecuredUser securedUser = (SecuredUser) auth.getPrincipal();
		User user = securedUser.getUser();
		User existingUser = userRepository.findOneByUserId(user.getUserId());
		if (existingUser != null) {
			user = existingUser;
		}
		user.setTimeZone(timezone);
		user.setUserLanguage(language);
		securedUser.setUser(userRepository.saveAndFlush(user));
		return auth;
	}

	protected Authentication getAuthentication(HttpServletRequest request, HttpServletResponse response) {
		return super.attemptAuthentication(request, response);
	}

	@Override
	protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response,
	                                        javax.servlet.FilterChain chain, Authentication authResult) throws IOException, ServletException {
		super.successfulAuthentication(request, response, chain, authResult);
	}


	@Override
	protected void unsuccessfulAuthentication(HttpServletRequest request, HttpServletResponse response,
	                                          AuthenticationException failed) throws IOException, ServletException {
		super.unsuccessfulAuthentication(request, response, failed);
	}

	public void setUserRepository(UserRepository userRepository) {
		this.userRepository = userRepository;
	}

}
