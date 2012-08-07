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

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.Test;
import org.ngrinder.AbstractNGrinderTransactionalTest;
import org.ngrinder.script.model.FileEntry;
import org.ngrinder.script.repository.FileEntityRepository;
import org.ngrinder.script.service.FileEntryService;
import org.ngrinder.script.service.MockFileEntityRepsotory;
import org.ngrinder.script.util.CompressionUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.ui.ModelMap;

/**
 * Class description.
 *
 * @author Mavlarn
 * @since 3.0
 */
public class ScriptControllerTest extends AbstractNGrinderTransactionalTest {
	
	@Autowired
	private ScriptController scriptController;

	@Autowired
	private FileEntityRepository fileEntityRepository;
	
	@Before
	public void before() throws IOException {
		CompressionUtil compressUtil = new CompressionUtil();
		
//		FileEntryService service = (FileEntryService) ReflectionTestUtils
//				.getField(scriptController, "fileEntryService");
//		MockFileEntityRepsotory repo = new MockFileEntityRepsotory();
//		File file = new File(System.getProperty("java.io.tmpdir"), "repo");
//		FileUtils.deleteQuietly(file);
//		compressUtil.unzip(new ClassPathResource("TEST_USER.zip").getFile(), file);
//		repo.setUserRepository(new File(file, getTestUser().getUserId()));
//		ReflectionTestUtils.setField(service, "fileEntityRepository", repo);
//		file.deleteOnExit();
		
		
		File testUserRoot = fileEntityRepository.getUserRepoDirectory(getTestUser()).getParentFile();
		FileUtils.deleteQuietly(testUserRoot);
		testUserRoot.mkdirs();
		compressUtil.unzip(new ClassPathResource("TEST_USER.zip").getFile(), testUserRoot);
		testUserRoot.deleteOnExit();
	}
	
	@Test
	public void testGet() {
		ModelMap model = new ModelMap();
		scriptController.get(getTestUser(), null, model);
	}

	@Test
	public void testAddFolder() {
		ModelMap model = new ModelMap();
		scriptController.addFolder(getTestUser(), null, "new_folder", model);
	}

	@Test
	public void testSaveAndGet() {
		ModelMap model = new ModelMap();
		//create
		scriptController.getCreateForm(getTestUser(), null, "test.com", "new_file.py", model);

		FileEntry script = (FileEntry)model.get("file");
		script.setContent("#test comment");
		scriptController.saveScript(getTestUser(), script.getPath(), script, model);
		//save and get
		model.clear();
		scriptController.getDetail(getTestUser(), null, model);
		FileEntry newScript = (FileEntry)model.get("file");
		assertThat(newScript.getFileName(), is(script.getFileName()));
		assertThat(newScript.getContent(), is(script.getContent()));
	}

	@Test
	public void testGetDetail() {
		ModelMap model = new ModelMap();
		scriptController.getDetail(getTestUser(), null, model);
	}

	@Test
	public void testGetDiff() {
		ModelMap model = new ModelMap();
		scriptController.addFolder(getTestUser(), null, "new_folder", model);
	}

	@Test
	public void testSearchFileEntity() {
		ModelMap model = new ModelMap();
		scriptController.addFolder(getTestUser(), null, "new_folder", model);
	}

	@Test
	public void testUploadFiles() {
		ModelMap model = new ModelMap();
		scriptController.addFolder(getTestUser(), null, "new_folder", model);
	}

	@Test
	public void testDelete() {
		ModelMap model = new ModelMap();
		scriptController.addFolder(getTestUser(), null, "new_folder", model);
	}

}
