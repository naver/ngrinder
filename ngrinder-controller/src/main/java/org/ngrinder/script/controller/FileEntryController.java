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
import org.ngrinder.common.controller.annotation.GlobalControllerModel;
import org.ngrinder.infra.spring.RemainedPath;
import org.ngrinder.model.User;
import org.ngrinder.script.model.FileEntry;
import org.ngrinder.script.service.FileEntryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletResponse;
import java.io.*;

import static org.ngrinder.common.util.ExceptionUtils.processException;

import lombok.RequiredArgsConstructor;

/**
 * FileEntry manipulation controller.
 *
 * @since 3.0
 */
@Controller
@RequestMapping("/script")
@GlobalControllerModel
@RequiredArgsConstructor
public class FileEntryController {

	private static final Logger LOG = LoggerFactory.getLogger(FileEntryController.class);

	private final FileEntryService fileEntryService;

	@GetMapping({"/list/**", ""})
	public String getAll(User user) {
		return "app";
	}

	@GetMapping({"/editor", "/new"})
	public String editor(User user) {
		return "app";
	}

	@GetMapping("/search/**")
	public String search(User user) {
		return "app";
	}

	@GetMapping("/detail/**")
	public String getOne(User user) {
		return "app";
	}

	/**
	 * Download file entry of given path.
	 *
	 * @param user     current user
	 * @param path     user
	 * @param response response
	 */
	@GetMapping("/download/**")
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
}
