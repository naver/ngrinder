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

import static com.google.common.collect.ImmutableMap.of;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.io.FilenameUtils.getPath;
import static org.apache.commons.lang3.StringUtils.*;
import static org.ngrinder.common.util.EncodingUtils.encodePathWithUTF8;
import static org.ngrinder.common.util.ExceptionUtils.processException;
import static org.ngrinder.common.util.PathUtils.removePrependedSlash;
import static org.ngrinder.common.util.PathUtils.trimPathSeparatorBothSides;
import static org.ngrinder.common.util.Preconditions.checkNotNull;

import com.nhncorp.lucy.security.xss.XssPreventer;
import org.apache.commons.io.FilenameUtils;
import org.ngrinder.common.controller.BaseController;
import org.ngrinder.common.controller.RestAPI;
import org.ngrinder.common.util.HttpContainerContext;
import org.ngrinder.common.util.PathUtils;
import org.ngrinder.common.util.UrlUtils;
import org.ngrinder.infra.spring.RemainedPath;
import org.ngrinder.model.User;
import org.ngrinder.script.handler.NullScriptHandler;
import org.ngrinder.script.handler.ProjectHandler;
import org.ngrinder.script.handler.ScriptHandler;
import org.ngrinder.script.handler.ScriptHandlerFactory;
import org.ngrinder.script.model.FileCategory;
import org.ngrinder.script.model.FileEntry;
import org.ngrinder.script.model.FileType;
import org.ngrinder.script.service.FileEntryService;
import org.ngrinder.script.service.ScriptValidationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * FileEntry manipulation API controller.
 *
 * @since 3.5.0
 */
@RestController
@RequestMapping("/script/api")
public class FileEntryApiController extends BaseController {

	private static final Logger LOG = LoggerFactory.getLogger(FileEntryApiController.class);

	private static final Comparator<FileEntry> DIRECTORY_PRIORITY_FILE_ENTRY_COMPARATOR = (o1, o2) -> {
		if (o1.getFileType() == FileType.DIR && o2.getFileType() != FileType.DIR) {
			return -1;
		}
		return (o1.getFileName().compareTo(o2.getFileName()));
	};

	@Autowired
	private FileEntryService fileEntryService;

	@Autowired
	private ScriptHandlerFactory handlerFactory;

	@Autowired
	HttpContainerContext httpContainerContext;

	@Autowired
	private ScriptValidationService scriptValidationService;

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
	 * Save a fileEntry and return to the the path.
	 *
	 * @param user                 current user
	 * @param fileEntry            file to be saved
	 * @param targetHosts          target host parameter
	 * @param validated            validated the script or not, 1 is validated, 0 is not.
	 * @param createLibAndResource true if lib and resources should be created as well.
	 * @return basePath
	 */
	@PostMapping("/save/**")
	public String save(User user, FileEntry fileEntry,
					   @RequestParam String targetHosts,
					   @RequestParam(defaultValue = "0") String validated,
					   @RequestParam(defaultValue = "false") boolean createLibAndResource) {
		if (fileEntry.getFileType().getFileCategory() == FileCategory.SCRIPT) {
			Map<String, String> map = of(
				"validated", validated,
				"targetHosts", trimToEmpty(targetHosts)
			);
			fileEntry.setProperties(map);
		}
		fileEntryService.save(user, fileEntry);

		String basePath = getPath(fileEntry.getPath());
		if (createLibAndResource) {
			fileEntryService.addFolder(user, basePath, "lib", getMessages("script.commit.libFolder"));
			fileEntryService.addFolder(user, basePath, "resources", getMessages("script.commit.resourceFolder"));
		}
		return encodePathWithUTF8(basePath);
	}

