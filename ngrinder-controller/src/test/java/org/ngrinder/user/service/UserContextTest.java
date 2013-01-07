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

import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.ngrinder.AbstractNGrinderTransactionalTest;
import org.ngrinder.security.NGrinderUserDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

/**
 * Class description.
 *
 * @author Mavlarn
 * @since 3.1
 */
public class UserContextTest extends AbstractNGrinderTransactionalTest {

	@Autowired
	private NGrinderUserDetailsService userDetailService;
	
	@Test
	public void testGetUser() {
		UserContext userCtx = new UserContext();
		
		//in super.beforeSetSecurity(), there is an admin user is set, but the auth is invalid
		try {
			userCtx.getCurrentUser();
			assertTrue(false);
		} catch (AuthenticationCredentialsNotFoundException e) {
			assertTrue(true);
		}

		UserDetails user = userDetailService.loadUserByUsername(getTestUser().getUserId());

		Authentication oriAuth = SecurityContextHolder.getContext().getAuthentication();

		UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(user, "123");
		SecurityContextHolder.getContext().setAuthentication(token);
		userCtx.getCurrentUser();
		assertTrue(true);

		SecurityContextHolder.getContext().setAuthentication(oriAuth);
	}
}
