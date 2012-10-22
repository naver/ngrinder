package org.ngrinder.infra.spring;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.ngrinder.AbstractNGrinderTransactionalTest;
import org.ngrinder.model.User;
import org.ngrinder.user.service.MockUserContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.MethodParameter;
import org.springframework.web.context.request.NativeWebRequest;

public class UserHandlerMethodArgumentResolverTest extends AbstractNGrinderTransactionalTest {

	private UserHandlerMethodArgumentResolver resolver;

	@Autowired
	private MockUserContext mockUserContext;

	@Test
	public void testUserHandlerMethodArgument() throws Exception {
		resolver = new UserHandlerMethodArgumentResolver();
		MethodParameter parameter = mock(MethodParameter.class);
		final Class<?> class1 = User.class;
		when(parameter.getParameterType()).thenAnswer(new Answer<Class<?>>() {
			@Override
			public Class<?> answer(InvocationOnMock invocation) throws Throwable {
				return class1;
			}
		});
		assertThat(resolver.supportsParameter(parameter), is(true));
		NativeWebRequest webRequest = mock(NativeWebRequest.class);
		when(webRequest.getParameter("ownerId")).thenReturn("admin");
		resolver.setUserContext(mockUserContext);
		Object resolveArgument = resolver.resolveArgument(parameter, null, webRequest, null);
		assertThat(((User) resolveArgument).getUserId(), is(getTestUser().getUserId()));
	}
}
