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

import net.grinder.SingleConsole;
import net.grinder.console.model.SampleModelImplementationEx;
import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Test;
import org.ngrinder.common.constant.ControllerConstants;
import org.ngrinder.common.util.CompressionUtils;
import org.ngrinder.model.PerfTest;
import org.ngrinder.model.Status;
import org.ngrinder.perftest.controller.PerfTestController;
import org.ngrinder.script.model.FileEntry;
import org.ngrinder.script.repository.MockFileEntityRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;

import java.io.File;
import java.io.IOException;
import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;

public class PerfTestCancellationTest extends AbstractAgentReadyTest implements ControllerConstants {

	@Autowired
	private MockPerfTestRunnableForCancellation perfTestRunnable;

	@Autowired
	public MockFileEntityRepository fileEntityRepository;

	@Autowired
	public ConsoleManager consoleManager;

	@Autowired
	public PerfTestController perfTestController;

	private PerfTest perfTest = null;

	@Before
	public void before() throws IOException {
		File tempRepo = new File(System.getProperty("java.io.tmpdir"), "repo");
		fileEntityRepository.setUserRepository(new File(tempRepo, getTestUser().getUserId()));
		File testUserRoot = fileEntityRepository.getUserRepoDirectory(getTestUser()).getParentFile();

		testUserRoot.mkdirs();
		CompressionUtils.unzip(new ClassPathResource("TEST_USER.zip").getFile(), testUserRoot);
		testUserRoot.deleteOnExit();

		FileEntry fileEntry = new FileEntry();
		fileEntry.setPath("test1.py");
		String worldString = IOUtils.toString(new ClassPathResource("world.py").getInputStream());
		if (fileEntry.getFileType().isEditable()) {
			fileEntry.setContent(worldString);
		} else {
			fileEntry.setContentBytes(worldString.getBytes());
		}
		fileEntityRepository.save(getTestUser(), fileEntry, "UTF-8");

		clearAllPerfTest();
		perfTest = createPerfTest("test1", Status.READY, null);
		List<PerfTest> allPerfTest = perfTestService.getAllPerfTest();
		assertThat(allPerfTest.size(), is(1));
		assertThat(consoleManager.getConsoleInUse().size(), is(0));

	}

	@Test
	public void testTestCancellationDuringPreparation() throws IOException {
		doCancel(1);
	}

	@Test
	public void testTestCancellationDuringPreparation2() throws IOException {
		doCancel(2);
	}

	private void doCancel(int substep) {
		// When the stop perftest is requested
		perfTestRunnable.setRunnable(new Runnable() {
			@Override
			public void run() {
				SingleConsole singleConsole = consoleManager.getConsoleInUse().get(0);
				SampleModelImplementationEx sampleModelMock = mock(SampleModelImplementationEx.class);
				singleConsole.setSampleModel(sampleModelMock);
				assertThat(singleConsole, notNullValue());
				perfTestController.stop(getTestUser(), String.valueOf(perfTest.getId()));
			}
		}, substep);
		perfTestRunnable.doStart();
		List<PerfTest> allPerfTest = perfTestService.getAllPerfTest();
		perfTestRunnable.doFinish(false);
		// Then
		assertThat(allPerfTest.get(0).getStatus(), is(Status.CANCELED));
		assertThat(consoleManager.getConsoleInUse().size(), is(0));
	}

	@Test
	public void testTestCancellationDuringExecutionPhase() throws IOException {
		// Given the testing perftest
		perfTest = createPerfTest("test1", Status.TESTING, null);
		// When the stop is requested
		perfTestController.stop(getTestUser(), String.valueOf(perfTest.getId()));
		perfTestRunnable.doFinish();
		// Then it should be canceled.
		assertThat(perfTestService.getOne(perfTest.getId()).getStatus(), is(Status.CANCELED));
		assertThat(consoleManager.getConsoleInUse().size(), is(0));

	}

}
