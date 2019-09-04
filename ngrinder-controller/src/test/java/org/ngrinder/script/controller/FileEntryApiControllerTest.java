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
package org.ngrinder.script.controller;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.Test;
import org.ngrinder.AbstractNGrinderTransactionalTest;
import org.ngrinder.common.util.CompressionUtils;
import org.ngrinder.infra.config.Config;
import org.ngrinder.script.model.FileEntry;
import org.ngrinder.script.repository.MockFileEntityRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class FileEntryApiControllerTest extends AbstractNGrinderTransactionalTest {

	@Autowired
	private FileEntryApiController scriptController;

	@Autowired
	private FileEntryController fileEntryController;

	@Autowired
	private MockFileEntityRepository fileEntityRepository;

	@Autowired
	private Config config;

	@Before
	public void before() throws IOException {
		File tempRepo = new File(System.getProperty("java.io.tmpdir"), "repo");
		fileEntityRepository.setUserRepository(new File(tempRepo, getTestUser().getUserId()));
		tempRepo.deleteOnExit();
		File testUserRoot = fileEntityRepository.getUserRepoDirectory(getTestUser()).getParentFile();
		FileUtils.deleteQuietly(testUserRoot);
		testUserRoot.mkdirs();
		CompressionUtils.unzip(new ClassPathResource("TEST_USER.zip").getFile(), testUserRoot);
		testUserRoot.deleteOnExit();

		config.getControllerProperties().addProperty("http.url", "http://127.0.0.1:80");
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testCreateAndGet() {
		Map<String, Object> response;
		String path = "test1-path";
		scriptController.addFolder(getTestUser(), "", path);
		// create
		response = scriptController.createScript(getTestUser(), path, "new_file.py", "test.com", null, "jython", false);
		FileEntry script = (FileEntry) response.get("file");
		script.setContent(script.getContent() + "#test comment");
		scriptController.save(getTestUser(), script, null, "", false);
		scriptController.validate(getTestUser(), script, "test.com");
		// save and get
		response = scriptController.getOne(getTestUser(), script.getPath(), -1L);
		FileEntry newScript = (FileEntry) response.get("file");
		assertThat(newScript.getFileName(), is(script.getFileName()));
		assertThat(newScript.getContent(), is(script.getContent()));

		// List<Long> versionList = newScript.getRevisions();
		// reversion list is not implemented yet.
		// assertThat(versionList.size(), is(2));
		scriptController.search(getTestUser(), "test");

		scriptController.delete(getTestUser(), Arrays.asList(path + "/new_file.py"));
		List<FileEntry> scriptList = scriptController.getAll(getTestUser(), path);
		assertThat(scriptList.size(), is(0));
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testCreateFolderSaveAndGet() {
		Map<String, Object> response;
		String path = "test2-path";

		// add folder
		scriptController.addFolder(getTestUser(), "", path);
		// create
		response = scriptController.createScript(getTestUser(), path, "file-for-search.py", "test.com", null, "jython", false);
		FileEntry script = (FileEntry) response.get("file");
		scriptController.save(getTestUser(), script, null, "", false);

		// save another script
		script.setPath(script.getPath().replace("file-for-search", "new-file-for-search"));
		scriptController.save(getTestUser(), script, null, "", false);
		// save and get
		scriptController.getOne(getTestUser(), script.getPath(), -1L);

		Collection<FileEntry> searchResult = scriptController.search(getTestUser(), "file-for-search");
		assertThat(searchResult.size(), is(2));

		// delete both files
		scriptController.delete(getTestUser(), Arrays.asList(path + "/file-for-search.py", path + "/new-file-for-search.py"));
		List<FileEntry> scriptList = scriptController.getAll(getTestUser(), path);
		assertThat(scriptList.size(), is(0));
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testUploadFiles() {
		String path = "test-upload-path";
		scriptController.addFolder(getTestUser(), "", path);

		String upFileName = "Uploaded";
		MultipartFile upFile = new MockMultipartFile("Uploaded.py", "Uploaded.py", null, "#test content...".getBytes());
		path = path + "/" + upFileName;
		scriptController.uploadFile(getTestUser(), path, "Uploaded file desc.", upFile);
		Collection<FileEntry> searchResult = scriptController.search(getTestUser(), "Uploaded");
		assertThat(searchResult.size(), is(1));
	}

	@Test
	public void testDownload() {
		String path = "download-path";
		String fileName = "download_file.py";
		scriptController.addFolder(getTestUser(), "", path);
		Map<String, Object> responseMap = scriptController.createScript(getTestUser(), path, fileName, "test.com", null, "jython", false);

		FileEntry script = (FileEntry) responseMap.get("file");
		script.setContent(script.getContent() + "#test comment");
		scriptController.save(getTestUser(), script, null, "", false);

		scriptController.createScript(getTestUser(), path, fileName, "", null, "", false);

		MockHttpServletResponse response = new MockHttpServletResponse();
		path = path + "/" + fileName;
		fileEntryController.download(getTestUser(), path, response);
	}
}
