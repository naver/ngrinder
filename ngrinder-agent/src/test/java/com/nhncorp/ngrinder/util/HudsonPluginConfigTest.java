package com.nhncorp.ngrinder.util;

import org.junit.Test;

public class HudsonPluginConfigTest {
	@Test
	public void testHudsonPluginConfigTest() {
		HudsonPluginConfig.getHudsonHost();
		HudsonPluginConfig.getHudsonPort();
		HudsonPluginConfig.isNeedToHudson();
	}
}
