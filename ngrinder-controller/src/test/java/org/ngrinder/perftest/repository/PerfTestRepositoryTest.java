package org.ngrinder.perftest.repository;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;
import org.ngrinder.AbstractNGNinderTransactionalTest;
import org.ngrinder.perftest.model.PerfTest;
import org.ngrinder.perftest.model.Status;
import org.springframework.beans.factory.annotation.Autowired;

public class PerfTestRepositoryTest extends AbstractNGNinderTransactionalTest {

	@Autowired
	public PerfTestRepository repo;

	@Test
	public void testPerfTest() {
		// Given 3 tests with different status
		PerfTest entity = new PerfTest();
		entity.setStatus(Status.FINISHED);
		repo.save(entity);
		PerfTest entity2 = new PerfTest();
		entity2.setStatus(Status.CANCELED);
		repo.save(entity2);
		PerfTest entity3 = new PerfTest();
		entity3.setStatus(Status.READY);
		repo.save(entity3);

		// Then all should be 3
		assertThat(repo.findAll().size(), is(3));
		// Then finished and canceled perftest should 2
		assertThat(repo.findAll(PerfTestSpecification.statusSetEqual(Status.FINISHED, Status.CANCELED)).size(), is(2));
	}
}
