package org.ngrinder.home.controller;

import org.ngrinder.common.constant.ControllerConstants;
import org.ngrinder.common.controller.BaseController;
import org.ngrinder.common.controller.RestAPI;
import org.ngrinder.home.model.PanelEntry;
import org.ngrinder.home.service.HomeService;
import org.ngrinder.script.handler.ScriptHandler;
import org.ngrinder.script.handler.ScriptHandlerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.PostConstruct;
import java.util.*;

import static java.util.Comparator.comparing;
import static org.ngrinder.common.constant.ControllerConstants.*;
import static org.ngrinder.common.util.CollectionUtils.buildMap;

/**
 * Home index page api controller.
 *
 * @since 3.5.0
 */
@RestController
@RequestMapping("/home/api")
public class HomeApiController extends BaseController {

	@Autowired
	private HomeService homeService;

	@Autowired
	private ScriptHandlerFactory scriptHandlerFactory;

	private List<TimeZone> timeZones = null;

	private static final String TIMEZONE_ID_PREFIXES = "^(Africa|America|Asia|Atlantic|Australia|Europe|Indian|Pacific)/.*";

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


	@RestAPI
	@GetMapping("/handlers")
	public List<ScriptHandler> getHandlers() {
		return scriptHandlerFactory.getVisibleHandlers();
	}

	@RestAPI
	@GetMapping("/panel")
	public Map<String, Object> getPanelEntries() {
		return buildMap("leftPanelEntries", getLeftPanelEntries(), "rightPanelEntries", getRightPanelEntries());
	}

	@RestAPI
	@GetMapping("/timezones")
	public List<TimeZone> getTimezones() {
		return timeZones;
	}

	@RestAPI
	@GetMapping("/config")
	public Map<String, Object> getCommonHomeConfig() {
		return buildMap(
			"askQuestionUrl", getConfig().getControllerProperties().getProperty(PROP_CONTROLLER_FRONT_PAGE_ASK_QUESTION_URL,
				getMessages(PROP_CONTROLLER_FRONT_PAGE_ASK_QUESTION_URL)),
			"seeMoreQuestionUrl", getConfig().getControllerProperties().getProperty(PROP_CONTROLLER_FRONT_PAGE_QNA_MORE_URL,
				getMessages(PROP_CONTROLLER_FRONT_PAGE_QNA_MORE_URL)),
			"seeMoreResourcesUrl", getConfig().getControllerProperties().getProperty(PROP_CONTROLLER_FRONT_PAGE_RESOURCES_MORE_URL),
			"userLanguage", getConfig().getControllerProperties().getProperty(ControllerConstants.PROP_CONTROLLER_DEFAULT_LANG),
			"signUpEnabled", getConfig().isSignUpEnabled());
	}

	private List<PanelEntry> getRightPanelEntries() {
		if (getConfig().getControllerProperties().getPropertyBoolean(PROP_CONTROLLER_FRONT_PAGE_ENABLED)) {
			// Get nGrinder Resource RSS
			String rightPanelRssURL = getConfig().getControllerProperties().getProperty(PROP_CONTROLLER_FRONT_PAGE_RESOURCES_RSS);
			return homeService.getRightPanelEntries(rightPanelRssURL);
		}
		return Collections.emptyList();
	}

	private List<PanelEntry> getLeftPanelEntries() {
		if (getConfig().getControllerProperties().getPropertyBoolean(PROP_CONTROLLER_FRONT_PAGE_ENABLED)) {
			// Make the i18n applied QnA panel. Depending on the user language, show the different QnA panel.
			String leftPanelRssURLKey = getMessages(PROP_CONTROLLER_FRONT_PAGE_QNA_RSS);
			// Make admin configure the QnA panel.
			String leftPanelRssURL = getConfig().getControllerProperties().getProperty(PROP_CONTROLLER_FRONT_PAGE_QNA_RSS,
				leftPanelRssURLKey);
			return homeService.getLeftPanelEntries(leftPanelRssURL);
		}
		return Collections.emptyList();
	}
}
