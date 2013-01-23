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

import java.net.InetAddress;
import java.net.UnknownHostException;

import sun.net.spi.nameservice.NameService;

/**
 * Locally managed DNS.
 * 
 * @author JunHo Yoon
 * @since 3.0
 */
@SuppressWarnings("restriction")
public class LocalManagedDns implements NameService {

	private NameService defaultDnsImpl = new DNSJavaNameService();

	/**
	 * Get host name by address.
	 * 
	 * @param ip
	 *            ip
	 * @return host name
	 * @throws UnknownHostException
	 *             occurs when hostname can not be found.
	 * @see sun.net.spi.nameservice.NameService#getHostByAddr(byte[])
	 */
	public String getHostByAddr(byte[] ip) throws UnknownHostException {
		String hostName = NameStore.getInstance().getReveredHost(DnsUtil.numericToTextFormat(ip));
		if (hostName == null) {
			return defaultDnsImpl.getHostByAddr(ip);
		} else {
			return hostName;
		}
	}

	/**
	 * Get ip by hostname.
	 * 
	 * @param name
	 *            hostname
	 * @return ip addresses
	 * @throws UnknownHostException
	 *             occurs when hostname can not be found.
	 * @see sun.net.spi.nameservice.NameService#getHostByAddr(byte[])
	 */
	public InetAddress[] lookupAllHostAddr(String name) throws UnknownHostException {

		String ipAddress = NameStore.getInstance().get(name);
		if (DnsUtil.isNotEmpty(ipAddress)) {
			InetAddress address = InetAddress.getByAddress(DnsUtil.textToNumericFormat(ipAddress));
			return new InetAddress[] { address };
		} else {
			return defaultDnsImpl.lookupAllHostAddr(name);
		}
	}

}
