package com.nhncorp.ngrinder.util;

import org.junit.Assert;
import org.junit.Test;

public class SystemPropertiesTest {

	@Test
	public void testSystemProperties() {
		Assert.assertNotNull(SystemProperties.GRINDER_VER);
		Assert.assertNotNull(SystemProperties.NGRINDER_VER);
	}
}
