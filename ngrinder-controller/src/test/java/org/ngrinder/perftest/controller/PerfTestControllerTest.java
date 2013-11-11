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

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.Test;
import org.ngrinder.common.exception.NGrinderRuntimeException;
import org.ngrinder.common.util.CompressionUtil;
import org.ngrinder.infra.config.Config;
import org.ngrinder.model.PerfTest;
import org.ngrinder.model.Role;
import org.ngrinder.model.Status;
import org.ngrinder.model.User;
import org.ngrinder.perftest.service.AbstractPerfTestTransactionalTest;
import org.ngrinder.script.repository.MockFileEntityRepository;
import org.ngrinder.service.IUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpEntity;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.ui.ModelMap;

/**
 * PerfTest Controller Test.
 * 
 * @author mavlarn
 * @Since 3.0
 */
public class PerfTestControllerTest extends AbstractPerfTestTransactionalTest {

	@Autowired
	private MockPerfTestController controller;

	@Autowired
	private Config config;

	@Autowired
	private IUserService userService;

	@Autowired
	public MockFileEntityRepository repo;

	@Before
	public void beforeCleanUp() throws IOException {
		clearAllPerfTest();
	}

	@Test
	public void testGetPerfTestDetail() {
		ModelMap model = new ModelMap();
		controller.getPerfTestDetail(getTestUser(), null, model);

		model.clear();
		controller.getPerfTestDetail(getTestUser(), 0L, model);

		assertThat(model.get(PARAM_TEST), nullValue());
		model.clear();
		long invalidId = 123123123123L;
		controller.getPerfTestDetail(getTestUser(), invalidId, model);
		assertThat(model.get(PARAM_TEST), nullValue());

		PerfTest createPerfTest = createPerfTest("hello", Status.READY, new Date());
		model.clear();
		controller.getPerfTestDetail(getTestUser(), createPerfTest.getId(), model);
		assertThat(model.get(PARAM_TEST), notNullValue());

	}

	@Test
	public void testGetResourcesOnScriptFolder() throws IOException {
		File file = new File(System.getProperty("java.io.tmpdir"), "repo");
		FileUtils.deleteQuietly(file);
		CompressionUtil.unzip(new ClassPathResource("TEST_USER.zip").getFile(), file);
		repo.setUserRepository(new File(file, getTestUser().getUserId()));
		controller.getResources(getTestUser(), "filefilter.txt", null, null);
	}

	@Test
	public void testDeleteTests() {
		String testName = "test1";
		PerfTest test = createPerfTest(testName, Status.READY, new Date());
		ModelMap model = new ModelMap();
		controller.deletePerfTests(getTestUser(), model, String.valueOf(test.getId()));

		model.clear();
		controller.getPerfTestDetail(getTestUser(), test.getId(), model);
		PerfTest testInDB = (PerfTest) model.get(PARAM_TEST);
		assertThat(testInDB, nullValue());

		model.clear();
		PerfTest test1 = createPerfTest(testName, Status.READY, new Date());
		PerfTest test2 = createPerfTest(testName, Status.READY, new Date());
		String delIds = "" + test1.getId() + "," + test2.getId();
		controller.deletePerfTests(getTestUser(), model, delIds);

		model.clear();
		controller.getPerfTestDetail(getTestUser(), test1.getId(), model);
		testInDB = (PerfTest) model.get(PARAM_TEST);
		assertThat(testInDB, nullValue());

		model.clear();
		controller.getPerfTestDetail(getTestUser(), test2.getId(), model);
		testInDB = (PerfTest) model.get(PARAM_TEST);
		assertThat(testInDB, nullValue());
	}

	@Test
	public void testSavePerfTestCloneAndLeaveCommentAndStop() {
		String testName = "test1";
		PerfTest test = createPerfTest(testName, Status.READY, null);
		long preId = test.getId();

		PerfTest cloneTest = newPerfTest(testName, Status.READY, null);
		cloneTest.setId(test.getId()); // set cloned test's ID as previous test

		ModelMap model = new ModelMap();
		controller.savePerfTest(getTestUser(), model, cloneTest, true);
		assertThat(preId, not(cloneTest.getId()));

		// test leave comment
		controller.leaveComment(getTestUser(), cloneTest.getId(), "TestComment", "");
		model.clear();
		controller.getPerfTestDetail(getTestUser(), cloneTest.getId(), model);
		PerfTest testInDB = (PerfTest) model.get(PARAM_TEST);
		assertThat(testInDB.getTestComment(), is("TestComment"));

		// test stop test
		cloneTest.setStatus(Status.TESTING);
		perfTestService.savePerfTest(cloneTest);
		controller.stopPerfTests(getTestUser(), model, String.valueOf(cloneTest.getId()));
	}

