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

import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.Test;
import org.ngrinder.AbstractNGrinderTransactionalTest;
import org.ngrinder.common.util.CompressionUtils;
import org.ngrinder.infra.config.Config;
import org.ngrinder.script.model.FileEntry;
import org.ngrinder.script.model.FileSaveParams;
import org.ngrinder.script.model.ScriptCreationParams;
import org.ngrinder.script.model.ScriptValidationParams;
import org.ngrinder.script.repository.MockFileEntityRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class FileEntryApiControllerTest extends AbstractNGrinderTransactionalTest {

	@Autowired
	private FileEntryApiController fileEntryController;

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
		fileEntryController.addFolder(getTestUser(), "", path);
		// create
		response = fileEntryController.createScript(getTestUser(), path, new ScriptCreationParams("new_file.py", "test.com", null, "jython", false));
		FileEntry script = (FileEntry) response.get("file");
		script.setContent(script.getContent() + "#test comment");
		fileEntryController.save(getTestUser(), new FileSaveParams(script, null, "", false));
		fileEntryController.validate(getTestUser(), new ScriptValidationParams(script, "test.com"));
		// save and get
		response = fileEntryController.getOne(getTestUser(), script.getPath(), -1L);
		FileEntry newScript = (FileEntry) response.get("file");
		assertThat(newScript.getFileName(), is(script.getFileName()));
		assertThat(newScript.getContent(), is(script.getContent()));

		// List<Long> versionList = newScript.getRevisions();
		// reversion list is not implemented yet.
		// assertThat(versionList.size(), is(2));
		fileEntryController.search(getTestUser(), "test");

		fileEntryController.delete(getTestUser(), Arrays.asList(path + "/new_file.py"));
		List<FileEntry> scriptList = fileEntryController.getAll(getTestUser(), path);
		assertThat(scriptList.size(), is(0));
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testCreateFolderSaveAndGet() {
		Map<String, Object> response;
		String path = "test2-path";

		// add folder
		fileEntryController.addFolder(getTestUser(), "", path);
		// create
		response = fileEntryController.createScript(getTestUser(), path, new ScriptCreationParams("file-for-search.py", "test.com", null, "jython", false));
		FileEntry script = (FileEntry) response.get("file");
		fileEntryController.save(getTestUser(), new FileSaveParams(script, null, "", false));

		// save another script
		script.setPath(script.getPath().replace("file-for-search", "new-file-for-search"));
		fileEntryController.save(getTestUser(), new FileSaveParams(script, null, "", false));
		// save and get
		fileEntryController.getOne(getTestUser(), script.getPath(), -1L);

		Collection<FileEntry> searchResult = fileEntryController.search(getTestUser(), "file-for-search");
		assertThat(searchResult.size(), is(2));

		// delete both files
		fileEntryController.delete(getTestUser(), Arrays.asList(path + "/file-for-search.py", path + "/new-file-for-search.py"));
		List<FileEntry> scriptList = fileEntryController.getAll(getTestUser(), path);
		assertThat(scriptList.size(), is(0));
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testUploadFiles() {
		String path = "test-upload-path";
		fileEntryController.addFolder(getTestUser(), "", path);

		String upFileName = "Uploaded";
		MultipartFile upFile = new MockMultipartFile("Uploaded.py", "Uploaded.py", null, "#test content...".getBytes());
		path = path + "/" + upFileName;
		fileEntryController.uploadFile(getTestUser(), path, "Uploaded file desc.", upFile);
		Collection<FileEntry> searchResult = fileEntryController.search(getTestUser(), "Uploaded");
		assertThat(searchResult.size(), is(1));
	}

	@Test
	public void testDownload() {
		String path = "download-path";
		String fileName = "download_file.py";
		fileEntryController.addFolder(getTestUser(), "", path);
		Map<String, Object> responseMap = fileEntryController.createScript(getTestUser(), path, new ScriptCreationParams(fileName, "test.com", null, "jython", false));

		FileEntry script = (FileEntry) responseMap.get("file");
		script.setContent(script.getContent() + "#test comment");
		fileEntryController.save(getTestUser(), new FileSaveParams(script, null, "", false));

		fileEntryController.createScript(getTestUser(), path, new ScriptCreationParams(fileName, "", null, "", false));

		MockHttpServletResponse response = new MockHttpServletResponse();
		path = path + "/" + fileName;
		fileEntryController.download(getTestUser(), path, response);
	}
}
