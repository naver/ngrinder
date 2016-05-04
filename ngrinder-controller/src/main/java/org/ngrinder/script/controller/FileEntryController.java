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

import com.google.common.base.Predicate;
import com.nhncorp.lucy.security.xss.XssPreventer;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.ngrinder.common.controller.BaseController;
import org.ngrinder.common.controller.RestAPI;
import org.ngrinder.common.util.EncodingUtils;
import org.ngrinder.common.util.HttpContainerContext;
import org.ngrinder.common.util.PathUtils;
import org.ngrinder.common.util.UrlUtils;
import org.ngrinder.infra.spring.RemainedPath;
import org.ngrinder.model.User;
import org.ngrinder.script.handler.ProjectHandler;
import org.ngrinder.script.handler.ScriptHandler;
import org.ngrinder.script.handler.ScriptHandlerFactory;
import org.ngrinder.script.model.FileCategory;
import org.ngrinder.script.model.FileEntry;
import org.ngrinder.script.model.FileType;
import org.ngrinder.script.service.FileEntryService;
import org.ngrinder.script.service.ScriptValidationService;
import org.python.google.common.collect.Maps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.annotation.Nullable;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.google.common.collect.Iterables.filter;
import static com.google.common.collect.Lists.newArrayList;
import static java.util.Collections.sort;
import static org.apache.commons.io.FilenameUtils.getPath;
import static org.ngrinder.common.util.EncodingUtils.encodePathWithUTF8;
import static org.ngrinder.common.util.ExceptionUtils.processException;
import static org.ngrinder.common.util.PathUtils.removePrependedSlash;
import static org.ngrinder.common.util.PathUtils.trimPathSeparatorBothSides;
import static org.ngrinder.common.util.Preconditions.checkNotNull;

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
	private ScriptValidationService scriptValidationService;

	@Autowired
	private ScriptHandlerFactory handlerFactory;

	@Autowired
	HttpContainerContext httpContainerContext;

	/**
	 * Get the list of file entries for the given user.
	 *
	 * @param user  current user
	 * @param path  path looking for.
	 * @param model model.
	 * @return script/list
	 */
	@RequestMapping({"/list/**", ""})
	public String getAll(User user, final @RemainedPath String path, ModelMap model) { // "fileName"
		model.addAttribute("files", getAllFiles(user, path));
		model.addAttribute("currentPath", path);
		model.addAttribute("svnUrl", getSvnUrlBreadcrumbs(user, path));
		model.addAttribute("handlers", handlerFactory.getVisibleHandlers());
		return "script/list";
	}

	/**
	 * Get the SVN url BreadCrumbs HTML string.
	 *
	 * @param user user
	 * @param path path
	 * @return generated HTML
	 */
	public String getSvnUrlBreadcrumbs(User user, String path) {
		String contextPath = httpContainerContext.getCurrentContextUrlFromUserRequest();
		String[] parts = StringUtils.split(path, '/');
		StringBuilder accumulatedPart = new StringBuilder(contextPath).append("/script/list");
		StringBuilder returnHtml = new StringBuilder().append("<a href='").append(accumulatedPart).append("'>")
				.append(contextPath).append("/svn/").append(user.getUserId()).append("</a>");
		for (String each : parts) {
			returnHtml.append("/");
			accumulatedPart.append("/").append(each);
			returnHtml.append("<a href='").append(accumulatedPart).append("'>").append(each).append("</a>");
		}
		return returnHtml.toString();
	}


	/**
	 * Get the script path BreadCrumbs HTML string.
	 *
	 * @param path path
	 * @return generated HTML
	 */
	public String getScriptPathBreadcrumbs(String path) {
		String contextPath = httpContainerContext.getCurrentContextUrlFromUserRequest();
		String[] parts = StringUtils.split(path, '/');
		StringBuilder accumulatedPart = new StringBuilder(contextPath).append("/script/list");
		StringBuilder returnHtml = new StringBuilder();
		for (int i = 0; i < parts.length; i++) {
			String each = parts[i];
			accumulatedPart.append("/").append(each);
			if (i != parts.length - 1) {
				returnHtml.append("<a target='_path_view' href='").append(accumulatedPart).append("'>").append(each)
						.append("</a>").append("/");
			} else {
				returnHtml.append(each);
			}
		}
		return returnHtml.toString();
	}

	/**
	 * Add a folder on the given path.
	 *
	 * @param user       current user
	 * @param path       path in which folder will be added
	 * @param folderName folderName
	 * @param model      model.
	 * @return redirect:/script/list/${path}
	 */
	@RequestMapping(value = "/new/**", params = "type=folder", method = RequestMethod.POST)
	public String addFolder(User user, @RemainedPath String path, @RequestParam("folderName") String folderName,
	                        ModelMap model) { // "fileName"
		fileEntryService.addFolder(user, path, StringUtils.trimToEmpty(folderName), "");
		model.clear();
		return "redirect:/script/list/" + encodePathWithUTF8(path);
	}

	/**
	 * Provide new file creation form data.
	 *
	 * @param user                  current user
	 * @param path                  path in which a file will be added
	 * @param testUrl               url which the script may use
	 * @param fileName              fileName
	 * @param scriptType            Type of script. optional
	 * @param createLibAndResources true if libs and resources should be created as well.
	 * @param redirectAttributes    redirect attributes storage
	 * @param model                 model.
	 * @return script/editor"
	 */
	@RequestMapping(value = "/new/**", params = "type=script", method = RequestMethod.POST)
	public String createForm(User user, @RemainedPath String path,
	                         @RequestParam(value = "testUrl", required = false) String testUrl,
	                         @RequestParam("fileName") String fileName,
	                         @RequestParam(value = "scriptType", required = false) String scriptType,
	                         @RequestParam(value = "createLibAndResource", defaultValue = "false") boolean createLibAndResources,
	                         @RequestParam(value = "options", required = false) String options,
	                         RedirectAttributes redirectAttributes, ModelMap model) {
		fileName = StringUtils.trimToEmpty(fileName);
		String name = "Test1";
		if (StringUtils.isEmpty(testUrl)) {
			testUrl = StringUtils.defaultIfBlank(testUrl, "http://please_modify_this.com");
		} else {
			name = UrlUtils.getHost(testUrl);
		}
		ScriptHandler scriptHandler = fileEntryService.getScriptHandler(scriptType);
		FileEntry entry = new FileEntry();
		entry.setPath(fileName);
		if (scriptHandler instanceof ProjectHandler) {
			if (!fileEntryService.hasFileEntry(user, PathUtils.join(path, fileName))) {
				fileEntryService.prepareNewEntry(user, path, fileName, name, testUrl, scriptHandler,
						createLibAndResources, options);
				redirectAttributes.addFlashAttribute("message", fileName + " project is created.");
				return "redirect:/script/list/" + encodePathWithUTF8(path) + "/" + fileName;
			} else {
				redirectAttributes.addFlashAttribute("exception", fileName
						+ " is already existing. Please choose the different name");
				return "redirect:/script/list/" + encodePathWithUTF8(path) + "/";
			}

		} else {
			String fullPath = PathUtils.join(path, fileName);
			if (fileEntryService.hasFileEntry(user, fullPath)) {
				model.addAttribute("file", fileEntryService.getOne(user, fullPath));
			} else {
				model.addAttribute("file", fileEntryService.prepareNewEntry(user, path, fileName, name, testUrl,
						scriptHandler, createLibAndResources, options));
			}
		}
		model.addAttribute("breadcrumbPath", getScriptPathBreadcrumbs(PathUtils.join(path, fileName)));
		model.addAttribute("scriptHandler", scriptHandler);
		model.addAttribute("createLibAndResource", createLibAndResources);
		return "script/editor";
	}

	/**
	 * Get the details of given path.
	 *
	 * @param user     user
	 * @param path     user
	 * @param revision revision. -1 if HEAD
	 * @param model    model
	 * @return script/editor
	 */
	@RequestMapping("/detail/**")
	public String getOne(User user, @RemainedPath String path,
	                     @RequestParam(value = "r", required = false) Long revision, ModelMap model) {
		FileEntry script = fileEntryService.getOne(user, path, revision);
		if (script == null || !script.getFileType().isEditable()) {
			LOG.error("Error while getting file detail on {}. the file does not exist or not editable", path);
			model.clear();
			return "redirect:/script/";
		}
		model.addAttribute("file", script);
		model.addAttribute("lastRevision", script.getLastRevision());
		model.addAttribute("curRevision", script.getRevision());
		model.addAttribute("scriptHandler", fileEntryService.getScriptHandler(script));
		model.addAttribute("ownerId", user.getUserId());
		model.addAttribute("breadcrumbPath", getScriptPathBreadcrumbs(path));
		return "script/editor";
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
	 * Search files on the query.
	 *
	 * @param user  current user
	 * @param query query string
	 * @param model model
	 * @return script/list
	 */
	@RequestMapping(value = "/search/**")
	public String search(User user, @RequestParam(required = true, value = "query") final String query,
	                     ModelMap model) {
		final String trimmedQuery = StringUtils.trimToEmpty(query);
		List<FileEntry> searchResult = newArrayList(filter(fileEntryService.getAll(user),
				new Predicate<FileEntry>() {
					@Override
					public boolean apply(@Nullable FileEntry input) {
						return input != null && input.getFileType() != FileType.DIR && StringUtils.containsIgnoreCase(new File(input.getPath()).getName(), trimmedQuery);
					}
				}));
		model.addAttribute("query", query);
		model.addAttribute("files", searchResult);
		model.addAttribute("currentPath", "");
		return "script/list";
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

	/**
	 * Upload a file.
	 *
	 * @param user        current user
	 * @param path        path
	 * @param description description
	 * @param file        multi part file
	 * @param model       model
	 * @return redirect:/script/list/${path}
	 */
	@RequestMapping(value = "/upload/**", method = RequestMethod.POST)
	public String upload(User user, @RemainedPath String path, @RequestParam("description") String description,
	                     @RequestParam("uploadFile") MultipartFile file, ModelMap model) {
		try {
			description = XssPreventer.escape(description);
			upload(user, path, description, file);
			model.clear();
			return "redirect:/script/list/" + encodePathWithUTF8(path);
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
	 * Delete files on the given path.
	 *
	 * @param user        user
	 * @param path        base path
	 * @param filesString file list delimited by ","
	 * @return json string
	 */
	@RequestMapping(value = "/delete/**", method = RequestMethod.POST)
	@ResponseBody
	public String delete(User user, @RemainedPath String path, @RequestParam("filesString") String filesString) {
		String[] files = filesString.split(",");
		fileEntryService.delete(user, path, files);
		Map<String, Object> rtnMap = new HashMap<String, Object>(1);
		rtnMap.put(JSON_SUCCESS, true);
		return toJson(rtnMap);
	}

	/**
	 * Create the given file.
	 *
	 * @param user      user
	 * @param fileEntry file entry
	 * @return success json string
	 */
	@RestAPI
	@RequestMapping(value = {"/api/", "/api"}, method = RequestMethod.POST)
	public HttpEntity<String> create(User user, FileEntry fileEntry) {
		fileEntryService.save(user, fileEntry);
		return successJsonHttpEntity();
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
	@RequestMapping(value = "/api/**", params = "action=upload", method = RequestMethod.POST)
	public HttpEntity<String> uploadForAPI(User user, @RemainedPath String path,
	                                       @RequestParam("description") String description,
	                                       @RequestParam("uploadFile") MultipartFile file) throws IOException {
		upload(user, path, description, file);
		return successJsonHttpEntity();
	}

	/**
	 * Check the file by given path.
	 *
	 * @param user user
	 * @param path path
	 * @return json string
	 */
	@RestAPI
	@RequestMapping(value = "/api/**", params = "action=view", method = RequestMethod.GET)
	public HttpEntity<String> viewOne(User user, @RemainedPath String path) {
		FileEntry fileEntry = fileEntryService.getOne(user, path, -1L);
		return toJsonHttpEntity(checkNotNull(fileEntry
				, "%s file is not viewable", path));
	}

	/**
	 * Get all files which belongs to given user.
	 *
	 * @param user user
	 * @return json string
	 */
	@RestAPI
	@RequestMapping(value = {"/api/**", "/api/", "/api"}, params = "action=all", method = RequestMethod.GET)
	public HttpEntity<String> getAll(User user) {
		return toJsonHttpEntity(fileEntryService.getAll(user));
	}

	/**
	 * Get all files which belongs to given user and path.
	 *
	 * @param user user
	 * @param path path
	 * @return json string
	 */
	@RestAPI
	@RequestMapping(value = {"/api/**", "/api/", "/api"}, method = RequestMethod.GET)
	public HttpEntity<String> getAll(User user, @RemainedPath String path) {
		return toJsonHttpEntity(getAllFiles(user, path));
	}

	private List<FileEntry> getAllFiles(User user, String path) {
		final String trimmedPath = StringUtils.trimToEmpty(path);
		List<FileEntry> files = newArrayList(filter(fileEntryService.getAll(user),
				new Predicate<FileEntry>() {
					@Override
					public boolean apply(@Nullable FileEntry input) {
						return input != null && trimPathSeparatorBothSides(getPath(input.getPath())).equals(trimmedPath);
					}
				}));
		sort(files, new Comparator<FileEntry>() {
			@Override
			public int compare(FileEntry o1, FileEntry o2) {
				if (o1.getFileType() == FileType.DIR && o2.getFileType() != FileType.DIR) {
					return -1;
				}
				return (o1.getFileName().compareTo(o2.getFileName()));
			}

		});
		for (FileEntry each : files) {
			each.setPath(removePrependedSlash(each.getPath()));
		}
		return files;
	}

	/**
	 * Delete file by given user and path.
	 *
	 * @param user user
	 * @param path path
	 * @return json string
	 */
	@RestAPI
	@RequestMapping(value = "/api/**", method = RequestMethod.DELETE)
	public HttpEntity<String> deleteOne(User user, @RemainedPath String path) {
		fileEntryService.delete(user, path);
		return successJsonHttpEntity();
	}


	/**
	 * Validate the script.
	 *
	 * @param user       current user
	 * @param fileEntry  fileEntry
	 * @param hostString hostString
	 * @return validation Result string
	 */
	@RequestMapping(value = "/api/validate", method = RequestMethod.POST)
	@RestAPI
	public HttpEntity<String> validate(User user, FileEntry fileEntry,
	                                   @RequestParam(value = "hostString", required = false) String hostString) {
		fileEntry.setCreatedUser(user);
		return toJsonHttpEntity(scriptValidationService.validate(user, fileEntry, false, hostString));
	}
}
