package org.ngrinder.perftest.service;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.lessThan;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.List;

import net.grinder.SingleConsole;
import net.grinder.util.ConsolePropertiesFactory;

import org.junit.Test;
import org.ngrinder.AbstractNGrinderTransactionalTest;
import org.ngrinder.common.exception.NGrinderRuntimeException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StopWatch;

public class ConsoleManagerTest extends AbstractNGrinderTransactionalTest {
	@Autowired
	private MockConsoleManager manager;

	@Test
	public void testConsoleManager() {
		int initialSize = manager.getAvailableConsoleSize();
		SingleConsole availableConsole = manager.getAvailableConsole(ConsolePropertiesFactory
				.createEmptyConsoleProperties());
		assertThat(manager.getAvailableConsoleSize(), is(initialSize - 1));
		manager.returnBackConsole(availableConsole);
		assertThat(manager.getAvailableConsoleSize(), is(initialSize));
	}

	@Test
	public void testConsoleManagerWhenExceedingLimit() {

		// Get all console
		int initialSize = manager.getAvailableConsoleSize();
		SingleConsole availableConsole = null;
		for (int i = 1; i <= initialSize; i++) {
			availableConsole = manager.getAvailableConsole(ConsolePropertiesFactory.createEmptyConsoleProperties());
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
					manager.returnBackConsole(lastConsole);
				} catch (InterruptedException e) {
				}

			}
		});
		elapseTime = new StopWatch();
		elapseTime.start();
		thread.start();
		// Try to get more console, it will return console just after console is
		// returned back
		SingleConsole anotherConsole = manager.getAvailableConsole(ConsolePropertiesFactory
				.createEmptyConsoleProperties());
		elapseTime.stop();
		assertThat(elapseTime.getTotalTimeSeconds(), lessThan(3000D));
		assertThat(manager.getAvailableConsoleSize(), is(0));
		manager.returnBackConsole(anotherConsole);

		// return console again is always allowed
		manager.returnBackConsole(anotherConsole);

		assertThat(manager.getAvailableConsoleSize(), is(1));
		assertThat(manager.getConsoleInUse().size(), is(initialSize - 1));
	}

	@Test
	public void testAvailableSocket() throws IOException {
		ServerSocket serverSocket = null;
		try {
			// When port is already used
			serverSocket = new ServerSocket(10111);
			int localPort = serverSocket.getLocalPort();

			// It should be excluded in available ports
			List<Integer> availablePorts = manager.getAvailablePorts(20, 10110);
			assertThat(availablePorts.contains(localPort), not(true));
			assertThat(availablePorts, hasSize(20));
		} finally {
			if (serverSocket != null) {
				serverSocket.close();
			}
		}
	}
}
