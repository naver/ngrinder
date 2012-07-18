package org.ngrinder.script.service;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.Test;
import org.ngrinder.AbstractNGNinderTransactionalTest;
import org.ngrinder.infra.init.DBInit;
import org.ngrinder.model.Role;
import org.ngrinder.model.User;
import org.ngrinder.script.model.FileEntry;
import org.ngrinder.script.model.FileType;
import org.ngrinder.script.util.CompressionUtil;
import org.ngrinder.user.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;

public class FileEntityRepositoryTest extends AbstractNGNinderTransactionalTest {

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
		fileEntry.setFileName("helloworld.txt");
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
	public void testPerfTest2() {
		FileEntry fileEntry = new FileEntry();
		fileEntry.setContent("HELLO WORLD2");
		fileEntry.setEncoding("UTF-8");
		fileEntry.setPath("helloworld.txt");
		fileEntry.setFileName("helloworld.txt");
		fileEntry.setDescription("WOW");
		User user = userService.getUserById("user1");
		repo.save(user, fileEntry, fileEntry.getEncoding());
	}

}
