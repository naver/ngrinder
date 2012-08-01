package org.ngrinder.perftest.service;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.List;

import net.grinder.AgentControllerDaemon;
import net.grinder.SingleConsole;
import net.grinder.common.GrinderProperties;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.ngrinder.common.constant.NGrinderConstants;
import org.ngrinder.infra.config.Config;
import org.ngrinder.perftest.model.PerfTest;
import org.ngrinder.perftest.model.Status;
import org.ngrinder.script.model.FileEntry;
import org.ngrinder.script.model.FileType;
import org.ngrinder.script.repository.FileEntityRepository;
import org.ngrinder.script.util.CompressionUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;

public class PerfTestRunnableTest extends AbstractPerfTestTransactionalTest implements NGrinderConstants {

	@Autowired
	private MockPerfTestRunnable perfTestRunnable;

	AgentControllerDaemon agentControllerDaemon;
	AgentControllerDaemon agentControllerDaemon2;

	@Autowired
	private AgentManager agentManager;

	@Before
	public void before() {
		clearAllPerfTest();
		createPerfTest("test1", Status.READY, new Date());
		createPerfTest("test2", Status.READY, new Date());
		List<PerfTest> allPerfTest = perfTestService.getAllPerfTest();
		assertThat(allPerfTest.size(), is(2));

		agentControllerDaemon = new AgentControllerDaemon();
		agentControllerDaemon.run(AGENT_SERVER_DAEMON_PORT);
		sleep(2000);
		assertThat(agentManager.getAllAttachedAgents().size(), is(1));
	}

	@After
	public void after() {
		agentControllerDaemon.shutdown();
		sleep(2000);
	}

	@Test
	public void testDoTest() {
		perfTestRunnable.startTest();

	}

	@Autowired
	public FileEntityRepository fileEntityRepository;

	@Test
	public void testStartConsole() throws IOException {
		// Get perf test
		PerfTest perfTest = perfTestService.getPerfTestCandiate();
		perfTest.setScriptName("/hello/world.py");
		assertThat(perfTest, not(nullValue()));

		// Start console
		SingleConsole singleConsole = perfTestRunnable.startConsole(perfTest);
		assertThat(singleConsole, not(nullValue()));
		assertThat(singleConsole.getConsolePort(), is(perfTest.getPort()));

		// Start agents
		perfTest.setAgentCount(1);
		GrinderProperties grinderProperties = perfTestService.getGrinderProperties(perfTest);
		perfTestRunnable.startAgentsOn(perfTest, grinderProperties, singleConsole);
		assertThat(agentManager.getAllFreeAgents().size(), is(0));

		// Distribute files
		prepareUserRepo();
		perfTestRunnable.distributeFileOn(perfTest, grinderProperties, singleConsole);

		// Run test
		perfTestRunnable.runTestOn(perfTest, grinderProperties, singleConsole);
		sleep(10000);
	}

	@Autowired
	private Config config;

	private void prepareUserRepo() throws IOException {
		CompressionUtil compressUtil = new CompressionUtil();
		File repo = config.getHome().getUserRepoDirectory(getTestUser().getUserId());
		FileUtils.deleteQuietly(repo);
		compressUtil.unzip(new ClassPathResource("TEST_USER.zip").getFile(), config.getHome().getRepoDirectoryRoot());
		FileEntry fileEntryDir = new FileEntry();
		fileEntryDir.setPath("/hello");
		fileEntryDir.setFileType(FileType.DIR);
		fileEntityRepository.save(getTestUser(), fileEntryDir, null);

		FileEntry fileEntry = new FileEntry();
		fileEntry.setPath("/hello/world.py");
		fileEntry.setContent("print 'HELLO'");
		fileEntry.setFileType(FileType.PYTHON_SCRIPT);
		fileEntityRepository.save(getTestUser(), fileEntry, "UTF-8");
	}
}
