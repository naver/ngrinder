package org.ngrinder.operation.cotroller;

import lombok.AllArgsConstructor;
import org.ngrinder.operation.service.AnnouncementService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/operation/announcement/api")
@AllArgsConstructor
public class AnnouncementApiController {

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
