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
package org.ngrinder.script.controller;

import org.ngrinder.common.controller.annotation.GlobalControllerModel;
import org.ngrinder.model.User;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import lombok.RequiredArgsConstructor;

/**
 * FileEntry manipulation controller.
 *
 * @since 3.0
 */
@SuppressWarnings({"SpringMVCViewInspection", "unused"})
@Controller
@RequestMapping("/script")
@GlobalControllerModel
@RequiredArgsConstructor
public class FileEntryController {

	@GetMapping({"/list/**", ""})
	public String getAll(User user) {
		return "app";
	}

	@GetMapping({"/editor", "/new"})
	public String editor(User user) {
		return "app";
	}

	@GetMapping("/search/**")
	public String search(User user) {
		return "app";
	}

	@GetMapping("/detail/**")
	public String getOne(User user) {
		return "app";
	}

}
