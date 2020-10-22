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

import static java.time.Instant.now;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;
import static org.ngrinder.common.constant.WebConstants.PARAM_TEST;
import static org.ngrinder.common.util.NoOp.noOp;
import static org.ngrinder.common.util.TypeConvertUtils.cast;

import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.Test;
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
import org.springframework.mock.web.MockHttpServletResponse;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class PerfTestControllerTest extends AbstractPerfTestTransactionalTest {

	@Autowired
	private PerfTestController controller;

	@Autowired
	private MockPerfTestApiController perfTestApiController;

	@Autowired
	private Config config;

	@Autowired
	private IUserService userService;

	@Autowired
	public MockFileEntityRepository repo;

	@Before
	public void beforeCleanUp() {
		clearAllPerfTest();
	}

	@Test
	public void testCreatePerfTest() {
		Map<String, Object> response = perfTestApiController.create(getTestUser());
		assertNotNull(response.get(PARAM_TEST));
	}

	@Test
	public void testInvalidPerfTest() {
		Map<String, Object> response = perfTestApiController.getOneDetail(getTestUser(), 0L);
		assertNull(response.get(PARAM_TEST));
		long invalidId = 123123123123L;
		response = perfTestApiController.getOneDetail(getTestUser(), invalidId);
		assertNull(response.get(PARAM_TEST));
	}

	@Test
	public void testGetPerfTestDetail() {
		PerfTest createPerfTest = createPerfTest("hello", Status.READY, now());
		Map<String, Object> response = perfTestApiController.getOneDetail(getTestUser(), createPerfTest.getId());
		assertNotNull(response.get(PARAM_TEST));
	}

	@Test
	public void testGetResourcesOnScriptFolder() throws IOException {
		File file = new File(System.getProperty("java.io.tmpdir"), "repo");
		FileUtils.deleteQuietly(file);
		CompressionUtils.unzip(new ClassPathResource("TEST_USER.zip").getFile(), file);
		repo.setUserRepository(new File(file, getTestUser().getUserId()));
		perfTestApiController.getResources(getTestUser(), "filefilter.txt", null);
	}

	@Test
	public void testDeleteTests() {
		String testName = "test1";
		PerfTest test = createPerfTest(testName, Status.READY, now());
		perfTestApiController.delete(getTestUser(), String.valueOf(test.getId()));

		PerfTest test1 = createPerfTest(testName, Status.READY, now());
		PerfTest test2 = createPerfTest(testName, Status.READY, now());

		Function<Map<String, Object>, PerfTest> perfTestOf = map -> (PerfTest) map.get(PARAM_TEST);

		PerfTest foundOne = perfTestOf.apply(perfTestApiController.getOneDetail(getTestUser(), test1.getId()));
		assertNotNull(foundOne.getId());
		foundOne = perfTestOf.apply(perfTestApiController.getOneDetail(getTestUser(), test2.getId()));
		assertNotNull(foundOne.getId());

		String delIds = "" + test1.getId() + "," + test2.getId();
		perfTestApiController.delete(getTestUser(), delIds);

		foundOne = perfTestOf.apply(perfTestApiController.getOneDetail(getTestUser(), test1.getId()));
		assertNull(foundOne);
		foundOne = perfTestOf.apply(perfTestApiController.getOneDetail(getTestUser(), test2.getId()));
		assertNull(foundOne);
	}

	@Test
	public void testSavePerfTestCloneAndLeaveCommentAndStop() {
		String testName = "test1";
		PerfTest test = createPerfTest(testName, Status.READY, null);
		long preId = test.getId();

		PerfTest cloneTest = newPerfTest(testName, Status.READY, null);
		cloneTest.setId(test.getId()); // set cloned test's ID as previous test

		perfTestApiController.saveOne(getTestUser(), cloneTest, true);
		assertThat(preId, not(cloneTest.getId()));

		// test leave comment
		Map<String, Object> params = new HashMap<>();
		params.put("testComment", "TestComment");
		params.put("tagString", "");
		perfTestApiController.leaveComment(getTestUser(), cloneTest.getId(), params);
		PerfTest foundOne = perfTestApiController.getOne(getTestUser(), cloneTest.getId());
		assertEquals(foundOne.getTestComment(), "TestComment");

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
		PerfTest test = createPerfTest(testName, Status.READY, now());
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
		newTest.setScm("svn");

		perfTestApiController.saveOne(getTestUser(), newTest, false);
		PerfTest savedTest = perfTestApiController.getOne(getTestUser(), newTest.getId());
		assertEquals(savedTest.getTestName(), newName);
		assertEquals(savedTest.getId(), test.getId());

		newTest.setStatus(Status.READY);
		perfTestApiController.saveOne(getTestUser(), newTest, false);
		savedTest = perfTestApiController.getOne(getTestUser(), newTest.getId());
		assertEquals(savedTest.getTestName(), newName);
		assertEquals(savedTest.getId(), test.getId());

		// test status id "START_TESTING", can not be saved.
		newTest.setStatus(Status.START_TESTING);
		try {
			newTest.setStatus(Status.START_TESTING);
			perfTestApiController.saveOne(getTestUser(), newTest, false);
			fail("test status id START_TESTING, can not be saved");
		} catch (IllegalArgumentException e) {
			noOp();
		}
	}

	@Test
	public void testGetTestList() {
		createPerfTest("new test1", Status.READY, now());
		Map<String, Object> response = perfTestApiController.getAllList(getTestUser(), null, null, null, PageRequest.of(0, 10));
		assertThat(((List) response.get("tests")).size(), is(1));
	}

	@Test
	public void testGetTestListByAdmin() {
		String testName = "new test1";
		createPerfTest(testName, Status.READY, now());
		User testAdmin = new User();
		testAdmin.setUserId("testAdmin");
		testAdmin.setPassword("testAdmin");
		testAdmin.setRole(Role.ADMIN);
		testAdmin.setTimeZone("Asia/Seoul");
		testAdmin = userService.save(testAdmin);

		Map<String, Object> response = perfTestApiController.getAllList(testAdmin, null, null, null, PageRequest.of(0, 10));
		List<PerfTest> tests = cast(response.get("tests"));
		assertTrue(tests.stream().anyMatch(test -> test.getTestName().equals(testName)));
	}

	@Test
	public void testGetTestListByOtherUser() {
		String testName = "new test1";
		PerfTest test = createPerfTest(testName, Status.READY, now());

		User otherTestUser = new User();
		otherTestUser.setUserId("testUser");
		otherTestUser.setPassword("testUser");
		otherTestUser.setRole(Role.USER);
		otherTestUser = userService.save(otherTestUser);
		otherTestUser.setTimeZone("Asia/Seoul");
		Map<String, Object> response = perfTestApiController.getAllList(otherTestUser, null, null, null, PageRequest.of(0, 10));

		assertThat(((List) response.get("tests")).size(), is(0));

		// test no permission for other user
		try {
			perfTestApiController.getOne(otherTestUser, test.getId());
			assertTrue(false);
		} catch (NGrinderRuntimeException e) {
			assertTrue(true);
		}
	}

	@Test
	public void testGetTestListByKeyWord() {
		String strangeName = "DJJHG^%R&*^%^565(^%&^%(^%(^";
		createPerfTest(strangeName, Status.READY, now());

		Sort sort = Sort.by(Sort.Direction.ASC, "testName");
		Pageable pageable = PageRequest.of(0, 10, sort);
		Map<String, Object> response = perfTestApiController.getAllList(getTestUser(), strangeName, null, null, pageable);
		assertThat(((List) response.get("tests")).size(), is(1));

		response = perfTestApiController.getAllList(getTestUser(), strangeName.substring(2, 10), null, null, PageRequest.of(0, 10));
		assertThat(((List) response.get("tests")).size(), is(1));
	}

	@Test
	public void testGetReportData() {
		String testName = "test1";
		PerfTest test = createPerfTest(testName, Status.FINISHED, now());
		controller.getReport(test.getId());

		perfTestApiController.getPerfGraph(test.getId(), "TPS,mean_time(ms)", true, 0);

		perfTestApiController.getReportSection(getTestUser(), test.getId(), 700);
	}

	@Test
	public void testGetMonitorData() {
		String testName = "test1";
		PerfTest test = createPerfTest(testName, Status.FINISHED, now());
		perfTestApiController.getMonitorGraph(test.getId(), "127.0.0.1", 0);

		long testId = 123456L;
		perfTestApiController.getMonitorGraph(testId, "127.0.0.1", 700);
	}

	@Test
	public void testDownloadReportData() {
		String testName = "test1";
		PerfTest test = createPerfTest(testName, Status.FINISHED, now());
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
		PerfTest test = createPerfTest(testName, Status.TESTING, now());
		test.setPort(11011);
		try {
			perfTestApiController.refreshTestRunning(getTestUser(), test.getId());
		} catch (NullPointerException e) {
			assertTrue(true);
		}
	}

	@Test
	public void testUpdateStatus() {
		String testName = "test1";
		PerfTest test = createPerfTest(testName, Status.TESTING, now());
		String testName2 = "test1";
		PerfTest test2 = createPerfTest(testName2, Status.START_AGENTS, now());

		String ids = test.getId() + "," + test2.getId();
		Map<String, Object> response = perfTestApiController.getStatuses(getTestUser(), ids);
		assertThat(response, notNullValue());
	}

	@Test
	public void testSearchTag() {
		List<String> rtn = perfTestApiController.searchTag(getAdminUser(), "");
		assertThat(rtn, notNullValue());
		rtn = perfTestApiController.searchTag(getAdminUser(), "test");
		assertThat(rtn, notNullValue());
	}

}
