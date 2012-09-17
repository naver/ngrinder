package org.ngrinder;

import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;

import java.net.URL;

import org.junit.Test;

public class NGrinderStarterTest {

	private NGrinderStarter starter = new NGrinderStarter();
	
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
}
