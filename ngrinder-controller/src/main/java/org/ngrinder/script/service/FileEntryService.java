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
package org.ngrinder.script.service;

import static org.ngrinder.common.util.CollectionUtils.buildMap;
import static org.ngrinder.common.util.CollectionUtils.newHashMap;
import static org.ngrinder.common.util.Preconditions.checkNotEmpty;
import static org.ngrinder.common.util.Preconditions.checkNotNull;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.apache.commons.lang.StringUtils;
import org.ngrinder.common.exception.NGrinderRuntimeException;
import org.ngrinder.common.util.HttpContainerContext;
import org.ngrinder.common.util.PathUtil;
import org.ngrinder.common.util.UrlUtils;
import org.ngrinder.infra.config.Config;
import org.ngrinder.model.User;
import org.ngrinder.script.handler.ScriptHandler;
import org.ngrinder.script.handler.ScriptHandlerFactory;
import org.ngrinder.script.model.FileEntry;
import org.ngrinder.script.model.FileType;
import org.ngrinder.script.repository.FileEntryRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.SVNURL;
import org.tmatesoft.svn.core.internal.io.fs.FSHook;
import org.tmatesoft.svn.core.internal.io.fs.FSHookEvent;
import org.tmatesoft.svn.core.internal.io.fs.FSHooks;
import org.tmatesoft.svn.core.wc.SVNClientManager;
import org.tmatesoft.svn.core.wc.SVNRevision;

/**
 * File entry service class.<br/>
 * 
 * This class is responsible for creating user svn repository whenever a user is created and connect
 * the user to the underlying svn.
 * 
 * @author JunHo Yoon
 * @since 3.0
 */
@Service
public class FileEntryService {

	private static final Logger LOG = LoggerFactory.getLogger(FileEntryService.class);

	private SVNClientManager svnClientManager = SVNClientManager.newInstance();

	@Autowired
	private Config config;

	@Autowired
	private HttpContainerContext httpContainerContext;

	@Autowired
	@Qualifier("cacheManager")
	private CacheManager cacheManager;

	@Autowired
	private FileEntryRepository fileEntityRepository;

	@Autowired
	private ScriptHandlerFactory scriptHandlerFactory;

	/**
	 * Initialize {@link FileEntryService}.
	 */
	@PostConstruct
	public void init() {
		// Add cache invalidation hook.
		FSHooks.registerHook(new FSHook() {
			@Override
			public void onHook(FSHookEvent event) throws SVNException {
				if (event.getType().equals(FSHooks.SVN_REPOS_HOOK_POST_COMMIT)) {
					String name = event.getReposRootDir().getName();
					invalidateCache(name);
				}
			}
		});

	}

	/**
	 * invalidate the file_entry_seach_cache.
	 * 
	 * @param userId
	 *            userId.
	 */
	public void invalidateCache(String userId) {
		cacheManager.getCache("file_entry_search_cache").evict(userId);
	}

	/**
	 * Create user svn repo.
	 * 
	 * This method is executed async way.
	 * 
	 * @param user
	 *            newly created user.
	 */
	@Async
	public void prepare(User user) {
		File newUserDirectory = getUserRepoDirectory(user);
		try {
			if (!newUserDirectory.exists()) {
				createUserRepo(user, newUserDirectory);
			}
		} catch (SVNException e) {
			LOG.error("Error while prepare user {}'s repo", user.getUserName(), e);
		}
	}

	private SVNURL createUserRepo(User user, File newUserDirectory) throws SVNException {
		return svnClientManager.getAdminClient().doCreateRepository(newUserDirectory, user.getUserId(), true, true);
	}

	private File getUserRepoDirectory(User user) {
		return new File(config.getHome().getRepoDirectoryRoot(), checkNotNull(user.getUserId()));
	}

	/**
	 * Get all {@link FileEntry} for the given user. This method is subject to be cached because it
	 * takes time.
	 * 
	 * @param user
	 *            user
	 * @return cached {@link FileEntry} list
	 */
	@Cacheable(value = "file_entry_search_cache", key = "#user.userId")
	public List<FileEntry> getAllFileEntries(User user) {
		return fileEntityRepository.findAll(user);
	}

