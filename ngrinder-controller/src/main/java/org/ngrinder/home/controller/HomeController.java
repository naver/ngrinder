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
package org.ngrinder.home.controller;

import static org.ngrinder.common.util.Preconditions.checkNotNull;

import java.io.IOException;
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
import org.ngrinder.common.util.ThreadUtil;
import org.ngrinder.home.service.HomeService;
import org.ngrinder.infra.config.Config;
import org.ngrinder.infra.logger.CoreLogger;
import org.ngrinder.model.Role;
import org.ngrinder.model.User;
import org.ngrinder.region.service.RegionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.propertyeditors.LocaleEditor;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
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
	private RegionService regionService;

	
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
	 * Provide nGrinder home.
	 * 
	 * @param user
	 *            user
	 * @param exception
	 *            exception if it's redirection from exception handling
	 * @param model
	 *            model
	 * @param response
	 *            response
	 * @param request
	 *            request
	 * @return index if loggined. login if not.
	 */
	@RequestMapping(value = { "/home", "/" })
	public String home(User user, @RequestParam(value = "exception", defaultValue = "") String exception,
					ModelMap model, HttpServletResponse response, HttpServletRequest request) {
		try {
			Role role = null;
			try {
				// set local language
				setLanguage(getCurrentUser().getUserLanguage(), response, request);
				setLoginPageDate(model);
				role = user.getRole();
			} catch (AuthenticationCredentialsNotFoundException e) {
				CoreLogger.LOGGER.info("Login Failure", e);
				return "login";
			}
			model.addAttribute("right_panel_entries", homeService.getRightPanelEntries());
			model.addAttribute("left_panel_entries", homeService.getLeftPanelEntries());
			if (StringUtils.isNotBlank(exception)) {
				model.addAttribute("exception", exception);
			}
			if (role == Role.ADMIN || role == Role.SUPER_USER || role == Role.USER) {
				return "index";
			} else {
				LOG.info("Invalid user role:{}", role.getFullName());
				return "login";
			}
		} catch (Exception e) {
			// Make the home reliable...
			model.addAttribute("exception", e.getMessage());
			return "index";
		}
	}

	/**
	 * Return health check message. If there are shutdown lock, it returns 503, otherwise return
	 * region lists.
	 * 
	 * @param response
	 *            response
	 * @return region list
	 */
	@ResponseBody
	@RequestMapping("/check/healthcheck")
	public String healthcheck(HttpServletResponse response) {
		if (config.hasShutdownLock()) {
			try {
				response.sendError(503, "the ngrinder is about to down");
			} catch (IOException e) {
				LOG.error("While running healthcheck() in HomeController, the error occurs.");
				LOG.error("Details : ", e);
			}
		}
		StringBuilder builder = new StringBuilder();
		builder.append(regionService.getCurrentRegion()).append("\n");
		return gson.toJson(regionService.getRegions());
	}

	/**
	 * Return health check message with 1 sec delay. If there are shutdown lock, it returns 503,
	 * otherwise return region lists.
	 * 
	 * @param sleep
	 *            in milliseconds.
	 * @param response
	 *            response
	 * @return region list
	 */
	@ResponseBody
	@RequestMapping("/check/healthcheck_slow")
	public String healthcheckSlowly(@RequestParam(value = "delay", defaultValue = "1000") int sleep,
					HttpServletResponse response) {
		ThreadUtil.sleep(sleep);
		if (config.hasShutdownLock()) {
			try {
				response.sendError(503, "the ngrinder is about to down");
			} catch (IOException e) {
				LOG.error("While running healthcheck() in HomeController, the error occurs.");
				LOG.error("Details : ", e);
			}
		}
		return regionService.getCurrentRegion() + ":" + StringUtils.join(regionService.getRegions().keySet(), "|");

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
		model.addFlashAttribute("exception", new NGrinderRuntimeException("Requested URL does not exist"));
		return "redirect:/doError";
	}

	/**
	 * Error redirection for second phase.
	 * 
	 * @param user
	 *            user
	 * @param model
	 *            model
	 * @param response
	 *            response
	 * @param request
	 *            request
	 * @return "index"
	 */
	@RequestMapping(value = "/doError")
	public String second(User user, ModelMap model, HttpServletResponse response, HttpServletRequest request) {
		return home(user, null, model, response, request);
	}

}
