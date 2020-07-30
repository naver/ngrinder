package org.ngrinder.home.controller;

import lombok.RequiredArgsConstructor;
import org.ngrinder.common.constant.ControllerConstants;
import org.ngrinder.home.model.PanelEntry;
import org.ngrinder.home.service.HomeService;
import org.ngrinder.infra.config.Config;
import org.ngrinder.script.handler.ScriptHandler;
import org.ngrinder.script.handler.ScriptHandlerFactory;
import org.ngrinder.user.service.UserContext;
import org.springframework.context.MessageSource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.PostConstruct;
import java.util.*;

import static java.util.Comparator.comparing;
import static org.ngrinder.common.constant.ControllerConstants.*;
import static org.ngrinder.common.util.CollectionUtils.buildMap;
import static org.ngrinder.common.util.NoOp.noOp;

/**
 * Home index page api controller.
 *
 * @since 3.5.0
 */
@RestController
@RequestMapping("/home/api")
@RequiredArgsConstructor
public class HomeApiController {

	private static final String TIMEZONE_ID_PREFIXES = "^(Africa|America|Asia|Atlantic|Australia|Europe|Indian|Pacific)/.*";

	private final HomeService homeService;

	private final ScriptHandlerFactory scriptHandlerFactory;

	private final UserContext userContext;

	private final Config config;

	private final MessageSource messageSource;

	private List<TimeZone> timeZones = null;

	@PostConstruct
	public void init() {
		timeZones = new ArrayList<>();
		final String[] timeZoneIds = TimeZone.getAvailableIDs();

		for (final String id : timeZoneIds) {
			if (id.matches(TIMEZONE_ID_PREFIXES) && !TimeZone.getTimeZone(id).getDisplayName().contains("GMT")) {
				timeZones.add(TimeZone.getTimeZone(id));
			}
		}

		timeZones.sort(comparing(TimeZone::getID));
	}


	@GetMapping("/handlers")
	public List<ScriptHandler> getHandlers() {
		return scriptHandlerFactory.getVisibleHandlers();
	}

	@GetMapping("/panel")
	public Map<String, Object> getPanelEntries() {
		return buildMap("leftPanelEntries", getLeftPanelEntries(), "rightPanelEntries", getRightPanelEntries());
	}

	@GetMapping("/timezones")
	public List<TimeZone> getTimezones() {
		return timeZones;
	}

	@GetMapping("/config")
	public Map<String, Object> getCommonHomeConfig() {
		return buildMap(
			"askQuestionUrl", config.getControllerProperties().getProperty(PROP_CONTROLLER_FRONT_PAGE_ASK_QUESTION_URL,
				getMessages(PROP_CONTROLLER_FRONT_PAGE_ASK_QUESTION_URL)),
			"seeMoreQuestionUrl", config.getControllerProperties().getProperty(PROP_CONTROLLER_FRONT_PAGE_QNA_MORE_URL,
				getMessages(PROP_CONTROLLER_FRONT_PAGE_QNA_MORE_URL)),
			"seeMoreResourcesUrl", config.getControllerProperties().getProperty(PROP_CONTROLLER_FRONT_PAGE_RESOURCES_MORE_URL),
			"userLanguage", config.getControllerProperties().getProperty(ControllerConstants.PROP_CONTROLLER_DEFAULT_LANG));
	}

	private List<PanelEntry> getRightPanelEntries() {
		if (config.getControllerProperties().getPropertyBoolean(PROP_CONTROLLER_FRONT_PAGE_ENABLED)) {
			// Get nGrinder Resource RSS
			String rightPanelRssURL = config.getControllerProperties().getProperty(PROP_CONTROLLER_FRONT_PAGE_RESOURCES_RSS);
			return homeService.getRightPanelEntries(rightPanelRssURL);
		}
		return Collections.emptyList();
	}

	private List<PanelEntry> getLeftPanelEntries() {
		if (config.getControllerProperties().getPropertyBoolean(PROP_CONTROLLER_FRONT_PAGE_ENABLED)) {
			// Make the i18n applied QnA panel. Depending on the user language, show the different QnA panel.
			String leftPanelRssURLKey = getMessages(PROP_CONTROLLER_FRONT_PAGE_QNA_RSS);
			// Make admin configure the QnA panel.
			String leftPanelRssURL = config.getControllerProperties().getProperty(PROP_CONTROLLER_FRONT_PAGE_QNA_RSS,
				leftPanelRssURLKey);
			return homeService.getLeftPanelEntries(leftPanelRssURL);
		}
		return Collections.emptyList();
	}

	@GetMapping("/messagesources/{locale}")
	public Map<String, String> getUserDefinedMessageSources(@PathVariable String locale) {
		return homeService.getUserDefinedMessageSources(locale);
	}

	/**
	 * Get the message from messageSource by the given key.
	 *
	 * @param key key of message
	 * @return the found message. If not found, the error message will return.
	 */
	private String getMessages(String key) {
		String userLanguage = "en";
		try {
			userLanguage = userContext.getCurrentUser().getUserLanguage();
		} catch (Exception e) {
			noOp();
		}
		Locale locale = new Locale(userLanguage);
		return messageSource.getMessage(key, null, locale);
	}
}