	/**
	 * for "saved" or "ready" test, can be modified, but for running or finished test, can not
	 * modify
	 */
	@Test
	public void testSavePerfTestExist() {
		String testName = "test1";
		String newName = "new test1";
		PerfTest test = createPerfTest(testName, Status.READY, new Date());
		test.setTestName(newName);

		PerfTest newTest = new PerfTest();
		newTest.setId(test.getId());
		newTest.setTestName(newName);
		newTest.setStatus(Status.SAVED);
		newTest.setThreshold(test.getThreshold());
		newTest.setDuration(test.getDuration());
		newTest.setVuserPerAgent(test.getVuserPerAgent());
		newTest.setScheduledTime(test.getScheduledTime());
		newTest.setIgnoreSampleCount(test.getIgnoreSampleCount());
		newTest.setTargetHosts(test.getTargetHosts());
		newTest.setScriptName(test.getScriptName());
		newTest.setProcesses(1);
		newTest.setThreads(1);
		newTest.setRegion(config.getRegion());
		newTest.setAgentCount(1);

		ModelMap model = new ModelMap();
		controller.savePerfTest(getTestUser(), model, newTest, false);
		controller.getPerfTestDetail(getTestUser(), newTest.getId(), model);
		PerfTest testInDB = (PerfTest) model.get(PARAM_TEST);
		assertThat(testInDB.getTestName(), is(newName));
		assertThat(testInDB.getId(), is(test.getId()));

		model.clear();
		newTest.setStatus(Status.READY);
		controller.savePerfTest(getTestUser(), model, newTest, false);
		controller.getPerfTestDetail(getTestUser(), newTest.getId(), model);
		testInDB = (PerfTest) model.get(PARAM_TEST);
		assertThat(testInDB.getTestName(), is(newName));
		assertThat(testInDB.getId(), is(test.getId()));

		// test status id "START_TESTING", can not be saved.
		newTest.setStatus(Status.START_TESTING);
		try {
			newTest.setStatus(Status.START_TESTING);
			controller.savePerfTest(getTestUser(), model, newTest, false);
			fail("test status id START_TESTING, can not be saved");
		} catch (IllegalArgumentException e) {
		}
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testGetTestList() {
		createPerfTest("new test1", Status.READY, new Date());
		ModelMap model = new ModelMap();
		controller.getPerfTestList(getTestUser(), null, null, null, null, model);
		Page<PerfTest> testPage = (Page<PerfTest>) model.get("testListPage");
		List<PerfTest> testList = testPage.getContent();
		assertThat(testList.size(), is(1));
	}

	@Test
	public void testGetTestListByAdmin() {
		String testName = "new test1";
		createPerfTest(testName, Status.READY, new Date());
		ModelMap model = new ModelMap();
		User testAdmin = new User();
		testAdmin.setUserId("testAdmin");
		testAdmin.setPassword("testAdmin");
		testAdmin.setRole(Role.ADMIN);
		testAdmin.setTimeZone("Asia/Seoul");
		testAdmin = userService.saveUser(testAdmin);

		controller.getPerfTestList(testAdmin, null, null, null, null, model);
		@SuppressWarnings("unchecked")
		Page<PerfTest> testPage = (Page<PerfTest>) model.get("testListPage");
		List<PerfTest> testList = testPage.getContent();
		boolean success = false;
		for (PerfTest perfTest : testList) {
			if (perfTest.getTestName().equals(testName)) {
				success = true;
			}
		}
		assertTrue(success);
	}

	@Test
	public void testGetTestListByOtherUser() {
		String testName = "new test1";
		PerfTest test = createPerfTest(testName, Status.READY, new Date());

		ModelMap model = new ModelMap();

		User otherTestUser = new User();
		otherTestUser.setUserId("testUser");
		otherTestUser.setPassword("testUser");
		otherTestUser.setRole(Role.USER);
		otherTestUser = userService.saveUser(otherTestUser);
		otherTestUser.setTimeZone("Asia/Seoul");
		controller.getPerfTestList(otherTestUser, null, null, null, null, model);
		@SuppressWarnings("unchecked")
		Page<PerfTest> testPage = (Page<PerfTest>) model.get("testListPage");
		List<PerfTest> testList = testPage.getContent();

		assertThat(testList.size(), is(0));

		// test no permission for other user
		model.clear();
		try {
			controller.getPerfTestDetail(otherTestUser, test.getId(), model);
			assertTrue(false);
		} catch (NGrinderRuntimeException e) {
			assertTrue(true);
		}
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testGetTestListByKeyWord() {
		String strangeName = "DJJHG^%R&*^%^565(^%&^%(^%(^";
		createPerfTest(strangeName, Status.READY, new Date());

		ModelMap model = new ModelMap();

		Sort sort = new Sort("testName");
		Pageable pageable = new PageRequest(0, 10, sort);
		controller.getPerfTestList(getTestUser(), strangeName, null, null, pageable, model);
		Page<PerfTest> testPage = (Page<PerfTest>) model.get("testListPage");
		List<PerfTest> testList = testPage.getContent();
		assertThat(testList.size(), is(1));

		controller.getPerfTestList(getTestUser(), strangeName.substring(2, 10), null, null, new PageRequest(0, 10),
						model);
		testPage = (Page<PerfTest>) model.get("testListPage");
		testList = testPage.getContent();
		assertThat(testList.size(), is(1));
	}

	@Test
	public void testGetReportData() {
		String testName = "test1";
		PerfTest test = createPerfTest(testName, Status.FINISHED, new Date());
		ModelMap model = new ModelMap();
		controller.getReport(model, test.getId());

		model.clear();
		controller.getReportData(model, test.getId(), "TPS,mean_time(ms)", 0);

		model.clear();
		controller.getReportSection(getTestUser(), model, test.getId(), 700);
	}

	@Test
	public void testGetMonitorData() {
		String testName = "test1";
		PerfTest test = createPerfTest(testName, Status.FINISHED, new Date());
		ModelMap model = new ModelMap();

		controller.getMonitorData(model, test.getId(), "127.0.0.1", 0);

		model.clear();
		long testId = 123456L;
		controller.getMonitorData(model, testId, "127.0.0.1", 700);
	}

	@Test
	public void testDownloadReportData() {
		String testName = "test1";
		PerfTest test = createPerfTest(testName, Status.FINISHED, new Date());
		HttpServletResponse resp = new MockHttpServletResponse();
		try {
			controller.downloadCSV(getTestUser(), resp, test.getId());
		} catch (IllegalStateException e) {
			// the report file doesn'r exist
			assertTrue(true);
		}
		resp.reset();
		controller.downloadLog(getTestUser(), "", test.getId(), resp);
	}

	@Test
	public void testRefreshTestRunning() {
		String testName = "test1";
		// it is not a running test, can not test get statistic data.
		PerfTest test = createPerfTest(testName, Status.TESTING, new Date());
		test.setPort(11011);
		ModelMap model = new ModelMap();
		try {
			controller.refreshTestRunning(getTestUser(), model, test.getId());
		} catch (NullPointerException e) {
			assertTrue(true);
		}
	}

	@Test
	public void testUpdateSatus() {
		String testName = "test1";
		PerfTest test = createPerfTest(testName, Status.TESTING, new Date());
		String testName2 = "test1";
		PerfTest test2 = createPerfTest(testName2, Status.START_AGENTS, new Date());

		String ids = test.getId() + "," + test2.getId();
		HttpEntity<String> rtnJson = controller.updateStatuses(getTestUser(), ids);
		assertThat(rtnJson.getBody(), notNullValue());
	}

	@Test
	public void testSearchTag() {
		HttpEntity<String> rtn = controller.searchTag(getAdminUser(), "");
		assertThat(rtn.getBody(), notNullValue());
		rtn = controller.searchTag(getAdminUser(), "test");
		assertThat(rtn.getBody(), notNullValue());
	}
}
