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
package org.ngrinder.operation.cotroller;

import static org.ngrinder.common.util.Preconditions.checkNotEmpty;

import org.ngrinder.common.controller.BaseController;
import org.ngrinder.common.controller.RestAPI;
import org.ngrinder.operation.service.SystemConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

/**
 * System configuration controller.
 *
 * @author Alex Qin
 * @since 3.1
 */
@Controller
@RequestMapping("/operation/system_config")
@PreAuthorize("hasAnyRole('A')")
public class SystemConfigController extends BaseController {

	@Autowired
	private SystemConfigService systemConfigService;

	/**
	 * Open the system configuration editor.
	 *
	 *  @return app
	 */
	@GetMapping("")
	public String systemConfig() {
		return "app";
	}

	/**
	 * Get the system configuration.
	 *
	 * @return system configuration
	 */
	@RestAPI
	@ResponseBody
	@GetMapping(value = "/api")
	public HttpEntity<String> getOne() {
		return toJsonHttpEntity(systemConfigService.getOne());
	}

	/**
	 * Save the system configuration.
	 *
	 * @param content system configuration content to be saved
	 * @return true if succeeded
	 */
	@RestAPI
	@ResponseBody
	@PostMapping(value = "/api")
	public HttpEntity<String> save(@RequestParam String content) {
		systemConfigService.save(checkNotEmpty(content, "content should be " +
				"passed as parameter"));
		return successJsonHttpEntity();
	}
}
