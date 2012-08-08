package org.ngrinder.script.repository;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Test;
import org.ngrinder.AbstractNGrinderTransactionalTest;
import org.ngrinder.infra.init.DBInit;
import org.ngrinder.script.model.FileEntry;
import org.ngrinder.script.model.FileType;
import org.ngrinder.script.service.MockFileEntityRepsotory;
import org.ngrinder.script.util.CompressionUtil;
import org.ngrinder.user.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.tmatesoft.svn.core.wc.SVNRevision;

public class FileEntryRepositoryTest extends AbstractNGrinderTransactionalTest {

	public MockFileEntityRepsotory repo = new MockFileEntityRepsotory();

	@Autowired
	public UserService userService;

	@Autowired
	public DBInit dbinit;

	/**
	 * Locate dumped user1 repo into tempdir
	 * 
	 * @throws IOException
	 */
	@Before
	public void before() throws IOException {
		CompressionUtil compressUtil = new CompressionUtil();

		File file = new File(System.getProperty("java.io.tmpdir"), "repo");
		FileUtils.deleteQuietly(file);
		compressUtil.unzip(new ClassPathResource("TEST_USER.zip").getFile(), file);
		repo.setUserRepository(new File(file, testUser.getUserId()));
	}

	@Test
	public void testFileEntitySaveAndDelte() {
		FileEntry fileEntry = new FileEntry();
		fileEntry.setContent("HELLO WORLD2");
		fileEntry.setEncoding("UTF-8");
		fileEntry.setPath("helloworld.txt");
		fileEntry.setDescription("WOW");
		int size = repo.findAll(testUser).size();
		repo.save(testUser, fileEntry, fileEntry.getEncoding());
		fileEntry.setPath("www");
		fileEntry.setFileType(FileType.DIR);
		repo.save(testUser, fileEntry, null);
		fileEntry.setPath("www/aa.py");
		fileEntry.setFileType(FileType.PYTHON_SCRIPT);
		repo.save(testUser, fileEntry, "UTF-8");
		assertThat(repo.findAll(testUser).size(), is(size + 2));
		repo.delete(testUser, new String[] { "helloworld.txt" });
		assertThat(repo.findAll(testUser).size(), is(size + 1));

		// Attempt to create duplicated path
		fileEntry.setPath("www");
		fileEntry.setFileType(FileType.DIR);
		try {
			// It should fail
			repo.save(testUser, fileEntry, null);
			fail("duplicated insert should be failed");
		} catch (Exception e) {

		}

	}

	@Test
	public void testBinarySaveAndLoad() throws IOException {
		FileEntry fileEntry = new FileEntry();
		fileEntry.setContent("HELLO WORLD2");
		fileEntry.setEncoding("UTF-8");
		fileEntry.setPath("helloworld.txt");
		fileEntry.setFileType(FileType.TXT);
		fileEntry.setDescription("WOW");
		repo.save(testUser, fileEntry, fileEntry.getEncoding());
		fileEntry.setPath("hello.zip");
		fileEntry.setEncoding(null);
		fileEntry.setFileType(FileType.UNKNOWN);
		byte[] byteArray = IOUtils.toByteArray(new ClassPathResource("TEST_USER.zip").getInputStream());
		fileEntry.setContentBytes(byteArray);
		repo.save(testUser, fileEntry, null);
		List<FileEntry> findAll = repo.findAll(testUser, "hello.zip");
		FileEntry foundEntry = findAll.get(0);
		System.out.println(foundEntry.getPath());
		assertThat(foundEntry.getFileSize(), is((long) byteArray.length));
		// commit again
		repo.save(testUser, fileEntry, null);
		findAll = repo.findAll(testUser, "hello.zip");
		assertThat(foundEntry.getFileSize(), is((long) byteArray.length));
	}

	@Test
	public void testBinarySaveAndLoadWithFindOne() throws IOException {
		FileEntry fileEntry = new FileEntry();
		fileEntry.setContent("HELLO WORLD2");
		fileEntry.setEncoding("UTF-8");
		fileEntry.setPath("hello.zip");
		fileEntry.setEncoding(null);
		fileEntry.setFileType(FileType.UNKNOWN);
		byte[] byteArray = IOUtils.toByteArray(new ClassPathResource("TEST_USER.zip").getInputStream());
		fileEntry.setContentBytes(byteArray);
		repo.save(testUser, fileEntry, null);
		FileEntry foundEntry = repo.findOne(testUser, "hello.zip", SVNRevision.HEAD);
		System.out.println(foundEntry);
		assertThat(foundEntry.getFileSize(), is((long) byteArray.length));
	}
}
