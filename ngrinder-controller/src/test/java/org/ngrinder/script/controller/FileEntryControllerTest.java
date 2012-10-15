/*
 * Copyright (C) 2012 - 2012 NHN Corporation
 * All rights reserved.
 *
 * This file is part of The nGrinder software distribution. Refer to
 * the file LICENSE which is part of The nGrinder distribution for
 * licensing details. The nGrinder distribution is available on the
 * Internet at http://nhnopensource.org/ngrinder
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS
 * FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE
 * COPYRIGHT HOLDERS OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.ngrinder.script.controller;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import org.springframework.mock.web.MockHttpServletResponse;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.Test;
import org.ngrinder.AbstractNGrinderTransactionalTest;
import org.ngrinder.infra.config.Config;
import org.ngrinder.script.model.FileEntry;
import org.ngrinder.script.repository.MockFileEntityRepsotory;
import org.ngrinder.script.util.CompressionUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.ui.ModelMap;
import org.springframework.web.multipart.MultipartFile;

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
	private MockFileEntityRepsotory fileEntityRepository;

	@Autowired
	private Config config;

	@Before
	public void before() throws IOException {
		CompressionUtil compressUtil = new CompressionUtil();

		File tempRepo = new File(System.getProperty("java.io.tmpdir"), "repo");
		fileEntityRepository.setUserRepository(new File(tempRepo, getTestUser().getUserId()));
		tempRepo.deleteOnExit();
		File testUserRoot = fileEntityRepository.getUserRepoDirectory(getTestUser()).getParentFile();
		FileUtils.deleteQuietly(testUserRoot);
		testUserRoot.mkdirs();
		compressUtil.unzip(new ClassPathResource("TEST_USER.zip").getFile(), testUserRoot);
		testUserRoot.deleteOnExit();

		config.getSystemProperties().addProperty("http.url", "http://127.0.0.1:80");
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testSaveAndGet() {
		ModelMap model = new ModelMap();
		String path = "test1-path";
		scriptController.addFolder(getTestUser(), "", path, model);
		// create
		scriptController.getCreateForm(getTestUser(), path, "test.com", "new_file.py", null, false, model);

		FileEntry script = (FileEntry) model.get("file");
		script.setContent(script.getContent() + "#test comment");
		scriptController.saveFileEntry(getTestUser(), path, script, "", false, model);
		scriptController.validate(getTestUser(), script, "test.com");
		// save and get
		model.clear();
		scriptController.getDetail(getTestUser(), script.getPath(), -1L, model);
		FileEntry newScript = (FileEntry) model.get("file");
		assertThat(newScript.getFileName(), is(script.getFileName()));
		assertThat(newScript.getContent(), is(script.getContent()));

		// List<Long> versionList = newScript.getRevisions();
		// reversion list is not implemented yet.
		// assertThat(versionList.size(), is(2));
		model.clear();
		scriptController.searchFileEntity(getTestUser(), "test", model);

		model.clear();
		scriptController.delete(getTestUser(), path, "new_file.py", model);
		scriptController.get(getTestUser(), path, model);
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
		scriptController.getCreateForm(getTestUser(), path, "test.com", "file-for-search.py", null, false, model);
		FileEntry script = (FileEntry) model.get("file");
		scriptController.saveFileEntry(getTestUser(), path, script, "", false, model);

		// save another script
		model.clear();
		script.setPath(script.getPath().replace("file-for-search", "new-file-for-search"));
		scriptController.saveFileEntry(getTestUser(), path, script, "", false, model);
		// save and get
		model.clear();
		scriptController.getDetail(getTestUser(), script.getPath(), -1L, model);

		model.clear();
		scriptController.searchFileEntity(getTestUser(), "file-for-search", model);
		Collection<FileEntry> searchResult = (Collection<FileEntry>) model.get("files");
		assertThat(searchResult.size(), is(2));

		model.clear();
		// delete both files
		scriptController.delete(getTestUser(), path, "file-for-search.py,new-file-for-search.py", model);
		scriptController.get(getTestUser(), path, model);
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
		scriptController.uploadFiles(getTestUser(), path, "Uploaded file desc.", upFile, model);
		model.clear();
		scriptController.searchFileEntity(getTestUser(), "Uploaded", model);
		Collection<FileEntry> searchResult = (Collection<FileEntry>) model.get("files");
		assertThat(searchResult.size(), is(1));
	}

	@Test
	public void testDownload() {
		ModelMap model = new ModelMap();
		String path = "download-path";
		String fileName = "download_file.py";
		scriptController.addFolder(getTestUser(), "", path, model);
		scriptController.getCreateForm(getTestUser(), path, "test.com", fileName, null, false, model);

		FileEntry script = (FileEntry) model.get("file");
		script.setContent(script.getContent() + "#test comment");
		scriptController.saveFileEntry(getTestUser(), path, script, "", false, model);

		scriptController.getCreateForm(getTestUser(), path, "", fileName, null, false, model);

		MockHttpServletResponse response = new MockHttpServletResponse();
		path = path + "/" + fileName;
		scriptController.download(getTestUser(), path, response);
	}
}
