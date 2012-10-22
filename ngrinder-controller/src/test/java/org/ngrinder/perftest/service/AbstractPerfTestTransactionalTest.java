package org.ngrinder.perftest.service;

import java.util.Date;
import java.util.List;

import org.junit.Before;
import org.ngrinder.AbstractNGrinderTransactionalTest;
import org.ngrinder.infra.AgentConfig;
import org.ngrinder.infra.config.MockAgentConfigInControllerSide;
import org.ngrinder.model.PerfTest;
import org.ngrinder.model.Status;
import org.ngrinder.perftest.repository.PerfTestRepository;
import org.ngrinder.perftest.repository.TagRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * In addition {@link AbstractNGrinderTransactionalTest}, this class provides basic function to
 * create {@link PerfTest}.
 * 
 * @author JunHo Yoon
 * @since 3.0
 * 
 */
abstract public class AbstractPerfTestTransactionalTest extends AbstractNGrinderTransactionalTest {
	protected static final Logger LOG = LoggerFactory.getLogger(AbstractPerfTestTransactionalTest.class);

	protected AgentConfig agentConfig1;
	protected AgentConfig agentConfig2;

	@Before
	public void firstInit() {
		agentConfig1 = new MockAgentConfigInControllerSide().init();
		agentConfig2 = new MockAgentConfigInControllerSide().init();
	}

	@Autowired
	protected PerfTestService perfTestService;

	@Autowired
	protected PerfTestRepository perfTestRepository;

	@Autowired
	protected TagRepository tagRepository;
	
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
		test.setProcessIncrement(1);
		test.setInitSleepTime(0);
		test.setProcessIncrementInterval(1000);
		test.setStatus(status);
		test.setCreatedUser(getTestUser());
		return test;
	}

	public PerfTest createPerfTest(String testName, Status status, Date scheduledTime) {
		PerfTest test = newPerfTest(testName, status, scheduledTime);
		perfTestService.savePerfTest(test);
		return test;
	}

	public PerfTest createPerfTest(PerfTest perfTest) {
		perfTestService.savePerfTest(perfTest);
		return perfTest;
	}
}
