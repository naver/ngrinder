package org.ngrinder;

import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertEquals;

import java.net.URL;

import javax.annotation.PostConstruct;

import org.junit.Test;

public class NGrinderStarterTest {
	
	private NGrinderStarter starter;
	
	@PostConstruct
	public void init() {
		starter = new NGrinderStarter() {
			@Override
			protected void printHelpAndExit(String message) {
			}

			@Override
			protected void printHelpAndExit(String message, Exception e) {
			}
		};
	}
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
		//default start mode is monitor
		String startMode = starter.getStartMode();
		assertEquals(startMode, "monitor");
	}
}
