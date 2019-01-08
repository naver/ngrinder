package org.ngrinder.infra.config;


import net.grinder.util.NetworkUtils;
import org.junit.Test;
import org.ngrinder.common.util.PropertiesWrapper;

import java.net.Inet6Address;
import java.net.InetAddress;

import static net.grinder.util.NetworkUtils.IPPortPair;
import static org.fest.assertions.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class DynamicCacheConfigTest {

	@Test
	public void testLocalHost() {
		IPPortPair pair = new IPPortPair(NetworkUtils.DEFAULT_LOCAL_HOST_ADDRESS, 10);
		assertThat(pair.isLocalHost()).isTrue();

		pair = new IPPortPair("10.10.10.10", 10);
		assertThat(pair.isLocalHost()).isFalse();

		for (InetAddress each : NetworkUtils.DEFAULT_LOCAL_ADDRESSES) {
			if (each instanceof Inet6Address) {
				final String hostAddress = each.getHostAddress();
				assertThat(new IPPortPair("[" + hostAddress + "]", 10).isLocalHost()).isTrue();
			}
		}
	}


	@Test
	public void testIPPair() {
		IPPortPair pair = new IPPortPair("[fe80::ecc6:7ab8:d4ac:c77a%10]", 10);
		assertThat(pair.getIP()).isEqualTo("fe80:0:0:0:ecc6:7ab8:d4ac:c77a%10");
		assertThat(pair.toString()).isEqualTo("[fe80:0:0:0:ecc6:7ab8:d4ac:c77a%10]:10");
	}

	@Test
	public void testClusterConfig() {
		Config config = new Config() {
			@Override
			public PropertiesWrapper getClusterProperties() {
				PropertiesWrapper mock = mock(PropertiesWrapper.class);
				when(mock.getProperty(PROP_CLUSTER_MEMBERS)).thenReturn("10.10.10.10;10.10.10.20,10.10.10.30");
				return mock;
			}
		};
		final String[] clusterURIs = config.getClusterURIs();
		assertThat(clusterURIs[0]).isEqualTo("10.10.10.10");
		assertThat(clusterURIs[1]).isEqualTo("10.10.10.20");
		assertThat(clusterURIs[2]).isEqualTo("10.10.10.30");
	}
}
