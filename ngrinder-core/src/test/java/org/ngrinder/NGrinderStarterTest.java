package org.ngrinder;

import org.junit.Test;

public class NGrinderStarterTest {
	@Test
	public void testNGrinderStarterJarResolution() {
		String path = ClassLoader.getSystemClassLoader().getResource(".").getPath();
		System.out.println(path);
	}
}
