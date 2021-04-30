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

import net.grinder.SingleConsole;
import net.grinder.SingleConsole.SamplingLifeCycleListener;
import net.grinder.common.GrinderProperties;
import net.grinder.common.processidentity.AgentIdentity;
import net.grinder.engine.controller.AgentControllerIdentityImplementation;
import net.grinder.statistics.StatisticsSet;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Test;
import org.ngrinder.agent.service.AgentService;
import org.ngrinder.agent.store.AgentInfoStore;
import org.ngrinder.common.constant.ControllerConstants;
import org.ngrinder.common.util.CompressionUtils;
import org.ngrinder.model.AgentInfo;
import org.ngrinder.model.PerfTest;
import org.ngrinder.model.Status;
import org.ngrinder.script.model.FileEntry;
import org.ngrinder.script.model.FileType;
import org.ngrinder.script.repository.MockFileEntityRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static java.nio.charset.Charset.defaultCharset;
import static net.grinder.message.console.AgentControllerState.READY;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.ngrinder.common.util.TypeConvertUtils.cast;

public class PerfTestRunnableTest extends AbstractAgentReadyTest implements ControllerConstants {

	@Autowired
	private MockPerfTestRunnable perfTestRunnable;

	@Autowired
	private AgentManager agentManager;

	@Autowired
	private AgentService agentService;

	@Autowired
	public MockFileEntityRepository fileEntityRepository;

	public PerfTest currentTest;

	@Autowired
	public ConsoleManager consoleManager;

	@Autowired
	private AgentInfoStore agentInfoStore;

	@Before
	public void before() throws IOException {
		File tempRepo = new File(System.getProperty("java.io.tmpdir"), "repo");
		fileEntityRepository.setUserRepository(new File(tempRepo, getTestUser().getUserId()));
		File testUserRoot = fileEntityRepository.getUserRepoDirectory(getTestUser()).getParentFile();
		testUserRoot.mkdirs();
		CompressionUtils.unzip(new ClassPathResource("TEST_USER.zip").getFile(), testUserRoot);
		testUserRoot.deleteOnExit();

		prepareUserRepo();
		clearAllPerfTest();
		currentTest = createPerfTest("test1", Status.READY, null);
		List<PerfTest> allPerfTest = perfTestService.getAllPerfTest();

		assertThat(allPerfTest.size(), is(1));
		allPerfTest.get(0).setScriptName("/hello/world.py");
		allPerfTest.get(0).setDuration(30000L);
		perfTestService.save(testUser, allPerfTest.get(0));

		int checkLoop = 0;
		while (true) {
			int agentCount = agentManager.getAllAttachedAgents().size();
			if (agentCount != 0 || checkLoop++ > 20) {
				break;
			}
			sleep(1000);
		}

		List<AgentIdentity> agentIdentities = new ArrayList<>(agentManager.getAllAttachedAgents());
		for (AgentIdentity agentIdentity : agentIdentities) {
			AgentControllerIdentityImplementation identity = cast(agentIdentity);
			identity.setSubregion("");
			AgentInfo agentInfo = new AgentInfo();
			agentInfo.setIp(identity.getIp());
			agentInfo.setPort(0);
			agentInfo.setName(identity.getName());
			agentInfo.setApproved(true);
			agentInfo.setState(READY);
			agentInfo.setAgentIdentity(identity);
			agentInfoStore.updateAgentInfo(agentInfo.getAgentKey(), agentInfo);
		}

		sleep(1000);
		List<AgentInfo> agentList = agentService.getAllAttached();
		for (AgentInfo each : agentList) {
			agentService.approve(each.getIp(), each.getName(), true);
		}
	}

	@Test
	public void testDoTest() throws IOException {
		assertThat(agentService.getAllAttachedFreeApprovedAgents().size(), is(1));
		perfTestRunnable.doStart();
		sleep(10000);
		assertThat(perfTestService.getAllTesting().size(), is(1));
		perfTestService.stop(getTestUser(), currentTest.getId());
		sleep(5000);
		perfTestRunnable.doFinish(false);
		assertThat(perfTestService.getAllTesting().size(), is(0));
		assertThat(perfTestService.getNextRunnablePerfTestPerfTestCandidate(), nullValue());
		assertThat(consoleManager.getConsoleInUse().size(), is(0));
	}

	private boolean ended = false;

	@Test
	public void testStartConsole() throws IOException {
		// Get perf test
		PerfTest perfTest = perfTestService.getNextRunnablePerfTestPerfTestCandidate();
		perfTest.setScriptName("/hello/world.py");
		assertThat(perfTest, not(nullValue()));

		// Distribute files
		GrinderProperties grinderProperties = perfTestService.prepareTest(perfTest);

		// Start console
		SingleConsole singleConsole = perfTestRunnable.startConsole(perfTest);
		assertThat(singleConsole, not(nullValue()));
		assertThat(singleConsole.getConsolePort(), is(perfTest.getPort()));

		// Start agents
		perfTest.setAgentCount(1);
		singleConsole.setReportPath(perfTestService.getReportFileDirectory(perfTest));

		perfTestRunnable.startAgentsOn(perfTest, grinderProperties, singleConsole);
		sleep(3000);
		perfTestRunnable.distributeFileOn(perfTest, singleConsole);

		singleConsole.addSamplingLifeCycleListener(new SamplingLifeCycleListener() {
			@Override
			public void onSamplingStarted() {
				System.out.println("Sampling is started");
			}

			@Override
			public void onSamplingEnded() {
				ended = true;
			}

			@Override
			public void onSampling(File file, StatisticsSet intervalStatistics, StatisticsSet cumulativeStatistics) {
			}
		});

		// Run test
		perfTestRunnable.runTestOn(perfTest, grinderProperties, singleConsole);
		sleep(10000);
		perfTestService.stop(getTestUser(), currentTest.getId());
		singleConsole.waitUntilAllAgentDisconnected();
		perfTestRunnable.doFinish(false);
		// Waiting for termination


		sleep(5000);
		assertThat(perfTestService.getAllTesting().size(), is(0));
		assertThat(perfTestService.getNextRunnablePerfTestPerfTestCandidate(), nullValue());
		assertThat(consoleManager.getConsoleInUse().size(), is(0));
	}

	private void prepareUserRepo() throws IOException {
		File userRepoDirectory = fileEntityRepository.getUserRepoDirectory(null);
		FileUtils.deleteQuietly(userRepoDirectory);
		CompressionUtils.unzip(new ClassPathResource("TEST_USER.zip").getFile(), userRepoDirectory.getParentFile());
		FileEntry fileEntryDir = new FileEntry();
		fileEntryDir.setPath("/hello");
		fileEntryDir.setFileType(FileType.DIR);
		fileEntityRepository.save(getTestUser(), fileEntryDir, null);

		FileEntry fileEntry = new FileEntry();
		fileEntry.setPath("/hello/world.py");
		String worldString = IOUtils.toString(new ClassPathResource("world.py").getInputStream(), defaultCharset());
		fileEntry.setContent(worldString);
		fileEntry.setFileType(FileType.PYTHON_SCRIPT);
		fileEntityRepository.save(getTestUser(), fileEntry, "UTF-8");
	}
}
