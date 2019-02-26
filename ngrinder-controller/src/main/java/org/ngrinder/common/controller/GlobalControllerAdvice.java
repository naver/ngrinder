package org.ngrinder.common.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
public class GlobalControllerAdvice {

	@Value("${ngrinder.version}")
	private String nGrinderVersion;

    @ModelAttribute
    public void globalAttributes(Model model) {
        model.addAttribute("nGrinderVersion", nGrinderVersion);
    }
}
