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
import java.util.Map.Entry;

import javax.annotation.PostConstruct;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.apache.commons.lang.StringUtils;
import org.ngrinder.common.exception.NGrinderRuntimeException;
import org.ngrinder.common.model.Home;
import org.ngrinder.common.util.EncodingUtil;
import org.ngrinder.infra.annotation.RuntimeOnlyComponent;
import org.ngrinder.infra.config.Config;
import org.ngrinder.model.User;
import org.ngrinder.script.model.FileEntry;
import org.ngrinder.script.model.FileType;
import org.ngrinder.user.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.tmatesoft.svn.core.ISVNDirEntryHandler;
import org.tmatesoft.svn.core.SVNDirEntry;
import org.tmatesoft.svn.core.SVNErrorCode;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.SVNNodeKind;
import org.tmatesoft.svn.core.SVNProperties;
import org.tmatesoft.svn.core.SVNProperty;
import org.tmatesoft.svn.core.SVNPropertyValue;
import org.tmatesoft.svn.core.SVNURL;
import org.tmatesoft.svn.core.internal.io.fs.FSRepositoryFactory;
import org.tmatesoft.svn.core.io.ISVNEditor;
import org.tmatesoft.svn.core.io.SVNRepository;
import org.tmatesoft.svn.core.io.diff.SVNDeltaGenerator;
import org.tmatesoft.svn.core.wc.SVNClientManager;
import org.tmatesoft.svn.core.wc.SVNRevision;

/**
 * SVN FileEntity abstraction. This class save and retrieve {@link FileEntry} from Database.
 * 
 * @author JunHo Yoon
 * @since 3.0
 */
@RuntimeOnlyComponent
public class FileEntryRepository {

	private static final Logger LOG = LoggerFactory.getLogger(FileEntryRepository.class);

	@Autowired
	private Config config;

	private Home home;

	/**
	 * Initialize the {@link FileEntryRepository}. This method should be performed to set up FS
	 * Repository.
	 */
	@PostConstruct
	public void init() {
		FSRepositoryFactory.setup();
		home = config.getHome();
	}

	@Autowired
	private UserRepository userRepository;

	/**
	 * Get user repository.
	 * 
	 * For unit test, This can be overridable.
	 * 
	 * @param user
	 *            the user
	 * @return user repository path.
	 */
	public File getUserRepoDirectory(User user) {
		return home.getUserRepoDirectory(user.getUserId());
	}

	/**
	 * Return all {@link FileEntry}s under the given path.
	 * 
	 * @param user
	 *            user
	 * @param path
	 *            path under which files are searched.
	 * @param revision
	 *            . null if head.
	 * @return found {@link FileEntry}s
	 */
	public List<FileEntry> findAll(User user, final String path, Long revision) {
		SVNRevision svnRevision = SVNRevision.HEAD;
		if (revision != null && -1L != revision) {
			svnRevision = SVNRevision.create(revision);
		}
		final List<FileEntry> fileEntries = new ArrayList<FileEntry>();
		SVNClientManager svnClientManager = SVNClientManager.newInstance();
		try {
			svnClientManager.getLogClient().doList(
							SVNURL.fromFile(getUserRepoDirectory(user)).appendPath(path, true), svnRevision,
							svnRevision, true, false, new ISVNDirEntryHandler() {
								@Override
								public void handleDirEntry(SVNDirEntry dirEntry) throws SVNException {

									FileEntry script = new FileEntry();
									// Exclude base path "/"
									if (StringUtils.isBlank(dirEntry.getRelativePath())) {
										return;
									}
									script.setPath(FilenameUtils.normalize(
													path + "/" + dirEntry.getRelativePath(), true));
									script.setCreatedDate(dirEntry.getDate());
									script.setLastModifiedDate(dirEntry.getDate());
									script.setDescription(dirEntry.getCommitMessage());
									script.setRevision(dirEntry.getRevision());
									script.setLastModifiedUser(userRepository.findOneByUserId(dirEntry
													.getAuthor()));
									if (dirEntry.getKind() == SVNNodeKind.DIR) {
										script.setFileType(FileType.DIR);
									} else {
										script.setFileSize(dirEntry.getSize());
									}
									fileEntries.add(script);
								}
							});
		} catch (Exception e) {
			LOG.debug("findAll() to the not existing folder {}", path);
		} finally {
			closeSVNClientManagerQuietly(svnClientManager);
		}
		return fileEntries;
	}

