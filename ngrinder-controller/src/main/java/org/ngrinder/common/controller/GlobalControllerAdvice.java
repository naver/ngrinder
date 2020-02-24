package org.ngrinder.common.controller;

import lombok.RequiredArgsConstructor;
import org.ngrinder.common.controller.annotation.GlobalControllerModel;
import org.ngrinder.infra.config.Config;
import org.ngrinder.operation.service.AnnouncementService;
import org.ngrinder.perftest.service.PerfTestService;
import org.ngrinder.user.service.UserContext;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import javax.servlet.ServletContext;

import static org.ngrinder.common.constant.ControllerConstants.PROP_CONTROLLER_GITHUB_BASE_URL;
import static org.ngrinder.common.constant.WebConstants.PARAM_PROCESS_THREAD_POLICY_SCRIPT;
import static org.ngrinder.common.util.NoOp.noOp;

/**
 * Includes common model attributes. These attributes uses in app.html
 *
 * @since 3.5.0
 */
@ControllerAdvice(annotations = GlobalControllerModel.class)
@RequiredArgsConstructor
public class GlobalControllerAdvice {

	@Value("${ngrinder.version}")
	private String version;

	private final UserContext userContext;

	private final AnnouncementService announcementService;

	private final PerfTestService perfTestService;

	private final ServletContext servletContext;

	private final Config config;

	@ModelAttribute
    public void globalAttributes(Model model) {
		model.addAttribute("version", version);
		model.addAttribute("contextPath", servletContext.getContextPath());
		model.addAttribute("clustered", config.isClustered());
		model.addAttribute("helpUrl", config.getHelpUrl());
		model.addAttribute("signUpEnabled", config.isSignUpEnabled());
		model.addAttribute("githubBaseUrl", config.getControllerProperties().getProperty(PROP_CONTROLLER_GITHUB_BASE_URL));
		model.addAttribute("hasNewAnnouncement", announcementService.isNew());
		model.addAttribute(PARAM_PROCESS_THREAD_POLICY_SCRIPT, perfTestService.getProcessAndThreadPolicyScript());

		try {
			model.addAttribute("currentUser", userContext.getCurrentUser());
		} catch (Exception e) {
			noOp();
		}
	}
}
