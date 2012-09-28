package org.ngrinder;

import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.net.URL;

import org.junit.Test;
import org.ngrinder.common.util.ThreadUtil;

public class NGrinderStarterTest {
	
	private NGrinderStarter starter = new TmpNGrinderStarter();
	
	@Test
	public void testNGrinderStarterJarResolution() {
		String path = ClassLoader.getSystemClassLoader().getResource(".").getPath();
		System.out.println(path);
	}
	
	@Test
	public void testToolsJarPath() {
		URL findToolsJarPath = starter.findToolsJarPath();
		assertThat(findToolsJarPath, notNullValue());
	}
	
	@Test
	public void testGetStartMode() {
		String startMode = starter.getStartMode();
		assertTrue(startMode.equals("monitor") || startMode.equals("agent"));
	}
	
	@Test
	public void testStartAgent() {		
		starter.startAgent(); //there is no agent properties, it can be started with default setting
		ThreadUtil.sleep(3000);
		starter.stopAgent();
	}
	
	@Test
	public void testStartMonitor() {		
		starter.startMonitor();
		ThreadUtil.sleep(3000);
		starter.stopMonitor();
		starter.stopMonitor();
	}
}

class TmpNGrinderStarter extends NGrinderStarter{
	@Override
	protected void printHelpAndExit(String message) {
	}

	@Override
	protected void printHelpAndExit(String message, Exception e) {
	}
}
