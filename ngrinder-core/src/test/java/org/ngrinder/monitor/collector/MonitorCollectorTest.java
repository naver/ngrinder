package org.ngrinder.monitor.collector;

import org.hyperic.jni.ArchLoaderException;
import org.hyperic.jni.ArchNotSupportedException;
import org.junit.Before;
import org.junit.Test;
import org.ngrinder.common.util.ThreadUtils;
import org.ngrinder.infra.AgentConfig;
import org.ngrinder.infra.ArchLoaderInit;
import org.ngrinder.monitor.share.domain.BandWidth;
import org.ngrinder.monitor.share.domain.SystemInfo;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertThat;

public class MonitorCollectorTest {
	@Before
	public void before() throws ArchNotSupportedException, ArchLoaderException {
		AgentConfig agentConfig = new AgentConfig.NullAgentConfig(1).init();
		new ArchLoaderInit().init(agentConfig.getHome().getNativeDirectory());
	}

	@Test
	public void testSystemDataCollection() throws IOException {
		SystemDataCollector collector = new SystemDataCollector();
		collector.refresh();

		int i = 0;
		boolean sent = false;
		boolean received = false;

		HttpURLConnection httpURLConnection = (HttpURLConnection) new URL("https://www.naver.com").openConnection();
		httpURLConnection.connect();
		httpURLConnection.disconnect();

		while (i++ < 3) {
			SystemInfo systemInfo = collector.execute();
			ThreadUtils.sleep(2000);
			BandWidth bandWidth = systemInfo.getBandWidth();
			if (bandWidth.getReceivedPerSec() != 0) {
				received = true;
			}
			if (bandWidth.getSentPerSec() != 0) {
				sent = true;
			}
			assertThat(systemInfo.getFreeMemory(), not(0l));
			assertThat(systemInfo.getTotalMemory(), not(0l));
		}
		assertThat("sent", sent, is(true));
		assertThat("received", received, is(true));
	}
}
