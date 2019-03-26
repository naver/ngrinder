package org.ngrinder.operation.cotroller;

import org.ngrinder.common.controller.RestAPI;
import org.ngrinder.operation.service.AnnouncementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/announcement/api")
public class AnnouncementApiController {

	@Autowired
	private AnnouncementService announcementService;

	@RestAPI
	@GetMapping
	public String getAnnouncement() {
		return announcementService.getOne();
	}
}
