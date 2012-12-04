package org.ngrinder.operation;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;
import org.ngrinder.AbstractNGrinderTransactionalTest;
import org.ngrinder.infra.config.Config;
import org.ngrinder.operation.cotroller.AnnouncementController;
import org.ngrinder.operation.service.AnnouncementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.ui.Model;

public class AnnouncementControllerTest extends AbstractNGrinderTransactionalTest {

	@Autowired
	private AnnouncementController controller;
	
	@Autowired
	private AnnouncementService service;
	
	@Autowired
	private Config config;
	
	@Test
	public void testOpenAnnouncement() {
		Model model = new ExtendedModelMap();
		controller.openAnnouncement(model);
		
		assertThat(model.containsAttribute("content"), is(true));
	}

	@Test
	public void testSaveAnnouncement() {
		Model model = new ExtendedModelMap();
		String content = "My test.";
		controller.saveAnnouncement(model, content);
		
		assertThat(model.containsAttribute("success"), is(true));
		assertThat(service.getAnnouncement(), is(content));
		assertThat(config.getAnnouncement(), is(content));
	}
}
