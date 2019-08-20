package org.ngrinder.operation.cotroller;

import org.ngrinder.common.controller.BaseController;
import org.ngrinder.operation.service.SystemConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import static org.ngrinder.common.util.Preconditions.checkNotEmpty;

@RestController
@RequestMapping("/operation/system_config/api")
@PreAuthorize("hasAnyRole('A')")
public class SystemConfigApiController extends BaseController {

	@Autowired
	private SystemConfigService systemConfigService;

	/**
	 * Get the system configuration.
	 *
	 * @return system configuration
	 */
	@GetMapping("")
	public String getOne() {
		return systemConfigService.getOne();
	}

	/**
	 * Save the system configuration.
	 *
	 * @param content system configuration content to be saved
	 */
	@PostMapping("")
	public void save(@RequestParam String content) {
		systemConfigService.save(checkNotEmpty(content, "content should be passed as parameter"));
	}
}
