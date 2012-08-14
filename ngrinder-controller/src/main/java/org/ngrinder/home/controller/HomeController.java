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
package org.ngrinder.home.controller;

import java.util.Locale;
import java.util.TimeZone;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.ngrinder.common.controller.NGrinderBaseController;
import org.ngrinder.common.exception.NGrinderRuntimeException;
import org.ngrinder.common.util.DateUtil;
import org.ngrinder.common.util.JSONUtil;
import org.ngrinder.home.service.HomeService;
import org.ngrinder.infra.config.Config;
import org.ngrinder.model.User;
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

/**
 * Home index page controller.
 * 
 * @author JunHo Yoon
 * @since 3.0
 */
@Controller
public class HomeController extends NGrinderBaseController {

	private static final Logger LOG = LoggerFactory.getLogger(HomeController.class);

	@Autowired
	private Config config;

	@Autowired
	private HomeService homeService;

	@RequestMapping(value = { "/home", "/" })
	public String home(User user, ModelMap model, HttpServletResponse response, HttpServletRequest request) {
		String roles = null;
		try {
			// set local language
			setLanguage(getCurrentUser().getUserLanguage(), response, request);
			setLoginPageDate(model);
			roles = user.getRole().getShortName();

		} catch (AuthenticationCredentialsNotFoundException e) {
			return "login";
		}

		model.addAttribute("right_panel_entries", homeService.getRightPanelEntries());
		model.addAttribute("left_panel_entries", homeService.getLeftPanelEntries());

		if (roles.contains("A") || roles.contains("U")) {
			return "index";
		} else {
			LOG.info("Invalid user role:{}", roles);
			return "login";
		}
	}

	public void setLanguage(String lan, HttpServletResponse response, HttpServletRequest request) {
		LocaleResolver localeResolver = RequestContextUtils.getLocaleResolver(request);
		if (lan == null) {
			throw new NGrinderRuntimeException("No User Language found!");
		}

		if (localeResolver == null) {
			throw new NGrinderRuntimeException("No LocaleResolver found!");
		}

		LocaleEditor localeEditor = new LocaleEditor();
		localeEditor.setAsText(lan);
		localeResolver.setLocale(request, response, (Locale) localeEditor.getValue());
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

	@ResponseBody
	@RequestMapping(value = "/changeTimeZone")
	public String changeTimeZone(String timeZone) {
		setTimeZone(timeZone);
		return JSONUtil.returnSuccess();
	}

	@RequestMapping(value = "/allTimeZone")
	public String getAllTimeZone(ModelMap model) {
		model.addAttribute("timeZones", DateUtil.getFilteredTimeZoneMap());
		return "allTimeZone";
	}

	@RequestMapping("/profile")
	public String userProfile(ModelMap model, User user) {
		model.addAttribute("user", user);
		model.addAttribute("action", "profile");
		return "user/userInfo";
	}

	// user save/modification is in UserController
	// @RequestMapping("/profile/save")
	// public String saveOrUpdateUserDetail(User user, ModelMap model, @ModelAttribute("user") User
	// updatedUser) {
	// if (updatedUser.exist()) {
	// getUserService().modifyUser(updatedUser);
	// }
	// return "/";
	// }
}
