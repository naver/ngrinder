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
