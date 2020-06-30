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

import static java.util.Collections.*;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.io.FilenameUtils.getPath;
import static org.apache.commons.lang3.StringUtils.*;
import static org.ngrinder.common.util.CollectionUtils.buildMap;
import static org.ngrinder.common.util.EncodingUtils.encodePathWithUTF8;
import static org.ngrinder.common.util.ExceptionUtils.processException;
import static org.ngrinder.common.util.NoOp.noOp;
import static org.ngrinder.common.util.PathUtils.removePrependedSlash;
import static org.ngrinder.common.util.PathUtils.trimPathSeparatorBothSides;
import static org.ngrinder.common.util.Preconditions.*;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.ngrinder.common.exception.NGrinderRuntimeException;
import org.kohsuke.github.GHTreeEntry;
import org.ngrinder.common.util.PathUtils;
import org.ngrinder.common.util.UrlUtils;
import org.ngrinder.infra.spring.RemainedPath;
import org.ngrinder.model.User;
import org.ngrinder.script.handler.NullScriptHandler;
import org.ngrinder.script.handler.ProjectHandler;
import org.ngrinder.script.handler.ScriptHandler;
import org.ngrinder.script.handler.ScriptHandlerFactory;
import org.ngrinder.script.model.*;
import org.ngrinder.script.service.FileEntryService;
import org.ngrinder.script.service.GitHubFileEntryService;
import org.ngrinder.script.service.ScriptValidationService;
import org.ngrinder.user.service.UserContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.MessageSource;
import org.springframework.http.ContentDisposition;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

import lombok.RequiredArgsConstructor;

import javax.servlet.http.HttpServletResponse;

/**
 * FileEntry manipulation API controller.
 *
 * @since 3.5.0
 */
@RestController
@RequestMapping("/script/api")
@RequiredArgsConstructor
public class FileEntryApiController {

	private static final Logger LOG = LoggerFactory.getLogger(FileEntryApiController.class);

	private static final Comparator<FileEntry> DIRECTORY_PRIORITY_FILE_ENTRY_COMPARATOR = (o1, o2) -> {
		if (o1.getFileType() == FileType.DIR && o2.getFileType() != FileType.DIR) {
			return -1;
		}
		return (o1.getFileName().compareTo(o2.getFileName()));
	};

	private final FileEntryService fileEntryService;

	private final ScriptHandlerFactory handlerFactory;

	private final ScriptValidationService scriptValidationService;

	private final MessageSource messageSource;

	private final UserContext userContext;

	private final GitHubFileEntryService gitHubFileEntryService;

	@GetMapping("/handlers")
	public List<ScriptHandler> getHandlers() {
		return handlerFactory.getVisibleHandlers();
	}

	/**
	 * Search files on the query.
	 *
	 * @param user  current user
	 * @param query query string
	 * @return list of filtered files
	 */
	@GetMapping("/search")
	public List<FileEntry> search(User user, @RequestParam String query) {
		String trimmedQuery = trimToEmpty(query);

		return fileEntryService.getAll(user)
			.stream()
			.filter(Objects::nonNull)
			.filter(fileEntry -> fileEntry.getFileType() != FileType.DIR)
			.filter(fileEntry -> containsIgnoreCase(new File(fileEntry.getPath()).getName(), trimmedQuery))
			.collect(toList());
	}

	/**
	 * Save a fileEntry.
	 *
	 * @param user                 current user
	 * @param fileSaveParams       model for params
	 */
	@PostMapping("/save/**")
	public void save(User user, @RequestBody FileSaveParams fileSaveParams) {
		FileEntry fileEntry = fileSaveParams.getFileEntry();

		if (fileEntry.getFileType().getFileCategory() == FileCategory.SCRIPT) {
			Map<String, String> map = buildMap(
				"validated", fileSaveParams.getValidated(),
				"targetHosts", trimToEmpty(fileSaveParams.getTargetHosts())
			);
			fileEntry.setProperties(map);
		}
		fileEntryService.save(user, fileEntry);

		String basePath = getPath(fileEntry.getPath());
		if (fileSaveParams.isCreateLibAndResource()) {
			fileEntryService.addFolder(user, basePath, "lib", getMessages("script.commit.libFolder"));
			fileEntryService.addFolder(user, basePath, "resources", getMessages("script.commit.resourceFolder"));
		}
	}

	/**
	 * Get the message from messageSource by the given key.
	 *
	 * @param key key of message
	 * @return the found message. If not found, the error message will return.
	 */
	private String getMessages(String key) {
		String userLanguage = "en";
		try {
			userLanguage = userContext.getCurrentUser().getUserLanguage();
		} catch (Exception e) {
			noOp();
		}
		Locale locale = new Locale(userLanguage);
		return messageSource.getMessage(key, null, locale);
	}

