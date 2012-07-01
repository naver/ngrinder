package org.ngrinder.util;

import org.junit.Assert;
import org.junit.Test;
import org.ngrinder.util.SystemProperties;

public class SystemPropertiesTest {

	@Test
	public void testSystemProperties() {
		Assert.assertNotNull(SystemProperties.GRINDER_VER);
		Assert.assertNotNull(SystemProperties.NGRINDER_VER);
	}
}
