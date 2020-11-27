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

import net.grinder.StopReason;
import net.grinder.common.GrinderProperties;
import net.grinder.console.model.ConsoleProperties;
import org.junit.Before;
import org.junit.Test;
import org.ngrinder.common.model.Home;
import org.ngrinder.common.util.ThreadUtils;
import org.ngrinder.infra.config.Config;
import org.ngrinder.model.PerfTest;
import org.ngrinder.model.Status;
import org.ngrinder.monitor.controller.model.SystemDataModel;
import org.ngrinder.monitor.share.domain.SystemInfo;
import org.ngrinder.perftest.repository.PerfTestRepository;
import org.ngrinder.perftest.service.monitor.MonitorClientService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.io.File;
import java.io.IOException;
import java.time.Instant;
import java.util.*;
import java.util.function.Predicate;

import static java.time.Instant.now;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertFalse;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;
import static org.springframework.data.domain.Pageable.unpaged;
import static org.springframework.test.util.ReflectionTestUtils.setField;

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
		createPerfTest("new Test1", Status.TESTING, now());
		createPerfTest("new Test2", Status.FINISHED, now());

		PerfTest candidate = testService.getNextRunnablePerfTestPerfTestCandidate();
		assertThat(candidate, nullValue());

		Pageable pageable = PageRequest.of(0, 10);
		Page<PerfTest> testList = testService.getPagedAll(getTestUser(), null, null, null, pageable);
		assertThat(testList.getContent().size(), is(2));
		testList = testService.getPagedAll(getTestUser(), null, null, "F", pageable);
		assertThat(testList.getContent().size(), is(1));

		// test with no paging
		testList = testService.getPagedAll(getTestUser(), null, null, null, unpaged());
		assertThat(testList.getContent().size(), is(2));
		testList = testService.getPagedAll(getTestUser(), null, null, "F", unpaged());
		assertThat(testList.getContent().size(), is(1));

		List<PerfTest> list = testService.getAllTesting();
		assertThat(list.size(), is(1));

		for (PerfTest test : list) {
			Instant now = now();
			test.setStartTime(now);

			PerfTest testTemp = testService.getOne(getTestUser(), test.getId());
			assertThat(testTemp.getId(), is(test.getId()));
			assertThat(testTemp.getStartTime(), is(now));

			testService.markAbnormalTermination(testTemp, StopReason.CANCEL_BY_USER);
			testService.markProgress(testTemp, "this test will be TESTING again");
			testService.markStatusAndProgress(testTemp, Status.TESTING, "this is just test unit");

			List<PerfTest> testingList = testService.getAll(getTestUser(), new Status[]{Status.TESTING});
			assertThat(testingList.size(), is(1));

			Long testCount = testService.count(getTestUser(), new Status[]{Status.TESTING});
			assertThat(testCount, is(1L));

			GrinderProperties properties = testService.getGrinderProperties(test);
			assertThat(properties, not(nullValue()));

		}

		createPerfTest("new Test2", Status.getProcessingOrTestingTestStatus()[0], now());
		list = testService.getCurrentlyRunningTest();
		assertThat(list.size(), is(2));

		PerfTest finishedTest = createPerfTest("new Test3", Status.ABNORMAL_TESTING, now());
		finishedTest.setPort(0); // need port number for finishing
		list = testService.getAllAbnormalTesting();
		assertThat(list.size(), is(1));

		testService.updatePerfTestAfterTestFinish(finishedTest);

		createPerfTest("new Test3", Status.START_AGENTS, now());

		List<PerfTest> errorList = testService.getAll(getTestUser(), new Status[]{Status.START_AGENTS});
		assertThat(errorList.size(), is(1));
		testService.markAbnormalTermination(errorList.get(0), "this is error test");
	}

	@Test
	public void testTestScriptAll() {
		int maxConcurrent = testService.getMaximumConcurrentTestCount();
		assertThat(maxConcurrent, is(10));

		PerfTest testScript = createPerfTest("new TestScript", Status.READY, now());
		testService.addCommentOn(getTestUser(), testScript.getId(), "this is TestScript method", "");

		PerfTest testing = testService.markProgressAndStatus(testScript, Status.TESTING, "It is testing from ready");
		assertThat(testing.getStatus(), is(Status.TESTING));

		File testPath = testService.getDistributionPath(testScript);
		assertThat(testPath, not(nullValue()));

		List<String> fileList = testService.getLogFiles(testScript.getId());
		assertThat(fileList, not(nullValue()));

		File scriptFile = testService.getLogFile(testScript.getId(), testScript.getScriptName());
		assertThat(scriptFile, not(nullValue()));

		ConsoleProperties consoleProperties = testService.createConsoleProperties(testScript);
		assertThat(consoleProperties, not(nullValue()));

	}

	@Test
	public void testGetReportDataWithExistingData() throws IOException {
		long testId = 123456L; // there is sample monitor data in test resources.

		// Given
		File testHomeDir = new ClassPathResource("world.py").getFile().getParentFile();
		Home mockHome = new Home(testHomeDir);
		LOG.debug("mock home dir is:{}", mockHome.getDirectory());
		Config spiedConfig = spy(config);
		when(spiedConfig.getHome()).thenReturn(mockHome);
		PerfTestService spiedService = spy(testService);
		setField(spiedService, "config", spiedConfig);

		// When
		// TPS,Errors,Mean_Test_Time_(ms)
		int interval = spiedService.getReportDataInterval(testId, "TPS", 700);

		// Then
		assertFalse(spiedService.getSingleReportData(testId, "TPS", interval).isEmpty());
		assertFalse(spiedService.getSingleReportData(testId, "Mean_Test_Time_(ms)", interval).isEmpty());
	}

	@Test
	public void testGetMonitorDataWithExistingData() throws IOException {
		// Given
		long testId = 123456L; // there is sample monitor data in test resources.
		File testHomeDir = new ClassPathResource("world.py").getFile().getParentFile();
		Home mockHome = new Home(testHomeDir);
		LOG.debug("mock home dir is:{}", mockHome.getDirectory());
		Config spiedConfig = spy(config);
		when(spiedConfig.getHome()).thenReturn(mockHome);
		PerfTestService spiedService = spy(testService);
		setField(spiedService, "config", spiedConfig);

		// When
		int interval = spiedService.getMonitorGraphInterval(testId, "127.0.0.1", 700);
		Map<String, Object> reportDataMap = spiedService.getMonitorGraph(testId, "127.0.0.1", interval);

		Predicate<Object> listNotEmpty = obj -> !((List) obj).isEmpty();

		// Then
		assertTrue(listNotEmpty.test(reportDataMap.get("cpu")));
		assertTrue(listNotEmpty.test(reportDataMap.get("memory")));
		assertTrue(listNotEmpty.test(reportDataMap.get("received")));
		assertTrue(listNotEmpty.test(reportDataMap.get("sent")));
	}

	@Test
	public void testGetProperSizedStatusString() {
		File tempRepo = new File(System.getProperty("java.io.tmpdir"), "test-repo");
		tempRepo.mkdir();
		tempRepo.deleteOnExit();
		MonitorClientService client = new MonitorClientService("127.0.0.1", 13243);
		client.init();
		Map<String, SystemDataModel> rtnMap = new HashMap<>();

		Random random = new Random();
		for (int i = 0; i < 80; i++) {
			client.update();
			SystemInfo info = client.getSystemInfo();
			if (info == null) {
				return;
			}
			info.setCustomValues(random.nextInt() + "," + random.nextInt());
			SystemDataModel data1 = new SystemDataModel(info, "3.1.2");
			rtnMap.put("test-" + random.nextInt(), data1);
			ThreadUtils.sleep(100);
		}
		String statusString = perfTestService.getProperSizedStatusString(rtnMap);
		System.out.println("Status string size is:" + statusString.length());
		assertTrue(statusString.length() < 9950);
	}
}