	/**
	 * Provide new file creation form data.
	 *
	 * @param user                  current user
	 * @param path                  path in which a file will be added
	 * @param testUrl               url which the script may use
	 * @param fileName              fileName
	 * @param scriptType            Type of script. optional
	 * @param createLibAndResource true if libs and resources should be created as well.
	 * @return response map
	 */
	@PostMapping(value = "/new/**", params = "type=script")
	public Map<String, Object> createScript(User user,
											@RemainedPath String path,
											@RequestParam("fileName") String fileName,
											@RequestParam(required = false) String testUrl,
											@RequestParam(required = false) String options,
											@RequestParam(required = false) String scriptType,
											@RequestParam(defaultValue = "false") boolean createLibAndResource) {
		fileName = trimToEmpty(fileName);
		String name = "Test1";
		if (isEmpty(testUrl)) {
			testUrl = defaultIfBlank(testUrl, "http://please_modify_this.com");
		} else {
			name = UrlUtils.getHost(testUrl);
		}
		ScriptHandler scriptHandler = fileEntryService.getScriptHandler(scriptType);
		FileEntry entry;
		if (scriptHandler instanceof ProjectHandler) {
			path += isEmpty(path) ? "" : "/";

			if (!fileEntryService.hasFileEntry(user, PathUtils.join(path, fileName))) {
				fileEntryService.prepareNewEntry(user, path, fileName, name, testUrl, scriptHandler, createLibAndResource, options);
				return of(
					"message", fileName + " project is created.",
					"path", "/script/list/" + encodePathWithUTF8(path) + fileName);
			} else {
				return of(
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

		save(user, entry, null, "0", createLibAndResource);

		return of("file", entry);
	}

	/**
	 * Add a folder on the given path.
	 *
	 * @param user       current user
	 * @param path       path in which folder will be added
	 * @param folderName folderName
	 */
	@PostMapping(value = "/new/**", params = "type=folder")
	public String addFolder(User user,
							@RemainedPath String path,
							@RequestParam String folderName) {
		fileEntryService.addFolder(user, path, trimToEmpty(folderName), "");
		return returnSuccess();
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
	@SuppressWarnings("SpellCheckingInspection")
	public Map<String, Object> getOne(User user,
									  @RemainedPath String path,
									  @RequestParam(value = "r", required = false) Long revision) {
		FileEntry script = fileEntryService.getOne(user, path, revision);
		if (script == null || !script.getFileType().isEditable()) {
			LOG.error("Error while getting file detail on {}. the file does not exist or not editable", path);
			return of();
		}

		ScriptHandler scriptHandler = fileEntryService.getScriptHandler(script);
		String codemirrorKey = scriptHandler.getCodemirrorKey();
		if (scriptHandler instanceof NullScriptHandler) {
			codemirrorKey = ((NullScriptHandler) scriptHandler).getCodemirrorKey(script.getFileType());
		}

		return of(
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
	 * @return json string
	 */
	@PostMapping("/delete")
	public String delete(User user, @RequestBody List<String> paths) {
		fileEntryService.delete(user, paths);
		return returnSuccess();
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
	public String uploadFile(User user,
							 @RemainedPath String path,
							 @RequestParam String description,
							 @RequestParam("uploadFile") MultipartFile file) {
		try {
			description = XssPreventer.escape(description);
			upload(user, path, description, file);
		} catch (IOException e) {
			LOG.error("Error while getting file content: {}", e.getMessage(), e);
			throw processException("Error while getting file content:" + e.getMessage(), e);
		}
		return returnSuccess();
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
	 * @return success json string
	 */
	@RestAPI
	@PostMapping({"/", ""})
	public String create(User user, FileEntry fileEntry) {
		fileEntryService.save(user, fileEntry);
		return returnSuccess();
	}

	/**
	 * Create the given file.
	 *
	 * @param user        user
	 * @param path        path
	 * @param description description
	 * @param file        multi part file
	 * @return success json string
	 */
	@RestAPI
	@PostMapping(value = "/**", params = "action=upload")
	public String uploadForAPI(User user,
							   @RemainedPath String path,
							   @RequestParam String description,
							   @RequestParam("uploadFile") MultipartFile file) throws IOException {
		upload(user, path, description, file);
		return returnSuccess();
	}

	/**
	 * Check the file by given path.
	 *
	 * @param user user
	 * @param path path
	 * @return json string
	 */
	@RestAPI
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
	@RestAPI
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
	@RestAPI
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
	 * @return json string
	 */
	@RestAPI
	@DeleteMapping("/**")
	public String deleteOne(User user, @RemainedPath String path) {
		fileEntryService.delete(user, path);
		return returnSuccess();
	}

	/**
	 * Validate the script.
	 *
	 * @param user       current user
	 * @param fileEntry  fileEntry
	 * @param hostString hostString
	 * @return validation Result string
	 */
	@RestAPI
	@PostMapping("/validate")
	public String validate(User user, FileEntry fileEntry,
									   @RequestParam(required = false) String hostString) {
		fileEntry.setCreatedUser(user);
		return scriptValidationService.validate(user, fileEntry, false, hostString);
	}
}
