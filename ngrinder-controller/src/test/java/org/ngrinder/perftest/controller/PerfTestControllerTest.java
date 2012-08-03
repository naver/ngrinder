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
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Test;
import org.ngrinder.AbstractNGrinderTransactionalTest;
import org.ngrinder.model.Role;
import org.ngrinder.model.User;
import org.ngrinder.perftest.model.PerfTest;
import org.ngrinder.perftest.service.PerfTestService;
import org.ngrinder.user.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.ui.ModelMap;

/**
 * Class description
 * 
 * @author mavlarn
 * @Since 3.0
 */
public class PerfTestControllerTest extends AbstractNGrinderTransactionalTest {

	@Autowired
	private PerfTestController controller;

	@Autowired
	private PerfTestService testService;

	@Autowired
	private UserService userService;

	private PerfTest createTempTests(String testName) {
		PerfTest test = new PerfTest();
		test.setTestName(testName);
		test.setThreshold("D");
		test.setDuration(120L);
		test.setIgnoreSampleCount(0);
		test.setTargetHosts("127.0.0.1");
		test.setScriptName("test1.py");
		testService.savePerfTest(test);
		return test;
	}

	/**
	 * Test method for
	 * {@link org.ngrinder.perftest.controller.PerfTestController#getTestList(org.springframework.ui.ModelMap, java.lang.String, boolean, org.springframework.data.domain.PageRequest)}
	 * .
	 */
	@SuppressWarnings("unchecked")
	@Test
	public void testGetTestList() {
		createTempTests("new test1");
		ModelMap model = new ModelMap();
		controller.getTestList(getTestUser(), null, false, null, model);
		Page<PerfTest> testPage = (Page<PerfTest>) model.get("testListPage");
		List<PerfTest> testList = testPage.getContent();

		assertThat(testList.size(), is(1));

	}
	
	@Test
	public void testGetTestListByAdmin() {
		String testName = "new test1";
		createTempTests(testName);
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
		createTempTests(testName);
		
		ModelMap model = new ModelMap();
		
		User testUser = new User();
		testUser.setUserId("testUser");
		testUser.setPassword("testUser");
		testUser.setRole(Role.USER);
		testUser = userService.saveUser(testUser);
		
		controller.getTestList(testUser, null, false, null, model);
		@SuppressWarnings("unchecked")
		Page<PerfTest> testPage = (Page<PerfTest>) model.get("testListPage");
		List<PerfTest> testList = testPage.getContent();

		assertThat(testList.size(), is(0));
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void testGetTestListByKeyWord() {
		String strangeName = "DJJHG^%R&*^%^565(^%&^%(^%(^";
		createTempTests(strangeName);
		
		ModelMap model = new ModelMap();
		
		controller.getTestList(getTestUser(), strangeName, false, new PageRequest(0, 10), model);
		Page<PerfTest> testPage = (Page<PerfTest>) model.get("testListPage");
		List<PerfTest> testList = testPage.getContent();
		assertThat(testList.size(), is(1));

		controller.getTestList(getTestUser(), strangeName.substring(2,10), false, 
				new PageRequest(0, 10), model);
		testPage = (Page<PerfTest>) model.get("testListPage");
		testList = testPage.getContent();
		assertThat(testList.size(), is(1));
	}

	/**
	 * Test method for
	 * {@link org.ngrinder.perftest.controller.PerfTestController#getTestDetail(org.springframework.ui.ModelMap, int)}
	 * .
	 */
	@Test
	@SuppressWarnings("unchecked")
	public void testGetTestDetail() {
		ModelMap model = new ModelMap();
		controller.getTestList(getTestUser(), null, false, null, model);
		Page<PerfTest> testPage = (Page<PerfTest>) model.get("testListPage");
		List<PerfTest> testList = testPage.getContent();
		assertThat(testList.size(), is(1));
	}
}
