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

	@Autowired
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
		return parameter.getParameterIndex() == 0 && parameter.getParameterType().equals(User.class);
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
	public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
			NativeWebRequest webRequest, WebDataBinderFactory binderFactory) throws Exception {
		User currentUser = getUserContext().getCurrentUser();

		String userParam = webRequest.getParameter("ownerId");
		if (StringUtils.isNotBlank(userParam) && currentUser.getRole().hasPermission(Permission.SWITCH_TO_ANYONE)) {
			return getUserService().getOne(userParam);
		}

		// User want to do something through other User status and this
		// switchUser is other user Id
		String switchUser = null;
		Cookie[] cookies = getCookies(webRequest);
		if (cookies != null) {
			for (Cookie cookie : cookies) {
				if ("switchUser".equals(cookie.getName()) && cookie.getMaxAge() != 0) {
					switchUser = cookie.getValue();
				}
			}
		}
		// Let this can be done with parameter as well.
		switchUser = StringUtils.defaultIfBlank(webRequest.getParameter("switchUser"), switchUser);
		
		if (currentUser.getUserId().equals(switchUser)) {
			currentUser.setOwnerUser(null);
		} else if (StringUtils.isNotEmpty(switchUser)) {
			User ownerUser = getUserService().getOne(switchUser);
			// CurrentUser should remember whose status he used
			if (currentUser.getRole().hasPermission(Permission.SWITCH_TO_ANYONE)
					|| (ownerUser.getFollowers() != null && ownerUser.getFollowers().contains(currentUser))) {
				currentUser.setOwnerUser(ownerUser);
				return ownerUser;
			}
		} else if (StringUtils.isEmpty(switchUser)) {
			currentUser.setOwnerUser(null);
		}

		return currentUser.getFactualUser();
	}

	Cookie[] getCookies(NativeWebRequest webRequest) {
		return ((ServletWebRequest) webRequest).getRequest().getCookies();
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
