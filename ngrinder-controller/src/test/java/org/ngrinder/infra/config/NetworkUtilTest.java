package org.ngrinder.infra.config;

import net.grinder.util.NetworkUtil;

import org.junit.Test;

public class NetworkUtilTest {
	@Test
	public void testNetwork() {
		System.out.println(NetworkUtil.getLocalHostName());;
	}
}
