package org.ngrinder.monitor.agent.collector;

import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertThat;

import java.io.File;

import org.apache.commons.lang.StringUtils;
import org.junit.Test;
import org.ngrinder.common.util.ThreadUtil;
import org.ngrinder.monitor.share.domain.BandWidth;
import org.ngrinder.monitor.share.domain.SystemInfo;

public class MonitorCollectorTest {

	@Test
	public void test() throws InterruptedException {
		String property = StringUtils.trimToEmpty(System.getProperty("java.library.path"));
		System.setProperty("java.library.path",
						property + File.pathSeparator + new File("./native_lib").getAbsolutePath());

		AgentSystemDataCollector collector = new AgentSystemDataCollector();
		collector.refresh();
		int i = 0;
		while (i++ < 100) {
			SystemInfo execute = collector.execute();
			BandWidth bandWidth = execute.getBandWidth();
			System.out.println(bandWidth);
			if (i != 1) {
				assertThat(bandWidth.getReceivedPerSec(), not(0L));
				assertThat(bandWidth.getSentPerSec(), not(0L));
			}
			ThreadUtil.sleep(3000);
		}
	}
}
