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
package org.ngrinder.script.service;

import static org.ngrinder.common.util.NoOp.noOp;
import static org.ngrinder.common.util.Preconditions.checkNotEmpty;
import static org.ngrinder.common.util.Preconditions.checkNotNull;

import java.io.File;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Predicate;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.ngrinder.common.exception.NGrinderRuntimeException;
import org.ngrinder.common.util.HttpContainerContext;
import org.ngrinder.infra.config.Config;
import org.ngrinder.model.User;
import org.ngrinder.script.model.FileEntry;
import org.ngrinder.script.model.FileType;
import org.ngrinder.script.repository.FileEntryRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.ehcache.EhCacheCacheManager;
import org.springframework.core.io.ClassPathResource;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.SVNURL;
import org.tmatesoft.svn.core.internal.io.fs.FSHook;
import org.tmatesoft.svn.core.internal.io.fs.FSHookEvent;
import org.tmatesoft.svn.core.internal.io.fs.FSHooks;
import org.tmatesoft.svn.core.wc.SVNClientManager;
import org.tmatesoft.svn.core.wc.SVNRevision;

import freemarker.template.Configuration;
import freemarker.template.DefaultObjectWrapper;
import freemarker.template.Template;

/**
 * File entry service class.<br/>
 * 
 * This class is responsible for creating user repo whenever user is created and connection b/w user
 * and underlying svn.
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
	private EhCacheCacheManager cacheManager;

	@Autowired
	private FileEntryRepository fileEntityRepository;

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
	 * Get all {@link FileEntry} for the given user which has give {@link FileType}.
	 * 
	 * @param user
	 *            user
	 * @param fileType
	 *            fileType
	 * @return cached {@link FileEntry} list
	 */
	public List<FileEntry> getAllFileEntries(User user, FileType fileType) {
		List<FileEntry> fileEntryList = getAllFileEntries(user);
		// Only python script is allowed right now.
		CollectionUtils.filter(fileEntryList, new Predicate() {
			@Override
			public boolean evaluate(Object object) {
				return ((FileEntry) object).getFileType() == FileType.PYTHON_SCRIPT;
			}
		});
		return fileEntryList;
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
		URL url;
		try {
			url = new URL(urlString);
			return (url.getHost() + url.getPath()).replaceAll("[\\&\\?\\%\\-]", "_");
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
	 * @return created file entry
	 */
	public FileEntry prepareNewEntry(User user, String path, String fileName, String url) {
		FileEntry fileEntry = new FileEntry();
		String filePath;
		if (!StringUtils.isBlank(path)) {
			filePath = path + "/" + fileName;
		} else {
			filePath = fileName;
		}
		fileEntry.setPath(filePath);
		fileEntry.setContent(loadFreeMarkerTemplate(user, url));
		addHostProperties(fileEntry, url);
		return fileEntry;
	}

	private void addHostProperties(FileEntry fileEntry, String url) {
		Map<String, String> map = new HashMap<String, String>();
		String host;
		try {
			host = new URL(url).getHost();
			map.put("targetHosts", StringUtils.trim(host));
			fileEntry.setProperties(map);
		} catch (MalformedURLException e) {
			// FALL THROUGH
			noOp();
		}
	}

	/**
	 * Create new FileEntry for the url.
	 * 
	 * @param user
	 *            user
	 * @param urlString
	 *            url to be tested.
	 * @return created new {@link FileEntry}
	 */
	public FileEntry prepareNewEntryForQuickTest(User user, String urlString) {
		String testNameFromUrl = getTestNameFromUrl(urlString);
		// addFolder(user, "", testNameFromUrl);
		// There might be race condition here... What if a user changes the SVN
		// repo while saving
		// newEntry??
		FileEntry newEntry = prepareNewEntry(user, testNameFromUrl, "script.py", urlString);
		newEntry.setDescription("Quick test for " + urlString);
		save(user, newEntry);
		return getFileEntry(user, testNameFromUrl + "/" + "script.py");
	}

	/**
	 * Load freemarker template for quick test.
	 * 
	 * @param user
	 *            user
	 * @param url
	 *            url
	 * @return generated test script
	 */
	public String loadFreeMarkerTemplate(User user, String url) {

		try {
			Configuration freemarkerConfig = new Configuration();
			ClassPathResource cpr = new ClassPathResource("script_template");
			freemarkerConfig.setDirectoryForTemplateLoading(cpr.getFile());
			freemarkerConfig.setObjectWrapper(new DefaultObjectWrapper());
			Template template = freemarkerConfig.getTemplate("basic_template.ftl");
			Map<String, Object> map = new HashMap<String, Object>();
			map.put("url", url);
			map.put("user", user);
			StringWriter writer = new StringWriter();
			template.process(map, writer);
			return writer.toString();
		} catch (Exception e) {
			LOG.error("Error while fetching template for quick start", e);
		}
		return "";
	}

	/**
	 * Get SVN URL for the given user and the given subpath.
	 * 
	 * @param user
	 *            user
	 * @param path
	 *            subpath
	 * @return SVN URL
	 */
	public String getSvnUrl(User user, String path) {
		String contextPath = httpContainerContext.getCurrentRequestUrlFromUserRequest();
		StringBuilder url = new StringBuilder(config.getSystemProperties().getProperty("http.url", contextPath));
		url.append("/svn/").append(user.getUserId());
		if (StringUtils.isNotEmpty(path)) {
			url.append("/").append(path.trim());
		}
		return url.toString();
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
	 * Get Lib and Resources. This method will collect the files of lib and resources folder on the
	 * same folder whre script is located.
	 * 
	 * @param user
	 *            user
	 * @param scriptPath
	 *            path of script
	 * @param revision
	 *            revision number. If head, it should be -1.
	 * @return {@link FileEntry} list
	 */
	public List<FileEntry> getLibAndResourcesEntries(User user, String scriptPath, Long revision) {
		String path = FilenameUtils.getPath(scriptPath);
		List<FileEntry> fileList = new ArrayList<FileEntry>();
		List<FileEntry> fileEntries = getFileEntries(user, path + "lib/", revision);
		for (FileEntry eachFileEntry : fileEntries) {
			FileType fileType = eachFileEntry.getFileType();
			if (fileType.isLibDistribtable()) {
				fileList.add(eachFileEntry);
			}
		}
		fileEntries = getFileEntries(user, path + "resources/", revision);
		for (FileEntry eachFileEntry : fileEntries) {
			FileType fileType = eachFileEntry.getFileType();
			if (fileType.isResourceDistributable()) {
				fileList.add(eachFileEntry);
			}
		}
		return fileList;
	}
}
