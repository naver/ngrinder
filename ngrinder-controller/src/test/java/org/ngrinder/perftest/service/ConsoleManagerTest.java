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
package org.ngrinder.perftest.service;

import net.grinder.SingleConsole;
import net.grinder.util.ConsolePropertiesFactory;
import net.grinder.util.NetworkUtils;
import org.junit.Ignore;
import org.junit.Test;
import org.ngrinder.common.exception.NGrinderRuntimeException;
import org.ngrinder.common.util.ThreadUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StopWatch;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

public class ConsoleManagerTest extends AbstractAgentReadyTest {
	@Autowired
	private MockConsoleManager manager;

	@Test
	@Ignore
	public void testConsoleManager() {
		int initialSize = manager.getAvailableConsoleSize();
		SingleConsole availableConsole = manager.getAvailableConsole(
				ConsolePropertiesFactory.createEmptyConsoleProperties());
		assertThat(manager.getAvailableConsoleSize(), is(initialSize - 1));
		manager.returnBackConsole("test", availableConsole);
		sleep(1000);
		assertThat(manager.getAvailableConsoleSize(), is(initialSize));
	}

	@Test
	public void testConsoleManagerWhenExceedingLimit() {
		// Get all console
		int initialSize = manager.getAvailableConsoleSize();
		SingleConsole availableConsole = null;
		for (int i = 1; i <= initialSize; i++) {
			availableConsole = manager.getAvailableConsole(
					ConsolePropertiesFactory.createEmptyConsoleProperties());
		}
		final SingleConsole lastConsole = availableConsole;
		assertThat(manager.getAvailableConsoleSize(), is(0));
		StopWatch elapseTime = new StopWatch();
		elapseTime.start();
		// Try to get more console, it will take time
		try {
			manager.getAvailableConsole(ConsolePropertiesFactory.createEmptyConsoleProperties());
			fail("should throw Exception");
		} catch (NGrinderRuntimeException e) {
		}
		elapseTime.stop();
		assertThat(elapseTime.getTotalTimeSeconds(), lessThan(3000D));
		// Let's try the case when console is returned back.
		Thread thread = new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					Thread.sleep(1000);
					manager.returnBackConsole("test", lastConsole);
				} catch (InterruptedException e) {
				}

			}
		});
		elapseTime = new StopWatch();
		elapseTime.start();
		thread.start();
		// Try to get more console, it will return console just after console is
		// returned back
		SingleConsole anotherConsole = manager.getAvailableConsole(
				ConsolePropertiesFactory.createEmptyConsoleProperties());
		elapseTime.stop();
		assertThat(elapseTime.getTotalTimeSeconds(), lessThan(3000D));
		assertThat(manager.getAvailableConsoleSize(), is(0));
		manager.returnBackConsole("test", anotherConsole);

		// return console again is always allowed
		manager.returnBackConsole("test", anotherConsole);
		ThreadUtils.sleep(2000);
		assertThat(manager.getAvailableConsoleSize(), is(1));
		assertThat(manager.getConsoleInUse().size(), is(initialSize - 1));
	}

	@Test
	public void testAvailableSocket() throws IOException {
		ServerSocket serverSocket = null;
		try {
			// When port is already used
			serverSocket = new ServerSocket(20111);
			int localPort = serverSocket.getLocalPort();

			// It should be excluded in available ports
			List<Integer> availablePorts = NetworkUtils.getAvailablePorts("", 20, 20111, 40000);
			assertThat(availablePorts.contains(localPort), not(true));
			assertThat(availablePorts.size(), is(20));
		} finally {
			if (serverSocket != null) {
				serverSocket.close();
			}
		}
	}
}
