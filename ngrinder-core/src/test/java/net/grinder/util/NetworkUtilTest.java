/* 
 * Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License. 
 */
package net.grinder.util;

import org.junit.Test;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.UnknownHostException;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

public class NetworkUtilTest {

	@Test
	public void testLocalAddresses() {
		assertThat(NetworkUtil.DEFAULT_LOCAL_ADDRESSES.size(), not(0));
		for (InetAddress each : NetworkUtil.DEFAULT_LOCAL_ADDRESSES) {
			assertThat(each.getHostAddress(), not(containsString("127.0.0.1")));
		}
	}

	@Test
	public void testIPAndPortPair() {
		NetworkUtil.IPPortPair ipAndPortPair = NetworkUtil.convertIPAndPortPair
				("2001:0:9d38:90d7:469:1f94:f5bf:cf5d", 0);
		assertThat(ipAndPortPair.getIP(), is("2001:0:9d38:90d7:469:1f94:f5bf:cf5d"));
		assertThat(ipAndPortPair.getPort(), is(0));
		ipAndPortPair = NetworkUtil.convertIPAndPortPair("[2001:0:9d38:90d7:469:1f94:f5bf:cf5d]:20", 0);
		assertThat(ipAndPortPair.getIP(), is("2001:0:9d38:90d7:469:1f94:f5bf:cf5d"));
		assertThat(ipAndPortPair.getPort(), is(20));
		ipAndPortPair = NetworkUtil.convertIPAndPortPair
				("2001:0:9d38:90d7:469:1f94:f5bf:cf5d", 0);
		assertThat(ipAndPortPair.getIP(), is("2001:0:9d38:90d7:469:1f94:f5bf:cf5d"));
		assertThat(ipAndPortPair.getPort(), is(0));
		ipAndPortPair = NetworkUtil.convertIPAndPortPair("127.0.0.1:20", 0);
		assertThat(ipAndPortPair.getIP(), is("127.0.0.1"));
		assertThat(ipAndPortPair.getPort(), is(20));
		ipAndPortPair = NetworkUtil.convertIPAndPortPair("127.0.0.1", 20);
		assertThat(ipAndPortPair.getIP(), is("127.0.0.1"));
		assertThat(ipAndPortPair.getPort(), is(20));
		ipAndPortPair = NetworkUtil.convertIPAndPortPair("127.2.0.1:40:40", 20);
		assertThat(ipAndPortPair.getIP(), is("127.0.0.1"));
		assertThat(ipAndPortPair.getPort(), is(40));
	}

	@Test
	public void testLocalHostAddress() throws UnknownHostException {
		String localHostAddress = NetworkUtil.getLocalHostAddress();
		assertThat(localHostAddress, notNullValue());
		assertThat(localHostAddress, not("127.0.0.1"));
		localHostAddress = NetworkUtil.getLocalHostAddress();
		assertThat(localHostAddress, notNullValue());
		assertThat(localHostAddress, not("127.0.0.1"));
	}

	@Test
	public void testLocalHostAddressByConnecting() {

		String localHostAddress = NetworkUtil.getLocalHostAddress("www.baidu.com", 80);
		assertThat(localHostAddress, notNullValue());
		assertThat(localHostAddress, not("127.0.0.1"));
		localHostAddress = NetworkUtil.getLocalHostAddress("www.invalidaddresssss", 80);
		assertThat(localHostAddress, notNullValue());
		assertThat(localHostAddress, not("127.0.0.1"));
	}

	@Test
	public void testLocalHostName() throws Exception {
		String localHostAddress = NetworkUtil.getLocalHostName();
		assertThat(localHostAddress, notNullValue());
		final InetAddress firstNonLoopbackAddress = NetworkUtil.getFirstNonLoopbackAddress(true, false);
		assertThat(firstNonLoopbackAddress.isLoopbackAddress(), is(false));
		assertThat(firstNonLoopbackAddress, instanceOf(Inet4Address.class));
	}

	@Test
	public void testLocalHostNameByConnecting() {
		String localHostAddress = NetworkUtil.getLocalHostName("www.baidu.com", 80);
		assertThat(localHostAddress, notNullValue());
	}

}
