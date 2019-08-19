package org.ngrinder.operation.cotroller;

import org.ngrinder.operation.service.AnnouncementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/announcement/api")
public class AnnouncementApiController {

	@Autowired
	private AnnouncementService announcementService;

	@GetMapping("")
	public String getAnnouncement() {
		return announcementService.getOne();
	}
}
