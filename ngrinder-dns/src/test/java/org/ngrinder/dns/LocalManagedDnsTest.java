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

import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.*;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.security.Security;

import org.junit.Ignore;
import org.junit.Test;

/**
 * To test the custom DNS, should to add vm arguments as below:
 * -Dsun.net.spi.nameservice.provider.1=dns,LocalManagedDns
 * -Dngrinder.etc.hosts=10.0.0.1:www.google.com,10.0.0.2:www.google2.com.
 *
 * @author JunHo Yoon
 */
public class LocalManagedDnsTest {

	private final LocalManagedDns localDNS = (LocalManagedDns)new LocalManagedDnsDescriptor().createNameService();


	@Test
	public void testCustomDNSLookup() throws UnknownHostException {
		System.setProperty("ngrinder.etc.hosts", "www.google.com:10.10.10.10,www.google.com:10.10.10.11");
		NameStore.getInstance().reset();
		assertThat(localDNS.lookupAllHostAddr("www.google.com").length, is(2));


		System.setProperty("ngrinder.etc.hosts", "www.google.com:10.10.10.10,www.google.com:10.10.10.10");
		NameStore.getInstance().reset();
		assertThat(localDNS.lookupAllHostAddr("www.google.com").length, is(1));
		assertThat(localDNS.getHostByAddr(DnsUtil.textToNumericFormat("10.10.10.10")), is("www.google.com"));
	}


	@Test
	public void testNotRegisteredDNSLookup() throws UnknownHostException {
		System.setProperty("ngrinder.etc.hosts", "www.google.com:10.10.10.10,www.google.com:10.10.10.11");
		NameStore.getInstance().reset();
		assertThat(localDNS.lookupAllHostAddr("www.naver.com").length, greaterThan(2));
	}


	@Test(expected = UnknownHostException.class)
	public void testNotExistDNSLookup() throws UnknownHostException {
		NameStore.getInstance().reset();
		localDNS.lookupAllHostAddr("www.wowwowwowow11.com");
	}


}
