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
package org.ngrinder.script.repository;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.EmptyStackException;
import java.util.List;

import javax.annotation.PostConstruct;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.apache.commons.lang.StringUtils;
import org.ngrinder.common.exception.NGrinderRuntimeException;
import org.ngrinder.common.model.Home;
import org.ngrinder.common.util.EncodingUtil;
import org.ngrinder.infra.config.Config;
import org.ngrinder.model.User;
import org.ngrinder.script.model.FileEntry;
import org.ngrinder.script.model.FileType;
import org.ngrinder.user.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.tmatesoft.svn.core.ISVNDirEntryHandler;
import org.tmatesoft.svn.core.SVNDirEntry;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.SVNNodeKind;
import org.tmatesoft.svn.core.SVNProperties;
import org.tmatesoft.svn.core.SVNProperty;
import org.tmatesoft.svn.core.SVNURL;
import org.tmatesoft.svn.core.internal.io.fs.FSRepositoryFactory;
import org.tmatesoft.svn.core.io.ISVNEditor;
import org.tmatesoft.svn.core.io.SVNRepository;
import org.tmatesoft.svn.core.io.diff.SVNDeltaGenerator;
import org.tmatesoft.svn.core.wc.SVNClientManager;
import org.tmatesoft.svn.core.wc.SVNRevision;

/**
 * SVN FileEntity abstraction
 * 
 * @author JunHo Yoon
 * @since 3.0
 */
@Repository
public class FileEntityRepository {

	private static final Logger LOG = LoggerFactory.getLogger(FileEntityRepository.class);

	@Autowired
	private Config config;

	private Home home;

	@PostConstruct
	public void init() {
		FSRepositoryFactory.setup();
		home = config.getHome();
	}

	@Autowired
	private UserService userService;

	/**
	 * Get user repository.
	 * 
	 * For unit test, This can be overridable.
	 * 
	 * @param user
	 *            the user
	 * @return user repository path.
	 */
	public File getUserRepository(User user) {
		return home.getUserRepoDirectory(user.getUserId());
	}

	public List<FileEntry> findAll(User user, final String path) {
		final List<FileEntry> fileEntries = new ArrayList<FileEntry>();
		SVNClientManager svnClientManager = SVNClientManager.newInstance();
		try {
			svnClientManager.getLogClient().doList(SVNURL.fromFile(getUserRepository(user)).appendPath(path, true),
					SVNRevision.HEAD, SVNRevision.HEAD, true, false, new ISVNDirEntryHandler() {
						@Override
						public void handleDirEntry(SVNDirEntry dirEntry) throws SVNException {

							FileEntry script = new FileEntry();
							if (StringUtils.isBlank(dirEntry.getRelativePath())) {
								return;
							}
							script.setPath(FilenameUtils.normalize(path + "/" + dirEntry.getRelativePath(), true));
							script.setCreatedDate(dirEntry.getDate());
							script.setLastModifiedDate(dirEntry.getDate());
							script.setDescription(dirEntry.getCommitMessage());
							script.setLastModifiedUser(userService.getUserById(dirEntry.getAuthor()));
							// script.setFileName(dirEntry.getName());
							if (dirEntry.getKind() == SVNNodeKind.DIR) {
								script.setFileType(FileType.DIR);
							} else {
								script.setFileSize(dirEntry.getSize());
							}
							fileEntries.add(script);
						}
					});
		} catch (Exception e) {
			throw new NGrinderRuntimeException("Error while fetching files from SVN", e);
		} finally {
			closeSVNClientManagerQuietly(svnClientManager);
		}
		return fileEntries;
	}

	public List<FileEntry> findAll(User user) {
		final List<FileEntry> scripts = new ArrayList<FileEntry>();
		SVNClientManager svnClientManager = SVNClientManager.newInstance();
		try {
			svnClientManager.getLogClient().doList(SVNURL.fromFile(getUserRepository(user)), SVNRevision.HEAD,
					SVNRevision.HEAD, true, true, new ISVNDirEntryHandler() {
						@Override
						public void handleDirEntry(SVNDirEntry dirEntry) throws SVNException {
							FileEntry script = new FileEntry();
							if (dirEntry.getKind() == SVNNodeKind.DIR) {
								return;
							}
							if (StringUtils.isBlank(dirEntry.getRelativePath())) {
								return;
							}
							script.setPath(dirEntry.getRelativePath());
							script.setDescription(dirEntry.getCommitMessage());
							script.setFileType(dirEntry.getKind() == SVNNodeKind.DIR ? FileType.DIR : null);
							script.setFileSize(dirEntry.getSize());
							scripts.add(script);
						}
					});
		} catch (Exception e) {
			throw new NGrinderRuntimeException("Error while fetching files from SVN", e);
		} finally {
			closeSVNClientManagerQuietly(svnClientManager);
		}
		return scripts;

	}

