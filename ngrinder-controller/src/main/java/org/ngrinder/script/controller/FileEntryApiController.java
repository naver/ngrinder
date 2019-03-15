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

import org.ngrinder.common.controller.RestAPI;
import org.ngrinder.infra.spring.RemainedPath;
import org.ngrinder.model.User;
import org.ngrinder.script.model.FileEntry;
import org.ngrinder.script.service.ScriptValidationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

import static org.ngrinder.common.util.Preconditions.checkNotNull;

/**
 * FileEntry manipulation api controller.
 *
 * @since 3.5.0
 */
@RestController
@RequestMapping("/script")
public class FileEntryApiController extends FileEntryBaseController {

	@Autowired
	private ScriptValidationService scriptValidationService;

	/**
	 * Delete files on the given path.
	 *
	 * @param user        user
	 * @param path        base path
	 * @param filesString file list delimited by ","
	 * @return json string
	 */
	@RequestMapping(value = "/delete/**", method = RequestMethod.POST)
	public String delete(User user, @RemainedPath String path, @RequestParam("filesString") String filesString) {
		String[] files = filesString.split(",");
		fileEntryService.delete(user, path, files);
		return returnSuccess();
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
	@RequestMapping(value = "/api/**", params = "action=upload", method = RequestMethod.POST)
	public String uploadForAPI(User user, @RemainedPath String path,
							   @RequestParam("description") String description,
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
	@RequestMapping(value = "/api/**", params = "action=view")
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
	@RequestMapping(value = {"/api/**", "/api/", "/api"}, params = "action=all")
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
	@RequestMapping({"/api/**", "/api/", "/api"})
	public List<FileEntry> getAll(User user, @RemainedPath String path) {
		return getAllFiles(user, path);
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
	@RequestMapping(value = "/api/validate", method = RequestMethod.POST)
	public String validate(User user, FileEntry fileEntry,
						   @RequestParam(value = "hostString", required = false) String hostString) {
		fileEntry.setCreatedUser(user);
		return scriptValidationService.validate(user, fileEntry, false, hostString);
	}
}
