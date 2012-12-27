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
package org.ngrinder;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;


import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.apache.commons.lang.StringUtils;
import org.junit.Test;
import org.ngrinder.common.util.ThreadUtil;

public class NGrinderStarterTest {

	private NGrinderStarter starter = new TmpNGrinderStarter();

	@Test
	public void testNGrinderStarterJarResolution() {
		String path = ClassLoader.getSystemClassLoader().getResource(".").getPath();
		System.out.println(path);
	}

	@Test
	public void testGetStartModeAndVersion() {
		String startMode = starter.getStartMode();
		assertTrue(startMode.equals("monitor") || startMode.equals("agent"));

		String version = starter.getVersion();
		assertTrue(StringUtils.isNotBlank(version));
	}

	@Test
	public void testStartAgent() {
		starter.startAgent("127.0.0.1"); // there is no agent properties, it can
											// be started with default setting
		ThreadUtil.sleep(3000);
		starter.stopAgent();
	}

	@Test
	public void testStartMonitor() {
		starter.startMonitor();
		ThreadUtil.sleep(3000);
		starter.stopMonitor();
		starter.stopMonitor();
	}
	
	@Test
	public void testIsValidCurrentDirectory() throws ClassNotFoundException, SecurityException, NoSuchMethodException,
			IllegalArgumentException, IllegalAccessException, InvocationTargetException {

		Class<?> nGrinderStartClass = Class.forName("org.ngrinder.NGrinderStarter");
		Method isValidCurrentDirectory = nGrinderStartClass
				.getDeclaredMethod("isValidCurrentDirectory", new Class[] {});
		isValidCurrentDirectory.setAccessible(true);
		assertFalse((Boolean) isValidCurrentDirectory.invoke(nGrinderStartClass));
	}
}

class TmpNGrinderStarter extends NGrinderStarter {
	@Override
	protected void printHelpAndExit(String message) {
	}

	@Override
	protected void printHelpAndExit(String message, Exception e) {
	}
}
