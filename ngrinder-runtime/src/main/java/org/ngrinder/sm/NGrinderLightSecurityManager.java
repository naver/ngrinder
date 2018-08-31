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
package org.ngrinder.sm;

import java.net.InetAddress;

/**
 * nGrinder security manager which less secured than NGrinderSecurityManager.
 * <p/>
 * This allows followings.
 * <p/>
 * <ul>
 * <li>multicast</li>
 * <li>tcp connection unspecified address</li>
 * </ul>
 *
 * @since 3.4.2
 */
public class NGrinderLightSecurityManager extends NGrinderSecurityManager {

	@Override
	public void checkMulticast(InetAddress maddr) {
	}

	@Override
	public void checkConnect(String host, int port) {
	}

	@Override
	public void checkConnect(String host, int port, Object context) {
	}
}
