package org.ngrinder.operation;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;
import org.ngrinder.AbstractNGrinderTransactionalTest;
import org.ngrinder.infra.config.Config;
import org.ngrinder.operation.cotroller.SystemConfigController;
import org.ngrinder.operation.service.SystemConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.ui.Model;

public class SystemConfigControllerTest extends AbstractNGrinderTransactionalTest {

	@Autowired
	private SystemConfigController controller;
	
	@Autowired
	private SystemConfigService service;
	
	@Autowired
	private Config config;
	
	@Test
	public void testOpenSystemConfiguration() {
		Model model = new ExtendedModelMap();
		controller.openSystemConfiguration(model);
		
		assertThat(model.containsAttribute("content"), is(true));
	}
	
	@Test
	public void testSaveSystemConfiguration() {
		Model model = new ExtendedModelMap();
		String content = "test=My test.";
		controller.saveSystemConfiguration(model, content);
		
		assertThat(model.containsAttribute("success"), is(true));
		assertThat(service.getSystemConfigFile(), is(content));
		assertThat(config.getSystemProperties().getProperty("test", ""), is("My test."));
	}
}
