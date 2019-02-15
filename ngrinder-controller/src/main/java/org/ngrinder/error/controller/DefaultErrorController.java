package org.ngrinder.error.controller;

import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;

import static javax.servlet.RequestDispatcher.ERROR_STATUS_CODE;
import static javax.servlet.http.HttpServletResponse.SC_NOT_FOUND;

@Controller
public class DefaultErrorController implements ErrorController {

	private static final String ERROR_HANDLING_PATH = "/error";

	@RequestMapping(ERROR_HANDLING_PATH)
	public String handleError(HttpServletRequest request) {
		if (request.getAttribute(ERROR_STATUS_CODE).equals(SC_NOT_FOUND)) {
			return "redirect:/doError?type=404";
		}
		return "redirect:/doError";
	}

	@Override
	public String getErrorPath() {
		return ERROR_HANDLING_PATH;
	}
}