	/**
	 * Provide new file creation form data.
	 *
	 * @param user                  current user
	 * @param path                  path in which a file will be added
	 * @param scriptCreationParams  model for params
	 * @return response map
	 */
	@PostMapping(value = "/new/**", params = "type=script")
	public Map<String, Object> createScript(User user,
											@RemainedPath String path,
											@RequestBody ScriptCreationParams scriptCreationParams) {
		String fileName = scriptCreationParams.getFileName();
		String testUrl = scriptCreationParams.getTestUrl();
		String options = scriptCreationParams.getOptions();
		boolean createLibAndResource = scriptCreationParams.isCreateLibAndResource();

		fileName = trimToEmpty(fileName);
		String name = "Test1";
		if (isEmpty(testUrl)) {
			testUrl = defaultIfBlank(testUrl, "http://please_modify_this.com");
		} else {
			name = UrlUtils.getHost(testUrl);
		}
		ScriptHandler scriptHandler = fileEntryService.getScriptHandler(scriptCreationParams.getScriptType());
		FileEntry entry;
		if (scriptHandler instanceof ProjectHandler) {
			path += isEmpty(path) ? "" : "/";

			if (!fileEntryService.hasFileEntry(user, PathUtils.join(path, fileName))) {
				fileEntryService.prepareNewEntry(user, path, fileName, name, testUrl, scriptHandler, createLibAndResource, options);
				return buildMap(
					"message", fileName + " project is created.",
					"path", "/script/list/" + encodePathWithUTF8(path) + fileName);
			} else {
				return buildMap(
					"message", fileName + " is already existing. Please choose the different name",
					"path", "/script/list/" + encodePathWithUTF8(path));
			}

		} else {
			String fullPath = PathUtils.join(path, fileName);
			if (fileEntryService.hasFileEntry(user, fullPath)) {
				entry = fileEntryService.getOne(user, fullPath);
			} else {
				entry = fileEntryService.prepareNewEntry(user, path, fileName, name, testUrl, scriptHandler, createLibAndResource, options);
			}
		}

		save(user, new FileSaveParams(entry, null, "0", createLibAndResource));

		return buildMap("file", entry);
	}

	/**
	 * Add a folder on the given path.
	 *
	 * @param user       current user
	 * @param path       path in which folder will be added
	 * @param folderName folderName
	 */
	@PostMapping(value = "/new/**", params = "type=folder")
	public void addFolder(User user,
							@RemainedPath String path,
							@RequestBody String folderName) {
		fileEntryService.addFolder(user, path, trimToEmpty(folderName), "");
	}

	/**
	 * Get the details of given path.
	 *
	 * @param user     user
	 * @param path     user
	 * @param revision revision. -1 if HEAD
	 * @return detail view properties
	 */
	@GetMapping("/detail/**")
	public Map<String, Object> getOne(User user,
									  @RemainedPath String path,
									  @RequestParam(value = "r", required = false) Long revision) {
		FileEntry script = fileEntryService.getOne(user, path, revision);
		if (script == null || !script.getFileType().isEditable()) {
			LOG.error("Error while getting file detail on {}. the file does not exist or not editable", path);
			return emptyMap();
		}

		ScriptHandler scriptHandler = fileEntryService.getScriptHandler(script);
		String codemirrorKey = scriptHandler.getCodemirrorKey();
		if (scriptHandler instanceof NullScriptHandler) {
			codemirrorKey = ((NullScriptHandler) scriptHandler).getCodemirrorKey(script.getFileType());
		}

		return buildMap(
			"file", script,
			"scriptHandler", scriptHandler,
			"codemirrorKey", codemirrorKey
		);
	}

	/**
	 * Delete files on the given path.
	 *
	 * @param user        user
	 * @param paths 	list of file paths to be deleted
	 */
	@DeleteMapping("/delete")
	public void delete(User user, @RequestBody List<String> paths) {
		fileEntryService.delete(user, paths);
	}


	/**
	 * Upload a file.
	 *
	 * @param user        current user
	 * @param path        path
	 * @param description description
	 * @param file        multi part file
	 */
	@PostMapping("/upload/**")
	public void uploadFile(User user,
							 @RemainedPath String path,
							 @RequestParam String description,
							 @RequestParam("uploadFile") MultipartFile file) {
		try {
			upload(user, path, description, file);
		} catch (IOException e) {
			LOG.error("Error while getting file content: {}", e.getMessage(), e);
			throw processException("Error while getting file content:" + e.getMessage(), e);
		}
	}

	private void upload(User user, String path, String description, MultipartFile file) throws IOException {
		FileEntry fileEntry = new FileEntry();
		fileEntry.setContentBytes(file.getBytes());
		fileEntry.setDescription(description);
		fileEntry.setPath(FilenameUtils.separatorsToUnix(FilenameUtils.concat(path, file.getOriginalFilename())));
		fileEntryService.save(user, fileEntry);
	}


