package org.ngrinder.perftest.service;

import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;
import net.grinder.SingleConsole;

import org.junit.Before;
import org.junit.Test;
import org.ngrinder.AbstractNGNinderTransactionalTest;
import org.ngrinder.perftest.model.PerfTest;
import org.springframework.beans.factory.annotation.Autowired;

public class PerfTestRunnableTest extends AbstractNGNinderTransactionalTest {

	@Autowired
	private MockPerfTestRunnable perfTestRunnable;

	@Autowired
	private PerfTestService perfTestService;

	@Before
	public void before() {
		PerfTest perfTest = new PerfTest();
	}

	@Test
	public void testDoTest() {
		perfTestRunnable.startTest();
	}

	@Test
	public void startConsoleTest() {
		PerfTest perfTestCandiate = perfTestService.getPerfTestCandiate();
		assertThat(perfTestCandiate, not(nullValue()));
		SingleConsole startConsole = perfTestRunnable.startConsole(perfTestCandiate);
		assertThat(startConsole, not(nullValue()));
	}
}
