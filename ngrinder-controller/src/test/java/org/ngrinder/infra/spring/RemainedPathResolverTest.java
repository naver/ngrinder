package org.ngrinder.infra.spring;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.Test;
import org.springframework.core.MethodParameter;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.context.request.NativeWebRequest;

public class RemainedPathResolverTest {

	@Test
	public void testResolveArgument() throws Exception {

		MethodParameter mock2 = mock(MethodParameter.class);
		RequestMapping requestMapping = mock(RequestMapping.class);
		when(mock2.getMethodAnnotation(RequestMapping.class)).thenReturn(requestMapping);
		when(requestMapping.value()).thenReturn(new String[] { "/list/**" });
		final RequestMapping requestMappingOnType = mock(RequestMapping.class);
		when(requestMappingOnType.value()).thenReturn(new String[] { "/script" });
		RemainedPathMethodArgumentResolver resolver = new RemainedPathMethodArgumentResolver() {
			@Override
			protected RequestMapping getDeclaringClassRequestMapping(MethodParameter parameter) {
				return requestMappingOnType;
			}
		};
		NativeWebRequest nativeWebRequestMock = mock(NativeWebRequest.class);
		when(nativeWebRequestMock.getAttribute(anyString(), anyInt())).thenReturn("/list/script/hello/world");
		assertThat((String) resolver.resolveArgument(mock2, null, nativeWebRequestMock, null), is("hello/world"));

	}
}
