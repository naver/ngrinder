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

import static com.google.common.collect.ImmutableMap.of;

import org.ngrinder.common.controller.BaseController;
import org.ngrinder.operation.service.AnnouncementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Announcement message management controller.
 *
 * @author Alex Qin
 * @since 3.1
 */
@Controller
@RequestMapping("/operation/announcement")
@PreAuthorize("hasAnyRole('A', 'S')")
public class AnnouncementController extends BaseController {

	@Autowired
	private AnnouncementService announcementService;

	/**
	 * Open the announcement editor.
	 *
	 * @return app
	 */
	@GetMapping("")
	public String open() {
		return "app";
	}

	/**
	 * Save the announcement.
	 *
	 * @param content new announcement content
	 * @return save result
	 */
	@ResponseBody
	@PostMapping("/api")
	public Map<String, Boolean> save(@RequestParam String content) {
		return of("success", announcementService.save(content));
	}
}
