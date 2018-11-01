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
package org.ngrinder.script.service;

import net.grinder.util.thread.Condition;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.ngrinder.AbstractNGrinderTransactionalTest;
import org.ngrinder.common.util.CompressionUtils;
import org.ngrinder.infra.init.ClassPathInit;
import org.ngrinder.infra.init.DBInit;
import org.ngrinder.script.model.FileEntry;
import org.ngrinder.script.repository.MockFileEntityRepository;
import org.ngrinder.service.IUserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;

import java.io.File;
import java.io.IOException;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;
import static org.ngrinder.common.constants.GrinderConstants.GRINDER_SECURITY_LEVEL_NORMAL;

public class ScriptValidationServiceTest extends AbstractNGrinderTransactionalTest {
	private static final Logger m_logger = LoggerFactory.getLogger(ScriptValidationServiceTest.class);

	@Autowired
	private MockLocalScriptTestDriveService validationService;

	@Autowired
	private MockScriptValidationService scriptValidationService;

	@Autowired
	private FileEntryService fileEntryService;

	@Autowired
	public MockFileEntityRepository repo;

	@Autowired
	public IUserService userService;

	@Autowired
	public DBInit dbinit;

	@Autowired
	public ClassPathInit classPathInit;

	public File repoDir;

	@Before
	public void before() throws IOException {
		repoDir = new File(System.getProperty("java.io.tmpdir"), "repo");
		FileUtils.deleteQuietly(repoDir);
		CompressionUtils.unzip(new ClassPathResource("TEST_USER.zip").getFile(), repoDir);
		repo.setUserRepository(new File(repoDir, getTestUser().getUserId()));
	}

	@After
	public void after() {
		FileUtils.deleteQuietly(repoDir);
	}

	@Test
	public void testValidation() throws IOException {
		File file = new ClassPathResource("/validation/script_1time.py").getFile();
		Condition m_eventSync = new Condition();
		File log = validationService.doValidate(file.getParentFile(), file, m_eventSync, false, GRINDER_SECURITY_LEVEL_NORMAL, "");
		assertThat(log.length(), greaterThan(1000L));
	}


	@Test(timeout = 30000)
	public void testInfiniteScriptValidation() throws IOException {
		String script = IOUtils.toString(new ClassPathResource("/validation/script_loop.py").getInputStream());
		FileEntry fileEntry = new FileEntry();
		fileEntry.setPath("/script.py");
		fileEntry.setContent(script);
		scriptValidationService.validate(getTestUser(), fileEntry, false, "");
	}

	@Test
	public void testNormalScriptValidation() throws IOException {
		String script = IOUtils.toString(new ClassPathResource("/validation/script_1time.py").getInputStream());
		FileEntry fileEntry = new FileEntry();
		fileEntry.setPath("/script2.py");
		fileEntry.setContent(script);
		String validateScript = scriptValidationService.validate(getTestUser(), fileEntry, false, "");
		assertThat(validateScript, not(containsString("Validation should be performed within")));
		assertThat(validateScript.length(), lessThan(10000));
	}

	@Test
	public void testScriptValidationWithSvnScript() throws IOException {
		String script = IOUtils.toString(new ClassPathResource("/validation/script_1time.py").getInputStream());
		FileEntry fileEntry = new FileEntry();
		fileEntry.setPath("/script2.py");
		fileEntry.setContent(script);
		fileEntryService.save(getTestUser(), fileEntry);
		fileEntry.setContent("");
		String validateScript = scriptValidationService.validate(getTestUser(), fileEntry, true, "");
		assertThat(validateScript, not(containsString("Validation should be performed")));
		assertThat(validateScript.length(), lessThan(10000));
	}
}
