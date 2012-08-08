package org.ngrinder.perftest.service;

import java.util.Date;

import org.ngrinder.AbstractNGrinderTransactionalTest;
import org.ngrinder.perftest.model.PerfTest;
import org.ngrinder.perftest.model.Status;
import org.ngrinder.perftest.repository.PerfTestRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * In addition {@link AbstractNGrinderTransactionalTest}, this class provides
 * basic function to create {@link PerfTest}
 * 
 * @author JunHo Yoon
 * @since 3.0
 * 
 */
abstract public class AbstractPerfTestTransactionalTest extends AbstractNGrinderTransactionalTest {
	protected static final Logger LOG = LoggerFactory.getLogger(AbstractPerfTestTransactionalTest.class);

	@Autowired
	protected PerfTestService perfTestService;

	@Autowired
	protected PerfTestRepository perfTestRepository;

	public void clearAllPerfTest() {
		perfTestRepository.deleteAll();
	}

	public PerfTest createPerfTest(String testName, Status status, Date scheduledTime) {
		PerfTest test = new PerfTest();
		test.setTestName(testName);
		test.setThreshold("D");
		test.setDuration(120L);
		test.setVuserPerAgent(10);
		test.setScheduledTime(scheduledTime);
		test.setIgnoreSampleCount(0);
		test.setTargetHosts("127.0.0.1");
		test.setScriptName("test1.py");
		test.setProcesses(10);
		test.setThreads(3);
		test.setProcessIncrement(1);
		test.setInitSleepTime(0);
		test.setProcessIncrementInterval(1000);
		test.setStatus(status);
		test.setCreatedUser(getTestUser());
		perfTestService.savePerfTest(test);
		return test;
	}

	public PerfTest createPerfTest(PerfTest perfTest) {
		perfTestService.savePerfTest(perfTest);
		return perfTest;
	}
}