	/**
	 * Get file entries from underlying svn for given path.
	 * 
	 * @param user
	 *            the user
	 * @param path
	 *            path in the repo
	 * @param revision
	 *            revision number. -1 if HEAD.
	 * @return file entry list
	 */
	public List<FileEntry> getFileEntries(User user, String path, Long revision) {
		// If it's not created, make one.
		prepare(user);
		return fileEntityRepository.findAll(user, path, revision);
	}

	/**
	 * Get single file entity.
	 * 
	 * The return value has content byte.
	 * 
	 * @param user
	 *            the user
	 * @param path
	 *            path in the svn repo
	 * @param revision
	 *            file revision.
	 * @return single file entity
	 */
	public FileEntry getFileEntry(User user, String path, long revision) {
		return fileEntityRepository.findOne(user, path, SVNRevision.create(revision));
	}

	/**
	 * Get single file entity.
	 * 
	 * The return value has content byte.
	 * 
	 * @param user
	 *            the user
	 * @param path
	 *            path in the svn repo
	 * @return single file entity
	 */
	public FileEntry getFileEntry(User user, String path) {
		return fileEntityRepository.findOne(user, path, SVNRevision.HEAD);
	}

	/**
	 * Check file existence.
	 * 
	 * @param user
	 *            user
	 * @param path
	 *            path in user repo
	 * @return true if exists.
	 */
	public boolean hasFileEntry(User user, String path) {
		return fileEntityRepository.hasFileEntry(user, path);
	}

	/**
	 * Add folder on the given path.
	 * 
	 * @param user
	 *            user
	 * @param path
	 *            base path
	 * @param comment
	 *            comment
	 * @param folderName
	 *            folder name
	 */
	public void addFolder(User user, String path, String folderName, String comment) {
		FileEntry entry = new FileEntry();
		entry.setPath(path + "/" + folderName);
		entry.setFileType(FileType.DIR);
		entry.setDescription(comment);
		fileEntityRepository.save(user, entry, null);
	}

	/**
	 * Get file entity for the given revision.
	 * 
	 * @param user
	 *            the user
	 * @param path
	 *            path in the repo
	 * @param revision
	 *            revision. if -1, HEAD
	 * @return file entity
	 */
	public FileEntry getFileEntry(User user, String path, Long revision) {
		SVNRevision svnRev = (revision == null || revision == -1) ? SVNRevision.HEAD : SVNRevision.create(revision);
		return fileEntityRepository.findOne(user, path, svnRev);
	}

	/**
	 * Save File entry.
	 * 
	 * @param user
	 *            the user
	 * @param fileEntity
	 *            fileEntity to be saved
	 */
	public void save(User user, FileEntry fileEntity) {
		prepare(user);
		checkNotEmpty(fileEntity.getPath());
		fileEntityRepository.save(user, fileEntity, fileEntity.getEncoding());
	}

	/**
	 * Delete file entries.
	 * 
	 * @param user
	 *            the user
	 * @param path
	 *            the base path
	 * @param files
	 *            files under base path
	 */
	public void delete(User user, String path, String[] files) {
		List<String> fileList = new ArrayList<String>();
		for (String each : files) {
			fileList.add(path + "/" + each);
		}
		fileEntityRepository.delete(user, fileList.toArray(new String[] {}));
	}

	String getTestNameFromUrl(String urlString) {
		try {
			URL url = new URL(urlString);
			String urlPath = "/".equals(url.getPath()) ? "" : url.getPath();
			return (url.getHost() + urlPath).replaceAll("[\\&\\?\\%\\-]", "_");
		} catch (MalformedURLException e) {
			throw new NGrinderRuntimeException("Error while translating " + urlString, e);
		}
	}

