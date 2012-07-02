package org.ngrinder.home.controller;

import java.util.Locale;
import java.util.TimeZone;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.ngrinder.common.controller.NGrinderBaseController;
import org.ngrinder.common.exception.NGrinderRuntimeException;
import org.ngrinder.common.util.DateUtil;
import org.ngrinder.infra.config.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.propertyeditors.LocaleEditor;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.support.RequestContextUtils;

@Controller
public class HomeController extends NGrinderBaseController {

	private static final Logger LOG = LoggerFactory.getLogger(HomeController.class);

	@Autowired
	private Config config;

	@RequestMapping(value = "/home")
	public String home(ModelMap model, HttpServletResponse response, HttpServletRequest request) {

		String roles;
		try {
			// set local language
			setLanguage(getCurrentUserInfo("userLanguage"), response, request);
			setLoginPageDate(model);
			roles = getCurrentUserInfo("role");
		} catch (AuthenticationCredentialsNotFoundException e) {
			return "login";
		}
		if (roles == null) {
			return "login";
		} else if (roles.indexOf("U") != -1) {
			return "redirect:/project/list";
		} else if (roles.indexOf("A") != -1 || roles.indexOf("S") != -1) {
			return "redirect:/agent/list?page=1";
		} else {
			LOG.info("Invalid user role:{}", roles);
			return "login";
		}
	}

	public void setLanguage(String lan, HttpServletResponse response, HttpServletRequest request) {
		LocaleResolver localeResolver = RequestContextUtils.getLocaleResolver(request);
		if (lan != null) {
			if (localeResolver == null) {
				throw new NGrinderRuntimeException("No LocaleResolver found!");
			}
			LocaleEditor localeEditor = new LocaleEditor();
			localeEditor.setAsText(lan);
			localeResolver.setLocale(request, response, (Locale) localeEditor.getValue());
		} else {
			throw new NGrinderRuntimeException("No User Language found!");
		}
	}

	@RequestMapping(value = "/login")
	public String login(ModelMap model) {
		setLoginPageDate(model);
		return "login";
	}

	public void setLoginPageDate(ModelMap model) {
		TimeZone defaultTime = TimeZone.getDefault();
		model.addAttribute("version", config.getSystemProperties().getProperty("VERSION", "UNKNOWN"));
		model.addAttribute("defaultTime", defaultTime.getID());
	}

	@RequestMapping(value = "/help")
	public String openHelp(ModelMap model) {
		setCurrentUserInfoForModel(model);
		return "help";
	}

	@RequestMapping(value = "/changeTimeZone")
	public @ResponseBody
	String changeTimeZone(String timeZone) {
		return setTimeZone(timeZone);
	}

	@RequestMapping(value = "/allTimeZone")
	public String getAllTimeZone(ModelMap model) {
		model.addAttribute("timeZones", DateUtil.getFilteredTimeZoneMap());

		return "allTimeZone";
	}
}
