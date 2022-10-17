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

import org.ngrinder.common.util.EncodingUtils;
import org.ngrinder.common.util.PathUtils;
import org.springframework.core.MethodParameter;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import static java.util.Objects.requireNonNull;

/**
 * Custom argument resolver to catch the unresolved remaining path.
 *
 * <pre>
 *  @RequestMapping("hello/**")
 * 	public String handleURL(@RemainedPath String path) {
 *   ....
 * 	}
 * </pre>
 *
 * When hello/world/1 url is called, world/1 will be provided in path.
 *
 * @since 3.0
 */
public class RemainedPathMethodArgumentResolver implements HandlerMethodArgumentResolver {

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * org.springframework.web.method.support.HandlerMethodArgumentResolver#supportsParameter(org
	 * .springframework.core.MethodParameter)
	 */
	@Override
	public boolean supportsParameter(MethodParameter parameter) {
		return parameter.getParameterAnnotation(RemainedPath.class) != null;
	}

	/*
	 * (non-Javadoc)
	 *
	 *
	 * @see
	 * org.springframework.web.method.support.HandlerMethodArgumentResolver#resolveArgument(org.
	 * springframework.core.MethodParameter,
	 * org.springframework.web.method.support.ModelAndViewContainer,
	 * org.springframework.web.context.request.NativeWebRequest,
	 * org.springframework.web.bind.support.WebDataBinderFactory)
	 */
	@Override
	public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
					NativeWebRequest webRequest, WebDataBinderFactory binderFactory) {
		AntPathMatcher pathMatcher = new AntPathMatcher();
		RequestMapping requestMappingOnMethod = parameter.getMethodAnnotation(RequestMapping.class);
		RequestMapping requestMappingOnClass = getDeclaringClassRequestMapping(parameter);
		String combine = pathMatcher.combine(requestMappingOnClass.value()[0], requireNonNull(requestMappingOnMethod).value()[0]);
		String path = ((ServletWebRequest) webRequest).getRequest().getRequestURI().substring(webRequest.getContextPath().length());
		return PathUtils.removePrependedSlash(pathMatcher.extractPathWithinPattern(combine, EncodingUtils.decodePathWithUTF8(path)));
	}

	/**
	 * Get the request mapping annotation on the given parameter.
	 *
	 * @param parameter parameter
	 * @return {@link RequestMapping} annotation
	 */
	protected RequestMapping getDeclaringClassRequestMapping(MethodParameter parameter) {
		return parameter.getDeclaringClass().getAnnotation(RequestMapping.class);
	}
}
