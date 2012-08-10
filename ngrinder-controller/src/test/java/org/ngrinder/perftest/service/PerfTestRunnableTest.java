package org.ngrinder.perftest.service;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.List;

import net.grinder.AgentControllerDaemon;
import net.grinder.SingleConsole;
import net.grinder.common.GrinderProperties;
import net.grinder.common.processidentity.WorkerProcessReport;
import net.grinder.communication.AgentControllerCommunicationDefauts;
import net.grinder.console.communication.ProcessControl.Listener;
import net.grinder.console.communication.ProcessControl.ProcessReports;
import net.grinder.console.communication.ProcessControlImplementation;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
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
		agentControllerDaemon.setAgentConfig(agentConfig1);
		agentControllerDaemon.run(AgentControllerCommunicationDefauts.DEFAULT_AGENT_CONTROLLER_SERVER_PORT);
		agentControllerDaemon2 = new AgentControllerDaemon();
		agentControllerDaemon2.setAgentConfig(agentConfig2);
		agentControllerDaemon2.run(AgentControllerCommunicationDefauts.DEFAULT_AGENT_CONTROLLER_SERVER_PORT);

		sleep(3000);
		assertThat(agentManager.getAllAttachedAgents().size(), is(2));
	}

	@After
	public void after() {
		agentControllerDaemon.shutdown();
		agentControllerDaemon2.shutdown();
		sleep(2000);
	}

	@Test
	public void testDoTest() {
		perfTestRunnable.startTest();

	}

	@Autowired
	public FileEntityRepository fileEntityRepository;

	private int currentProcessCount = 10;
	private Object processCountSync = new Object();

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
		perfTest.setAgentCount(2);
		GrinderProperties grinderProperties = perfTestService.getGrinderProperties(perfTest);
		singleConsole.setReportPath(perfTestService.getReportFileDirectory(perfTest.getId()));
		perfTestRunnable.startAgentsOn(perfTest, grinderProperties, singleConsole);
		assertThat(agentManager.getAllFreeAgents().size(), is(0));

		// Distribute files
		prepareUserRepo();
		perfTestRunnable.distributeFileOn(perfTest, grinderProperties, singleConsole);

		singleConsole.getConsoleComponent(ProcessControlImplementation.class).addProcessStatusListener(new Listener() {

			@Override
			public void update(ProcessReports[] processReports) {
				synchronized (processCountSync) {
					currentProcessCount = 0;
					for (ProcessReports each : processReports) {
						for (WorkerProcessReport eachWorker : each.getWorkerProcessReports()) {
							if (eachWorker.getState() == 2) {
								currentProcessCount++;
							}
						}
					}
				}
			}
		});

		// Run test
		perfTestRunnable.runTestOn(perfTest, grinderProperties, singleConsole);
		sleep(20000);

		// Waiting for termination
		for (int i = 1; i < 100; i++) {
			sleep(1000);
			synchronized (processCountSync) {
				if (currentProcessCount == 0) {
					return;
				}
			}
			System.out.println("**** Current Process Count : " + currentProcessCount);

		}

		fail("Process is not finished within 100 sec");
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
		String worldString = IOUtils.toString(new ClassPathResource("world.py").getInputStream());
		fileEntry.setContent(worldString);
		fileEntry.setFileType(FileType.PYTHON_SCRIPT);
		fileEntityRepository.save(getTestUser(), fileEntry, "UTF-8");
	}
}
