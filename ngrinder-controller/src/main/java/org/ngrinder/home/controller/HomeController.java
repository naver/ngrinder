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

import static org.ngrinder.common.util.Preconditions.checkNotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.ngrinder.common.controller.NGrinderBaseController;
import org.ngrinder.common.exception.NGrinderRuntimeException;
import org.ngrinder.common.util.DateUtil;
import org.ngrinder.home.service.HomeService;
import org.ngrinder.infra.config.Config;
import org.ngrinder.infra.logger.CoreLogger;
import org.ngrinder.model.Role;
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
import org.springframework.web.servlet.mvc.support.RedirectAttributesModelMap;
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
	private HomeService homeService;

	@Autowired
	private Config config;

	private static final String TIMEZONE_ID_PREFIXES = "^(Africa|America|Asia|Atlantic|"
					+ "Australia|Europe|Indian|Pacific)/.*";

	private List<TimeZone> timeZones = null;

	/**
	 * Initialize {@link HomeController}.
	 * <ul>
	 * <li>Get all timezones.</li>
	 * </ul>
	 */
	@PostConstruct
	public void init() {
		timeZones = new ArrayList<TimeZone>();
		final String[] timeZoneIds = TimeZone.getAvailableIDs();
		for (final String id : timeZoneIds) {
			if (id.matches(TIMEZONE_ID_PREFIXES) && !TimeZone.getTimeZone(id).getDisplayName().contains("GMT")) {
				timeZones.add(TimeZone.getTimeZone(id));
			}
		}
		Collections.sort(timeZones, new Comparator<TimeZone>() {
			public int compare(final TimeZone a, final TimeZone b) {
				return a.getID().compareTo(b.getID());
			}
		});
	}

	/**
	 * Home.
	 * 
	 * @param user
	 *            user
	 * @param model
	 *            model
	 * @param response
	 *            {@link HttpServletResponse}
	 * @param request
	 *            {@link HttpServletRequest}
	 * @return "index" if loggined. "login", otherwise.
	 */
	@RequestMapping(value = { "/home", "/" })
	public String home(User user, ModelMap model, HttpServletResponse response, HttpServletRequest request) {
		Role role = null;
		try {
			// set local language
			setLanguage(user.getUserLanguage(), response, request);
			setLoginPageDate(model);
			role = user.getRole();
		} catch (AuthenticationCredentialsNotFoundException e) {
			CoreLogger.LOGGER.info("Login Failure", e);
			return "login";
		}
		model.addAttribute("right_panel_entries", homeService.getRightPanelEntries());
		model.addAttribute("left_panel_entries", homeService.getLeftPanelEntries());

		if (role == Role.ADMIN || role == Role.SUPER_USER || role == Role.USER) {
			return "index";
		} else {
			LOG.info("Invalid user role:{}", role.getFullName());
			return "login";
		}
	}

	private void setLanguage(String lan, HttpServletResponse response, HttpServletRequest request) {
		LocaleResolver localeResolver = checkNotNull(RequestContextUtils.getLocaleResolver(request),
						"No LocaleResolver found!");
		LocaleEditor localeEditor = new LocaleEditor();
		localeEditor.setAsText(StringUtils.defaultIfBlank(lan,
						config.getSystemProperties().getProperty(NGRINDER_PROP_DEFAULT_LANGUAGE, "en")));
		localeResolver.setLocale(request, response, (Locale) localeEditor.getValue());
	}

	/**
	 * Provide login page.
	 * 
	 * @param model
	 *            model
	 * @return "login" if not logged in. Otherwise, "/"
	 */
	@RequestMapping(value = "/login")
	public String login(ModelMap model) {
		setLoginPageDate(model);
		try {
			getCurrentUser();
		} catch (Exception e) {
			CoreLogger.LOGGER.info("Login Failure " + e.getMessage());
			return "login";
		}
		model.clear();
		return "redirect:/";
	}

	private void setLoginPageDate(ModelMap model) {
		TimeZone defaultTime = TimeZone.getDefault();
		model.addAttribute("timezones", timeZones);
		model.addAttribute("defaultTime", defaultTime.getID());
	}

	/**
	 * Change time zone.
	 * 
	 * @param user
	 *            user
	 * @param timeZone
	 *            time zone
	 * @return success json message
	 */
	@ResponseBody
	@RequestMapping(value = "/changeTimeZone")
	public String changeTimeZone(User user, String timeZone) {
		user.setTimeZone(timeZone);
		return returnSuccess();
	}

	/**
	 * Get all timezones.
	 * 
	 * @param model
	 *            model
	 * @return allTimeZone
	 */
	@RequestMapping(value = "/allTimeZone")
	public String getAllTimeZone(ModelMap model) {
		model.addAttribute("timeZones", DateUtil.getFilteredTimeZoneMap());
		return "allTimeZone";
	}

	/**
	 * Error redirection to 404.
	 * 
	 * @param model
	 *            model
	 * @return "redirect:/doError"
	 */
	@RequestMapping(value = "/error_404")
	public String error404(RedirectAttributesModelMap model) {
		model.addFlashAttribute("exception", new NGrinderRuntimeException("Requested URL does not exist !"));
		return "redirect:/doError";
	}

	/**
	 * Error redirection for second phase.
	 * 
	 * @param model
	 *            model
	 * @return "index"
	 */
	@RequestMapping(value = "/doError")
	public String second(ModelMap model) {
		return "index";
	}

}
