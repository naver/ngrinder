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
package org.ngrinder.common.controller;

import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;

import net.grinder.common.GrinderProperties;

import org.ngrinder.common.constant.GrinderConstants;
import org.ngrinder.model.User;
import org.ngrinder.perftest.model.PerfTest;
import org.ngrinder.user.service.UserContext;
import org.ngrinder.user.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.ModelAttribute;

public class NGrinderBaseController implements GrinderConstants {

	public static final String ERROR_PAGE = "errors/error";
	@Autowired
	private MessageSource messageSource;

	@Autowired
	private UserContext userContext;

	@Autowired
	private UserService userService;

	@ModelAttribute("currentUser")
	public User getCurrentUser() {
		try {
			return userContext.getCurrentUser();
		} catch (AuthenticationCredentialsNotFoundException e) {

		}
		return null;
	}

	/**
	 * @param param
	 *            userId or timeZone or userLanguage or role
	 * @return
	 */
	public String getCurrentUserInfo(String param) {
		User user = getCurrentUser();
		if (param.equals(P_USERID))
			return user.getUserId();
		else if (param.equals("role"))
			return user.getRole().getShortName();
		else if (param.equals("userLanguage"))
			return user.getUserLanguage();
		else if (param.equals("timeZone"))
			return user.getTimeZone();
		else
			return user.toString();

	}

	public void setTimeZone(String timeZone) {
		User user = userContext.getCurrentUser();
		user.setTimeZone(timeZone);
		userService.saveUser(user);
	}

	protected void setCurrentUserInfoForModel(ModelMap model) {
		model.put(GrinderConstants.P_USERID, getCurrentUserInfo("userId"));
		model.put("timeZone", getCurrentUserInfo("timeZone"));
	}

	protected void addMsgToModel(ModelMap model, String message) {
		model.addAttribute(GrinderConstants.P_MESSAGE, message);
	}

	protected int getOffSet(String userLocalId) {
		if (userLocalId == null) {
			return 0;
		}
		return TimeZone.getDefault().getRawOffset() - TimeZone.getTimeZone(userLocalId).getRawOffset();
	}

	protected void convertServerTimeToUserTime(List<?> list, String userLocalId) {
		int rawOffset = getOffSet(userLocalId);

		for (Object obj : list) {
			if (obj instanceof PerfTest) {
				PerfTest pro = (PerfTest) obj;
				Date lastModified = pro.getLastModifiedDate();
				pro.setLastModifiedDate(new Date(lastModified.getTime() - rawOffset));
			}
		}
	}

	protected void convertServerTimeToUserTimeForBean(Object obj, String userLocalId) {
		int rawOffset = getOffSet(userLocalId);
		if (obj instanceof PerfTest) {
			PerfTest pro = (PerfTest) obj;
			Date lastModified = pro.getLastModifiedDate();
			pro.setLastModifiedDate(new Date(lastModified.getTime() - rawOffset));
		}
	}

	protected void convertPropertiesKeys(GrinderProperties props) {
		Set<Object> copyKeySet = new HashSet<Object>(props.keySet());

		for (Object obj : copyKeySet) {
			String key = (String) obj;

			if (key.startsWith("grinder.") || key.startsWith("ngrinder.")) {
				Object value = props.get(key);
				props.remove(key);
				props.put(key.replace(".", "_"), value);
			}
		}
	}

	protected Map<String, Object> getMessageMap(Object isSuccess, String message) {
		Map<String, Object> result = new HashMap<String, Object>();

		result.put(GrinderConstants.P_SUCCESS, isSuccess);
		result.put(GrinderConstants.P_MESSAGE, message);

		return result;
	}

	protected String getErrorMessages(String key) {
		Locale locale = null;
		String message = null;
		try {
			locale = new Locale(getCurrentUserInfo("userLanguage"));
			message = messageSource.getMessage(key, null, locale);
		} catch (Exception e) {
			return "Getting message error:" + e.getMessage();
		}
		return message;
	}

}
