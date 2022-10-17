package org.ngrinder.monitor.collector;

import org.junit.Test;
import org.ngrinder.monitor.share.domain.BandWidth;
import org.ngrinder.monitor.share.domain.SystemInfo;

import static org.hamcrest.Matchers.not;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.spy;
import static org.ngrinder.common.util.ThreadUtils.sleep;

public class MonitorCollectorTest {

	@Test
	public void testSystemDataCollection() throws ClassNotFoundException {
		SystemDataCollector spyCollector = spy(new SystemDataCollector());
		spyCollector.refresh();

		MonitorCollectorTest.class.getClassLoader().loadClass("org.ngrinder.common.util.SystemInfoUtils");
		sleep(1000);

		int i = 0;

		while (i++ < 3) {
			SystemInfo systemInfo = spyCollector.execute();
			sleep(2000);
			BandWidth bandWidth = systemInfo.getBandWidth();

			assertThat(bandWidth.getReceived(), not(0L));
			assertThat(bandWidth.getSent(), not(0L));
			assertThat(systemInfo.getFreeMemory(), not(0L));
			assertThat(systemInfo.getCpuUsedPercentage(), not(0.0));
		}
	}
}
