package org.ngrinder.home.service;

import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.lessThanOrEqualTo;
import static org.junit.Assert.assertThat;

import java.io.IOException;
import java.util.List;

import org.junit.Test;
import org.ngrinder.AbstractNGNinderTransactionalTest;
import org.ngrinder.home.model.PanelEntry;
import org.ngrinder.home.service.HomeService;
import org.springframework.beans.factory.annotation.Autowired;

public class HomeServiceTest extends AbstractNGNinderTransactionalTest {

	@Autowired
	private HomeService homeService;

	@Test
	public void testHome() throws IOException {
		List<PanelEntry> leftPanelEntries = homeService.getLeftPanelEntries();
		assertThat(leftPanelEntries.size(), greaterThan(2));
		assertThat(leftPanelEntries.size(), lessThanOrEqualTo(8));

		List<PanelEntry> rightPanel = homeService.getRightPanelEntries();
		assertThat(rightPanel.size(), greaterThanOrEqualTo(2));
		assertThat(rightPanel.size(), lessThanOrEqualTo(8));

	}
}
