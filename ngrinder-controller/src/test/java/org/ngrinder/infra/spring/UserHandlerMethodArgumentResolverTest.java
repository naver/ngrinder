package org.ngrinder.infra.spring;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

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

public class UserHandlerMethodArgumentResolverTest extends AbstractNGrinderTransactionalTest {

	private UserHandlerMethodArgumentResolver resolver;

	@Autowired
	private MockUserContext mockUserContext;

	@Autowired
	private UserService userService;

	@Test
	public void testUserHandlerMethodArgument() throws Exception {
		
		//create a tmp test user "TEST2_USER" for this test
		User user = new User();
		user.setUserId("TEST2_USER");
		user.setUserName("TEST2_USER");
		user.setEmail("TEST2_USER@nhn.com");
		user.setPassword("123");
		user.setRole(Role.USER);
		userRepository.save(user);
		
		
		resolver = new UserHandlerMethodArgumentResolver();
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
		
		//test1 scenario: general user can not check other user's script
		// has parameter "ownerId", and current user is general, resolved user is "TEST_USER"
		NativeWebRequest webRequest1 = mock(NativeWebRequest.class);
		when(webRequest1.getParameter("ownerId")).thenReturn("TEST2_USER");
		resolver.setUserContext(mockUserContext);
		Object resolveArgument1 = resolver.resolveArgument(parameter, null, webRequest1, null);
		assertThat(((User) resolveArgument1).getUserId(), is(getTestUser().getUserId()));
		
		//test2 scenario: admin can check other user's script
		// has parameter "ownerId", and current user is Admin, resolved user is "TEST2_USER"
		NativeWebRequest webRequest2 = mock(NativeWebRequest.class);
		when(webRequest2.getParameter("ownerId")).thenReturn("TEST2_USER");
		User adminUser = new User("tmpAdminId", "tmpAdminId", "tmpAdminPwd", "admin@nhn.com", Role.ADMIN);
		MockUserContext adminUserContext = mock(MockUserContext.class);
		when(adminUserContext.getCurrentUser()).thenReturn(adminUser);
		resolver.setUserContext(adminUserContext);
		Object resolveArgument2 = resolver.resolveArgument(parameter, null, webRequest2, null);
		assertThat(((User) resolveArgument2).getUserId(), is("TEST2_USER"));

		
		//test3 scenario: general user switch to use other's permission
		// has parameter "switchUserId", resolved user id is "TEST2_USER"
		NativeWebRequest webRequest3 = mock(NativeWebRequest.class);
		when(webRequest3.getParameter("switchUserId")).thenReturn("TEST2_USER");
		resolver.setUserContext(mockUserContext);
		Object resolveArgument3 = resolver.resolveArgument(parameter, null, webRequest3, null);
		assertThat(((User) resolveArgument3).getUserId(), is("TEST2_USER"));
		//current user's owner is "TEST2_USER"
		assertThat(getTestUser().getOwnerUser().getUserId(), is("TEST2_USER"));
		
		//test4 scenario: general user switch back to its own user permission
		// has parameter "switchUserId", resolved user id is "TEST_USER"
		NativeWebRequest webRequest4 = mock(NativeWebRequest.class);
		when(webRequest4.getParameter("switchUserId")).thenReturn("TEST_USER");
		resolver.setUserContext(mockUserContext);
		Object resolveArgument4 = resolver.resolveArgument(parameter, null, webRequest4, null);
		assertThat(((User) resolveArgument4).getUserId(), is("TEST_USER"));
		//current user's owner is null
		assertThat(getTestUser().getOwnerUser(), nullValue());
	}
}
