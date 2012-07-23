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

import static org.ngrinder.common.util.Preconditions.checkNotEmpty;
import static org.ngrinder.common.util.Preconditions.checkNotNull;

import java.io.File;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.ngrinder.common.exception.NGrinderRuntimeException;
import org.ngrinder.common.util.HttpContainerContext;
import org.ngrinder.infra.config.Config;
import org.ngrinder.model.User;
import org.ngrinder.script.model.FileEntry;
import org.ngrinder.script.model.FileType;
import org.ngrinder.script.repository.FileEntityRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.core.io.ClassPathResource;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.tmatesoft.svn.core.SVNErrorCode;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.wc.SVNClientManager;
import org.tmatesoft.svn.core.wc.SVNRevision;

import freemarker.template.Configuration;
import freemarker.template.DefaultObjectWrapper;
import freemarker.template.Template;

/**
 * File entry service class. This class is responsible for creating user repo
 * whenever user is created and connection b/w user and underlying svn.
 * 
 * @author JunHo Yoon
 */
@Service
public class FileEntryService {

	private static final Logger LOG = LoggerFactory.getLogger(FileEntryService.class);

	private SVNClientManager svnClientManager = SVNClientManager.newInstance();

	@Autowired
	private Config config;

	@Autowired
	private HttpContainerContext httpContainerContext;

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
				svnClientManager.getAdminClient().doCreateRepository(newUserDirectory, user.getUserId(), true, true);
			}
		} catch (SVNException e) {
			LOG.error("Error while prepare user {}'s repo", user.getUserName(), e);
		}
	}

	private File getUserRepoDirectory(User user) {
		return new File(config.getHome().getUserRepoDirectory(), checkNotNull(user.getUserId()));
	}

	@Autowired
	private FileEntityRepository fileEntityRepository;

	@Cacheable("file_entry_search_cache")
	public List<FileEntry> getAllFileEntries(User user) {
		return fileEntityRepository.findAll(user);
	}

	/**
	 * Get file entries from undelying svn for given path.
	 * 
	 * @param user
	 *            the user
	 * @param path
	 *            path in the repo
	 * @return file entry list
	 */
	public List<FileEntry> getFileEntries(User user, String path) {
		// If it's not created, make one.
		prepare(user);
		return fileEntityRepository.findAll(user, path);
	}

	/**
	 * Get single file entity.
	 * 
	 * @param user
	 *            the user
	 * @param path
	 *            path in the repo
	 * @return singl file entity
	 */
	public FileEntry getFileEntry(User user, String path) {
		return fileEntityRepository.findOne(user, path, SVNRevision.HEAD);
	}

	public boolean hasFileEntry(User user, String path) {
		try {
			fileEntityRepository.findOne(user, path, SVNRevision.HEAD);
		} catch (NGrinderRuntimeException e) {
			if (e.getCause() instanceof SVNException) {
				if (((SVNException) e.getCause()).getErrorMessage().getErrorCode().equals(SVNErrorCode.FS_NOT_FOUND))
					return false;
			}
			throw e;
		}
		return true;
	}

	public void addFolder(User user, String path, String folderName) {
		FileEntry entry = new FileEntry();
		entry.setPath(path + "/" + folderName);
		entry.setFileType(FileType.DIR);
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
	 *            revision
	 * @return file entity
	 */
	public FileEntry getFileEntry(User user, String path, Long revision) {
		return fileEntityRepository.findOne(user, path, SVNRevision.create(revision));
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

	/**
	 * Create new FileEntry.
	 * 
	 * @param user
	 * @param path
	 * @param fileName
	 * @param langauge
	 * @param url
	 * @return
	 */
	public FileEntry prepareNewEntry(User user, String path, String fileName, String langauge, String url) {
		FileEntry fileEntry = new FileEntry();
		fileEntry.setPath(path + "/" + fileName);
		fileEntry.setFileType(FileType.getFileType(langauge));
		fileEntry.setContent(loadFreeMarkerTemplate(user, url));
		return fileEntry;
	}

	/**
	 * Load freemarker template for quick test.
	 * 
	 * @param user
	 * @param url
	 * @return
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

	public String getSvnUrl(User user, String path) {
		String contextPath = httpContainerContext.getCurrentRequestUrlFromUserRequest();
		StringBuilder url = new StringBuilder(config.getSystemProperties().getProperty("http.url", contextPath));
		url.append("/svn/").append(user.getUserId());
		if (StringUtils.isNotEmpty(path)) {
			url.append("/").append(path.trim());
		}
		return url.toString();
	}
}
