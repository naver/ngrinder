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
import java.util.*;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

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

		PerfTest candidate = testService.getNextRunnablePerfTestPerfTestCandidate();
		assertThat(candidate, nullValue());

		Pageable pageable = new PageRequest(0, 10);
		Page<PerfTest> testList = testService.getPagedAll(getTestUser(), null, null, null, pageable);
		assertThat(testList.getContent().size(), is(2));
		testList = testService.getPagedAll(getTestUser(), null, null, "F", pageable);
		assertThat(testList.getContent().size(), is(1));

		// test with no paging
		testList = testService.getPagedAll(getTestUser(), null, null, null, null);
		assertThat(testList.getContent().size(), is(2));
		testList = testService.getPagedAll(getTestUser(), null, null, "F", null);
		assertThat(testList.getContent().size(), is(1));

		List<PerfTest> list = testService.getAllTesting();
		assertThat(list.size(), is(1));

		for (PerfTest test : list) {
			long systemTimeMills = System.currentTimeMillis();
			test.setStartTime(new Date(systemTimeMills));

			PerfTest testTemp = testService.getOne(getTestUser(), test.getId());
			assertThat(testTemp.getId(), is(test.getId()));
			assertThat(testTemp.getStartTime().getTime(), is(systemTimeMills));

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

		createPerfTest("new Test2", Status.getProcessingOrTestingTestStatus()[0], new Date());
		list = testService.getCurrentlyRunningTest();
		assertThat(list.size(), is(2));

		PerfTest finishedTest = createPerfTest("new Test3", Status.ABNORMAL_TESTING, new Date());
		finishedTest.setPort(0); // need port number for finishing
		list = testService.getAllAbnormalTesting();
		assertThat(list.size(), is(1));

		testService.updatePerfTestAfterTestFinish(finishedTest);

		createPerfTest("new Test3", Status.START_AGENTS, new Date());

		List<PerfTest> errorList = testService.getAll(getTestUser(), new Status[]{Status.START_AGENTS});
		assertThat(errorList.size(), is(1));
		testService.markAbnormalTermination(errorList.get(0), "this is error test");
	}

	@Test
	public void testTestScriptAll() {
		int maxConcurrent = testService.getMaximumConcurrentTestCount();
		assertThat(maxConcurrent, is(10));

		PerfTest testScript = createPerfTest("new TestScript", Status.READY, new Date());
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
		Config mockConfig = spy(config);
		when(mockConfig.getHome()).thenReturn(mockHome);
		PerfTestService mockService = spy(testService);
		mockService.setConfig(mockConfig);

		// When
		// TPS,Errors,Mean_Test_Time_(ms)
		int interval = mockService.getReportDataInterval(testId, "TPS", 700);

		// Then
		assertThat(mockService.getSingleReportDataAsJson(testId, "TPS", interval).length(), greaterThan(100));
		assertThat(mockService.getSingleReportDataAsJson(testId, "Mean_Test_Time_(ms)", interval).length(),
				greaterThan(100));
	}

	@Test
	public void testGetMonitorDataWithExistingData() throws IOException {
		// Given
		long testId = 123456L; // there is sample monitor data in test resources.
		File testHomeDir = new ClassPathResource("world.py").getFile().getParentFile();
		Home mockHome = new Home(testHomeDir);
		LOG.debug("mock home dir is:{}", mockHome.getDirectory());
		Config mockConfig = spy(config);
		when(mockConfig.getHome()).thenReturn(mockHome);
		PerfTestService mockService = spy(testService);
		mockService.setConfig(mockConfig);

		// When
		int interval = mockService.getMonitorGraphInterval(testId, "127.0.0.1", 700);
		Map<String, String> reportDataMap = mockService.getMonitorGraph(testId, "127.0.0.1", interval);

		// Then
		assertThat(reportDataMap.get("cpu").length(), greaterThanOrEqualTo(300));
		assertThat(reportDataMap.get("memory").length(), greaterThanOrEqualTo(300));
		assertThat(reportDataMap.get("received").length(), greaterThanOrEqualTo(300));
		assertThat(reportDataMap.get("sent").length(), greaterThanOrEqualTo(300));
	}

	@Test
	public void testGetProperSizedStatusString() {
		File tempRepo = new File(System.getProperty("java.io.tmpdir"), "test-repo");
		tempRepo.mkdir();
		tempRepo.deleteOnExit();
		MonitorClientService client = new MonitorClientService("127.0.0.1", 13243);
		client.init();
		Map<String, SystemDataModel> rtnMap = new HashMap<String, SystemDataModel>();

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

	@Test
	public void testCleanUpRuntimeOnlyData() {

		PerfTest test = createPerfTest("new test", Status.READY, new Date());
		test.setAgentState("{\"NC-PL-DEV013\":{\"freeMemory\":2937684,\"totalMemory\":8301204,\"cpuUsedPercentage\":31.234259,\"receivedPerSec\":1874668,\"sentPerSec\":1881129}}");
		test.setMonitorState("{\"127.0.0.1\":{\"freeMemory\":1091352,\"totalMemory\":4042436,\"cpuUsedPercentage\":0.24937657,\"receivedPerSec\":102718,\"sentPerSec\":135072}}");
		test.setRunningSample("{\"process\":1,\"peakTpsForGraph\":2192.0,\"lastSampleStatistics\":[{\"Peak_TPS\":0.0,\"Tests\":2145.0,\"Mean_time_to_first_byte\":0.3142191142191142,\"testDescription\":\"Test1\",\"Response_bytes_per_second\":62205.0,\"Errors\":0.0,\"TPS\":2145.0,\"testNumber\":1,\"Mean_Test_Time_(ms)\":0.4205128205128205}],\"thread\":1,\"cumulativeStatistics\":[{\"Peak_TPS\":2192.0,\"Tests\":197185.0,\"Mean_time_to_first_byte\":0.3229910997286812,\"testDescription\":\"Test1\",\"Response_bytes_per_second\":57481.98148390145,\"Errors\":0.0,\"TPS\":1982.1372925483258,\"testNumber\":1,\"Mean_Test_Time_(ms)\":0.4425539468012273}],\"tpsChartData\":2145.0,\"success\":true,\"totalStatistics\":{\"Peak_TPS\":2192.0,\"Tests\":197185.0,\"Mean_time_to_first_byte\":0.3229910997286812,\"Response_bytes_per_second\":57481.98148390145,\"Errors\":0.0,\"TPS\":1982.1372925483258,\"Mean_Test_Time_(ms)\":0.4425539468012273},\"test_time\":105}");
		perfTestService.save(getTestUser(), test);

		PerfTest testInDB = perfTestService.getOne(test.getId());
		assertTrue(testInDB.getAgentState().length() > 0 && testInDB.getMonitorState().length() > 0);
		test.setAgentState(null);
		test.setMonitorState(null);
		test.setRunningSample(null);
		perfTestService.save(getTestUser(), test);
		testInDB = perfTestService.getOne(test.getId());
		assertTrue(testInDB.getAgentState() == null && testInDB.getMonitorState() == null);

	}
}
