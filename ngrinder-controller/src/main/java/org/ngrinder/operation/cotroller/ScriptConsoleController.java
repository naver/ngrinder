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

import org.ngrinder.common.controller.annotation.GlobalControllerModel;
import org.springframework.context.annotation.Profile;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Profile("production")
@Controller
@RequestMapping("/operation/script_console")
@GlobalControllerModel
@PreAuthorize("hasAnyRole('A')")
public class ScriptConsoleController {

	@SuppressWarnings("SpringMVCViewInspection")
	@GetMapping({"", "/"})
	public String scriptConsole() {
		return "app";
	}

}
