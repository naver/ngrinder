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

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.ngrinder.common.controller.BaseController;
import org.ngrinder.common.util.HttpContainerContext;
import org.ngrinder.infra.spring.RemainedPath;
import org.ngrinder.model.User;
import org.ngrinder.script.model.FileCategory;
import org.ngrinder.script.model.FileEntry;
import org.ngrinder.script.service.FileEntryService;
import org.python.google.common.collect.Maps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.Map;

import static org.apache.commons.io.FilenameUtils.getPath;
import static org.ngrinder.common.util.EncodingUtils.encodePathWithUTF8;
import static org.ngrinder.common.util.ExceptionUtils.processException;

/**
 * FileEntry manipulation controller.
 *
 * @author JunHo Yoon
 * @since 3.0
 */
@Controller
@RequestMapping("/script")
public class FileEntryController extends BaseController {

	private static final Logger LOG = LoggerFactory.getLogger(FileEntryController.class);

	@Autowired
	private FileEntryService fileEntryService;

	@Autowired
	HttpContainerContext httpContainerContext;

	@GetMapping({"/list/**", ""})
	public String getAll() {
		return "app";
	}

	@GetMapping({"/editor", "/new"})
	public String editor() {
		return "app";
	}

	@GetMapping(value = "/search/**")
	public String search() {
		return "app";
	}

	/**
	 * Download file entry of given path.
	 *
	 * @param user     current user
	 * @param path     user
	 * @param response response
	 */
	@RequestMapping("/download/**")
	public void download(User user, @RemainedPath String path, HttpServletResponse response) {
		FileEntry fileEntry = fileEntryService.getOne(user, path);
		if (fileEntry == null) {
			LOG.error("{} requested to download not existing file entity {}", user.getUserId(), path);
			return;
		}
		response.reset();
		try {
			response.addHeader(
					"Content-Disposition",
					"attachment;filename="
							+ java.net.URLEncoder.encode(FilenameUtils.getName(fileEntry.getPath()), "utf8"));
		} catch (UnsupportedEncodingException e1) {
			LOG.error(e1.getMessage(), e1);
		}
		response.setContentType("application/octet-stream; charset=UTF-8");
		response.addHeader("Content-Length", "" + fileEntry.getFileSize());
		byte[] buffer = new byte[4096];
		ByteArrayInputStream fis = null;
		OutputStream toClient = null;
		try {
			fis = new ByteArrayInputStream(fileEntry.getContentBytes());
			toClient = new BufferedOutputStream(response.getOutputStream());
			int readLength;
			while (((readLength = fis.read(buffer)) != -1)) {
				toClient.write(buffer, 0, readLength);
			}
		} catch (IOException e) {
			throw processException("error while download file", e);
		} finally {
			IOUtils.closeQuietly(fis);
			IOUtils.closeQuietly(toClient);
		}
	}

	/**
	 * Save a fileEntry and return to the the path.
	 *
	 * @param user                 current user
	 * @param fileEntry            file to be saved
	 * @param targetHosts          target host parameter
	 * @param validated            validated the script or not, 1 is validated, 0 is not.
	 * @param createLibAndResource true if lib and resources should be created as well.
	 * @param model                model
	 * @return redirect:/script/list/${basePath}
	 */
	@RequestMapping(value = "/save/**", method = RequestMethod.POST)
	public String save(User user, FileEntry fileEntry,
	                   @RequestParam String targetHosts, @RequestParam(defaultValue = "0") String validated,
	                   @RequestParam(defaultValue = "false") boolean createLibAndResource, ModelMap model) {
		if (fileEntry.getFileType().getFileCategory() == FileCategory.SCRIPT) {
			Map<String, String> map = Maps.newHashMap();
			map.put("validated", validated);
			map.put("targetHosts", StringUtils.trim(targetHosts));
			fileEntry.setProperties(map);
		}
		fileEntryService.save(user, fileEntry);

		String basePath = getPath(fileEntry.getPath());
		if (createLibAndResource) {
			fileEntryService.addFolder(user, basePath, "lib", getMessages("script.commit.libFolder"));
			fileEntryService.addFolder(user, basePath, "resources", getMessages("script.commit.resourceFolder"));
		}
		model.clear();
		return "redirect:/script/list/" + encodePathWithUTF8(basePath);
	}
}
