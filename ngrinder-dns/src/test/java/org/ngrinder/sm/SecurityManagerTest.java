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

/*
 * Copyright (C) 2012 - 2012 NHN Corporation
 * All rights reserved.
 *
 * This file is part of The nGrinder software distribution. Refer to
 * the file LICENSE which is part of The nGrinder distribution for
 * licensing details. The nGrinder distribution is available on the
 * Internet at http://nhnopensource.org/ngrinder
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS
 * FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE
 * COPYRIGHT HOLDERS OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 */

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.Inet4Address;
import java.net.UnknownHostException;

import junit.framework.Assert;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.ArrayUtils;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * 
 * @author Tobi
 * @since 3.0
 */
public class SecurityManagerTest {

	public static boolean SM_TEST = false;

	private static final String PATH = new File("/").getAbsolutePath();

	@BeforeClass
	// @Ignore
	public static void init() {
		SM_TEST = true;
		System.setProperty("ngrinder.exec.path", PATH);
		System.setProperty("ngrinder.etc.hosts", "10.34.64.36,CN14748-D-1:127.0.0.1,localhost:127.0.0.1");
		System.setProperty("ngrinder.console.ip", "10.34.63.53");
		// -Djava.security.manager=org.ngrinder.sm.NGrinderSecurityManager
		System.setSecurityManager(new MockNGrinderSecurityManager());
	}

	@AfterClass
	public static void disbaleSecurity() {
		SM_TEST = false;
	}

	@Test
	// @Ignore
	public void testNGrinderSecurityManager() {
		System.out.println(new File("hell").getAbsolutePath());
		System.out.println(System.getProperty("user.home"));
	}

	@Test(expected = SecurityException.class)
	public void testNGrinderSecurityManagerNetAccessNotAllowed() throws UnknownHostException {
		System.out.println(ArrayUtils.toString(Inet4Address.getAllByName("www.google.com")));
	}

	@Test
	public void testNGrinderSecurityManagerNetAccessAllowed() throws UnknownHostException {
		System.out.println(ArrayUtils.toString(Inet4Address.getAllByName("10.34.64.36")));
		Assert.assertTrue(true);
	}

	@Test
	// @Ignore
	public void testNGrinderSecurityManagerFile() {
		boolean readTag = false, writeTag = false;
		BufferedReader fis = null;
		BufferedWriter fos = null;
		try {
			fis = new BufferedReader(new FileReader(PATH + "/input.txt"));
		} catch (FileNotFoundException ioe) {
			readTag = true;
		} finally {
			IOUtils.closeQuietly(fis);
		}
		Assert.assertTrue(readTag);

		try {
			fos = new BufferedWriter(new FileWriter(PATH + "/output.txt"));
			fos.write("Hello SecurityManager.");
		} catch (IOException ioe) {
			System.out.println("I/O failed for SecurityManagerTest.");
			System.err.println(ioe);
		} catch (SecurityException e) {
			System.err.println("Do not have the file write access in \"ngrinder.exec.path\"");
			writeTag = true;
		} finally {
			IOUtils.closeQuietly(fos);
		}
		Assert.assertTrue(writeTag);
	}

}
