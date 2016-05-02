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
package org.ngrinder.perftest.controller;

import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.Test;
import org.ngrinder.common.util.CompressionUtils;
import org.ngrinder.perftest.service.AbstractAgentReadyTest;
import org.ngrinder.script.repository.MockFileEntityRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.ui.ModelMap;
import org.tmatesoft.svn.core.wc.SVNRevision;

/**
 * {@link PerfTestController} test with repository supports.
 * 
 * @author mavlarn
 * @Since 3.0
 */
public class PerfTestControllerWithRepoTest extends AbstractAgentReadyTest {

	@Autowired
	private MockPerfTestController controller;

	@Autowired
	public MockFileEntityRepository repo;

	/**
	 * Locate dumped user1 repo into tempdir.
	 * 
	 * @throws IOException
	 */
	@Before
	public void before() throws IOException {
		File file = new File(System.getProperty("java.io.tmpdir"), "repo");
		FileUtils.deleteQuietly(file);
		CompressionUtils.unzip(new ClassPathResource("TEST_USER.zip").getFile(), file);
		repo.setUserRepository(new File(file, getTestUser().getUserId()));
	}

	@Test
	public void testGetQuickStart() {
		ModelMap model = new ModelMap();
		controller.getQuickStart(getTestUser(), "http://naver.com", "jython", model);
		assertThat(repo.findOne(getTestUser(), "naver.com/TestRunner.py", SVNRevision.HEAD), notNullValue());
	}

}
