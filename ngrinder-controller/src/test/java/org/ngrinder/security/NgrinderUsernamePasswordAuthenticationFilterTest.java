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

import org.junit.Test;
import org.ngrinder.AbstractNGrinderTransactionalTest;
import org.ngrinder.model.User;
import org.ngrinder.user.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.ngrinder.user.repository.UserSpecification.idEqual;

public class NgrinderUsernamePasswordAuthenticationFilterTest extends AbstractNGrinderTransactionalTest {
	private MockNgrinderUsernamePasswordAuthenticationFilter filter = new MockNgrinderUsernamePasswordAuthenticationFilter();

	private class MockNgrinderUsernamePasswordAuthenticationFilter extends NgrinderUsernamePasswordAuthenticationFilter {
		protected org.springframework.security.core.Authentication getAuthentication(
				javax.servlet.http.HttpServletRequest request, javax.servlet.http.HttpServletResponse response) {
			Authentication auth = mock(Authentication.class);
			when(auth.getPrincipal()).thenReturn(new SecuredUser(getTestUser(), null));
			return auth;
		};
	};

	@Autowired
	private UserRepository userRepository;

	@Test
	public void testFilter() {
		filter.setUserRepository(userRepository);
		HttpServletRequest req = mock(HttpServletRequest.class);
		when(req.getParameter("user_timezone")).thenReturn("Korean");
		when(req.getParameter("native_language")).thenReturn("KoreanLang");
		HttpServletResponse res = mock(HttpServletResponse.class);
		filter.attemptAuthentication(req, res);
		Optional<User> findOne = userRepository.findOne(idEqual(getTestUser().getId()));
		if (!findOne.isPresent()) {
			fail();
		}
		User user = findOne.get();
		assertThat(user.getUserLanguage(), is("KoreanLang"));
		assertThat(user.getTimeZone(), is("Korean"));

	}
}
