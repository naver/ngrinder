package org.ngrinder.util;

import org.junit.Test;
import org.ngrinder.util.HudsonPluginConfig;

public class HudsonPluginConfigTest {
	@Test
	public void testHudsonPluginConfigTest() {
		HudsonPluginConfig.getHudsonHost();
		HudsonPluginConfig.getHudsonPort();
		HudsonPluginConfig.isNeedToHudson();
	}
}
