package org.ngrinder.infra.config;


import com.google.common.collect.Lists;
import net.grinder.util.NetworkUtils;
import net.grinder.util.Pair;
import org.junit.Assume;
import org.junit.Test;
import org.ngrinder.common.constant.ClusterConstants;
import org.ngrinder.common.util.PropertiesWrapper;
import org.ngrinder.infra.logger.CoreLogger;

import java.net.Inet6Address;
import java.net.InetAddress;

import static com.google.common.collect.Lists.newArrayList;
import static net.grinder.util.NetworkUtils.*;
import static org.fest.assertions.Assertions.assertThat;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class DynamicCacheConfigTest {

	@Test
	public void testDynamicCacheConfig() {
		Assume.assumeThat(DEFAULT_LOCAL_ADDRESSES.size(), greaterThanOrEqualTo(2));
		// When
		DynamicCacheConfig dynamicCacheConfig = new DynamicCacheConfig() {
			@Override
			int getClusterPort() {
				return 10010;
			}
		};
		// If
		Config config = mock(Config.class);
		PropertiesWrapper propertiesWrapper = mock(PropertiesWrapper.class);
		String address1 = removeScopedMarkerFromIP(DEFAULT_LOCAL_ADDRESSES.get(0).getHostAddress());
		when(propertiesWrapper.getProperty(ClusterConstants.PROP_CLUSTER_MODE)).thenReturn("advanced");
		when(config.getClusterURIs()).thenReturn(new String[]{address1, "210.10.10.1"});
		when(config.isClustered()).thenReturn(true);
		when(config.getClusterProperties()).thenReturn(propertiesWrapper);
		dynamicCacheConfig.setConfig(config);
		Pair<NetworkUtils.IPPortPair, String> cacheProperties = dynamicCacheConfig.createManualDiscoveryCacheProperties(newArrayList("hello", "world"));
		NetworkUtils.IPPortPair first = cacheProperties.getFirst();
		// Then
		assertThat(first.getIP()).isEqualTo(address1);
		assertThat(first.getPort()).isEqualTo(10010);
		assertThat(cacheProperties.getSecond()).contains("hello");
		assertThat(cacheProperties.getSecond()).contains("world");

		assertThat(dynamicCacheConfig.dynamicCacheManager()).isNotNull();
	}

	@Test
	public void testPeerProperties() {
		// When
		DynamicCacheConfig dynamicCacheConfig = new DynamicCacheConfig() {
			@Override
			int getClusterPort() {
				return 10010;
			}

			@Override
			protected String[] getClusterURIs() {
				return new String[]{"10.10.10.10:20010", NetworkUtils.DEFAULT_LOCAL_HOST_ADDRESS, "10.10.10.20"};
			}
		};
		final Pair<IPPortPair, String> hello = dynamicCacheConfig.createManualDiscoveryCacheProperties(Lists.newArrayList("hello"));
		assertThat(hello.getSecond()).isEqualTo("peerDiscovery=manual,rmiUrls=//10.10.10.10:20010/hello|//10.10.10.20:10010/hello");
		assertThat(hello.getFirst().toString()).isEqualTo(NetworkUtils.DEFAULT_LOCAL_HOST_ADDRESS + ":10010");
	}

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


	@Test
	public void testEasyCluster() {
		// When
		DynamicCacheConfig dynamicCacheConfig = new DynamicCacheConfig() {
			@Override
			int getClusterPort() {

				int port = 10010;
				try {
					final InetAddress byName = InetAddress.getByName(getClusterHostName());
					port = NetworkUtils.checkPortAvailability(byName, port, 30);
				} catch (Exception e) {
					CoreLogger.LOGGER.error("The cluster port {} is failed to bind. Please check network configuration.", port);
				}
				return port;
			}

			@Override
			protected String getClusterMode() {
				return "easy";
			}

			@Override
			public String getClusterHostName() {
				return "localhost";
			}

			@Override
			protected boolean isClustered() {
				return true;
			}
		};
		final Pair<IPPortPair, String> autoDiscoveryCacheProperties = dynamicCacheConfig.createAutoDiscoveryCacheProperties();
		assertThat(autoDiscoveryCacheProperties.getSecond()).isEqualTo("peerDiscovery=automatic, multicastGroupAddress=230.0.0.1,multicastGroupPort=4446, timeToLive=32");
		assertThat(autoDiscoveryCacheProperties.getFirst().toString()).isEqualTo("127.0.0.1" + ":10010");
		dynamicCacheConfig.dynamicCacheManager();
		dynamicCacheConfig.dynamicCacheManager();

	}

}
