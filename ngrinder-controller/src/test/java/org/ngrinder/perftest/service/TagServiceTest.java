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

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.ngrinder.perftest.repository.TagSpecification.hasPerfTest;

import java.util.Date;
import java.util.List;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.ngrinder.model.PerfTest;
import org.ngrinder.model.Status;
import org.ngrinder.model.Tag;
import org.ngrinder.perftest.repository.PerfTestRepository;
import org.ngrinder.perftest.repository.TagRepository;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * {@link TagService} test.
 *
 * @author JunHo Yoon
 * @since 3.0
 */
public class TagServiceTest extends AbstractPerfTestTransactionalTest {

	@Autowired
	private TagService tagService;

	@Autowired
	private TagRepository tagRepository;

	@Autowired
	private PerfTestService perfTestService;

	@Autowired
	private PerfTestRepository perfTestRepository;

	@Before
	public void beforeTag() {
		List<PerfTest> findAll = perfTestRepository.findAll();
		for (PerfTest perfTest : findAll) {
			perfTest.getTags().clear();
		}
		perfTestRepository.save(findAll);
		perfTestRepository.flush();
		perfTestRepository.deleteAll();
		tagRepository.deleteAll();
	}

	@Test
	public void testTagService() {
		Tag entity = new Tag("HELLO");
		entity.setLastModifiedUser(getTestUser());
		entity.setCreatedUser(getTestUser());
		tagRepository.save(entity);
		Set<Tag> addTags = tagService.addTags(getTestUser(), new String[]{"HELLO", "WORLD"});
		assertThat(addTags.size(), is(2));
		assertThat(tagRepository.findAll().size(), is(2));

		addTags = tagService.addTags(getTestUser(), new String[]{"HELLO", "world"});
		assertThat(addTags.size(), is(2));
		assertThat(tagRepository.findAll().size(), is(3));

		addTags = tagService.addTags(getTestUser(), new String[]{});
		assertThat(tagRepository.findAll().size(), is(3));


		tagService.deleteTags(getTestUser());
		assertThat(tagRepository.findAll().size(), is(0));
	}

	@Test
	public void testTagging() {
		PerfTest newPerfTest = newPerfTest("hello", Status.SAVED, new Date());
		newPerfTest.setTagString("HELLO,world");
		createPerfTest(newPerfTest);
		newPerfTest.setTagString("HELLO,WORLD");
		PerfTest updated = createPerfTest(newPerfTest);
		PerfTest perfTestWithTag = perfTestService.getOneWithTag(updated.getId());
		List<Tag> listTags = tagService.getAllTags(getTestUser(), "H");
		assertThat(listTags.size(), is(1));
		assertThat(tagRepository.count(hasPerfTest()), is(2L));
		assertThat(perfTestWithTag.getTags().size(), is(2));
	}

}
