package org.ngrinder.infra.report;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Date;

import org.junit.Test;
import org.ngrinder.infra.config.Config;

public class PeriodicCollectDataToGAServiceTest {

	@Test
	public void test() {
		Config configMock = mock(Config.class);
		when(configMock.isUsageReportEnabled()).thenReturn(true);
		when(configMock.getVesion()).thenReturn("test-0.0.1");
		PeriodicCollectDataToGAService gaService = new PeriodicCollectDataToGAService() {
			@Override
			protected int getUsage(Date start, Date end) {
				return 10;
			}

		};
		gaService.setConfig(configMock);
		gaService.reportUsage();
	}

}
