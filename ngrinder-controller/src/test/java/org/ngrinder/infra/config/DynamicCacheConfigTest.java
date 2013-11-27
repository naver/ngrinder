package org.ngrinder.infra.config;


import net.grinder.util.NetworkUtil;
import net.grinder.util.Pair;
import org.junit.Assume;
import org.junit.Test;

import static com.google.common.collect.Lists.newArrayList;
import static net.grinder.util.NetworkUtil.*;
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
			int getClusterListenerPort() {
				return 10010;
			}
		};
		// If
		Config config = mock(Config.class);
		String address1 = removeScopedMarkerFromIP(DEFAULT_LOCAL_IP4_ADDRESSES.get(0).getHostAddress());
		when(config.getClusterURIs()).thenReturn(new String[]{address1, "210.10.10.1"});
		when(config.isCluster()).thenReturn(true);
		dynamicCacheConfig.setConfig(config);
		Pair<NetworkUtil.IPPortPair, String> cacheProperties = dynamicCacheConfig.createCacheProperties(newArrayList("hello", "world"));
		NetworkUtil.IPPortPair first = cacheProperties.getFirst();
		// Then
		assertThat(first.getIP()).isEqualTo(address1);
		assertThat(first.getPort()).isEqualTo(10010);
		assertThat(cacheProperties.getSecond()).contains("hello");
		assertThat(cacheProperties.getSecond()).contains("world");

		assertThat(dynamicCacheConfig.dynamicCacheManager()).isNotNull();
	}
}
