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

import javax.servlet.http.Cookie;

import org.apache.commons.lang.StringUtils;
import org.ngrinder.model.Permission;
import org.ngrinder.model.User;
import org.ngrinder.user.service.UserContext;
import org.ngrinder.user.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.MethodParameter;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

/**
 * {@link HandlerMethodArgumentResolver} for {@link User} argument.
 * 
 * It passes the current user instance on {@link User} argument.
 * 
 * @author JunHo Yoon
 * @since 3.0
 */
public class UserHandlerMethodArgumentResolver implements HandlerMethodArgumentResolver {

	private UserContext userContext;

	@Autowired
	private UserService userService;

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.springframework.web.method.support.HandlerMethodArgumentResolver#
	 * supportsParameter(org .springframework.core.MethodParameter)
	 */
	@Override
	public boolean supportsParameter(MethodParameter parameter) {
		return parameter.getParameterType().equals(User.class);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.springframework.web.method.support.HandlerMethodArgumentResolver#
	 * resolveArgument(org. springframework.core.MethodParameter,
	 * org.springframework.web.method.support.ModelAndViewContainer,
	 * org.springframework.web.context.request.NativeWebRequest,
	 * org.springframework.web.bind.support.WebDataBinderFactory)
	 */
	@Override
	public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer, NativeWebRequest webRequest,
			WebDataBinderFactory binderFactory) throws Exception {
		User currentUser = getUserContext().getCurrentUser();
		// User want to do something through other User status and this
		// switchUserId is other user Id
		String userParam = webRequest.getParameter("ownerId");
		String switchUserId = null;
		for (Cookie cookie : ((ServletWebRequest) webRequest).getRequest().getCookies()) {
			if ("switchUser".equals(cookie.getName()) && cookie.getMaxAge() != 0) {
				switchUserId = cookie.getValue();
			}
		}

		if (StringUtils.isNotBlank(userParam) && currentUser.getRole().hasPermission(Permission.SWITCH_TO_ANYONE)) {
			return getUserService().getUserById(userParam);
		} 

		if (currentUser.getUserId().equals(switchUserId)) {
			currentUser.setOwnerUser(null);
		} else if (StringUtils.isNotEmpty(switchUserId)) {
			User ownerUser = getUserService().getUserById(switchUserId);
			// CurrentUser should remember whose status he used
			if (currentUser.getRole().hasPermission(Permission.SWITCH_TO_ANYONE) || ownerUser.getFollowers().contains(currentUser)) {
				currentUser.setOwnerUser(ownerUser);
				return ownerUser;
			}
		} else if (StringUtils.isEmpty(switchUserId)) {
			currentUser.setOwnerUser(null);
		}

		return currentUser.getFactualUser();
	}

	/**
	 * Get current user context.<br/>
	 * This method is provided for XML based spring bean injection.
	 * 
	 * @return user context
	 */
	public UserContext getUserContext() {
		return userContext;
	}

	/**
	 * Set the current user context.<br/>
	 * This method is provided for XML based spring bean injection.
	 * 
	 * @param userContext
	 *            user context.
	 */
	public void setUserContext(UserContext userContext) {
		this.userContext = userContext;
	}

	public UserService getUserService() {
		return userService;
	}

	public void setUserService(UserService userService) {
		this.userService = userService;
	}
}
