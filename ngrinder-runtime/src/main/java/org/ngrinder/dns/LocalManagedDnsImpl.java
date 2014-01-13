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

import org.xbill.DNS.*;
import sun.net.spi.nameservice.NameService;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * DNS Java DNS resolver.
 *
 * @author JunHo Yoon
 * @since 3.0
 */
@SuppressWarnings({"restriction", "WeakerAccess"})
public class LocalManagedDnsImpl implements NameService {

	public LocalManagedDnsImpl() {
		Cache cache = createCache();
		Lookup.setDefaultCache(cache, DClass.IN);
	}

	protected Cache createCache() {
		return new Cache();
	}

	/**
	 * Finds A records (ip addresses) for the host name.
	 *
	 * @param name host name to resolve.
	 * @return All the ip addresses found for the host name.
	 * @throws UnknownHostException occurs when name is not available in DNS
	 */
	public InetAddress[] lookupAllHostAddr(String name) throws UnknownHostException {
		try {

			final Lookup lookup = new Lookup(name, Type.A);
			Record[] records = lookup.run();
			if (records == null) {
				throw new UnknownHostException(name);
			}

			InetAddress[] array = new InetAddress[records.length];
			for (int i = 0; i < records.length; i++) {
				ARecord a = (ARecord) records[i];
				array[i] = a.getAddress();
			}
			return array;
		} catch (TextParseException e) {
			throw new UnknownHostException(e.getMessage());
		}
	}

	/**
	 * Finds PTR records (reverse dns lookups) for the ip address.
	 *
	 * @param ip ip address to lookup.
	 * @return The host name found for the ip address.
	 * @throws UnknownHostException occurs when id is not available in DNS
	 */
	public String getHostByAddr(byte[] ip) throws UnknownHostException {
		try {
			String addr = DnsUtils.numericToTextFormat(ip);
			Record[] records = new Lookup(addr, Type.PTR).run();
			if (records == null) {
				throw new UnknownHostException(addr);
			}
			PTRRecord ptr = (PTRRecord) records[0];
			return ptr.getTarget().toString();
		} catch (TextParseException e) {
			throw new UnknownHostException(e.getMessage());
		}
	}
}
