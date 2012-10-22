package org.ngrinder.perftest.service;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;

import java.io.File;
import java.io.IOException;
import java.util.List;

import net.grinder.SingleConsole;

import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Test;
import org.ngrinder.common.constant.NGrinderConstants;
import org.ngrinder.model.PerfTest;
import org.ngrinder.model.Status;
import org.ngrinder.perftest.controller.PerfTestController;
import org.ngrinder.script.model.FileEntry;
import org.ngrinder.script.repository.MockFileEntityRepsotory;
import org.ngrinder.script.util.CompressionUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.ui.ModelMap;

public class PerfTestCancellationTest extends AbstractPerfTestTransactionalTest implements NGrinderConstants {

	@Autowired
	private MockPerfTestRunnableForCanclation perfTestRunnable;

	@Autowired
	public MockFileEntityRepsotory fileEntityRepository;

	@Autowired
	public ConsoleManager consoleManager;

	@Autowired
	public PerfTestController perfTestController;
	
	private PerfTest perfTest = null;
	
	@Before
	public void before() throws IOException {
		CompressionUtil compressUtil = new CompressionUtil();
		File tempRepo = new File(System.getProperty("java.io.tmpdir"), "repo");
		fileEntityRepository.setUserRepository(new File(tempRepo, getTestUser().getUserId()));
		File testUserRoot = fileEntityRepository.getUserRepoDirectory(getTestUser()).getParentFile();

		testUserRoot.mkdirs();
		compressUtil.unzip(new ClassPathResource("TEST_USER.zip").getFile(), testUserRoot);
		testUserRoot.deleteOnExit();

		FileEntry fileEntry = new FileEntry();
		fileEntry.setPath("test1.py");
		String worldString = IOUtils.toString(new ClassPathResource("world.py").getInputStream());
		if (fileEntry.getFileType().isEditable()) {
			fileEntry.setContent(worldString);
		} else {
			fileEntry.setContentBytes(worldString.getBytes());
		}
		fileEntityRepository.save(getTestUser(), fileEntry, "UTF-8");

		clearAllPerfTest();
		perfTest = createPerfTest("test1", Status.READY, null);
		List<PerfTest> allPerfTest = perfTestService.getAllPerfTest();
		assertThat(allPerfTest.size(), is(1));
		assertThat(consoleManager.getConsoleInUse().size(), is(0));
		
		
	}


	@Test
	public void testTestCancelationDuringPreparation() throws IOException {
		doCancel(1);
	}

	@Test
	public void testTestCancelationDuringPreparationOnSecond() throws IOException {
		doCancel(2);
	}

	private void doCancel(int count) {
		// When the stop perftest is requested
		perfTestRunnable.setRunnable(new Runnable() {
			@Override
			public void run() {
				SingleConsole singleConsole = consoleManager.getConsoleInUse().get(0);
				assertThat(singleConsole, notNullValue());
				perfTestController.stopPerfTests(getTestUser(), new ModelMap(), String.valueOf(perfTest.getId()));
			}
		}, 1);
		perfTestRunnable.testDrive();
		List<PerfTest> allPerfTest = perfTestService.getAllPerfTest();
		// Then
		assertThat(allPerfTest.get(0).getStatus(), is(Status.CANCELED));
		assertThat(consoleManager.getConsoleInUse().size(), is(0));
	}
	
	@Test
	public void testTestCancelationDuringExecutionPhase() throws IOException {
		// Given the testing perftest
		perfTest = createPerfTest("test1", Status.TESTING, null);
		// When the stop is requested
		perfTestController.stopPerfTests(getTestUser(), new ModelMap(), String.valueOf(perfTest.getId()));
		perfTestRunnable.finishTest();
		// Then it should be canceled.
		assertThat(perfTestService.getPerfTest(perfTest.getId()).getStatus(), is(Status.CANCELED));
		assertThat(consoleManager.getConsoleInUse().size(), is(0));
		
	}

}
