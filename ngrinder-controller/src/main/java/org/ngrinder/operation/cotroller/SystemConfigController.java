package org.ngrinder.operation.cotroller;

import org.ngrinder.common.controller.NGrinderBaseController;
import org.ngrinder.infra.annotation.RuntimeOnlyController;
import org.ngrinder.model.PerfTest;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@RuntimeOnlyController
@RequestMapping("/operation/systemConfig")
@PreAuthorize("hasAnyRole('A', 'S')")
public class SystemConfigController extends NGrinderBaseController {
	
	@RequestMapping("")
	public String saveTestOption(@RequestParam(value = "option", required = false) PerfTest test, Model model) {
		return "operation/systemConfig";
	}
}
