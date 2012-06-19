package com.nhncorp.ngrinder.monitor;

import org.junit.Test;

public class ControllerPropertiesTest {
	@Test
	public void testControllerProperties() {
		ControllerProperties.getConsoleHost();
		ControllerProperties.getConsolePort();
		ControllerProperties.setConsoleHost("127.0.0.1");
		ControllerProperties.setConsolePort(8080);
	}
}
