package org.ngrinder.perftest.repository;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import org.junit.Before;
import org.junit.Test;
import org.ngrinder.AbstractNGrinderTransactionalTest;
import org.ngrinder.model.PerfTest;
import org.ngrinder.model.Status;
import org.ngrinder.model.Tag;
import org.springframework.beans.factory.annotation.Autowired;

public class PerfTestRepositoryTest extends AbstractNGrinderTransactionalTest {

	@Autowired
	public PerfTestRepository repo;

	@Autowired
	private TagRepository tagRepository;

	
	@Before
	public void before() {
		repo.deleteAll();
		tagRepository.deleteAll();
	}

	@Test
	public void testPerfTestSearchBasedOnStatus() {
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

	@SuppressWarnings("serial")
	@Test
	public void testPerfTestTag() {
		PerfTest entity = new PerfTest();
		entity.setTestName("test1");
		entity.setTags(new TreeSet<Tag>() {
			{
				add(new Tag("hello"));
				add(new Tag("world"));
			}
		});
		entity = repo.save(entity);
		PerfTest findOne = repo.findOne(entity.getId());
		SortedSet<Tag> tags = findOne.getTags();
		assertThat(tags.first(), is(new Tag("hello")));
		assertThat(tags.last(), is(new Tag("world")));
	}

	
	@SuppressWarnings("serial")
	@Test
	public void testPerfTestTag2() {
		final Tag hello = tagRepository.save(new Tag("hello"));
		final Tag world = tagRepository.save(new Tag("world"));
		final Tag world2 = tagRepository.save(new Tag("world2"));

		PerfTest entity = new PerfTest();
		entity.setTestName("test1");
		entity.setTags(new TreeSet<Tag>() {
			{
				add(hello);
				add(world);
			}
		});
		entity = repo.save(entity);
		SortedSet<Tag> tags2 = entity.getTags();
		assertThat(tags2.first(), is(hello));
		assertThat(tags2.last(), is(world));
		
		PerfTest entity2 = new PerfTest();
		entity2.setTestName("test1");
		entity2.setTags(new TreeSet<Tag>() {
			{
				add(hello);
				add(world2);
			}
		});
		repo.save(entity2);
		assertThat(tagRepository.findAll().size(), is(3));
		assertThat(repo.findAll(PerfTestSpecification.hasTag("world")).size(), is(1));
		assertThat(repo.findAll(PerfTestSpecification.hasTag("hello")).size(), is(2));
		
	
		List<Tag> findAll = tagRepository.findAll();
	
		for (Tag tag : findAll) {
			System.out.println(tag.getId() + " - " + tag.getPerfTests());
		}
		assertThat(findAll.size(), is(3));
	}
}
