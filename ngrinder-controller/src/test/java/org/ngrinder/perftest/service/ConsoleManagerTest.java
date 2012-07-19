package org.ngrinder.perftest.service;

import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.lessThan;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;
import net.grinder.SingleConsole;

import org.junit.Test;
import org.ngrinder.AbstractNGNinderTransactionalTest;
import org.ngrinder.common.exception.NGrinderRuntimeException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StopWatch;

public class ConsoleManagerTest extends AbstractNGNinderTransactionalTest {
	@Autowired
	private ConsoleManager manager;

	@Test
	public void testConsoleManager() {
		ConsoleManager manager = new ConsoleManager();
		manager.init();
		int initialSize = manager.getAvailableConsoleSize();
		SingleConsole availableConsole = manager.getAvailableConsole();
		assertThat(manager.getAvailableConsoleSize(), is(initialSize - 1));
		manager.returnBackConsole(availableConsole);
		assertThat(manager.getAvailableConsoleSize(), is(initialSize));
	}

	@Test
	public void testConsoleManagerWhenExceedingLimit() {
		manager = spy(manager);
		when(manager.getMaxWaitingMiliSecond()).thenReturn(3L);

		manager.init();
		// Get all console
		int initialSize = manager.getAvailableConsoleSize();
		SingleConsole availableConsole = null;
		for (int i = 1; i <= initialSize; i++) {
			availableConsole = manager.getAvailableConsole();
		}
		final SingleConsole lastConsole = availableConsole;
		assertThat(manager.getAvailableConsoleSize(), is(0));
		StopWatch elapseTime = new StopWatch();
		elapseTime.start();
		// Try to get more console, it will take time
		try {
			manager.getAvailableConsole();
		} catch (NGrinderRuntimeException e) {
		}
		elapseTime.stop();
		assertThat(elapseTime.getTotalTimeSeconds(), greaterThanOrEqualTo(3D));

		// Let's try the case when console is returned back.
		Thread thread = new Thread(new Runnable() {
			@Override
			public void run() {
				sleep(1000);
				manager.returnBackConsole(lastConsole);
			}
		});
		elapseTime = new StopWatch();
		elapseTime.start();
		thread.start();
		// Try to get more console, it will return console just after console is
		// returned back
		manager.getAvailableConsole();
		elapseTime.stop();
		assertThat(elapseTime.getTotalTimeSeconds(), lessThan(2D));
		assertThat(manager.getAvailableConsoleSize(), is(0));
		manager.returnBackConsole(lastConsole);

		// return console again is always allowed
		manager.returnBackConsole(lastConsole);

		assertThat(manager.getAvailableConsoleSize(), is(1));
		assertThat(manager.getConsoleInUse(), hasSize(initialSize - 1));
	}
}
