package org.ngrinder.operation.cotroller;

import org.ngrinder.common.controller.NGrinderBaseController;
import org.ngrinder.operation.service.AnnouncementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Announcement controller.
 * 
 * @author Alex Qin
 * @since 3.1
 */
@Controller
@RequestMapping("/operation/announcement")
@PreAuthorize("hasAnyRole('A', 'S')")
public class AnnouncementController extends NGrinderBaseController {

	@Autowired
	private AnnouncementService announcementService;

	/**
	 * open announcement editor.
	 * 
	 * @param model
	 *            model.
	 * @return operation/announcement
	 */
	@RequestMapping("")
	public String openAnnouncement(Model model) {
		String announcement = announcementService.getAnnouncement();
		model.addAttribute("announcement", announcement);
		model.addAttribute("content", announcement);
		return "operation/announcement";
	}

	/**
	 * Save announcement.
	 * 
	 * @param model
	 *            model.
	 * @param content
	 *            file content.
	 * @return operation/announcement
	 */
	@RequestMapping("/save")
	public String saveAnnouncement(Model model, @RequestParam final String content) {
		model.addAttribute("success", announcementService.saveAnnouncement(content));
		return openAnnouncement(model);
	}
}
