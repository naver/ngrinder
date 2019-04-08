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

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.Test;
import org.ngrinder.common.constant.WebConstants;
import org.ngrinder.common.exception.NGrinderRuntimeException;
import org.ngrinder.common.util.CompressionUtils;
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
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpEntity;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.ui.ModelMap;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;
import static org.ngrinder.common.util.TypeConvertUtils.cast;

/**
 * PerfTest Controller Test.
 *
 * @author mavlarn
 * @since 3.0
 */
public class PerfTestControllerTest extends AbstractPerfTestTransactionalTest implements WebConstants {

	@Autowired
	private MockPerfTestController controller;

	@Autowired
	private PerfTestApiController perfTestApiController;

	@Autowired
	private Config config;

	@Autowired
	private IUserService userService;

	@Autowired
	public MockFileEntityRepository repo;

	@Autowired
	private ObjectMapper objectMapper;

	@Before
	public void beforeCleanUp() throws IOException {
		clearAllPerfTest();
	}

	@Test
	public void testGetPerfTestDetail() {
		ModelMap model = new ModelMap();
		controller.getOne(getTestUser(), null, model);
		assertThat(model.get(PARAM_TEST), notNullValue());
		model.clear();

		controller.getOne(getTestUser(), 0L, model);
		assertThat(model.get(PARAM_TEST), notNullValue());
		model.clear();
		long invalidId = 123123123123L;
		controller.getOne(getTestUser(), invalidId, model);
		assertThat(model.get(PARAM_TEST), notNullValue());

		PerfTest createPerfTest = createPerfTest("hello", Status.READY, new Date());
		model.clear();
		controller.getOne(getTestUser(), createPerfTest.getId(), model);
		assertThat(model.get(PARAM_TEST), notNullValue());

	}

	@Test
	public void testGetResourcesOnScriptFolder() throws IOException {
		File file = new File(System.getProperty("java.io.tmpdir"), "repo");
		FileUtils.deleteQuietly(file);
		CompressionUtils.unzip(new ClassPathResource("TEST_USER.zip").getFile(), file);
		repo.setUserRepository(new File(file, getTestUser().getUserId()));
		controller.getResources(getTestUser(), "filefilter.txt", null);
	}

	@Test
	public void testDeleteTests() {
		String testName = "test1";
		PerfTest test = createPerfTest(testName, Status.READY, new Date());
		ModelMap model = new ModelMap();
		perfTestApiController.delete(getTestUser(), String.valueOf(test.getId()));
		model.clear();
		PerfTest test1 = createPerfTest(testName, Status.READY, new Date());
		PerfTest test2 = createPerfTest(testName, Status.READY, new Date());
		String delIds = "" + test1.getId() + "," + test2.getId();
		perfTestApiController.delete(getTestUser(), delIds);

		model.clear();
		controller.getOne(getTestUser(), test1.getId(), model);
		assertThat(((PerfTest) model.get(PARAM_TEST)).getId(), nullValue());
		model.clear();
		controller.getOne(getTestUser(), test2.getId(), model);
		assertThat(((PerfTest) model.get(PARAM_TEST)).getId(), nullValue());
	}

