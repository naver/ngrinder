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
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletResponse;

import org.junit.Test;
import org.ngrinder.model.Role;
import org.ngrinder.model.User;
import org.ngrinder.perftest.model.PerfTest;
import org.ngrinder.perftest.model.Status;
import org.ngrinder.perftest.service.AbstractPerfTestTransactionalTest;
import org.ngrinder.user.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.ui.ModelMap;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 * Class description
 * 
 * @author mavlarn
 * @Since 3.0
 */
public class PerfTestControllerTest extends AbstractPerfTestTransactionalTest {

	@Autowired
	private PerfTestController controller;

	@Autowired
	private UserService userService;

	@Test
	public void testDeleteTests() {
		String testName = "test1";
		PerfTest test = createPerfTest(testName, Status.READY, new Date());
		ModelMap model = new ModelMap();
		controller.deleteTests(getTestUser(), model, String.valueOf(test.getId()));

		model.clear();
		controller.getTestDetail(getTestUser(), test.getId(), model);
		PerfTest testInDB = (PerfTest) model.get(PARAM_TEST);
		assertThat(testInDB, nullValue());

		model.clear();
		PerfTest test1 = createPerfTest(testName, Status.READY, new Date());
		PerfTest test2 = createPerfTest(testName, Status.READY, new Date());
		String delIds = "" + test1.getId() + "," + test2.getId();
		controller.deleteTests(getTestUser(), model, delIds);

		model.clear();
		controller.getTestDetail(getTestUser(), test1.getId(), model);
		testInDB = (PerfTest) model.get(PARAM_TEST);
		assertThat(testInDB, nullValue());

		model.clear();
		controller.getTestDetail(getTestUser(), test2.getId(), model);
		testInDB = (PerfTest) model.get(PARAM_TEST);
		assertThat(testInDB, nullValue());
	}

	/**
	 * for "saved" or "ready" test, can be modified, but for running or finished
	 * test, can not modify
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
		newTest.setStatus(Status.READY);
		newTest.setThreshold(test.getThreshold());
		newTest.setDuration(test.getDuration());
		newTest.setVuserPerAgent(test.getVuserPerAgent());
		newTest.setScheduledTime(test.getScheduledTime());
		newTest.setIgnoreSampleCount(test.getIgnoreSampleCount());
		newTest.setTargetHosts(test.getTargetHosts());
		newTest.setScriptName(test.getScriptName());

		ModelMap model = new ModelMap();
		controller.saveTest(getTestUser(), model, newTest);
		controller.getTestDetail(getTestUser(), newTest.getId(), model);
		PerfTest testInDB = (PerfTest) model.get(PARAM_TEST);

		assertThat(testInDB.getTestName(), is(newName));
		assertThat(testInDB.getId(), is(test.getId()));

		// test status id "START_TESTING", can not be saved.
		try {
			newTest.setStatus(Status.START_TESTING);
			controller.saveTest(getTestUser(), model, newTest);
			fail("test status id START_TESTING, can not be saved");
		} catch (IllegalArgumentException e) {

		}

	}

	@SuppressWarnings("unchecked")
	@Test
	public void testGetTestList() {
		createPerfTest("new test1", Status.READY, new Date());
		ModelMap model = new ModelMap();
		controller.getTestList(getTestUser(), null, false, null, model);
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
		testAdmin = userService.saveUser(testAdmin);

		controller.getTestList(testAdmin, null, false, null, model);
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
		createPerfTest(testName, Status.READY, new Date());

		ModelMap model = new ModelMap();

		User otherTestUser = new User();
		otherTestUser.setUserId("testUser");
		otherTestUser.setPassword("testUser");
		otherTestUser.setRole(Role.USER);
		otherTestUser = userService.saveUser(otherTestUser);

		controller.getTestList(otherTestUser, null, false, null, model);
		@SuppressWarnings("unchecked")
		Page<PerfTest> testPage = (Page<PerfTest>) model.get("testListPage");
		List<PerfTest> testList = testPage.getContent();

		assertThat(testList.size(), is(0));
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testGetTestListByKeyWord() {
		String strangeName = "DJJHG^%R&*^%^565(^%&^%(^%(^";
		createPerfTest(strangeName, Status.READY, new Date());

		ModelMap model = new ModelMap();

		Sort sort = new Sort("testName");
		Pageable pageable = new PageRequest(0, 10, sort);
		controller.getTestList(getTestUser(), strangeName, false, pageable, model);
		Page<PerfTest> testPage = (Page<PerfTest>) model.get("testListPage");
		List<PerfTest> testList = testPage.getContent();
		assertThat(testList.size(), is(1));

		controller.getTestList(getTestUser(), strangeName.substring(2, 10), false, new PageRequest(0, 10), model);
		testPage = (Page<PerfTest>) model.get("testListPage");
		testList = testPage.getContent();
		assertThat(testList.size(), is(1));
	}

	@Test
	public void testUpdateVuser() {
		ModelMap model = new ModelMap();
		String rtn = controller.updateVuser(20, model);
		JsonParser parser = new JsonParser();
		JsonObject json = (JsonObject) parser.parse(rtn);
		int threadCount = json.get(PARAM_THREAD_COUNT).getAsInt();
		int processCount = json.get(PARAM_PROCESS_COUNT).getAsInt();
		assertThat(threadCount, is(2));
		assertThat(processCount, is(10));
	}

	@Test
	public void testGetReportData() {
		String testName = "test1";
		PerfTest test = createPerfTest(testName, Status.FINISHED, new Date());
		ModelMap model = new ModelMap();
		controller.getReportData(getTestUser(), model, test.getId(), "tps,errors", 0);
	}

	@Test
	public void testDownloadReportData() {
		String testName = "test1";
		PerfTest test = createPerfTest(testName, Status.FINISHED, new Date());

		HttpServletResponse resp = new MockHttpServletResponse();
		controller.downloadReportData(getTestUser(), resp, test.getId());
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
}
