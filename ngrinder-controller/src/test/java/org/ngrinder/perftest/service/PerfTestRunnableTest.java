package org.ngrinder.perftest.service;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.List;

import net.grinder.SingleConsole;

import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.Test;
import org.ngrinder.perftest.model.PerfTest;
import org.ngrinder.perftest.model.Status;
import org.ngrinder.script.model.FileEntry;
import org.ngrinder.script.model.FileType;
import org.ngrinder.script.service.MockFileEntityRepsotory;
import org.ngrinder.script.util.CompressionUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;

public class PerfTestRunnableTest extends AbstractPerfTestTransactionalTest {

	@Autowired
	private MockPerfTestRunnable perfTestRunnable;

	@Before
	public void before() {
		clearAllPerfTest();
		createPerfTest("test1", Status.READY, new Date());
		createPerfTest("test2", Status.READY, new Date());
		List<PerfTest> allPerfTest = perfTestService.getAllPerfTest();
		assertThat(allPerfTest.size(), is(2));
	}

	@Test
	public void testDoTest() {
		perfTestRunnable.startTest();
	}

	@Autowired
	public MockFileEntityRepsotory repo;

	@Test
	public void testStartConsole() throws IOException {
		PerfTest perfTestCandiate = perfTestService.getPerfTestCandiate();
		assertThat(perfTestCandiate, not(nullValue()));
		SingleConsole singleConsole = perfTestRunnable.startConsole(perfTestCandiate);
		assertThat(singleConsole, not(nullValue()));
		assertThat(singleConsole.getConsolePort(), not(0));

		prepareUserRepo();

		perfTestRunnable.distributeFileOn(perfTestCandiate, singleConsole);
	}

	private void prepareUserRepo() throws IOException {
		CompressionUtil compressUtil = new CompressionUtil();

		File file = new File(System.getProperty("java.io.tmpdir"), "repo");
		FileUtils.deleteQuietly(file);
		compressUtil.unzip(new ClassPathResource("TEST_USER.zip").getFile(), file);
		repo.setUserRepository(new File(file, "TEST_USER"));
		FileEntry fileEntryDir = new FileEntry();
		fileEntryDir.setPath("/hello");
		fileEntryDir.setFileType(FileType.DIR);
		repo.save(getTestUser(), fileEntryDir, null);

		FileEntry fileEntry = new FileEntry();
		fileEntry.setPath("/hello/world.py");
		fileEntry.setContent("print 'HELLO'");
		fileEntry.setFileType(FileType.PYTHON_SCRIPT);
		repo.save(getTestUser(), fileEntry, "UTF-8");
	}
}