	public FileEntry findOne(User user, String path, SVNRevision revision) {
		final FileEntry script = new FileEntry();
		SVNClientManager svnClientManager = null;
		ByteArrayOutputStream outputStream = null;
		try {
			svnClientManager = SVNClientManager.newInstance();

			SVNURL userRepoUrl = SVNURL.fromFile(getUserRepository(user));
			SVNRepository repo = svnClientManager.createRepository(userRepoUrl, true);
			SVNNodeKind nodeKind = repo.checkPath(path, -1);
			if (nodeKind == SVNNodeKind.NONE) {
				return null;
			}
			outputStream = new ByteArrayOutputStream();
			SVNProperties fileProperty = new SVNProperties();
			// Get File.
			repo.getFile(path, -1L, fileProperty, outputStream);
			String lastRevision = fileProperty.getStringValue(SVNProperty.REVISION);
			SVNDirEntry info = repo.info(path, Long.parseLong(lastRevision));
			byte[] byteArray = outputStream.toByteArray();
			script.setPath(path);
			script.setFileType(FileType.getFileType(FilenameUtils.getExtension(script.getFileName())));
			if (script.getFileType().isEditable()) {
				String autoDetectedEncoding = EncodingUtil.detectEncoding(byteArray, "UTF-8");
				script.setContent(new String(byteArray, autoDetectedEncoding));
				script.setEncoding(autoDetectedEncoding);
			} else {
				script.setContentBytes(byteArray);
			}
			script.setDescription(info.getCommitMessage());

			final List<Long> revisions = new ArrayList<Long>();
			script.setRevisions(revisions);
		} catch (Exception e) {
			LOG.error("Error while fetching files from SVN", e);
			throw new NGrinderRuntimeException("Error while fetching files from SVN", e);
		} finally {
			closeSVNClientManagerQuietly(svnClientManager);
			IOUtils.closeQuietly(outputStream);
		}
		return script;
	}

	/**
	 * Save fileEntry on the {@link FileEntry.getPath()} location
	 * 
	 * @param user
	 *            the user
	 * @param fileEntry
	 *            fileEntry to be saved
	 */
	public void save(User user, FileEntry fileEntry, String encoding) {
		SVNClientManager svnClientManager = null;
		ISVNEditor editor = null;
		String checksum = null;
		InputStream bais = null;
		try {
			svnClientManager = SVNClientManager.newInstance();
			SVNRepository repo = svnClientManager.createRepository(SVNURL.fromFile(getUserRepository(user)), true);
			SVNDirEntry dirEntry = repo.info(fileEntry.getPath(), -1);
			editor = repo.getCommitEditor(fileEntry.getDescription(), null, true, null);
			editor.openRoot(-1);
			if (fileEntry.getFileType() == FileType.DIR) {
				editor.addDir(fileEntry.getPath(), null, -1);
			} else {
				if (dirEntry == null) {
					// If it's new file
					editor.addFile(fileEntry.getPath(), null, -1);
				} else {
					// If it's modification
					editor.openFile(fileEntry.getPath(), -1);
				}

				editor.applyTextDelta(fileEntry.getPath(), null);

				// Calc diff
				final SVNDeltaGenerator deltaGenerator = new SVNDeltaGenerator();
				if (fileEntry.getFileType().isEditable()) {
					bais = new ByteArrayInputStream(fileEntry.getContent().getBytes(
							encoding == null ? "UTF-8" : encoding));
				} else {
					// TODO: if the file name has no extension with dot(.) when it is creating a data as script file,
					// it will be shown null pointer exception on the follow code.
					bais = new ByteArrayInputStream(fileEntry.getContentBytes());
				}
				checksum = deltaGenerator.sendDelta(fileEntry.getPath(), bais, editor, true);
			}
			// Finally push
			editor.closeFile(fileEntry.getPath(), checksum);
		} catch (Exception e) {
			abortSVNEditorQuietly(editor);
			LOG.error("Error while saving file to SVN", e);
			throw new NGrinderRuntimeException("Error while saving file to SVN", e);
		} finally {
			closeSVNEditorQuietly(editor);
			closeSVNClientManagerQuietly(svnClientManager);
			IOUtils.closeQuietly(bais);
		}
	}

