package org.ngrinder.script.service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import junit.framework.Assert;

import org.junit.After;
import org.junit.Test;
import org.ngrinder.NGrinderIocTransactionalTestBase;
import org.ngrinder.script.model.Library;

import org.ngrinder.user.model.User;
import org.ngrinder.user.util.UserUtil;
import org.springframework.beans.factory.annotation.Autowired;

public class LibraryServiceTest extends NGrinderIocTransactionalTestBase {

	private static Set<Library> libraries = new HashSet<Library>();

	@Autowired
	private LibraryService libraryService;

	@After
	public void clearLibraries() {
		for (Library library : libraries) {
			libraryService.deleteLibrary(library.getFileName());
		}
	}

	@Test
	public void testSaveLibrary() {
		List<Library> libraries1 = libraryService.getLibraries();

		this.saveLibrary("save");

		List<Library> libraries2 = libraryService.getLibraries();

		Assert.assertEquals(libraries1.size() + 1, libraries2.size());
	}

	@Test
	public void testDeleteLibrary() {
		List<Library> libraries1 = libraryService.getLibraries();

		Library library = this.saveLibrary("delete");
		libraryService.deleteLibrary(library.getFileName());

		List<Library> libraries2 = libraryService.getLibraries();

		Assert.assertEquals(libraries1.size(), libraries2.size());
	}

	@Test
	public void testgetLibraries() {
		this.clearLibraries();

		this.saveLibrary("get1");
		this.saveLibrary("get2");
		this.saveLibrary("get3");
		List<Library> libraries = libraryService.getLibraries();

		Assert.assertEquals(3, libraries.size());
	}

	@Test
	public void testgetLibraries2() {

		User user = new User();
		user.setId(123L);
		user.setUserName("tmp_user9");
		UserUtil.setCurrentUser(user);
		List<Library> libraries = libraryService.getLibraries();

		Assert.assertEquals(0, libraries.size());
	}

	private Library saveLibrary(String key) {
		if (null == key) {
			key = "";
		}
		Library library = new Library();
		library.setContentBytes(("testScript" + key).getBytes());
		library.setFileName("abc" + key + ".jar");
		library.setFileSize(123);

		libraryService.saveLibrary(library);

		libraries.add(library);
		return library;
	}
}
