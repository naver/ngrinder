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

import org.junit.Ignore;
import org.junit.Test;
import org.ngrinder.perftest.model.PerfTest;
import org.ngrinder.perftest.model.Status;
import org.ngrinder.perftest.repository.PerfTestRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractJUnit4SpringContextTests;

/**
 * Class description.
 * 
 * @author Mavlarn
 * @since
 */
@ContextConfiguration({ "classpath:applicationContext.xml" })
public class AddTestRecord extends AbstractJUnit4SpringContextTests {

	@Autowired
	private PerfTestService testService;

	public void createTempTests() {
		PerfTest test = new PerfTest();
		test.setTestName("new Test1");
		test.setThreshold("D");
		test.setDuration(120L);
		test.setIgnoreSampleCount(0);
		test.setTargetHosts("127.0.0.1");
		test.setScriptName("test1.py");
		test.setProcesses(10);
		test.setProcessIncrement(1);
		test.setProcessIncrementInterval(1000);
		testService.savePerfTest(test);

		test = new PerfTest();
		test.setTestName("new Test2");
		test.setStatus(Status.FINISHED);
		test.setThreshold("D");
		test.setDuration(120L);
		test.setIgnoreSampleCount(0);
		test.setTargetHosts("127.0.0.1");
		test.setScriptName("test2.py");
		test.setProcesses(10);
		test.setProcessIncrement(1);
		test.setProcessIncrementInterval(1000);
		testService.savePerfTest(test);

	}

	@Autowired
	PerfTestRepository perfTestRepository;

	@Test
	public void doNothing() {

	}

	@Test
	@Ignore("Only enable this when test data is necessary.")
	public void testGetTestListAll() {
		createTempTests();
		createTempTests();
		createTempTests();
		createTempTests();
		createTempTests();
		createTempTests();
		createTempTests();
		createTempTests();
		createTempTests();
		createTempTests();
		System.out.println(perfTestRepository.findAll());
	}

}