	/**
	 * Quietly close svn editor
	 * 
	 * @param editor
	 *            editor to be closed.
	 */
	private void abortSVNEditorQuietly(ISVNEditor editor) {
		if (editor == null) {
			return;
		}
		try {
			editor.abortEdit();
		} catch (SVNException e) {
		}
	}

	/**
	 * Quietly close svn editor
	 * 
	 * @param editor
	 *            editor to be closed.
	 */
	private void closeSVNEditorQuietly(ISVNEditor editor) {
		if (editor == null) {
			return;
		}
		try {
			// recursively close
			while (true) {
				editor.closeDir();
			}
		} catch (EmptyStackException e) {
		} catch (SVNException e) {
		} finally {
			try {
				editor.closeEdit();
			} catch (SVNException e) {
			}
		}
	}

	public void delete(User user, String[] paths) {
		SVNClientManager svnClientManager = null;
		ISVNEditor editor = null;
		try {
			svnClientManager = SVNClientManager.newInstance();
			SVNRepository repo = svnClientManager.createRepository(SVNURL.fromFile(getUserRepository(user)), true);

			editor = repo.getCommitEditor("delete", null, true, null);
			editor.openRoot(-1);
			for (String each : paths) {
				editor.deleteEntry(each, -1);
			}
		} catch (Exception e) {
			abortSVNEditorQuietly(editor);
			LOG.error("Error while deleting file from SVN", e);
			throw new NGrinderRuntimeException("Error while deleting files from SVN", e);
		} finally {
			closeSVNEditorQuietly(editor);
			closeSVNClientManagerQuietly(svnClientManager);
		}
	}

	private void closeSVNClientManagerQuietly(SVNClientManager svnClientManager) {
		if (svnClientManager != null) {
			svnClientManager.dispose();
		}
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
		SVNClientManager svnClientManager = null;
		try {
			svnClientManager = SVNClientManager.newInstance();
			SVNURL userRepoUrl = SVNURL.fromFile(getUserRepository(user));
			SVNRepository repo = svnClientManager.createRepository(userRepoUrl, true);
			SVNNodeKind nodeKind = repo.checkPath(path, -1);
			return (nodeKind != SVNNodeKind.NONE);
		} catch (Exception e) {
			LOG.error("Error while fetching files from SVN", e);
			throw new NGrinderRuntimeException("Error while checking file existence from SVN", e);
		} finally {
			closeSVNClientManagerQuietly(svnClientManager);
		}
	}

	public void writeContentTo(User user, String path, File toPath) {
		SVNClientManager svnClientManager = null;
		FileOutputStream fileOutputStream = null;
		try {
			svnClientManager = SVNClientManager.newInstance();

			SVNURL userRepoUrl = SVNURL.fromFile(getUserRepository(user));
			SVNRepository repo = svnClientManager.createRepository(userRepoUrl, true);
			SVNNodeKind nodeKind = repo.checkPath(path, -1);
			if (nodeKind == SVNNodeKind.NONE || nodeKind == SVNNodeKind.DIR) {
				throw new NGrinderRuntimeException("It's not pssible write directory. nodeKind is " + nodeKind);
			}
			fileOutputStream = new FileOutputStream(new File(toPath, FilenameUtils.getName(path)));
			SVNProperties fileProperty = new SVNProperties();
			// Get File.
			repo.getFile(path, -1L, fileProperty, fileOutputStream);
		} catch (Exception e) {
			LOG.error("Error while fetching files from SVN", e);
			throw new NGrinderRuntimeException("Error while fetching files from SVN", e);
		} finally {
			closeSVNClientManagerQuietly(svnClientManager);
			IOUtils.closeQuietly(fileOutputStream);
		}
		return;
	}
}
