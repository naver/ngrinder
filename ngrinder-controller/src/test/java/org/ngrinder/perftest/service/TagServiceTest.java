/*
 * Copyright (C) 2012 - 2012 NHN Corporation
 * All rights reserved.
 *
 * This file is part of The nGrinder software distribution. Refer to
 * the file LICENSE which is part of The nGrinder distribution for
 * licensing details. The nGrinder distribution is available on the
 * Internet at http://nhnopensource.org/ngrinder
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS
 * FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE
 * COPYRIGHT HOLDERS OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
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
 * {@link TagService} test
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
	}
	
	@Test
	public void testPerfTestTagging() {
		PerfTest newPerfTest = newPerfTest("hello", Status.SAVED, new Date());
		newPerfTest.setTags(tagService.addTags(getTestUser(), new String[]{"HELLO", "world"}));
		createPerfTest(newPerfTest);
		newPerfTest.setTags(tagService.addTags(getTestUser(), new String[]{"HELLO", "WORLD"}));
		PerfTest createPerfTest = createPerfTest(newPerfTest);
		PerfTest perfTestWithTag = perfTestService.getPerfTestWithTag(createPerfTest.getId());
		assertThat(tagRepository.count(hasPerfTest()), is(1L));
		assertThat(perfTestWithTag.getTags().size(), is(2));
	}

}
