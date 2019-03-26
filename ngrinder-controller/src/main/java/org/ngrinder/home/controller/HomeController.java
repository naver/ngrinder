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

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.apache.commons.lang.StringUtils;
import org.ngrinder.common.constant.ControllerConstants;
import org.ngrinder.common.controller.BaseController;
import org.ngrinder.common.util.ThreadUtils;
import org.ngrinder.infra.logger.CoreLogger;
import org.ngrinder.model.Role;
import org.ngrinder.model.User;
import org.ngrinder.region.service.RegionService;
import org.ngrinder.script.handler.ScriptHandlerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.propertyeditors.LocaleEditor;
import org.springframework.http.HttpEntity;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.support.RequestContextUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import static org.ngrinder.common.util.ExceptionUtils.processException;
import static org.ngrinder.common.util.Preconditions.checkNotNull;

/**
 * Home index page controller.
 *
 * @author JunHo Yoon
 * @since 3.0
 */
@Controller
public class HomeController extends BaseController implements ControllerConstants {

	private static final Logger LOG = LoggerFactory.getLogger(HomeController.class);

	@Autowired
	private RegionService regionService;

	@Autowired
	private ScriptHandlerFactory scriptHandlerFactory;

	private static Gson rawObjectJsonSerializer = new GsonBuilder().setPrettyPrinting().create();

	/**
	 * Return nGrinder index page.
	 *
	 * @param user      user
	 * @param exception exception if it's redirected from exception handler
	 * @param region    region. where this access comes from. it's optional
	 * @param model     model
	 * @param response  response
	 * @param request   request
	 * @return "index" if already logged in. Otherwise "login".
	 */
	@RequestMapping(value = {"/home", "/"})
	public String home(User user, @RequestParam(value = "exception", defaultValue = "") String exception,
					   @RequestParam(value = "region", defaultValue = "") String region, ModelMap model,
					   HttpServletResponse response, HttpServletRequest request) {
		try {
			Role role;
			try {
				recordReferrer(region);
				// set local language
				setLanguage(getCurrentUser().getUserLanguage(), response, request);
				role = user.getRole();
			} catch (AuthenticationCredentialsNotFoundException e) {
				return "app";
			}
			model.addAttribute("handlers", scriptHandlerFactory.getVisibleHandlers());

			if (StringUtils.isNotBlank(exception)) {
				model.addAttribute("exception", exception);
			}
			if (role == Role.ADMIN || role == Role.SUPER_USER || role == Role.USER) {
				return "app";
			} else {
				LOG.info("Invalid user role:{}", role.getFullName());
				return "app";
			}
		} catch (Exception e) {
			// Make the home reliable...
			model.addAttribute("exception", e.getMessage());
			return "app";
		}
	}

	private void recordReferrer(String region) {
		if (StringUtils.isNotEmpty(region)) {
			CoreLogger.LOGGER.info("Accessed to {}", region);
		}
	}

	/**
	 * Return the health check message. If there is shutdown lock, it returns
	 * 503. Otherwise it returns region lists.
	 *
	 * @param response response
	 * @return region list
	 */
	@RequestMapping("/check/healthcheck")
	public HttpEntity<String> healthCheck(HttpServletResponse response) {
		if (getConfig().hasShutdownLock()) {
			try {
				response.sendError(503, "nGrinder is about to down");
			} catch (IOException e) {
				LOG.error("While running healthCheck() in HomeController, the error occurs.");
				LOG.error("Details : ", e);
			}
		}
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("current", regionService.getCurrent());
		map.put("regions", regionService.getAll());
		return toJsonHttpEntity(map, rawObjectJsonSerializer);
	}

	/**
	 * Return health check message with 1 sec delay. If there is shutdown lock,
	 * it returns 503. Otherwise, it returns region lists.
	 *
	 * @param sleep    in milliseconds.
	 * @param response response
	 * @return region list
	 */
	@ResponseBody
	@RequestMapping("/check/healthcheck_slow")
	public HttpEntity<String> healthCheckSlowly(@RequestParam(value = "delay", defaultValue = "1000") int sleep,
												HttpServletResponse response) {
		ThreadUtils.sleep(sleep);
		return healthCheck(response);
	}

	private void setLanguage(String lan, HttpServletResponse response, HttpServletRequest request) {
		LocaleResolver localeResolver = checkNotNull(RequestContextUtils.getLocaleResolver(request),
				"No LocaleResolver found!");
		LocaleEditor localeEditor = new LocaleEditor();
		String language = StringUtils.defaultIfBlank(lan,
				getConfig().getControllerProperties().getProperty(PROP_CONTROLLER_DEFAULT_LANG));
		localeEditor.setAsText(language);
		localeResolver.setLocale(request, response, (Locale) localeEditor.getValue());
	}

	/**
	 * Return the login page.
	 *
	 * @param model model
	 * @return "login" if not logged in. Otherwise, "/"
	 */
	@RequestMapping(value = "/login")
	public String login(ModelMap model) {
		try {
			getCurrentUser();
		} catch (Exception e) {
			CoreLogger.LOGGER.info("Login Failure " + e.getMessage());
			return "app";
		}
		model.clear();
		return "app";
	}

	/**
	 * Error redirection to 404.
	 *
	 * @return "redirect:/doError"
	 */
	@RequestMapping(value = "/error_404")
	public String error404(ModelMap model) {
		model.clear();
		return "redirect:/doError?type=404";
	}

	/**
	 * Error redirection as a second phase.
	 *
	 * @param user     user
	 * @param model    model
	 * @param response response
	 * @param request  request
	 * @return "index"
	 */
	@RequestMapping(value = "/doError")
	public String second(User user, ModelMap model, HttpServletResponse response, HttpServletRequest request) {
		String parameter = request.getParameter("type");
		if ("404".equals(parameter)) {
			model.addAttribute("exception", processException("Requested URL does not exist"));
		}
		return home(user, null, null, model, response, request);
	}
}
