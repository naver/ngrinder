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
import org.ngrinder.common.util.JSONUtil;
import org.ngrinder.perftest.model.PerfTest;
import org.ngrinder.user.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.ui.ModelMap;

public class NGrinderBaseController implements GrinderConstants {

	public static final String ERROR_PAGE = "errors/error";
	@Autowired
	private MessageSource messageSource;

	/**
	 * @param param
	 *            userId or timeZone or userLanguage or role
	 * @return
	 */
	public String getCurrentUserInfo(String param) {
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		if (auth == null) {
			throw new AuthenticationCredentialsNotFoundException("No athenticated");
		}
		Object obj = auth.getPrincipal();
		if (!(obj instanceof User)) {
			throw new AuthenticationCredentialsNotFoundException("Invalid athentication");
		}
		User user = (User) obj;
		if (param.equals(P_USERID))
			return user.getUserId();
		else if (param.equals("role"))
			return auth.getAuthorities().toString();
		else if (param.equals("userLanguage"))
			return user.getUserLanguage();
		else if (param.equals("timeZone"))
			return user.getTimeZone();
		else
			return user.toString();

	}

	public String setTimeZone(String timeZone) {
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();

		if (auth != null) {
			Object obj = auth.getPrincipal();
			if (obj instanceof User) {
				User user = (User) obj;
				user.setTimeZone(timeZone);
				return JSONUtil.returnSuccess();
			}
		}

		return JSONUtil.returnError();
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
