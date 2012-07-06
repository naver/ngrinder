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
package org.ngrinder.perftest.service;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Before;
import org.junit.Test;
import org.ngrinder.NGrinderIocTransactionalTestBase;
import org.ngrinder.perftest.model.PerfTest;
import org.ngrinder.perftest.model.Status;
import org.ngrinder.user.model.User;
import org.ngrinder.user.util.UserUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

/**
 * Class description.
 *
 * @author Mavlarn
 * @since
 */
public class TestServiceTest extends NGrinderIocTransactionalTestBase {

	@Autowired
	private TestService testService;
	
	@Before
	public void createTempTests () {
		User user = new User();
		user.setId(123L);
		user.setUserName("TEST_USER");
		UserUtil.setCurrentUser(user);
		
		PerfTest test = new PerfTest();
		test.setTestName("new Test1");
		test.setThreshold("D");
		test.setDuration(120);
		test.setIgnoreSampleCount(0);
		test.setTargetHosts("127.0.0.1");
		test.setScriptName("test1.py");
		testService.savePerfTest(test);
		
		test = new PerfTest();
		test.setCreateUser("TEST_USER");
		test.setTestName("new Test2");
		test.setStatus(Status.FINISHED);
		test.setThreshold("D");
		test.setDuration(120);
		test.setIgnoreSampleCount(0);
		test.setTargetHosts("127.0.0.1");
		test.setScriptName("test1.py");
		testService.savePerfTest(test);
		
	}
	
	@Test
	public void testGetTestListAll () {
		Pageable pageable = new PageRequest(0, 10);
		Page<PerfTest> testList = testService.getTestList("TEST_USER", false, pageable);
		assertThat(testList.getContent().size(), is(2));
		testList = testService.getTestList("TEST_USER", true, pageable);
		assertThat(testList.getContent().size(), is(1));
	}
}