	@Test
	public void testSavePerfTestCloneAndLeaveCommentAndStop() {
		String testName = "test1";
		PerfTest test = createPerfTest(testName, Status.READY, null);
		long preId = test.getId();

		PerfTest cloneTest = newPerfTest(testName, Status.READY, null);
		cloneTest.setId(test.getId()); // set cloned test's ID as previous test

		ModelMap model = new ModelMap();
		controller.saveOne(getTestUser(), cloneTest, true, model);
		assertThat(preId, not(cloneTest.getId()));

		// test leave comment
		Map<String, Object> params = new HashMap<>();
		params.put("testComment", "TestComment");
		params.put("tagString", "");
		perfTestApiController.leaveComment(getTestUser(), cloneTest.getId(), params);
		model.clear();
		controller.getOne(getTestUser(), cloneTest.getId(), model);
		PerfTest testInDB = (PerfTest) model.get(PARAM_TEST);
		assertThat(testInDB.getTestComment(), is("TestComment"));

		// test stop test
		cloneTest.setStatus(Status.TESTING);
		perfTestService.save(getTestUser(), cloneTest);
		perfTestApiController.stop(getTestUser(), String.valueOf(cloneTest.getId()));
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
		newTest.setProcesses(2);
		newTest.setThreads(2);
		newTest.setVuserPerAgent(newTest.getProcesses() * newTest.getThreads());
		newTest.setRegion(config.getRegion());
		newTest.setAgentCount(1);

		ModelMap model = new ModelMap();
		controller.saveOne(getTestUser(), newTest, false, model);
		controller.getOne(getTestUser(), newTest.getId(), model);
		PerfTest testInDB = (PerfTest) model.get(PARAM_TEST);
		assertThat(testInDB.getTestName(), is(newName));
		assertThat(testInDB.getId(), is(test.getId()));

		model.clear();
		newTest.setStatus(Status.READY);
		controller.saveOne(getTestUser(), newTest, false, model);
		controller.getOne(getTestUser(), newTest.getId(), model);
		testInDB = (PerfTest) model.get(PARAM_TEST);
		assertThat(testInDB.getTestName(), is(newName));
		assertThat(testInDB.getId(), is(test.getId()));

		// test status id "START_TESTING", can not be saved.
		newTest.setStatus(Status.START_TESTING);
		try {
			newTest.setStatus(Status.START_TESTING);
			controller.saveOne(getTestUser(), newTest, false, model);
			fail("test status id START_TESTING, can not be saved");
		} catch (IllegalArgumentException e) {
		}
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testGetTestList() throws IOException {
		createPerfTest("new test1", Status.READY, new Date());
		HttpEntity<String> response = perfTestApiController.getAllList(getTestUser(), null, null, null, PageRequest.of(0, 10));
		Map<String, Object> result = objectMapper.readValue(response.getBody(), Map.class);
		assertThat(((List)result.get("tests")).size(), is(1));
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testGetTestListByAdmin() throws IOException {
		String testName = "new test1";
		createPerfTest(testName, Status.READY, new Date());
		User testAdmin = new User();
		testAdmin.setUserId("testAdmin");
		testAdmin.setPassword("testAdmin");
		testAdmin.setRole(Role.ADMIN);
		testAdmin.setTimeZone("Asia/Seoul");
		testAdmin = userService.save(testAdmin);

		HttpEntity<String> response = perfTestApiController.getAllList(testAdmin, null, null, null, PageRequest.of(0, 10));
		Map<String, Object> result = objectMapper.readValue(response.getBody(), Map.class);
		List<Map<String, Object>> testList = cast(result.get("tests"));
		boolean success = false;
		for (Map<String, Object> perfTest : testList) {
			if (perfTest.get("testName").equals(testName)) {
				success = true;
			}
		}
		assertTrue(success);
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testGetTestListByOtherUser() throws IOException {
		String testName = "new test1";
		PerfTest test = createPerfTest(testName, Status.READY, new Date());

		User otherTestUser = new User();
		otherTestUser.setUserId("testUser");
		otherTestUser.setPassword("testUser");
		otherTestUser.setRole(Role.USER);
		otherTestUser = userService.save(otherTestUser);
		otherTestUser.setTimeZone("Asia/Seoul");
		HttpEntity<String> response = perfTestApiController.getAllList(otherTestUser, null, null, null, PageRequest.of(0, 10));

		Map<String, Object> result = objectMapper.readValue(response.getBody(), Map.class);
		assertThat(((List)result.get("tests")).size(), is(0));

		// test no permission for other user
		try {
			controller.getOne(otherTestUser, test.getId());
			assertTrue(false);
		} catch (NGrinderRuntimeException e) {
			assertTrue(true);
		}
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testGetTestListByKeyWord() throws IOException {
		String strangeName = "DJJHG^%R&*^%^565(^%&^%(^%(^";
		createPerfTest(strangeName, Status.READY, new Date());

		Sort sort = new Sort(Sort.Direction.ASC, "testName");
		Pageable pageable = PageRequest.of(0, 10, sort);
		HttpEntity<String> response = perfTestApiController.getAllList(getTestUser(), strangeName, null, null, pageable);

		Map<String, Object> result = objectMapper.readValue(response.getBody(), Map.class);
		assertThat(((List)result.get("tests")).size(), is(1));

		response = perfTestApiController.getAllList(getTestUser(), strangeName.substring(2, 10), null, null, PageRequest.of(0, 10));
		result = objectMapper.readValue(response.getBody(), Map.class);
		assertThat(((List)result.get("tests")).size(), is(1));
	}

	@Test
	public void testGetReportData() {
		String testName = "test1";
		PerfTest test = createPerfTest(testName, Status.FINISHED, new Date());
		ModelMap model = new ModelMap();
		controller.getReport(model, test.getId());

		model.clear();
		perfTestApiController.getPerfGraph(test.getId(), "TPS,mean_time(ms)", true, 0);

		model.clear();
		perfTestApiController.getReportSection(getTestUser(), test.getId(), 700);
	}

	@Test
	public void testGetMonitorData() {
		String testName = "test1";
		PerfTest test = createPerfTest(testName, Status.FINISHED, new Date());
		controller.getMonitorGraph(test.getId(), "127.0.0.1", 0);

		long testId = 123456L;
		controller.getMonitorGraph(testId, "127.0.0.1", 700);
	}

	@Test
	public void testDownloadReportData() {
		String testName = "test1";
		PerfTest test = createPerfTest(testName, Status.FINISHED, new Date());
		HttpServletResponse resp = new MockHttpServletResponse();
		try {
			controller.downloadCSV(getTestUser(), test.getId(), resp);
		} catch (IllegalStateException e) {
			// the report file doesn't exist
			assertTrue(true);
		}
		resp.reset();
		controller.downloadLog(getTestUser(), test.getId(), "log", resp);
	}

	@Test
	public void testRefreshTestRunning() {
		String testName = "test1";
		// it is not a running test, can not test get statistic data.
		PerfTest test = createPerfTest(testName, Status.TESTING, new Date());
		test.setPort(11011);
		try {
			controller.refreshTestRunning(getTestUser(), test.getId());
		} catch (NullPointerException e) {
			assertTrue(true);
		}
	}

	@Test
	public void testUpdateStatus() {
		String testName = "test1";
		PerfTest test = createPerfTest(testName, Status.TESTING, new Date());
		String testName2 = "test1";
		PerfTest test2 = createPerfTest(testName2, Status.START_AGENTS, new Date());

		String ids = test.getId() + "," + test2.getId();
		HttpEntity<String> rtnJson = controller.getStatuses(getTestUser(), ids);
		assertThat(rtnJson.getBody(), notNullValue());
	}

	@Test
	public void testSearchTag() {
		List<String> rtn = perfTestApiController.searchTag(getAdminUser(), "");
		assertThat(rtn, notNullValue());
		rtn = perfTestApiController.searchTag(getAdminUser(), "test");
		assertThat(rtn, notNullValue());
	}

}
