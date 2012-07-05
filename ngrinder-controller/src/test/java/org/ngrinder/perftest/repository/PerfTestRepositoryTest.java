package org.ngrinder.perftest.repository;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.ngrinder.NGrinderIocTransactionalTestBase;
import org.ngrinder.perftest.model.PerfTest;
import org.ngrinder.perftest.model.Status;
import org.springframework.beans.factory.annotation.Autowired;

public class PerfTestRepositoryTest extends NGrinderIocTransactionalTestBase {

	@Autowired
	public PerfTestRepository repo;

	@Before
	public void before() {
		PerfTest entity = new PerfTest();
		entity.setStatus(Status.FINISHED);
		repo.save(entity);
		PerfTest entity2 = new PerfTest();
		entity2.setStatus(Status.CANCELED);
		repo.save(entity2);
		PerfTest entity3 = new PerfTest();
		entity3.setStatus(Status.READY);
		repo.save(entity3);
	}

	@Test
	public void testPerfTest() { 
		List<PerfTest> findAll = repo.findAll(PerfTest.statusSetEqual(Status.FINISHED, Status.CANCELED));
		assertThat(findAll.size(), is(2));
	}
}
