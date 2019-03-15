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
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.ngrinder.common.controller.BaseController;
import org.ngrinder.common.util.HttpContainerContext;
import org.ngrinder.model.User;
import org.ngrinder.script.model.FileEntry;
import org.ngrinder.script.model.FileType;
import org.ngrinder.script.service.FileEntryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Nullable;
import java.io.IOException;
import java.util.Comparator;
import java.util.List;

import static com.google.common.collect.Iterables.filter;
import static com.google.common.collect.Lists.newArrayList;
import static java.util.Collections.sort;
import static org.apache.commons.io.FilenameUtils.getPath;
import static org.ngrinder.common.util.PathUtils.removePrependedSlash;
import static org.ngrinder.common.util.PathUtils.trimPathSeparatorBothSides;

/**
 * FileEntry manipulation base controller.
 *
 * @since 3.5.0
 */
public class FileEntryBaseController extends BaseController {

	@Autowired
	protected FileEntryService fileEntryService;

	@Autowired
	protected HttpContainerContext httpContainerContext;

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

	protected List<FileEntry> getAllFiles(User user, String path) {
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

	protected void upload(User user, String path, String description, MultipartFile file) throws IOException {
		FileEntry fileEntry = new FileEntry();
		fileEntry.setContentBytes(file.getBytes());
		fileEntry.setDescription(description);
		fileEntry.setPath(FilenameUtils.separatorsToUnix(FilenameUtils.concat(path, file.getOriginalFilename())));
		fileEntryService.save(user, fileEntry);
	}
}
