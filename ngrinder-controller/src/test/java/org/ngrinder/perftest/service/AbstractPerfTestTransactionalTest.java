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
package org.ngrinder.perftest.service;

import org.ngrinder.AbstractNGrinderTransactionalTest;
import org.ngrinder.infra.config.Config;
import org.ngrinder.model.PerfTest;
import org.ngrinder.model.Status;
import org.ngrinder.perftest.repository.PerfTestRepository;
import org.ngrinder.perftest.repository.TagRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Date;
import java.util.List;

/**
 * In addition {@link org.ngrinder.AbstractNGrinderTransactionalTest}, this class provides basic function to
 * create {@link org.ngrinder.model.PerfTest} and run the test.
 *
 * @author JunHo Yoon
 * @since 3.0
 */
public abstract class AbstractPerfTestTransactionalTest extends AbstractNGrinderTransactionalTest {
	protected static final Logger LOG = LoggerFactory.getLogger(AbstractPerfTestTransactionalTest.class);


	@Autowired
	protected PerfTestService perfTestService;

	@Autowired
	protected PerfTestRepository perfTestRepository;

	@Autowired
	protected TagRepository tagRepository;

	@Autowired
	protected Config config;

	public void clearAllPerfTest() {
		List<PerfTest> findAll = perfTestRepository.findAll();
		for (PerfTest perfTest : findAll) {
			perfTest.getTags().clear();
		}
		perfTestRepository.save(findAll);
		perfTestRepository.flush();
		perfTestRepository.deleteAll();
		perfTestRepository.flush();
		tagRepository.deleteAll();
		tagRepository.flush();
	}

	public PerfTest newPerfTest(String testName, Status status, Date scheduledTime) {
		PerfTest test = new PerfTest();
		test.setTestName(testName);
		test.setThreshold("D");
		test.setDuration(4000L);
		test.setAgentCount(1);
		test.setVuserPerAgent(4);
		test.setScheduledTime(scheduledTime);
		test.setIgnoreSampleCount(0);
		test.setTargetHosts("127.0.0.1");
		test.setScriptName("test1.py");
		test.setProcesses(2);
		test.setThreads(2);
		test.setRampUpStep(1);
		test.setRampUpInitSleepTime(0);
		test.setRampUpIncrementInterval(1000);
		test.setStatus(status);
		test.setCreatedUser(getTestUser());
		test.setRegion(config.getRegion());
		test.setSamplingInterval(1);
		return test;
	}

	public PerfTest createPerfTest(String testName, Status status, Date scheduledTime) {
		PerfTest perftest = newPerfTest(testName, status, scheduledTime);
		return createPerfTest(perftest);
	}

	public PerfTest createPerfTest(PerfTest perfTest) {
		perfTestService.save(getTestUser(), perfTest);
		return perfTest;
	}

}
