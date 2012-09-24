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

import junit.framework.Assert;

import org.apache.commons.io.IOUtils;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * 
 * @author Tobi
 * @since 3.0
 */
public class SecurityManagerTest {

	public static void main(String[] args) {

		SecurityManagerTest.init();
		SecurityManagerTest smt = new SecurityManagerTest();
		smt.testNGrinderSecurityManager1();
		smt.testNGrinderSecurityManager2();
	}

	private static final String PATH = new File("/").getAbsolutePath();

	@BeforeClass
	// @Ignore
	public static void init() {
		System.setProperty("ngrinder.exec.path", PATH);
		System.setProperty("ngrinder.etc.hosts", "10.34.64.36,CN14748-D-1:127.0.0.1,localhost:127.0.0.1");
		System.setProperty("ngrinder.console.ip", "10.34.63.53");
		// -Djava.security.manager=org.ngrinder.sm.NGrinderSecurityManager
		System.setSecurityManager(new NGrinderSecurityManager());
	}

	@Test
	// @Ignore
	public void testNGrinderSecurityManager1() {
		System.out.println(new File("hell").getAbsolutePath());
		System.out.println(System.getProperty("user.home"));
	}

	@Test
	// @Ignore
	public void testNGrinderSecurityManager2() {
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
