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

import org.ngrinder.common.controller.BaseController;
import org.ngrinder.operation.service.AnnouncementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

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
	 * @param model
	 *            model
	 * @return operation/announcement
	 */
	@RequestMapping(value = "", method = RequestMethod.GET)
	public String open(Model model) {
		String announcement = announcementService.getOne();
		model.addAttribute("announcement", announcement);
		model.addAttribute("content", announcement);
		return "operation/announcement";
	}

	/**
	 * Save the announcement.
	 * 
	 * @param model
	 *            model
	 * @param content
	 *            new announcement content
	 * @return operation/announcement
	 */
	@RequestMapping(value = "", method = RequestMethod.POST)
	public String save(Model model, @RequestParam final String content) {
		model.addAttribute("success", announcementService.save(content));
		String announcement = announcementService.getOne();
		model.addAttribute("announcement", announcement);
		model.addAttribute("content", announcement);
		return "operation/announcement";
	}
}
