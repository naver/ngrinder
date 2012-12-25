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

import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.UnknownHostException;

import org.junit.Test;

public class NetworkUtilTest {
	@Test
	public void testLocalHostAddress() {
		try {
			System.out.println("Local addr:" + InetAddress.getLocalHost().getHostAddress());
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
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
		localHostAddress = NetworkUtil.getLocalHostAddress("www.invalidaddress", 80);
		assertThat(localHostAddress, notNullValue());
		assertThat(localHostAddress, not("127.0.0.1"));
	}
   
	@Test
	public void testLocalHostName() throws ClassNotFoundException, SecurityException, NoSuchMethodException, IllegalArgumentException, IllegalAccessException, InvocationTargetException {
		try {
			System.out.println("Local host:" + InetAddress.getLocalHost().getHostName());
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
		String localHostAddress = NetworkUtil.getLocalHostName();
		System.out.println("NetworkUtil.getLocalHostName:" + localHostAddress);
		assertThat(localHostAddress, notNullValue());
		
		Class<?> networkClas = Class.forName("net.grinder.util.NetworkUtil");
		Method NonLoopbackMethod= networkClas.getDeclaredMethod("getFirstNonLoopbackAddress", new Class[] {boolean.class, boolean.class});
		NonLoopbackMethod.setAccessible(true);
		NonLoopbackMethod.invoke(networkClas, true,false);
		
	}
}
