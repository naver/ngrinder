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
package org.ngrinder.infra.spring;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.ngrinder.common.util.TypeConvertUtils.cast;

import javax.servlet.http.Cookie;

import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.ngrinder.AbstractNGrinderTransactionalTest;
import org.ngrinder.model.Role;
import org.ngrinder.model.User;
import org.ngrinder.user.service.MockUserContext;
import org.ngrinder.user.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.MethodParameter;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.context.request.ServletWebRequest;

import com.google.common.collect.Lists;

public class UserHandlerMethodArgumentResolverTest extends AbstractNGrinderTransactionalTest {

	private UserHandlerMethodArgumentResolver resolver;

	@Autowired
	private MockUserContext mockUserContext;

	@Autowired
	private UserService userService;

	String switchUser;

	@Test
	public void testUserHandlerMethodArgument() throws Exception {

		// create a tmp test user "TEST2_USER" for this test
		User user = new User();
		user.setUserId("TEST2_USER");
		user.setUserName("TEST2_USER");
		user.setEmail("TEST2_USER@nhn.com");
		user.setPassword("123");
		user.setRole(Role.USER);
		user = userRepository.save(user);
		User testUser = getTestUser();
		testUser.setFollowers(Lists.newArrayList(user));
		userRepository.save(testUser);

		resolver = new UserHandlerMethodArgumentResolver() {
			@Override
			Cookie[] getCookies(NativeWebRequest webRequest) {
				return new Cookie[] { new Cookie("switchUser", switchUser) };
			}
		};
		resolver.setUserService(userService);
		MethodParameter parameter = mock(MethodParameter.class);
		final Class<?> class1 = User.class;
		when(parameter.getParameterType()).thenAnswer(new Answer<Class<?>>() {
			@Override
			public Class<?> answer(InvocationOnMock invocation) throws Throwable {
				return class1;
			}
		});
		assertThat(resolver.supportsParameter(parameter), is(true));

		// test1 scenario: general user can not check other user's script
		// has parameter "ownerId", and current user is general, resolved user is "TEST_USER"
		ServletWebRequest webRequest1 = mock(ServletWebRequest.class);
		when(webRequest1.getParameter("ownerId")).thenReturn("TEST2_USER");
		resolver.setUserContext(mockUserContext);
		Object resolveArgument1 = resolver.resolveArgument(parameter, null, webRequest1, null);
		assertThat(((User) resolveArgument1).getUserId(), is(getTestUser().getUserId()));

		// test2 scenario: admin can check other user's script
		// has parameter "ownerId", and current user is Admin, resolved user is "TEST2_USER"
		ServletWebRequest webRequest2 = mock(ServletWebRequest.class);
		when(webRequest2.getParameter("ownerId")).thenReturn("TEST2_USER");
		User adminUser = new User("tmpAdminId", "tmpAdminId", "tmpAdminPwd", "admin@nhn.com", Role.ADMIN);
		MockUserContext adminUserContext = mock(MockUserContext.class);
		when(adminUserContext.getCurrentUser()).thenReturn(adminUser);
		resolver.setUserContext(adminUserContext);
		Object resolveArgument2 = resolver.resolveArgument(parameter, null, webRequest2, null);
		assertThat(((User) resolveArgument2).getUserId(), is("TEST2_USER"));

		// test3 scenario: general user switch to use other's permission
		// has parameter "switchUser", resolved user id is "TEST2_USER"
		ServletWebRequest webRequest3 = mock(ServletWebRequest.class);
		switchUser = "TEST2_USER";
		resolver.setUserContext(mockUserContext);
		User resolveArgument3 = cast(resolver.resolveArgument(parameter, null, webRequest3, null));
		assertThat(((User) resolveArgument3).getUserId(), is("TEST_USER"));
		// current user's owner is "TEST2_USER"
		// assertThat(resolveArgument3.getOwnerUser().getUserId(), is("TEST2_USER"));

		// test4 scenario: general user switch back to its own user permission
		// has parameter "switchUserId", resolved user id is "TEST_USER"
		ServletWebRequest webRequest4 = mock(ServletWebRequest.class);
		switchUser = "TEST_USER";
		resolver.setUserContext(mockUserContext);
		Object resolveArgument4 = resolver.resolveArgument(parameter, null, webRequest4, null);
		assertThat(((User) resolveArgument4).getUserId(), is("TEST_USER"));
		// current user's owner is null
	}
}
