/*
 * Copyright (C) 2012 - 2012 NHN Corporation
 * All rights reserved.
 *
 * This file is part of The nGrinder software distribution. Refer to
 * the file LICENSE which is part of The nGrinder distribution for
 * licensing details. The nGrinder distribution is available on the
 * Internet at http://nhnopensource.org/ngrinder
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS
 * FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE
 * COPYRIGHT HOLDERS OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.ngrinder.perftest.controller;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletResponse;

import org.junit.Before;
import org.junit.Test;
import org.ngrinder.common.exception.NGrinderRuntimeException;
import org.ngrinder.model.PerfTest;
import org.ngrinder.model.Role;
import org.ngrinder.model.Status;
import org.ngrinder.model.User;
import org.ngrinder.perftest.service.AbstractPerfTestTransactionalTest;
import org.ngrinder.service.IUserService;
import org.springframework.beans.factory.annotation.Autowired;
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
	private IUserService userService;

	@Before
	public void beforeCleanUp() {
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
		long invalidId = 1234567890;
		controller.getPerfTestDetail(getTestUser(), invalidId, model);
		assertThat(model.get(PARAM_TEST), nullValue());


		PerfTest createPerfTest = createPerfTest("hello", Status.READY, new Date());
		model.clear();
		controller.getPerfTestDetail(getTestUser(), createPerfTest.getId(), model);
		assertThat(model.get(PARAM_TEST), notNullValue());

	}

	@Test
	public void testGetResourcesOnScriptFolder() {
		controller.getResourcesOnScriptFolder(getTestUser(), "", null);
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
		cloneTest.setId(test.getId()); //set cloned test's ID as previous test

		ModelMap model = new ModelMap();
		controller.savePerfTest(getTestUser(), model, cloneTest, true);
		assertThat(preId, not(cloneTest.getId()));
		
		//test leave comment
		controller.leaveComment(getTestUser(), "TestComment", "", cloneTest.getId());
		model.clear();
		controller.getPerfTestDetail(getTestUser(), cloneTest.getId(), model);
		PerfTest testInDB = (PerfTest) model.get(PARAM_TEST);
		assertThat(testInDB.getTestComment(), is("TestComment"));
		
		//test stop test
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
		
		//test no permission for other user
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
		controller.getPerfTestList(getTestUser(), strangeName, null,  null, pageable, model);
		Page<PerfTest> testPage = (Page<PerfTest>) model.get("testListPage");
		List<PerfTest> testList = testPage.getContent();
		assertThat(testList.size(), is(1));

		controller.getPerfTestList(getTestUser(), strangeName.substring(2, 10), null,  null,
						new PageRequest(0, 10), model);
		testPage = (Page<PerfTest>) model.get("testListPage");
		testList = testPage.getContent();
		assertThat(testList.size(), is(1));
	}

	@Test
	public void testGetReportData() {
		String testName = "test1";
		PerfTest test = createPerfTest(testName, Status.FINISHED, new Date());
		ModelMap model = new ModelMap();
		controller.getReport(getTestUser(), model, test.getId());

		model.clear();
		controller.getReportData(getTestUser(), model, test.getId(), "TPS,mean_time(ms)", 0);

		model.clear();
		controller.getReportDiv(getTestUser(), model, test.getId(), 700);
	}

	@Test
	public void testDownloadReportData() {
		String testName = "test1";
		PerfTest test = createPerfTest(testName, Status.FINISHED, new Date());
		HttpServletResponse resp = new MockHttpServletResponse();
		try {
			controller.downloadReportData(getTestUser(), resp, test.getId());
		} catch (IllegalStateException e) {
			//the report file doesn'r exist
			assertTrue(true);
		}
		resp.reset();
		controller.downloadLogData(getTestUser(), "", test.getId(), resp);
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
		HttpEntity<String> rtnJson = controller.updateStatus(getTestUser(), ids);
		assertThat(rtnJson.getBody(), notNullValue());
	}
}
