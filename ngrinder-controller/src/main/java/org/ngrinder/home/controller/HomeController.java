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

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.ngrinder.common.constant.ControllerConstants;
import org.ngrinder.common.controller.annotation.GlobalControllerModel;
import org.ngrinder.infra.config.Config;
import org.ngrinder.infra.logger.CoreLogger;
import org.ngrinder.model.Role;
import org.ngrinder.model.User;
import org.ngrinder.script.handler.ScriptHandlerFactory;
import org.ngrinder.user.service.UserContext;
import org.springframework.beans.propertyeditors.LocaleEditor;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.support.RequestContextUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Locale;

import static org.ngrinder.common.util.Preconditions.checkNotNull;

import lombok.RequiredArgsConstructor;

/**
 * Home index page controller.
 *
 * @since 3.0
 */
@SuppressWarnings("SpringMVCViewInspection")
@Slf4j
@Controller
@GlobalControllerModel
@RequiredArgsConstructor
public class HomeController implements ControllerConstants {

	private final ScriptHandlerFactory scriptHandlerFactory;

	private final UserContext userContext;

	private final Config config;

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
	@GetMapping({"/home", "/"})
	public String home(User user, @RequestParam(value = "exception", defaultValue = "") String exception,
					   @RequestParam(value = "region", defaultValue = "") String region, ModelMap model,
					   HttpServletResponse response, HttpServletRequest request) {
		try {
			Role role;
			try {
				recordReferrer(region);
				// set local language
				setLanguage(userContext.getCurrentUser().getUserLanguage(), response, request);
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
				log.info("Invalid user role:{}", role.getFullName());
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

	private void setLanguage(String lan, HttpServletResponse response, HttpServletRequest request) {
		LocaleResolver localeResolver = checkNotNull(RequestContextUtils.getLocaleResolver(request),
				"No LocaleResolver found!");
		LocaleEditor localeEditor = new LocaleEditor();
		String language = StringUtils.defaultIfBlank(lan,
				config.getControllerProperties().getProperty(PROP_CONTROLLER_DEFAULT_LANG));
		localeEditor.setAsText(language);
		localeResolver.setLocale(request, response, (Locale) localeEditor.getValue());
	}

	/**
	 * Return the login page.
	 *
	 * @param model model
	 * @return "login" if not logged in. Otherwise, "/"
	 */
	@GetMapping("/login")
	public String login(ModelMap model) {
		try {
			userContext.getCurrentUser();
		} catch (Exception e) {
			CoreLogger.LOGGER.info("Login Failure " + e.getMessage());
			return "app";
		}
		model.clear();
		return "app";
	}
}
