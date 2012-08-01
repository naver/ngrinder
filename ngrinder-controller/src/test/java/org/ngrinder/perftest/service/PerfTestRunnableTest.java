package org.ngrinder.perftest.service;

import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;

import java.util.Date;

import net.grinder.SingleConsole;

import org.junit.Before;
import org.junit.Test;
import org.ngrinder.perftest.model.PerfTest;
import org.ngrinder.perftest.model.Status;
import org.springframework.beans.factory.annotation.Autowired;

public class PerfTestRunnableTest extends AbstractPerfTestTransactionalTest {

	@Autowired
	private MockPerfTestRunnable perfTestRunnable;

	@Before
	public void before() {
		createPerfTest("test1", Status.READY, new Date());
		createPerfTest("test2", Status.READY, new Date());
	}

	@Test
	public void testDoTest() {
		perfTestRunnable.startTest();
	}

	@Test
	public void testStartConsole() {
		PerfTest perfTestCandiate = perfTestService.getPerfTestCandiate();
		assertThat(perfTestCandiate, not(nullValue()));
		SingleConsole startConsole = perfTestRunnable.startConsole(perfTestCandiate);
		assertThat(startConsole, not(nullValue()));
	}
}
