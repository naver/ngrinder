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

import org.junit.Test;
import org.ngrinder.common.util.ThreadUtils;
import org.ngrinder.infra.AgentConfig;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public class NGrinderStarterTest {

	private NGrinderStarter starter = new MockNGrinderStarter();

	@Test
	public void testJavaVersionCheck() {
		NGrinderStarter.checkJavaVersion("1.6");
	}

	@Test
	public void testGetStartModeAndVersion() {
		String startMode = starter.getStartMode();
		assertTrue(startMode.equals("monitor") || startMode.equals("agent"));

		String version = starter.getVersion();
		assertThat(version, containsString("3."));
	}

	@Test
	public void testStartAgent() {
		starter.startAgent("127.0.0.1"); // there is no agent properties, it can
		// be started with default setting
		ThreadUtils.sleep(2000);
		starter.stopAgent();
	}

	@Test
	public void testStartMonitor() {
		starter.startMonitor();
		ThreadUtils.sleep(2000);
		starter.stopMonitor();
		starter.stopMonitor();
	}

	@Test
	public void testIsValidCurrentDirectory() {
		assertThat(starter.isValidCurrentDirectory(), is(false));
	}
}

class MockNGrinderStarter extends NGrinderStarter {

	@Override
	protected void checkRunningDirectory() {

	}

	@Override
	protected AgentConfig createAgentConfig() {
		return new AgentConfig.NullAgentConfig(1).init();
	}

	@Override
	protected void printHelpAndExit(String message) {
	}

	@Override
	protected void printHelpAndExit(String message, Exception e) {
	}
}
