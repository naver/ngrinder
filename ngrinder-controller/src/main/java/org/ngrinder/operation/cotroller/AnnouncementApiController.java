package org.ngrinder.operation.cotroller;

import lombok.RequiredArgsConstructor;

import org.ngrinder.operation.service.AnnouncementService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/operation/announcement/api")
@RequiredArgsConstructor
public class AnnouncementApiController {

	private final AnnouncementService announcementService;

	@GetMapping("")
	public String getAnnouncement() {
		return announcementService.getOne();
	}

	@PostMapping("")
	@PreAuthorize("hasAnyRole('A', 'S')")
	public void save(@RequestBody String content) {
		announcementService.save(content);
	}
}
