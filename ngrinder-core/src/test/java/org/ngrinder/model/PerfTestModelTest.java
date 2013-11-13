package org.ngrinder.model;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertThat;

/**
 * PerfTest test
 */
public class PerfTestModelTest {

	@Test
	public void testPerfTestMerging() {
		PerfTest source = new PerfTest();
		source.setStatus(Status.SAVED);
		PerfTest target = new PerfTest();
		target.setAgentCount(10);
		target.merge(source);
		assertThat(target.getStatus(), is(Status.SAVED));
		assertThat(target.getAgentCount(), is(10));
	}


	@Test
	public void testPerfTestCloning() {
		PerfTest source = new PerfTest();
		source.setStatus(Status.SAVED);
		source.setAgentCount(10);
		PerfTest target = new PerfTest();
		source.cloneTo(target);
		// Not cloneable field
		assertThat(target.getStatus(), not(Status.SAVED));
		// cloneable field
		assertThat(target.getAgentCount(), is(10));
	}
}
