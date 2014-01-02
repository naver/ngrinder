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

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import java.net.InetAddress;
import java.util.Set;

/**
 * NameStore test class
 *
 * @author mavlarn
 * @since 3.0
 */
public class NameStoreTest {

	@Before
	public void before() {
		NameStore.getInstance().reset();
	}

	@Test
	public void testNameStoreInit() {
		System.setProperty("ngrinder.etc.hosts", "aaa.com:1.1.1.1,bbb.com:2.2.2.2");
		NameStore.getInstance().reset();
		Set<InetAddress> ips = NameStore.getInstance().get("aaa.com");
		assertThat(ips.iterator().next().getHostAddress(), is("1.1.1.1"));

		NameStore.getInstance().remove("bbb.com");
		ips = NameStore.getInstance().get("bbb.com");
		assertThat(ips, nullValue());

	}

	@Test
	public void testNameStoreInitInvalid() {
		System.setProperty("ngrinder.etc.hosts", "bbb.com:,1.1.1.1,");
		NameStore.getInstance().reset();
		Set<InetAddress> ips = NameStore.getInstance().get("aaa.com");
		assertThat(ips, nullValue());

	}

	@Test
	public void testNameStoreInitEmpty() {
		System.setProperty("ngrinder.etc.hosts", "");
		NameStore.getInstance().reset();
		Set<InetAddress> ips = NameStore.getInstance().get("bbb.com");
		assertThat(ips, nullValue());
	}

}
