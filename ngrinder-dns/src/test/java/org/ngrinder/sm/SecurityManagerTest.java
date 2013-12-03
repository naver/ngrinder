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

import junit.framework.Assert;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.ArrayUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.*;
import java.net.Inet4Address;
import java.net.UnknownHostException;

import static org.junit.Assert.fail;

public class SecurityManagerTest {


	private static final String PATH = new File("/").getAbsolutePath();
	private SecurityManager preSecurityManager;

	@Before
	public void init() {
		System.setProperty("ngrinder.exec.path", PATH);
		System.setProperty("ngrinder.etc.hosts", "10.34.64.36,CN14748-D-1:127.0.0.1,localhost:127.0.0.1");
		System.setProperty("ngrinder.console.ip", "10.34.63.53");
		preSecurityManager = System.getSecurityManager();
		System.setSecurityManager(new MockNGrinderSecurityManager());
	}

	@After
	public  void disableSecurity() {
		System.setSecurityManager(preSecurityManager);
	}




	@Test
	public void testAllowedNetworkAccess() throws UnknownHostException {
		ArrayUtils.toString(Inet4Address.getAllByName("10.34.64.36"));
	}

	@Test(expected = SecurityException.class)
	public void testNotAllowedNetworkAccess() throws UnknownHostException {
		Inet4Address.getAllByName("www.google.com");
	}

	@Test
	public void testAllowedFileAccess() {
		new File("hell").getAbsolutePath();
		// This should be passed
	}

	@Test
	public void testNotAllowedFileAccess() {
		boolean readTag = false, writeTag = false;
		BufferedReader fis = null;
		BufferedWriter fos = null;
		try {
			fis = new BufferedReader(new FileReader(PATH + "/input.txt"));
			fail("Read should not be allowed");
		} catch (Exception ioe) {
		} finally {
			IOUtils.closeQuietly(fis);
		}

		try {
			fos = new BufferedWriter(new FileWriter(PATH + "/output.txt"));
			fos.write("Hello SecurityManager.");
			fail("This should not be reached");
			Assert.assertTrue(writeTag);
		} catch (IOException ioe) {
			fail("This should not be reached");
		} catch (SecurityException e) {
		} finally {
			IOUtils.closeQuietly(fos);
		}
	}

}