	/**
	 * Create the given file.
	 *
	 * @param user      user
	 * @param fileEntry file entry
	 */
	@PostMapping({"/", ""})
	public void create(User user, FileEntry fileEntry) {
		fileEntryService.save(user, fileEntry);
	}

	/**
	 * Create the given file.
	 *
	 * @param user        user
	 * @param path        path
	 * @param description description
	 * @param file        multi part file
	 */
	@PostMapping(value = "/**", params = "action=upload")
	public void uploadAPI(User user,
							   @RemainedPath String path,
							   @RequestParam String description,
							   @RequestParam("uploadFile") MultipartFile file) throws IOException {
		upload(user, path, description, file);
	}

	/**
	 * Check the file by given path.
	 *
	 * @param user user
	 * @param path path
	 * @return json string
	 */
	@GetMapping(value = "/**", params = "action=view")
	public FileEntry viewOne(User user, @RemainedPath String path) {
		FileEntry fileEntry = fileEntryService.getOne(user, path, -1L);
		return checkNotNull(fileEntry, "%s file is not viewable", path);
	}

	/**
	 * Get all files which belongs to given user.
	 *
	 * @param user user
	 * @return json string
	 */
	@GetMapping(value = {"/**", "/", ""}, params = "action=all")
	public List<FileEntry> getAll(User user) {
		return fileEntryService.getAll(user);
	}

	/**
	 * Get all files which belongs to given user and path.
	 *
	 * @param user user
	 * @param path path
	 * @return json string
	 */
	@GetMapping({"/**", "/", ""})
	public List<FileEntry> getAll(User user, @RemainedPath String path) {
		String trimmedPath = trimToEmpty(path);

		return fileEntryService.getAll(user)
				.stream()
				.filter(Objects::nonNull)
				.filter(fileEntry -> trimPathSeparatorBothSides(getPath(fileEntry.getPath())).equals(trimmedPath))
				.sorted(DIRECTORY_PRIORITY_FILE_ENTRY_COMPARATOR)
				.peek(fileEntry -> fileEntry.setPath(removePrependedSlash(fileEntry.getPath())))
				.collect(toList());
	}

	/**
	 * Delete file by given user and path.
	 *
	 * @param user user
	 * @param path path
	 */
	@DeleteMapping("/**")
	public void deleteOne(User user, @RemainedPath String path) {
		fileEntryService.delete(user, path);
	}

	/**
	 * Validate the script.
	 *
	 * @param user                    current user
	 * @param scriptValidationParams  model for params
	 * @return validation Result string
	 */
	@PostMapping("/validate")
	public String validate(User user, @RequestBody ScriptValidationParams scriptValidationParams) {
		FileEntry fileEntry = scriptValidationParams.getFileEntry();
		fileEntry.setCreatedUser(user);
		return scriptValidationService.validate(user, fileEntry, false, scriptValidationParams.getHostString());
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
			throw new NGrinderRuntimeException("download requested file not exists in file entity " + path);
		}

		response.reset();

		response.setContentType("application/octet-stream; charset=UTF-8");
		ContentDisposition contentDisposition = ContentDisposition.builder("attachment")
			.filename(fileEntry.getFileName(), StandardCharsets.UTF_8)
			.build();
		response.setHeader("Content-Disposition", contentDisposition.toString());
		response.addHeader("Content-Length", "" + fileEntry.getFileSize());

		try (ByteArrayInputStream fis = new ByteArrayInputStream(fileEntry.getContentBytes());
			OutputStream os = new BufferedOutputStream(response.getOutputStream())) {
			IOUtils.copy(fis, os);
		} catch (IOException e) {
			throw processException("error while download file", e);
		}
	}

	@GetMapping("/github-config")
	public Set<GitHubConfig> getGitHubConfig(User user) {
		try {
			return gitHubFileEntryService.getAllGitHubConfig(user);
		} catch (FileNotFoundException e) {
			return emptySet();
		}
	}

	@PostMapping("/github-config")
	public void createGitHubConfig(User user) {
		fileEntryService.createGitHubConfig(user);
	}

	@GetMapping("/github")
	public Map<String, List<GHTreeEntry>> getGitHubScripts(User user, boolean refresh) throws FileNotFoundException {
		if (refresh) {
			gitHubFileEntryService.evictGitHubScriptCache(user);
		}
		return gitHubFileEntryService.getScripts(user);
	}

	@PostMapping("/github/validate")
	public void validateGithubConfig(@RequestBody FileEntry fileEntry) {
		gitHubFileEntryService.validate(fileEntry);
	}
}
