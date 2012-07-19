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

import java.io.IOException;
import java.util.Collection;
import java.util.List;

import org.apache.commons.pool.impl.GenericKeyedObjectPool.Config;
import org.ngrinder.common.controller.NGrinderBaseController;
import org.ngrinder.infra.spring.RemainedPath;
import org.ngrinder.model.User;
import org.ngrinder.script.model.FileEntry;
import org.ngrinder.script.service.FileEntryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.web.util.UrlUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;

@Controller
@RequestMapping("/script")
public class ScriptController extends NGrinderBaseController {

	@SuppressWarnings("unused")
	private static final Logger LOG = LoggerFactory.getLogger(ScriptController.class);

	@Autowired
	private FileEntryService fileEntryService;

	@RequestMapping("/list/**")
	public String get(User user, @RemainedPath String path, ModelMap model) { // "fileName"
		List<FileEntry> files = fileEntryService.getFileEntries(user, path);
		model.addAttribute("files", files);
		model.addAttribute("currentPath", path);
		model.addAttribute("svnUrl", fileEntryService.getSvnUrl(user, path));
		return "script/scriptList";
	}

	@RequestMapping(value = "/create/**", params = "type=folder", method = RequestMethod.POST)
	public String addFolder(User user, @RemainedPath String path, @RequestParam String folderName, ModelMap model) { // "fileName"
		try {
			fileEntryService.addFolder(user, path, folderName);
		} catch (Exception e) {
			return "error/errors";
		}
		return "redirect:/script/list/" + path;
	}

	@RequestMapping(value = "/create/**", params = "type=script", method = RequestMethod.POST)
	public String getCreateForm(User user, @RemainedPath String path, @RequestParam String language,
			@RequestParam String testUrl, @RequestParam String fileName, ModelMap model) { // "fileName"
		if (fileEntryService.hasFileEntry(user, path + "/" + fileName)) {
			return "error/duplicated";
		}

		model.addAttribute("file", fileEntryService.prepareNewEntry(user, path, fileName, language, testUrl));
		return "script/scriptEditor";
	}

	@RequestMapping("/detail/**")
	public String getDetail(User user, @RemainedPath String path, ModelMap model) { // "fileName"
		FileEntry script = fileEntryService.getFileEntry(user, path);
		if (!script.getFileType().isEditable()) {
			return "error/errors";
		}
		model.addAttribute("script", script);
		return "script/scriptEditor";
	}

	@RequestMapping("/compare/**")
	public String getDiff(User user, @RemainedPath String path, @RequestParam Long revision, ModelMap model) {
		FileEntry file = fileEntryService.getFileEntry(user, path);
		FileEntry oldFile = fileEntryService.getFileEntry(user, path, revision);
		model.addAttribute("file", file);
		model.addAttribute("oldFile", oldFile);
		return "script/diff";
	}

	@RequestMapping(value = "/search/**")
	public String searchFileEntity(User user, final @RequestParam(required = true) String query, ModelMap model) {
		Collection<FileEntry> searchResult = Collections2.filter(fileEntryService.getAllFileEntries(user),
				new Predicate<FileEntry>() {
					@Override
					public boolean apply(FileEntry input) {
						return input.getPath().contains(query);
					}
				});
		model.addAttribute("files", searchResult);
		model.addAttribute("currentPath", "");
		return "script/scriptList";
	}

	@RequestMapping(value = "/save/**", method = RequestMethod.POST)
	public String create(User user, @RemainedPath String path, FileEntry script, ModelMap model) {
		// TODO : Fix scriptEditor.ftl to pass right script parameter
		fileEntryService.save(user, script);
		return get(user, path, model);
	}

	@RequestMapping(value = "/upload/**", method = RequestMethod.POST)
	public String uploadFiles(User user, @RemainedPath String path, FileEntry script,
			@RequestParam("uploadFile") MultipartFile file, ModelMap model) throws IOException {
		script.setContentBytes(file.getBytes());
		fileEntryService.save(user, script);
		return get(user, path, model);
	}

	@RequestMapping(value = "/delete/**")
	public String delete(User user, @RemainedPath String path, @RequestParam String filesString, ModelMap model) {
		String[] files = filesString.split(",");
		fileEntryService.delete(user, path, files);
		return "redirect:/script/list" + path;
	}

}
