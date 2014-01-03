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
package org.ngrinder.dns;

import org.junit.Test;
import org.xbill.DNS.Cache;
import org.xbill.DNS.Name;
import org.xbill.DNS.RRset;
import org.xbill.DNS.SetResponse;

import java.net.InetAddress;
import java.net.UnknownHostException;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

/**
 * To test the custom DNS, should to add vm arguments as below:
 * -Dsun.net.spi.nameservice.provider.1=dns,LocalManagedDnsProxy
 * -Dngrinder.etc.hosts=10.0.0.1:www.google.com,10.0.0.2:www.google2.com.
 *
 * @author JunHo Yoon
 */
public class LocalManagedDnsTest {

	private final LocalManagedDnsProxy localDNS = (LocalManagedDnsProxy) new LocalManagedDnsDescriptor().createNameService();


	@Test
	public void testCustomDNSLookup() throws UnknownHostException {
		System.setProperty("ngrinder.etc.hosts", "www.google.com:10.10.10.10,www.google.com:10.10.10.11");
		NameStore.getInstance().reset();
		InetAddress[] inetAddresses = localDNS.lookupAllHostAddr("www.google.com");
		assertThat(inetAddresses.length, is(2));
		System.setProperty("ngrinder.etc.hosts", "www.google.com:10.10.10.10,www.google.com:10.10.10.10");
		NameStore.getInstance().reset();
		assertThat(localDNS.lookupAllHostAddr("www.google.com").length, is(1));
		assertThat(localDNS.getHostByAddr(DnsUtils.textToNumericFormat("10.10.10.10")), is("www.google.com"));
	}


	@Test
	public void testNotRegisteredDNSLookup() throws UnknownHostException {
		System.setProperty("ngrinder.etc.hosts", "www.google.com:10.10.10.10,www.google.com:10.10.10.11");
		NameStore.getInstance().reset();
		assertThat(localDNS.lookupAllHostAddr("www.naver.com").length, greaterThan(1));
	}


	@Test(expected = UnknownHostException.class)
	public void testNotExistDNSLookup() throws UnknownHostException {
		NameStore.getInstance().reset();
		localDNS.lookupAllHostAddr("www.wowwowwowow11.com");
	}

	public RRset[] result = null;
	int count = 0;

	@Test
	public void testCache() throws UnknownHostException {

		final Cache cache = new Cache() {
			@Override
			public SetResponse lookupRecords(Name name, int type, int minCred) {
				final SetResponse setResponse = super.lookupRecords(name, type, minCred);
				if (count++ == 0) {
					assertThat(setResponse.isUnknown(), is(true));
				} else {
					assertThat(setResponse.isUnknown(), is(false));
				}
				return setResponse;    //To change body of overridden methods use File | Settings | File Templates.
			}

		};
		LocalManagedDnsImpl impl = new LocalManagedDnsImpl() {
			@Override
			protected Cache createCache() {
				return cache;
			}
		};
		assertThat(impl.lookupAllHostAddr("www.google.com"), notNullValue());
		assertThat(impl.lookupAllHostAddr("www.google.com"), notNullValue());
		assertThat(impl.lookupAllHostAddr("www.google.com"), notNullValue());
		assertThat(impl.lookupAllHostAddr("www.google.com"), notNullValue());
		assertThat(impl.lookupAllHostAddr("www.google.com"), notNullValue());
	}

}
