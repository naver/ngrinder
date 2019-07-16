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
package org.ngrinder.common.controller;

import org.ngrinder.common.constant.ControllerConstants;
import org.ngrinder.common.constant.WebConstants;
import org.ngrinder.common.exception.NGrinderRuntimeException;
import org.ngrinder.infra.config.Config;
import org.ngrinder.model.User;
import org.ngrinder.operation.service.AnnouncementService;
import org.ngrinder.region.service.RegionService;
import org.ngrinder.user.service.UserContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.servlet.ModelAndView;

import javax.annotation.PostConstruct;
import java.util.*;

import static com.google.common.collect.ImmutableMap.of;
import static org.ngrinder.common.util.NoOp.noOp;

/**
 * Controller base containing widely used methods.
 *
 * @author JunHo Yoon
 * @since 3.0
 */
public class BaseController implements WebConstants {
	private static Map<String, Object> successJson;
	private static Map<String, Object> errorJson;

	@Autowired
	private MessageSource messageSource;

	@Autowired
	private UserContext userContext;

	public Config getConfig() {
		return config;
	}

	public void setConfig(Config config) {
		this.config = config;
	}

	@Autowired
	private Config config;

	@Autowired
	private AnnouncementService announcementService;

	@Autowired
	private RegionService regionService;

	@PostConstruct
	void initJSON() {
		successJson = of(JSON_SUCCESS, true);
		errorJson = of(JSON_SUCCESS, false);
	}

	/**
	 * Get the current user.
	 *
	 * @return current user
	 */
	public User getCurrentUser() {
		return userContext.getCurrentUser();
	}

	protected void putPageIntoModelMap(Map<String, Object> result, Pageable pageable) {
		Map<String, Object> page = new HashMap<>();
		page.put("pageNumber", pageable.getPageNumber());
		page.put("pageSize", pageable.getPageSize());
		result.put("page", page);
		final Iterator<Sort.Order> iterator = pageable.getSort().iterator();
		if (iterator.hasNext()) {
			Sort.Order sortProp = iterator.next();
			result.put("sort", sortProp.getProperty() + "," + sortProp.getDirection());
		}
	}

	/**
	 * Provide the current login user as a model attribute. If it's not found, return empty user.
	 *
	 * @return login user
	 */
	@ModelAttribute("currentUser")
	public User currentUser() {
		try {
			return getCurrentUser();
		} catch (AuthenticationCredentialsNotFoundException e) {
			// Fall through
			noOp();
		}
		return new User();
	}

	/**
	 * Provide the announcement content as a model attribute.
	 *
	 * @return announcement content
	 */
	@ModelAttribute("announcement")
	public String announcement() {
		return announcementService.getOne();
	}

	@ModelAttribute("enableScriptConsole")
	public boolean isEnableScriptConsole() {
		return config.getControllerProperties().getPropertyBoolean(ControllerConstants
				.PROP_CONTROLLER_ENABLE_SCRIPT_CONSOLE);
	}

	/**
	 * Provide the announcement content as a model attribute.
	 *
	 * @return announcement content
	 */
	@ModelAttribute("announcement_new")
	public boolean announcementNew() {
		return announcementService.isNew();
	}

	/**
	 * Provide the boolean value representing that it's clustered or not as a model attributes.
	 *
	 * @return clustered mark
	 */
	@ModelAttribute("clustered")
	public boolean isClustered() {
		return config.isClustered();
	}

	/**
	 * Provide the help URL as a model attribute.
	 *
	 * @return help URL
	 */
	@ModelAttribute("helpUrl")
	public String helpUrl() {
		return config.getHelpUrl();
	}


	@ModelAttribute("visibleRegions")
	public List<String> availRegions() {
		return regionService.getAllVisibleRegionNames();
	}

	/**
	 * Provide the announcement hide cookie value as a model attribute.
	 *
	 * @param announcementHide true if hidden.
	 * @return announcement content
	 */
	@ModelAttribute("announcement_hide")
	public boolean announcement(
			@CookieValue(value = "announcement_hide", defaultValue = "false") boolean announcementHide) {
		return announcementHide;
	}

	/**
	 * Get the message from messageSource by the given key.
	 *
	 * @param key key of message
	 * @return the found message. If not found, the error message will return.
	 */
	protected String getMessages(String key) {
		String userLanguage = "en";
		try {
			userLanguage = getCurrentUser().getUserLanguage();
		} catch (Exception e) {
			noOp();
		}
		Locale locale = new Locale(userLanguage);
		return messageSource.getMessage(key, null, locale);
	}

	/**
	 * Return success json
	 *
	 * @return Map containing the json message
	 */
	public Map<String, Object> returnSuccess() {
		return successJson;
	}

	/**
	 * Return error json
	 *
	 * @return Map containing the json message
	 */
	public Map<String, Object> returnError() {
		return errorJson;
	}

	/**
	 * Exception handler to forward to front page showing the error message box.
	 *
	 * @param e occurred exception
	 * @return modal and view having the exception message
	 */
	@ExceptionHandler({NGrinderRuntimeException.class})
	public ModelAndView handleException(NGrinderRuntimeException e) {
		//noinspection SpringMVCViewInspection
		ModelAndView modelAndView = new ModelAndView("forward:/");
		modelAndView.addObject("exception", e.getMessage());
		return modelAndView;
	}

}
