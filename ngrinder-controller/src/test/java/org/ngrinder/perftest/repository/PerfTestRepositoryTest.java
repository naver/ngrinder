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
package org.ngrinder.perftest.repository;

import org.junit.Before;
import org.junit.Test;
import org.ngrinder.AbstractNGrinderTransactionalTest;
import org.ngrinder.model.PerfTest;
import org.ngrinder.model.Status;
import org.ngrinder.model.Tag;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Optional;
import java.util.SortedSet;
import java.util.TreeSet;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.fail;
import static org.ngrinder.perftest.repository.PerfTestSpecification.idEqual;

public class PerfTestRepositoryTest extends AbstractNGrinderTransactionalTest {

	@Autowired
	public PerfTestRepository perfTestRepository;

	@Autowired
	private TagRepository tagRepository;

	@Before
	public void before() {
		List<PerfTest> findAll = perfTestRepository.findAll();
		for (PerfTest perfTest : findAll) {
			perfTest.getTags().clear();
		}
		perfTestRepository.saveAll(findAll);
		perfTestRepository.flush();
		perfTestRepository.deleteAll();
		perfTestRepository.flush();
		tagRepository.deleteAll();
		tagRepository.flush();
	}

	@Test
	public void testPerfTestSearchBasedOnStatus() {
		// Given 3 tests with different status
		PerfTest entity = new PerfTest();
		entity.setStatus(Status.FINISHED);
		perfTestRepository.save(entity);
		PerfTest entity2 = new PerfTest();
		entity2.setStatus(Status.CANCELED);
		perfTestRepository.save(entity2);
		PerfTest entity3 = new PerfTest();
		entity3.setStatus(Status.READY);
		perfTestRepository.save(entity3);

		// Then all should be 3
		assertThat(perfTestRepository.findAll().size(), is(3));
		// Then finished and canceled perftest should 2
		assertThat(perfTestRepository.findAll(PerfTestSpecification.statusSetEqual(Status.FINISHED, Status.CANCELED))
						.size(), is(2));
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
		entity = perfTestRepository.save(entity);
		Optional<PerfTest> findOne = perfTestRepository.findOne(idEqual(entity.getId()));
		if (!findOne.isPresent()) {
			fail();
		}
		PerfTest perfTest = findOne.get();
		SortedSet<Tag> tags = perfTest.getTags();
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
		entity = perfTestRepository.save(entity);
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
		perfTestRepository.save(entity2);
		assertThat(tagRepository.findAll().size(), is(3));
		assertThat(perfTestRepository.findAll(PerfTestSpecification.hasTag("world")).size(), is(1));
		assertThat(perfTestRepository.findAll(PerfTestSpecification.hasTag("hello")).size(), is(2));
		assertThat(tagRepository.findAll().size(), is(3));
	}
}
