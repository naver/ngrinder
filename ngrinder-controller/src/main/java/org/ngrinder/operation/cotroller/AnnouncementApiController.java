package org.ngrinder.operation.cotroller;

import org.ngrinder.operation.service.AnnouncementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/operation/announcement/api")
public class AnnouncementApiController {

	@Autowired
	private AnnouncementService announcementService;

	@GetMapping("")
	public String getAnnouncement() {
		return announcementService.getOne();
	}

	@PostMapping("")
	@PreAuthorize("hasAnyRole('A', 'S')")
	public void save(@RequestParam String content) {
		announcementService.save(content);
	}
}
