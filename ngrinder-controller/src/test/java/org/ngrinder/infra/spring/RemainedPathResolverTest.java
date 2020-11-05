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

import org.junit.Test;
import org.springframework.core.MethodParameter;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.context.request.ServletWebRequest;

import javax.servlet.http.HttpServletRequest;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

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
		HttpServletRequest httpServletRequestMock = mock(HttpServletRequest.class);
		when(httpServletRequestMock.getRequestURI()).thenReturn("/script/list/hello/world");
		when(httpServletRequestMock.getContextPath()).thenReturn("");
		assertThat(resolver.resolveArgument(mock2, null,
			new ServletWebRequest(httpServletRequestMock), null), is("hello/world"));

		when(httpServletRequestMock.getRequestURI()).thenReturn("ngrinder/script/list/hello/world");
		when(httpServletRequestMock.getContextPath()).thenReturn("ngrinder");
		assertThat(resolver.resolveArgument(mock2, null,
			new ServletWebRequest(httpServletRequestMock), null), is("hello/world"));

	}
}
