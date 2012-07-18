/*
 * Copyright 2012 NHNCorp, Inc.
 *
 * NHN Corp licenses this file to you under the Apache License, version 2.0
 * (the "License"); you may not use this file except in compliance with the
 * License.  You may obtain a copy of the License at:
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
package org.ngrinder.infra.spring;

import org.springframework.core.MethodParameter;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.support.WebArgumentResolver;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;
import org.springframework.web.servlet.HandlerMapping;

/**
 * {@link WebArgumentResolver} which puts the PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE attribute to parameter with the
 * RemainedPath annotation.
 * 
 * @author JunHo Yoon
 * 
 */
public class RemainedPathMethodArgumentResolver implements HandlerMethodArgumentResolver {

	@Override
	public boolean supportsParameter(MethodParameter parameter) {
		return parameter.getParameterAnnotation(RemainedPath.class) != null;
	}

	private AntPathMatcher pathMatcher = new AntPathMatcher();

	@Override
	public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
			NativeWebRequest webRequest, WebDataBinderFactory binderFactory) throws Exception {
		RequestMapping requestMappingOnMethod = parameter.getMethodAnnotation(RequestMapping.class);
		RequestMapping requestMappingOnClass = getDeclaringClassRequestMapping(parameter);
		String combine = pathMatcher.combine(requestMappingOnClass.value()[0], requestMappingOnMethod.value()[0]);
		return pathMatcher.extractPathWithinPattern(combine, (String) webRequest.getAttribute(
				HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE, NativeWebRequest.SCOPE_REQUEST));
	}

	@SuppressWarnings("unchecked")
	protected RequestMapping getDeclaringClassRequestMapping(MethodParameter parameter) {
		return (RequestMapping) parameter.getDeclaringClass().getAnnotation(RequestMapping.class);
	}
}