	/**
	 * Create new FileEntry.
	 * 
	 * @param user
	 *            user
	 * @param path
	 *            path
	 * @param fileName
	 *            fileName
	 * @param url
	 *            url
	 * @param scriptHandler
	 *            script handler
	 * @param createLibAndResources
	 *            true if lib and resources should be created
	 * @return created file entry. null if it's the project creation.
	 */
	public FileEntry prepareNewEntry(User user, String path, String fileName, String url, ScriptHandler scriptHandler,
					boolean createLibAndResources) {
		FileEntry fileEntry = new FileEntry();
		String targetPath = PathUtil.removePrependedSlash(path + "/" + fileName);
		fileEntry.setPath(targetPath);
		boolean proceed = scriptHandler.prepareScriptEnv(user, targetPath, fileName, url, createLibAndResources);
		if (!proceed) {
			return null;
		}
		String content = loadTemplate(user, scriptHandler, url, fileName);
		fileEntry.setContent(content);
		fileEntry.setProperties(buildMap("targetHosts", UrlUtils.getHost(url)));
		return fileEntry;
	}

	/**
	 * Create new FileEntry for the url.
	 * 
	 * @param user
	 *            user
	 * @param urlString
	 *            url to be tested.
	 * @param scriptHandler
	 *            scriptHandler
	 * @return created new {@link FileEntry}
	 */
	public FileEntry prepareNewEntryForQuickTest(User user, String urlString, ScriptHandler scriptHandler) {
		String testNameFromUrl = getTestNameFromUrl(urlString);
		FileEntry newEntry = prepareNewEntry(user, testNameFromUrl, "script.py", urlString, scriptHandler, false);
		newEntry.setDescription("Quick test for " + urlString);
		save(user, newEntry);
		return getFileEntry(user, testNameFromUrl + "/" + "script.py");
	}

	/**
	 * Load freemarker template for quick test.
	 * 
	 * @param user
	 *            user
	 * @param handler
	 *            handler
	 * @param url
	 *            url
	 * @param projectName
	 *            projectName
	 * @return generated test script
	 */
	public String loadTemplate(User user, ScriptHandler handler, String url, String name) {
		Map<String, Object> map = newHashMap();
		map.put("url", url);
		map.put("userName", user.getUserName());
		map.put("name", name);
		return handler.getScriptTemplate(map);
	}

	/**
	 * Get SVN URL for the given user and the given subpath. Base path and the subpath is separated
	 * by ####.
	 * 
	 * @param user
	 *            user
	 * @param path
	 *            subpath
	 * @return SVN URL
	 */
	public String getSvnUrl(User user, String path) {
		String contextPath = getCurrentContextPathFromUserRequest();
		StringBuilder url = new StringBuilder(contextPath);
		url.append("/svn/").append(user.getUserId());
		if (StringUtils.isNotEmpty(path)) {
			url.append("/").append(path.trim());
		}
		return url.toString();
	}

	/**
	 * Get current context path url by user request.
	 * 
	 * @return context path
	 */
	public String getCurrentContextPathFromUserRequest() {
		return config.getSystemProperties().getProperty("http.url",
						httpContainerContext.getCurrentContextUrlFromUserRequest());
	}

	/**
	 * Get a SVN content into given dir.
	 * 
	 * @param user
	 *            user
	 * @param fromPath
	 *            path in svn subpath
	 * @param toDir
	 *            to directory
	 */
	public void writeContentTo(User user, String fromPath, File toDir) {
		fileEntityRepository.writeContentTo(user, fromPath, toDir);
	}

	/**
	 * Get the appropriate {@link ScriptHandler} subclass for the given {@link FileEntry}.
	 * 
	 * @param scriptEntry
	 *            script entry
	 * @return scriptHandler
	 */
	public ScriptHandler getScriptHandler(FileEntry scriptEntry) {
		return scriptHandlerFactory.getHandler(scriptEntry);
	}

	/**
	 * Get the appropriate {@link ScriptHandler} subclass for the given ScriptHandler key.
	 * 
	 * @param key
	 *            script entry
	 * @return scriptHandler
	 */
	public ScriptHandler getScriptHandler(String key) {
		return scriptHandlerFactory.getHandler(key);
	}
}
