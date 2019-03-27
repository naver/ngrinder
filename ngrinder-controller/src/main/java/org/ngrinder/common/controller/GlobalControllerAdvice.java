package org.ngrinder.common.controller;

import org.ngrinder.infra.config.Config;
import org.ngrinder.user.service.UserContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import static org.ngrinder.common.util.NoOp.noOp;

@ControllerAdvice
public class GlobalControllerAdvice {

	@Value("${ngrinder.version}")
	private String version;

	@Autowired
	private UserContext userContext;

	@Autowired
	private Config config;

	@ModelAttribute
    public void globalAttributes(Model model) {
		model.addAttribute("version", version);
		model.addAttribute("clustered", config.isClustered());
		model.addAttribute("helpUrl", config.getHelpUrl());
		try {
			model.addAttribute("currentUser", userContext.getCurrentUser());
		} catch (Exception e) {
			noOp();
		}
	}
}
