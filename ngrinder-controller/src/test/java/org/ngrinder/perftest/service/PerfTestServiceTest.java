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
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import net.grinder.StopReason;
import net.grinder.common.GrinderProperties;
import net.grinder.console.model.ConsoleProperties;

import org.junit.Before;
import org.junit.Test;
import org.ngrinder.common.model.Home;
import org.ngrinder.common.util.ThreadUtil;
import org.ngrinder.infra.config.Config;
import org.ngrinder.model.PerfTest;
import org.ngrinder.model.Status;
import org.ngrinder.monitor.controller.model.SystemDataModel;
import org.ngrinder.monitor.share.domain.SystemInfo;
import org.ngrinder.perftest.repository.PerfTestRepository;
import org.ngrinder.perftest.service.monitor.MonitorClientSerivce;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
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
	

	@Test
	public void testGetReportDataWithExistData() throws IOException {
		long testId = 123456L; //there is sample monitor data in test resources.

		//test resource dir
		File testHomeDir = new ClassPathResource("").getFile();
		Home mockHome = new Home(testHomeDir);
		LOG.debug("mock home dir is:{}", mockHome.getDirectory());
		Config mockConfig = spy(config);
		when(mockConfig.getHome()).thenReturn(mockHome);
		PerfTestService mockService = spy(testService);
		mockService.setConfig(mockConfig);
		
		//TPS,Errors,Mean_Test_Time_(ms)
		int interval = mockService.getReportDataInterval(testId, "TPS", 700);
		String reportDataCPU = mockService.getReportDataAsString(testId, "TPS", interval);
		String reportDataMsT = mockService.getReportDataAsString(testId, "Mean_Test_Time_(ms)", interval);

		assertTrue(reportDataCPU.length() > 100);
		assertTrue(reportDataMsT.length() > 100);
	}

	@Test
	public void testGetMonitorDataWithExistData() throws IOException {
		long testId = 123456L; //there is sample monitor data in test resources.

		//test resource dir
		File testHomeDir = new ClassPathResource("").getFile();
		Home mockHome = new Home(testHomeDir);
		LOG.debug("mock home dir is:{}", mockHome.getDirectory());
		Config mockConfig = spy(config);
		when(mockConfig.getHome()).thenReturn(mockHome);
		PerfTestService mockService = spy(testService);
		mockService.setConfig(mockConfig);
		
		int interval = mockService.getSystemMonitorDataInterval(testId, "127.0.0.1", 700);
		Map<String, String> reportDataMap = mockService.getSystemMonitorDataAsString(testId, "127.0.0.1", interval);
		String cpuStr = reportDataMap.get("cpu");
		LOG.debug("CPU monitor string is:{}", cpuStr);
		assertTrue(cpuStr.length() > 300);
		assertTrue(reportDataMap.get("memory").length() > 300);
		assertTrue(reportDataMap.get("received").length() > 300);
		assertTrue(reportDataMap.get("sent").length() > 300);
	}
	
	@Test
	public void testGetProperSizedStatusString() {
		File tempRepo = new File(System.getProperty("java.io.tmpdir"), "test-repo");
		tempRepo.mkdir();
		tempRepo.deleteOnExit();
		MonitorClientSerivce client = new MonitorClientSerivce();
		client.init("127.0.0.1", 13243, tempRepo, null);
		
		Map<String, SystemDataModel> rtnMap = new HashMap<String, SystemDataModel>();

		Random random = new Random();
		for (int i = 0; i < 80; i++) {
			SystemInfo info = client.getMonitorData();
			if (info == null) {
				return;
			}
			info.setCustomValues(random.nextInt() + "," + random.nextInt());
			SystemDataModel data1 = new SystemDataModel(info, "3.1.2");
			rtnMap.put("test-" + random.nextInt(), data1);
			ThreadUtil.sleep(100);
		}
		String statusString = perfTestService.getProperSizedStatusString(rtnMap);
		System.out.println("Status string size is:" + statusString.length());
		assertTrue(statusString.length() < 9950);
	}
}
