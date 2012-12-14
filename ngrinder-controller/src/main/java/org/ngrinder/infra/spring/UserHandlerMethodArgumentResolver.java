/*
 * Copyright (C) 2012 - 2012 NHN Corporation
 * All rights reserved.
 *
 * This file is part of The nGrinder software distribution. Refer to
 * the file LICENSE which is part of The nGrinder distribution for
 * licensing details. The nGrinder distribution is available on the
 * Internet at http://nhnopensource.org/ngrinder
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS
 * FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE
 * COPYRIGHT HOLDERS OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
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
