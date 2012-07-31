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
import org.ngrinder.model.Role;
import org.ngrinder.model.User;
import org.ngrinder.script.model.FileEntry;
import org.ngrinder.script.model.FileType;
import org.ngrinder.script.service.MockFileEntityRepsotory;
import org.ngrinder.script.util.CompressionUtil;
import org.ngrinder.user.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.tmatesoft.svn.core.wc.SVNRevision;

public class FileEntryRepositoryTest extends AbstractNGrinderTransactionalTest {

	@Autowired
	public MockFileEntityRepsotory repo;

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
		dbinit.createUser("user1", "user1", Role.USER, "user1", "user1@gmail.com");
		CompressionUtil compressUtil = new CompressionUtil();

		File file = new File(System.getProperty("java.io.tmpdir"), "repo");
		FileUtils.deleteQuietly(file);
		compressUtil.unzip(new ClassPathResource("user1.zip").getFile(), file);
		repo.setUserRepository(new File(file, "user1"));
	}

	@Test
	public void testFileEntitySaveAndDelte() {
		FileEntry fileEntry = new FileEntry();
		fileEntry.setContent("HELLO WORLD2");
		fileEntry.setEncoding("UTF-8");
		fileEntry.setPath("helloworld.txt");
		fileEntry.setDescription("WOW");
		User user = userService.getUserById("user1");
		int size = repo.findAll(user).size();
		repo.save(user, fileEntry, fileEntry.getEncoding());
		fileEntry.setPath("www");
		fileEntry.setFileType(FileType.DIR);
		repo.save(user, fileEntry, null);
		fileEntry.setPath("www/aa.py");
		fileEntry.setFileType(FileType.PYTHON_SCRIPT);
		repo.save(user, fileEntry, "UTF-8");
		assertThat(repo.findAll(user).size(), is(size + 2));
		repo.delete(user, new String[] { "helloworld.txt" });
		assertThat(repo.findAll(user).size(), is(size + 1));

		// Attempt to create duplicated path
		fileEntry.setPath("www");
		fileEntry.setFileType(FileType.DIR);
		try {
			// It should fail
			repo.save(user, fileEntry, null);
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
		User user = userService.getUserById("user1");
		repo.save(user, fileEntry, fileEntry.getEncoding());
		fileEntry.setPath("hello.zip");
		fileEntry.setEncoding(null);
		fileEntry.setFileType(FileType.UNKNOWN);
		byte[] byteArray = IOUtils.toByteArray(new ClassPathResource("user1.zip").getInputStream());
		fileEntry.setContentBytes(byteArray);
		repo.save(user, fileEntry, null);
		List<FileEntry> findAll = repo.findAll(user, "hello.zip");
		FileEntry foundEntry = findAll.get(0);
		System.out.println(foundEntry.getPath());
		assertThat(foundEntry.getFileSize(), is((long) byteArray.length));
		// commit again
		repo.save(user, fileEntry, null);
		findAll = repo.findAll(user, "hello.zip");
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
		byte[] byteArray = IOUtils.toByteArray(new ClassPathResource("user1.zip").getInputStream());
		fileEntry.setContentBytes(byteArray);
		User user = userService.getUserById("user1");
		repo.save(user, fileEntry, null);
		FileEntry foundEntry = repo.findOne(user, "hello.zip", SVNRevision.HEAD);
		System.out.println(foundEntry);
		assertThat(foundEntry.getFileSize(), is((long) byteArray.length));
	}
}
