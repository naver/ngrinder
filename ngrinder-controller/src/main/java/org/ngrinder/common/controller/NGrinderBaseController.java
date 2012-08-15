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
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import org.ngrinder.common.constant.NGrinderConstants;
import org.ngrinder.model.User;
import org.ngrinder.perftest.model.PerfTest;
import org.ngrinder.user.service.UserContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.web.bind.annotation.ModelAttribute;

public class NGrinderBaseController implements NGrinderConstants {

	public static final String ERROR_PAGE = "errors/error";

	protected static final int DEFAULT_PAGE_LIMIT = 20;
	
	@Autowired
	private MessageSource messageSource;

	@Autowired
	private UserContext userContext;

	public User getCurrentUser() {
		return userContext.getCurrentUser();
	}

	@ModelAttribute("currentUser")
	public User currentUser() {
		try {
			return getCurrentUser();
		} catch (AuthenticationCredentialsNotFoundException e) {
		}
		return new User();
	}

	public void setTimeZone(String timeZone) {
		User user = userContext.getCurrentUser();
		user.setTimeZone(timeZone);
		//now time zone is not saved in user table.
		//userService.saveUser(user);
	}

	//not we use '@ModelAttribute("currentUser")' to add currentUser
//	protected void setCurrentUserInfoForModel(ModelMap model) {
//		model.put(PARAM_USERID, getCurrentUser().getUserId());
//		model.put(PARAM_TIMEZONE, getCurrentUser().getTimeZone());
//	}

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
				modifyPerfTestDate(rawOffset, pro);
			}
		}
	}

	/**
	 * Modify the date in PerfTest object with the provided locale
	 * @param rawOffset
	 * @param pro
	 */
	private void modifyPerfTestDate(int rawOffset, PerfTest pro) {
		Date lastModified = pro.getLastModifiedDate();
		if (lastModified != null) {
			pro.setLastModifiedDate(new Date(lastModified.getTime() - rawOffset));
		}
		Date createdDate = pro.getCreatedDate();
		if (createdDate != null) {
			pro.setCreatedDate(new Date(createdDate.getTime() - rawOffset));
		}
		Date scheduledTime = pro.getScheduledTime();
		if (lastModified != null) {
			pro.setScheduledTime(new Date(scheduledTime.getTime() - rawOffset));
		}
	}

	protected void convertServerTimeToUserTimeForBean(Object obj, String userLocalId) {
		int rawOffset = getOffSet(userLocalId);
		if (obj instanceof PerfTest) {
			PerfTest pro = (PerfTest) obj;
			modifyPerfTestDate(rawOffset, pro);
		}
	}

	//it should not be used any more.
//	protected void convertPropertiesKeys(GrinderProperties props) {
//		Set<Object> copyKeySet = new HashSet<Object>(props.keySet());
//
//		for (Object obj : copyKeySet) {
//			String key = (String) obj;
//
//			if (key.startsWith("grinder.") || key.startsWith("ngrinder.")) {
//				Object value = props.get(key);
//				props.remove(key);
//				props.put(key.replace(".", "_"), value);
//			}
//		}
//	}
//
//	protected Map<String, Object> getMessageMap(Object isSuccess, String message) {
//		Map<String, Object> result = new HashMap<String, Object>();
//
//		result.put(JSON_SUCCESS, isSuccess);
//		result.put(JSON_MESSAGE, message);
//
//		return result;
//	}

	protected String getErrorMessages(String key) {
		Locale locale = null;
		String message = null;
		try {
			locale = new Locale(getCurrentUser().getUserLanguage());
			message = messageSource.getMessage(key, null, locale);
		} catch (Exception e) {
			return "Getting message error:" + e.getMessage();
		}
		return message;
	}

}
