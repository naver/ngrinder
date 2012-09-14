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
package org.ngrinder.common.util;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;

import org.junit.Test;
import org.ngrinder.NGrinderStarter;
import org.ngrinder.model.User;

/**
 * Class description.
 * 
 * @author Mavlarn
 * @since
 */
public class ReflectionUtilTest {

	/**
	 * Test method for
	 * {@link net.grinder.util.ReflectionUtil#getFieldValue(java.lang.Object, java.lang.String)}
	 * .
	 */
	@Test
	public void testGetFieldValue() {
		User testUser = new User();
		testUser.setUserId("TMP_UID");
		String rtnUid = (String) ReflectionUtil.getFieldValue(testUser, "userId");
		assertThat(rtnUid, is("TMP_UID"));
	}

	@Test
	public void testReflectionUtil() throws MalformedURLException {
		URLClassLoader urlClassLoader = (URLClassLoader) ClassLoader.getSystemClassLoader();
		ReflectionUtil.invokePrivateMethod(urlClassLoader, "addURL", new Object[] { new File("hello").toURI().toURL() });

	}

	@Test
	public void testToolsJarPath() {
		NGrinderStarter starter = new NGrinderStarter() {
			@Override
			protected void printHelpAndExit(String message) {
			}

			@Override
			protected void printHelpAndExit(String message, Exception e) {
			}
		};
		URL findToolsJarPath = starter.findToolsJarPath();
		assertThat(findToolsJarPath, notNullValue());
	}

}
