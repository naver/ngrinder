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
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;

import java.io.File;
import java.util.Date;
import java.util.List;

import net.grinder.StopReason;
import net.grinder.common.GrinderProperties;
import net.grinder.console.model.ConsoleProperties;

import org.junit.Before;
import org.junit.Test;
import org.ngrinder.model.PerfTest;
import org.ngrinder.model.Status;
import org.ngrinder.perftest.repository.PerfTestRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

/**
 * {@link PerfTestService} test.
 * 
 * @author Mavlarn
 * @since 3.0
 */
public class PerfTestServiceTest extends AbstractPerfTestTransactionalTest {

	@Autowired
	private PerfTestService testService;

	@Autowired
	PerfTestRepository perfTestRepository;

	@Before
	public void clearPerfTest() {
		clearAllPerfTest();
	}

	@Test
	public void testGetTestListAll() {

		createPerfTest("new Test1", Status.TESTING, new Date());
		createPerfTest("new Test2", Status.FINISHED, new Date());

		PerfTest candiate = testService.getPerfTestCandiate();
		assertThat(candiate, nullValue());

		Pageable pageable = new PageRequest(0, 10);
		Page<PerfTest> testList = testService.getPerfTestList(getTestUser(), null, null, null, pageable);
		assertThat(testList.getContent().size(), is(2));
		testList = testService.getPerfTestList(getTestUser(), null, null, "F", pageable);
		assertThat(testList.getContent().size(), is(1));

		// test with no paging
		testList = testService.getPerfTestList(getTestUser(), null, null, null, null);
		assertThat(testList.getContent().size(), is(2));
		testList = testService.getPerfTestList(getTestUser(), null, null, "F", null);
		assertThat(testList.getContent().size(), is(1));

		List<PerfTest> list = testService.getTestingPerfTest();
		assertThat(list.size(), is(1));

		for (PerfTest test : list) {
			long systemTimeMills = System.currentTimeMillis();
			test.setStartTime(new Date(systemTimeMills));

			PerfTest testTemp = testService.getPerfTest(getTestUser(), test.getId());
			assertThat(testTemp.getId(), is(test.getId()));
			assertThat(testTemp.getStartTime().getTime(), is(systemTimeMills));

			testService.markAbromalTermination(testTemp, StopReason.CANCEL_BY_USER);
			testService.markProgress(testTemp, "this test will be TESTING again");
			testService.markStatusAndProgress(testTemp, Status.TESTING, "this is just test unit");

			List<PerfTest> testingList = testService.getPerfTest(getTestUser(), Status.TESTING);
			assertThat(testingList.size(), is(1));

			Long testCount = testService.getPerfTestCount(getTestUser(), Status.TESTING);
			assertThat(testCount, is(1L));

			GrinderProperties properties = testService.getGrinderProperties(test);
			assertThat(properties, not(nullValue()));

		}

		createPerfTest("new Test2", Status.getProcessingOrTestingTestStatus()[0], new Date());
		list = testService.getCurrentlyRunningTest();
		assertThat(list.size(), is(2));

		PerfTest finishedTest = createPerfTest("new Test3", Status.ABNORMAL_TESTING, new Date());
		finishedTest.setPort(0); // need port number for finishing
		list = testService.getAbnoramlTestingPerfTest();
		assertThat(list.size(), is(1));

		testService.updatePerfTestAfterTestFinish(finishedTest);

		createPerfTest("new Test3", Status.START_AGENTS, new Date());

		List<PerfTest> errorList = testService.getPerfTest(getTestUser(), Status.START_AGENTS);
		assertThat(errorList.size(), is(1));
		testService.markAbromalTermination(errorList.get(0), "this is error test");
	}

	@Test
	public void testTestScriptAll() {
		int maxConcurrent = testService.getMaximumConcurrentTestCount();
		assertThat(maxConcurrent, is(10));

		PerfTest testScript = createPerfTest("new TestScript", Status.READY, new Date());
		testService.addCommentOn(getTestUser(), testScript.getId(), "this is TestScript method", "");

		PerfTest testing = testService.markProgressAndStatus(testScript, Status.TESTING, "It is testing from ready");
		assertThat(testing.getStatus(), is(Status.TESTING));

		File testPath = testService.getPerfTestDistributionPath(testScript);
		assertThat(testPath, not(nullValue()));

		List<String> fileList = testService.getLogFiles(testScript.getId());
		assertThat(fileList, not(nullValue()));

		File scriptFile = testService.getLogFile(testScript.getId(), testScript.getScriptName());
		assertThat(scriptFile, not(nullValue()));

		ConsoleProperties consoleProperties = testService.createConsoleProperties(testScript);
		assertThat(consoleProperties, not(nullValue()));

	}

}
