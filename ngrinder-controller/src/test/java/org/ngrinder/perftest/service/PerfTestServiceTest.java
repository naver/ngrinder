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

import java.util.Date;
import java.util.List;

import org.junit.Test;
import org.ngrinder.perftest.model.PerfTest;
import org.ngrinder.perftest.model.Status;
import org.ngrinder.perftest.repository.PerfTestRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

/**
 * {@link PerfTestService} test
 * 
 * @author Mavlarn
 * @since 3.0
 */
public class PerfTestServiceTest extends AbstractPerfTestTransactionalTest {

	@Autowired
	private PerfTestService testService;

	@Autowired
	PerfTestRepository perfTestRepository;
	
	@Test
	public void testGetTestListAll() {
		
		createPerfTest("new Test1", Status.TESTING, new Date());
		createPerfTest("new Test2", Status.FINISHED, new Date());
		
		Pageable pageable = new PageRequest(0, 10);
		Page<PerfTest> testList = testService.getPerfTestList(getTestUser(), null, false, pageable);
		assertThat(testList.getContent().size(), is(2));
		testList = testService.getPerfTestList(getTestUser(), null, true, pageable);
		assertThat(testList.getContent().size(), is(1));

		// test with no paging
		testList = testService.getPerfTestList(getTestUser(), null, false, null);
		assertThat(testList.getContent().size(), is(2));
		testList = testService.getPerfTestList(getTestUser(), null, true, null);
		assertThat(testList.getContent().size(), is(1));

		List<PerfTest> list = testService.getTestingPerfTest();
		assertThat(list.size(), is(1));

		createPerfTest("new Test2", Status.getProcessingOrTestingTestStatus()[0], new Date());
		testService.getCurrentlyRunningTest();
		assertThat(list.size(), is(2));
		
		PerfTest finishedTest = createPerfTest("new Test2", Status.ABNORMAL_TESTING, new Date());
		testService.getAbnoramlTestingPerfTest();
		assertThat(list.size(), is(1));
		
		testService.updatePerfTestAfterTestFinish(finishedTest);
	}
	

}
