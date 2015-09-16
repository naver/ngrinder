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
import org.ngrinder.script.repository.MockFileEntityRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.ui.ModelMap;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributesModelMap;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

/**
 * Class description.
 * 
 * @author Mavlarn
 * @since 3.0
 */
public class FileEntryControllerTest extends AbstractNGrinderTransactionalTest {

	@Autowired
	private FileEntryController scriptController;

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
	public void testSaveAndGet() {
		ModelMap model = new ModelMap();
		String path = "test1-path";
		scriptController.addFolder(getTestUser(), "", path, model);
		// create
		scriptController.createForm(getTestUser(), path, "test.com", "new_file.py", "jython", false,
						null, new RedirectAttributesModelMap(), model);

		FileEntry script = (FileEntry) model.get("file");
		script.setContent(script.getContent() + "#test comment");
		scriptController.save(getTestUser(), script, null, "", false, model);
		scriptController.validate(getTestUser(), script, "test.com");
		// save and get
		model.clear();
		scriptController.getOne(getTestUser(), script.getPath(), -1L, model);
		FileEntry newScript = (FileEntry) model.get("file");
		assertThat(newScript.getFileName(), is(script.getFileName()));
		assertThat(newScript.getContent(), is(script.getContent()));

		// List<Long> versionList = newScript.getRevisions();
		// reversion list is not implemented yet.
		// assertThat(versionList.size(), is(2));
		model.clear();
		scriptController.search(getTestUser(), "test", model);

		model.clear();
		scriptController.delete(getTestUser(), path, "new_file.py");
		scriptController.getAll(getTestUser(), path, model);
		List<FileEntry> scriptList = (List<FileEntry>) model.get("files");
		assertThat(scriptList.size(), is(0));
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testCreateFolderSaveAndGet() {
		ModelMap model = new ModelMap();
		String path = "test2-path";

		// add folder
		scriptController.addFolder(getTestUser(), "", path, model);
		// create
		scriptController.createForm(getTestUser(), path, "test.com", "file-for-search.py", "jython", false,
						null, new RedirectAttributesModelMap(), model);
		FileEntry script = (FileEntry) model.get("file");
		scriptController.save(getTestUser(), script, null, "", false, model);

		// save another script
		model.clear();
		script.setPath(script.getPath().replace("file-for-search", "new-file-for-search"));
		scriptController.save(getTestUser(), script, null, "", false, model);
		// save and get
		model.clear();
		scriptController.getOne(getTestUser(), script.getPath(), -1L, model);

		model.clear();
		scriptController.search(getTestUser(), "file-for-search", model);
		Collection<FileEntry> searchResult = (Collection<FileEntry>) model.get("files");
		assertThat(searchResult.size(), is(2));

		model.clear();
		// delete both files
		scriptController.delete(getTestUser(), path, "file-for-search.py,new-file-for-search.py");
		scriptController.getAll(getTestUser(), path, model);
		List<FileEntry> scriptList = (List<FileEntry>) model.get("files");
		assertThat(scriptList.size(), is(0));
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testUploadFiles() {
		ModelMap model = new ModelMap();
		String path = "test-upload-path";
		scriptController.addFolder(getTestUser(), "", path, model);

		String upFileName = "Uploaded";
		MultipartFile upFile = new MockMultipartFile("Uploaded.py", "Uploaded.py", null, "#test content...".getBytes());
		path = path + "/" + upFileName;
		scriptController.upload(getTestUser(), path, "Uploaded file desc.", upFile, model);
		model.clear();
		scriptController.search(getTestUser(), "Uploaded", model);
		Collection<FileEntry> searchResult = (Collection<FileEntry>) model.get("files");
		assertThat(searchResult.size(), is(1));
	}

	@Test
	public void testDownload() {
		ModelMap model = new ModelMap();
		String path = "download-path";
		String fileName = "download_file.py";
		scriptController.addFolder(getTestUser(), "", path, model);
		RedirectAttributesModelMap attrMap = new RedirectAttributesModelMap();
		scriptController.createForm(getTestUser(), path, "test.com", fileName, "jython", false,
			null, attrMap, model);

		FileEntry script = (FileEntry) model.get("file");
		script.setContent(script.getContent() + "#test comment");
		scriptController.save(getTestUser(), script, null, "", false, model);

		scriptController.createForm(getTestUser(), path, "", fileName, "", false, null, attrMap,
			model);

		MockHttpServletResponse response = new MockHttpServletResponse();
		path = path + "/" + fileName;
		scriptController.download(getTestUser(), path, response);
	}
}
