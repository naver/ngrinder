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
import static org.junit.Assert.fail;

import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.ngrinder.AbstractNGNinderTransactionalTest;
import org.ngrinder.perftest.model.PerfTest;
import org.ngrinder.perftest.service.PerfTestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.ui.ModelMap;

/**
 * Class description
 * 
 * @author mavlarn
 * @Since 3.0
 */
public class PerfTestControllerTest extends AbstractNGNinderTransactionalTest {

	@Autowired
	private PerfTestController controller;

	@Autowired
	private PerfTestService testService;

	@Before
	public void createTempTests() {
		PerfTest test = new PerfTest();
		test.setTestName("new Test1");
		test.setThreshold("D");
		test.setDuration(120);
		test.setIgnoreSampleCount(0);
		test.setTargetHosts("127.0.0.1");
		test.setScriptName("test1.py");
		testService.savePerfTest(test);
	}

	/**
	 * Test method for
	 * {@link org.ngrinder.perftest.controller.PerfTestController#getTestList(org.springframework.ui.ModelMap, java.lang.String, boolean, org.springframework.data.domain.PageRequest)}
	 * .
	 */
	@SuppressWarnings("unchecked")
	@Test
	public void testGetTestList() {
		ModelMap model = new ModelMap();
		controller.getTestList(getTestUser(), null, false, null, model);
		Page<PerfTest> testPage = (Page<PerfTest>) model.get("testListPage");
		List<PerfTest> testList = testPage.getContent();

		assertThat(testList.size(), is(1));

	}

	/**
	 * Test method for
	 * {@link org.ngrinder.perftest.controller.PerfTestController#getTestDetail(org.springframework.ui.ModelMap, int)}.
	 */
	@Test
	@SuppressWarnings("unchecked")
	public void testGetTestDetail() {
		ModelMap model = new ModelMap();
		controller.getTestList(getTestUser(), null, false, null, model);
		Page<PerfTest> testPage = (Page<PerfTest>) model.get("testListPage");
		List<PerfTest> testList = testPage.getContent();
		assertThat(testList.size(), is(1));

		PerfTest test = testList.get(0);
		Assert.assertTrue(test.getCreatedUser().getUserId().equals(getTestUser().getUserId()));
	}

	/**
	 * Test method for
	 * {@link org.ngrinder.perftest.controller.PerfTestController#createTest(org.springframework.ui.ModelMap, org.ngrinder.perftest.model.PerfTest)}
	 * .
	 */
	@Test
	public void testCreateTest() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for
	 * {@link org.ngrinder.perftest.controller.PerfTestController#deleteTestt(org.springframework.ui.ModelMap, java.lang.String)}
	 * .
	 */
	@Test
	public void testDeleteTestt() {
		fail("Not yet implemented");
	}

}
