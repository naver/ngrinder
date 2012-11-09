package org.ngrinder.security;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.junit.Test;
import org.ngrinder.AbstractNGrinderTransactionalTest;
import org.ngrinder.model.User;
import org.ngrinder.user.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;

public class NgrinderUsernamePasswordAuthenticationFilterTest extends AbstractNGrinderTransactionalTest {
	private MockNgrinderUsernamePasswordAuthenticationFilter filter = new MockNgrinderUsernamePasswordAuthenticationFilter();

	private class MockNgrinderUsernamePasswordAuthenticationFilter extends NgrinderUsernamePasswordAuthenticationFilter {
		protected org.springframework.security.core.Authentication getAuthentification(
						javax.servlet.http.HttpServletRequest request, javax.servlet.http.HttpServletResponse response) {
			Authentication auth = mock(Authentication.class);
			when(auth.getPrincipal()).thenReturn(new SecuredUser(getTestUser(), null));
			return auth;
		};

<<<<<<< OURS
		public void setUserRepository(UserRepository userRepository) {
			this.userRepository = userRepository;
		}
=======
>>>>>>> THEIRS
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
		User findOne = userRepository.findOne(getTestUser().getId());
		assertThat(findOne.getUserLanguage(), is("KoreanLang"));
		assertThat(findOne.getTimeZone(), is("Korean"));

	}
}
