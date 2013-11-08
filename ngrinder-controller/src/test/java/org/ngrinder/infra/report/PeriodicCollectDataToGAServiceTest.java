package org.ngrinder.infra.report;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Date;

import org.junit.Test;
import org.ngrinder.infra.config.Config;

public class PeriodicCollectDataToGAServiceTest {

	@Test
	public void testReport() {
		Config configMock = mock(Config.class);
		when(configMock.isUsageReportEnabled()).thenReturn(true);
		when(configMock.getVersion()).thenReturn("test-0.0.1");
		PeriodicCollectDataToGAService gaService = new PeriodicCollectDataToGAService() {
			@Override
			protected int getUsage(Date start, Date end) {
				return 10;
			}

			protected void doRandomDelay() {
				// No delay for unit test.
			};
		};
		gaService.setConfig(configMock);
		gaService.reportUsage();
	}

}