	/**
	 * Return all {@link FileEntry}s which user have. It excludes {@link FileType#DIR} entries.
	 * 
	 * @param user
	 *            user
	 * @return found {@link FileEntry}s
	 */
	public List<FileEntry> findAll(User user) {
		final List<FileEntry> scripts = new ArrayList<FileEntry>();
		SVNClientManager svnClientManager = SVNClientManager.newInstance();
		try {
			svnClientManager.getLogClient().doList(SVNURL.fromFile(getUserRepoDirectory(user)),
							SVNRevision.HEAD, SVNRevision.HEAD, true, true, new ISVNDirEntryHandler() {
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
									script.setRevision(dirEntry.getRevision());
									script.setFileType(dirEntry.getKind() == SVNNodeKind.DIR ? FileType.DIR
													: null);
									script.setFileSize(dirEntry.getSize());
									scripts.add(script);
								}
							});
		} catch (Exception e) {
			LOG.error("Error while fetching files from SVN for {}", user.getUserId());
			LOG.debug("Error details :", e);
			return new ArrayList<FileEntry>();

		} finally {
			closeSVNClientManagerQuietly(svnClientManager);
		}
		return scripts;

	}

	/**
	 * Return a {@link FileEntry} for the given path and revision.
	 * 
	 * @param user
	 *            user
	 * @param path
	 *            path in the svn repo
	 * @param revision
	 *            revision of the file
	 * @return found {@link FileEntry}, null if not found
	 */
	public FileEntry findOne(User user, String path, SVNRevision revision) {
		final FileEntry script = new FileEntry();
		SVNClientManager svnClientManager = null;
		ByteArrayOutputStream outputStream = null;
		try {
			svnClientManager = SVNClientManager.newInstance();

			SVNURL userRepoUrl = SVNURL.fromFile(getUserRepoDirectory(user));
			SVNRepository repo = svnClientManager.createRepository(userRepoUrl, true);
			SVNNodeKind nodeKind = repo.checkPath(path, -1);
			if (nodeKind == SVNNodeKind.NONE) {
				return null;
			}
			outputStream = new ByteArrayOutputStream();
			SVNProperties fileProperty = new SVNProperties();
			// Get File.
			repo.getFile(path, revision.getNumber(), fileProperty, outputStream);
			String lastRevisionStr = fileProperty.getStringValue(SVNProperty.REVISION);
			long lastRevision = Long.parseLong(lastRevisionStr);
			SVNDirEntry info = repo.info(path, lastRevision);
			byte[] byteArray = outputStream.toByteArray();
			script.setPath(path);
			for (String name : fileProperty.nameSet()) {
				script.getProperties().put(name, fileProperty.getStringValue(name));
			}
			script.setFileType(FileType.getFileTypeByExtension(FilenameUtils.getExtension(script
							.getFileName())));
			if (script.getFileType().isEditable()) {
				String autoDetectedEncoding = EncodingUtil.detectEncoding(byteArray, "UTF-8");
				script.setContent(new String(byteArray, autoDetectedEncoding));
				script.setEncoding(autoDetectedEncoding);
				script.setContentBytes(byteArray);
			} else {
				script.setContentBytes(byteArray);
			}
			script.setDescription(info.getCommitMessage());
			script.setRevision(lastRevision);
		} catch (Exception e) {
			LOG.error("Error while fetching a file from SVN {} {}", user.getUserId(), path);
			return null;
		} finally {
			closeSVNClientManagerQuietly(svnClientManager);
			IOUtils.closeQuietly(outputStream);
		}
		return script;
	}

	private void addPropertyValue(ISVNEditor editor, FileEntry fileEntry) throws SVNException {
		if (fileEntry.getFileType() != FileType.DIR) {
			for (Entry<String, String> each : fileEntry.getProperties().entrySet()) {
				editor.changeFileProperty(fileEntry.getPath(), each.getKey(),
								SVNPropertyValue.create(each.getValue()));
			}
		}
	}

	/**
	 * Save fileEntry on the {@link FileEntry.getPath()} location.
	 * 
	 * @param user
	 *            the user
	 * @param fileEntry
	 *            fileEntry to be saved
	 * @param encoding
	 *            file encoding with which fileEntry is saved. It is meaningful only FileEntry is
	 *            editable.
	 * 
	 */
	public void save(User user, FileEntry fileEntry, String encoding) {
		SVNClientManager svnClientManager = null;
		ISVNEditor editor = null;
		String checksum = null;
		InputStream bais = null;
		try {
			svnClientManager = SVNClientManager.newInstance();
			SVNRepository repo = svnClientManager.createRepository(
							SVNURL.fromFile(getUserRepoDirectory(user)), true);
			SVNDirEntry dirEntry = repo.info(fileEntry.getPath(), -1);

			// Add base pathes
			String fullPath = "";
			// Check.. first
			for (String each : getPathFragment(fileEntry.getPath())) {
				fullPath = fullPath + "/" + each;
				SVNDirEntry folderStepEntry = repo.info(fullPath, -1);
				if (folderStepEntry != null && folderStepEntry.getKind() == SVNNodeKind.FILE) {
					throw new NGrinderRuntimeException("User " + user.getUserId()
									+ " tried to create folder " + fullPath + ". It's file..");
				}
			}

			editor = repo.getCommitEditor(fileEntry.getDescription(), null, true, null);
			editor.openRoot(-1);
			fullPath = "";
			for (String each : getPathFragment(fileEntry.getPath())) {
				fullPath = fullPath + "/" + each;
				try {
					editor.addDir(fullPath, null, -1);
				} catch (Exception e) {
					// ignore
				}
			}

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
					bais = new ByteArrayInputStream(fileEntry.getContentBytes());
				}
				checksum = deltaGenerator.sendDelta(fileEntry.getPath(), bais, editor, true);
			}

			addPropertyValue(editor, fileEntry);
			editor.closeFile(fileEntry.getPath(), checksum);
		} catch (Exception e) {
			abortSVNEditorQuietly(editor);
			// If it's adding the folder which already exists... ignore..
			if (e instanceof SVNException && fileEntry.getFileType() == FileType.DIR) {
				if (SVNErrorCode.FS_ALREADY_EXISTS
								.equals(((SVNException) e).getErrorMessage().getErrorCode())) {
					return;
				}
			}
			LOG.error("Error while saving file to SVN", e);
			throw new NGrinderRuntimeException("Error while saving file to SVN", e);
		} finally {
			closeSVNEditorQuietly(editor);
			closeSVNClientManagerQuietly(svnClientManager);
			IOUtils.closeQuietly(bais);
		}
	}

	String[] getPathFragment(String path) {
		String basePath = FilenameUtils.getPath(path);
		return StringUtils.split(FilenameUtils.separatorsToUnix(basePath), "/");

	}

	/**
	 * Quietly close svn editor.
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
			// FALL THROUGH
		}
	}

	/**
	 * Quietly close svn editor. This is convenient method.
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
			// FALL THROUGH
		} catch (SVNException e) {
			// FALL THROUGH
		} finally {
			try {
				editor.closeEdit();
			} catch (SVNException e) {
				// FALL THROUGH
			}
		}
	}

	/**
	 * Delete file entries on given paths. If the one of paths does not exist, all deletion is
	 * canceled.
	 * 
	 * @param user
	 *            user
	 * @param paths
	 *            paths of file entries.
	 */
	public void delete(User user, String[] paths) {
		SVNClientManager svnClientManager = null;
		ISVNEditor editor = null;
		try {
			svnClientManager = SVNClientManager.newInstance();
			SVNRepository repo = svnClientManager.createRepository(
							SVNURL.fromFile(getUserRepoDirectory(user)), true);

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
			SVNURL userRepoUrl = SVNURL.fromFile(getUserRepoDirectory(user));
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

	/**
	 * Copy {@link FileEntry} to the given path.
	 * 
	 * This method only work for the file not dir.
	 * 
	 * @param user
	 *            user
	 * @param path
	 *            path of {@link FileEntry}
	 * @param toPath
	 *            file dir path to write.
	 */
	public void writeContentTo(User user, String path, File toPath) {
		SVNClientManager svnClientManager = null;
		FileOutputStream fileOutputStream = null;
		try {
			svnClientManager = SVNClientManager.newInstance();

			SVNURL userRepoUrl = SVNURL.fromFile(getUserRepoDirectory(user));
			SVNRepository repo = svnClientManager.createRepository(userRepoUrl, true);
			SVNNodeKind nodeKind = repo.checkPath(path, -1);
			// If it's DIR, it does not work.
			if (nodeKind == SVNNodeKind.NONE || nodeKind == SVNNodeKind.DIR) {
				throw new NGrinderRuntimeException("It's not pssible write directory. nodeKind is "
								+ nodeKind);
			}
			File destFile = new File(toPath, FilenameUtils.getName(path));
			// Prepare parent folders
			toPath.mkdirs();
			fileOutputStream = new FileOutputStream(destFile);
			SVNProperties fileProperty = new SVNProperties();
			// Get file.
			repo.getFile(path, -1L, fileProperty, fileOutputStream);
		} catch (Exception e) {
			LOG.error("Error while fetching files from SVN", e);
			throw new NGrinderRuntimeException("Error while fetching files from SVN", e);
		} finally {
			closeSVNClientManagerQuietly(svnClientManager);
			IOUtils.closeQuietly(fileOutputStream);
		}
	}
}
