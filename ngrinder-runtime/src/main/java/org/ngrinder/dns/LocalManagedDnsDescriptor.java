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

/**
 * <p>
 * Constructs the LocalManagedDnsProxy and returns references to it.
 * </p>
 * 
 * @author JunHo Yoon
 * @since 3.0
 */
@SuppressWarnings({"restriction", "WeakerAccess"})
public class LocalManagedDnsDescriptor implements sun.net.spi.nameservice.NameServiceDescriptor {

	private static final String DNS_PROVIDER_NAME = "LocalManagedDns";

	private static final sun.net.spi.nameservice.NameService nameService = new LocalManagedDnsProxy();


	/**
	 * @return The string "dns"
	 */
	public String getType() {
		return "dns";
	}

	/**
	 * @return The string "dnsjava"
	 */
	public String getProviderName() {
		return DNS_PROVIDER_NAME;
	}

	/**
	 * This doesn't actually create a name service provider, it returns a reference to the one that
	 * was already created as class load time.
	 * 
	 * @return The dnsjava name service provider
	 */

	public sun.net.spi.nameservice.NameService createNameService() {
		return nameService;
	}
}