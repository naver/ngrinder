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
package org.ngrinder.script.repository;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.apache.commons.lang.StringUtils;
import org.ngrinder.common.exception.NGrinderRuntimeException;
import org.ngrinder.common.model.Home;
import org.ngrinder.common.util.EncodingUtils;
import org.ngrinder.infra.config.Config;
import org.ngrinder.model.User;
import org.ngrinder.script.model.FileCategory;
import org.ngrinder.script.model.FileEntry;
import org.ngrinder.script.model.FileType;
import org.ngrinder.user.repository.UserRepository;
import org.ngrinder.user.service.UserContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.tmatesoft.svn.core.*;
import org.tmatesoft.svn.core.auth.ISVNAuthenticationManager;
import org.tmatesoft.svn.core.internal.io.fs.FSRepositoryFactory;
import org.tmatesoft.svn.core.internal.wc.DefaultSVNOptions;
import org.tmatesoft.svn.core.io.ISVNEditor;
import org.tmatesoft.svn.core.io.SVNRepository;
import org.tmatesoft.svn.core.io.diff.SVNDeltaGenerator;
import org.tmatesoft.svn.core.wc.SVNClientManager;
import org.tmatesoft.svn.core.wc.SVNRevision;
import org.tmatesoft.svn.core.wc.SVNWCUtil;

import javax.annotation.PostConstruct;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.EmptyStackException;
import java.util.List;
import java.util.Map.Entry;

import static org.ngrinder.common.util.CollectionUtils.newArrayList;
import static org.ngrinder.common.util.ExceptionUtils.processException;
import static org.ngrinder.common.util.NoOp.noOp;
import static org.ngrinder.common.util.Preconditions.checkNotNull;

/**
 * SVN FileEntity repository.
 *
 * This class save and retrieve {@link FileEntry} from Local SVN folders.
 *
 * @author JunHo Yoon
 * @since 3.0
 */
@Profile("production")
@Component
public class FileEntryRepository {

	private static final Logger LOG = LoggerFactory.getLogger(FileEntryRepository.class);

	@Autowired
	private Config config;

	private Home home;

	private File subversionHome;

	/**
	 * Initialize the {@link FileEntryRepository}. This method should be
	 * performed to set up FS Repository.
	 */
	@PostConstruct
	public void init() {
		FSRepositoryFactory.setup();
		home = config.getHome();
		subversionHome = home.getSubFile("subversion");
	}

	@Autowired
	private UserRepository userRepository;

	/**
	 * Get user repository.
	 *
	 * For unit test, This can be overridable.
	 *
	 * @param user the user
	 * @return user repository path.
	 */
	public File getUserRepoDirectory(User user) {
		return home.getUserRepoDirectory(user.getUserId());
	}

	/**
	 * Return all {@link FileEntry}s under the given path.
	 *
	 * @param user     user
	 * @param path     path under which files are searched.
	 * @param revision . null if head.
	 * @return found {@link FileEntry}s
	 */
	public List<FileEntry> findAll(User user, final String path, Long revision) {
		return findAll(user, path, revision, false);
	}

	/**
	 * Return all {@link FileEntry}s under the given path.
	 *
	 * @param user      user
	 * @param path      path under which files are searched.
	 * @param revision  null if head.
	 * @param recursive true if recursive finding
	 * @return found {@link FileEntry}s
	 */
	public List<FileEntry> findAll(User user, final String path, Long revision, boolean recursive) {
		SVNRevision svnRevision = SVNRevision.HEAD;
		if (revision != null && -1L != revision) {
			svnRevision = SVNRevision.create(revision);
		}
		final List<FileEntry> fileEntries = newArrayList();
		SVNClientManager svnClientManager = getSVNClientManager();
		try {
			svnClientManager.getLogClient().doList(SVNURL.fromFile(getUserRepoDirectory(user)).appendPath(path, true),
					svnRevision, svnRevision, true, recursive, new ISVNDirEntryHandler() {
				@Override
				public void handleDirEntry(SVNDirEntry dirEntry) throws SVNException {

					FileEntry script = new FileEntry();
					// Exclude base path "/"
					if (StringUtils.isBlank(dirEntry.getRelativePath())) {
						return;
					}
					script.setPath(FilenameUtils.normalize(path + "/" + dirEntry.getRelativePath(), true));
					script.setCreatedDate(dirEntry.getDate());
					script.setLastModifiedDate(dirEntry.getDate());
					script.setDescription(dirEntry.getCommitMessage());
					script.setRevision(dirEntry.getRevision());
					if (dirEntry.getKind() == SVNNodeKind.DIR) {
						script.setFileType(FileType.DIR);
					} else {
						script.getFileType();
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
	 * Return all {@link FileEntry}s which user have. It excludes
	 * {@link FileType#DIR} entries.
	 *
	 * @param user user
	 * @return found {@link FileEntry}s
	 */
	public List<FileEntry> findAll(final User user) {
		final List<FileEntry> scripts = newArrayList();
		SVNClientManager svnClientManager = getSVNClientManager();
		try {
			svnClientManager.getLogClient().doList(SVNURL.fromFile(getUserRepoDirectory(user)), SVNRevision.HEAD,
					SVNRevision.HEAD, false, true, new ISVNDirEntryHandler() {
				@Override
				public void handleDirEntry(SVNDirEntry dirEntry) throws SVNException {
					FileEntry script = new FileEntry();
					String relativePath = dirEntry.getRelativePath();
					if (StringUtils.isBlank(relativePath)) {
						return;
					}
					script.setCreatedDate(dirEntry.getDate());
					script.setLastModifiedDate(dirEntry.getDate());
					script.setPath(relativePath);
					script.setDescription(dirEntry.getCommitMessage());
					long reversion = dirEntry.getRevision();
					script.setRevision(reversion);
					script.setFileType(dirEntry.getKind() == SVNNodeKind.DIR ? FileType.DIR : null);
					script.setFileSize(dirEntry.getSize());
					scripts.add(script);
				}
			});
		} catch (Exception e) {
			LOG.error("Error while fetching files from SVN for {}", user.getUserId());
			LOG.debug("Error details :", e);
			throw new NGrinderRuntimeException(e);
		} finally {
			closeSVNClientManagerQuietly(svnClientManager);
		}
		return scripts;

	}

	/**
	 * Return a {@link FileEntry} for the given path and revision.
	 *
	 * @param user     user
	 * @param path     path in the svn repo
	 * @param revision revision of the file
	 * @return found {@link FileEntry}, null if not found
	 */
	public FileEntry findOne(User user, String path, SVNRevision revision) {
		final FileEntry script = new FileEntry();
		SVNClientManager svnClientManager = null;
		ByteArrayOutputStream outputStream = null;
		try {
			svnClientManager = getSVNClientManager();

			SVNURL userRepoUrl = SVNURL.fromFile(getUserRepoDirectory(user));
			if (userRepoUrl == null) {
				return null;
			}
			SVNRepository repo = svnClientManager.createRepository(userRepoUrl, true);
			SVNNodeKind nodeKind = repo.checkPath(path, -1);
			if (nodeKind == SVNNodeKind.NONE) {
				return null;
			}
			outputStream = new ByteArrayOutputStream();
			SVNProperties fileProperty = new SVNProperties();
			// Get File.
			repo.getFile(path, revision.getNumber(), fileProperty, outputStream);
			SVNDirEntry lastRevisionedEntry = repo.info(path, -1);
			long lastRevisionNumber = (lastRevisionedEntry == null) ? -1 : lastRevisionedEntry.getRevision();
			String revisionStr = fileProperty.getStringValue(SVNProperty.REVISION);
			long revisionNumber = Long.parseLong(revisionStr);
			SVNDirEntry info = repo.info(path, revisionNumber);
			byte[] byteArray = outputStream.toByteArray();
			script.setPath(path);
			for (String name : fileProperty.nameSet()) {
				script.getProperties().put(name, fileProperty.getStringValue(name));
			}
			script.setFileType(FileType.getFileTypeByExtension(FilenameUtils.getExtension(script.getFileName())));
			if (script.getFileType().isEditable()) {
				String autoDetectedEncoding = EncodingUtils.detectEncoding(byteArray, "UTF-8");
				script.setContent((new String(byteArray, autoDetectedEncoding)).replaceAll("&quot;","\""));
				script.setEncoding(autoDetectedEncoding);
				script.setContentBytes(byteArray);
			} else {
				script.setContentBytes(byteArray);
			}
			script.setDescription(info.getCommitMessage());
			script.setRevision(revisionNumber);
			script.setLastRevision(lastRevisionNumber);
			script.setCreatedUser(user);
		} catch (Exception e) {
			LOG.error("Error while fetching a file from SVN {}", user.getUserId() + "_" + path, e);
			return null;
		} finally {
			closeSVNClientManagerQuietly(svnClientManager);
			IOUtils.closeQuietly(outputStream);
		}
		return script;
	}

	private void addPropertyValue(ISVNEditor editor, FileEntry fileEntry) throws SVNException {
		if (fileEntry.getFileType().getFileCategory() == FileCategory.SCRIPT) {
			editor.changeFileProperty(fileEntry.getPath(), "targetHosts", SVNPropertyValue.create(""));
		}
		for (Entry<String, String> each : fileEntry.getProperties().entrySet()) {
			editor.changeFileProperty(fileEntry.getPath(), each.getKey(), SVNPropertyValue.create(each.getValue()));
		}
	}

	/**
	 * Save fileEntry on the {@link FileEntry.getPath()} location.
	 *
	 * @param user      the user
	 * @param fileEntry fileEntry to be saved
	 * @param encoding  file encoding with which fileEntry is saved. It is meaningful
	 *                  only FileEntry is editable.
	 */
	public void save(User user, FileEntry fileEntry, String encoding) {
		SVNClientManager svnClientManager = null;
		ISVNEditor editor = null;
		String checksum = null;
		InputStream bais = null;
		try {
			svnClientManager = getSVNClientManager();
			SVNRepository repo = svnClientManager.createRepository(SVNURL.fromFile(getUserRepoDirectory(user)), true);
			SVNDirEntry dirEntry = repo.info(fileEntry.getPath(), -1);

			// Add base paths
			String fullPath = "";
			// Check.. first
			for (String each : getPathFragment(fileEntry.getPath())) {
				fullPath = fullPath + "/" + each;
				SVNDirEntry folderStepEntry = repo.info(fullPath, -1);
				if (folderStepEntry != null && folderStepEntry.getKind() == SVNNodeKind.FILE) {
					throw processException("User " + user.getUserId() + " tried to create folder "
							+ fullPath + ". It's file..");
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
					// FALL THROUGH
					noOp();
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
				if (fileEntry.getContentBytes() == null && fileEntry.getFileType().isEditable()) {
					bais = new ByteArrayInputStream(checkNotNull(fileEntry.getContent()).getBytes(
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
				if (SVNErrorCode.FS_ALREADY_EXISTS.equals(((SVNException) e).getErrorMessage().getErrorCode())) {
					return;
				}
			}
			LOG.error("Error while saving file to SVN", e);
			throw processException("Error while saving file to SVN", e);
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
	 * @param editor editor to be closed.
	 */
	private void abortSVNEditorQuietly(ISVNEditor editor) {
		if (editor == null) {
			return;
		}
		try {
			editor.abortEdit();
		} catch (SVNException e) {
			// FALL THROUGH
			noOp();
		}
	}

	/**
	 * Quietly close svn editor. This is convenient method.
	 *
	 * @param editor editor to be closed.
	 */
	private void closeSVNEditorQuietly(ISVNEditor editor) {
		if (editor == null) {
			return;
		}
		try {
			// recursively close
			//noinspection InfiniteLoopStatement
			while (true) {
				editor.closeDir();
			}
		} catch (EmptyStackException e) {
			// FALL THROUGH
			noOp();
		} catch (SVNException e) {
			// FALL THROUGH
			noOp();
		} finally {
			try {
				editor.closeEdit();
			} catch (SVNException e) {
				// FALL THROUGH
				noOp();
			}
		}
	}

	/**
	 * Delete file entries on given paths. If the one of paths does not exist,
	 * all deletion is canceled.
	 *
	 * @param user  user
	 * @param paths paths of file entries.
	 */
	public void delete(User user, List<String> paths) {
		SVNClientManager svnClientManager = null;
		ISVNEditor editor = null;
		try {
			svnClientManager = getSVNClientManager();
			SVNRepository repo = svnClientManager.createRepository(SVNURL.fromFile(getUserRepoDirectory(user)), true);

			editor = repo.getCommitEditor("delete", null, true, null);
			editor.openRoot(-1);
			for (String each : paths) {
				editor.deleteEntry(each, -1);
			}
		} catch (Exception e) {
			abortSVNEditorQuietly(editor);
			LOG.error("Error while deleting file from SVN", e);
			throw processException("Error while deleting files from SVN", e);
		} finally {
			closeSVNEditorQuietly(editor);
			closeSVNClientManagerQuietly(svnClientManager);
		}
	}

	@Autowired
	UserContext userContext;

	/**
	 * Get svn client manager with the designated subversionHome.
	 *
	 * @return svn client manager
	 */
	public SVNClientManager getSVNClientManager() {
		DefaultSVNOptions options = SVNWCUtil.createDefaultOptions(subversionHome, true);
		ISVNAuthenticationManager authManager = SVNWCUtil.createDefaultAuthenticationManager(subversionHome,
				getCurrentUserId(), null, false);
		return SVNClientManager.newInstance(options, authManager);
	}

	protected String getCurrentUserId() {
		try {
			return userContext.getCurrentUser().getUserId();
		} catch (Exception e) {
			return "default";
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
	 * @param user user
	 * @param path path in user repo
	 * @return true if exists.
	 */
	public boolean hasOne(User user, String path) {
		SVNClientManager svnClientManager = null;
		try {
			svnClientManager = getSVNClientManager();
			SVNURL userRepoUrl = SVNURL.fromFile(getUserRepoDirectory(user));
			SVNRepository repo = svnClientManager.createRepository(userRepoUrl, true);
			SVNNodeKind nodeKind = repo.checkPath(path, -1);
			return (nodeKind != SVNNodeKind.NONE);
		} catch (Exception e) {
			LOG.error("Error while fetching files from SVN", e);
			throw processException("Error while checking file existence from SVN", e);
		} finally {
			closeSVNClientManagerQuietly(svnClientManager);
		}
	}

	/**
	 * Copy {@link FileEntry} to the given path.
	 *
	 * This method only work for the file not dir.
	 *
	 * @param user      user
	 * @param path      path of {@link FileEntry}
	 * @param toPathDir file dir path to write.
	 */
	public void writeContentTo(User user, String path, File toPathDir) {
		SVNClientManager svnClientManager = null;
		FileOutputStream fileOutputStream = null;
		try {
			svnClientManager = getSVNClientManager();

			SVNURL userRepoUrl = SVNURL.fromFile(getUserRepoDirectory(user));
			SVNRepository repo = svnClientManager.createRepository(userRepoUrl, true);
			SVNNodeKind nodeKind = repo.checkPath(path, -1);
			// If it's DIR, it does not work.
			if (nodeKind == SVNNodeKind.NONE || nodeKind == SVNNodeKind.DIR) {
				throw processException("It's not possible to write directory. nodeKind is " + nodeKind);
			}
			//noinspection ResultOfMethodCallIgnored
			toPathDir.mkdirs();
			File destFile = new File(toPathDir, FilenameUtils.getName(path));
			// Prepare parent folders
			fileOutputStream = new FileOutputStream(destFile);
			SVNProperties fileProperty = new SVNProperties();
			// Get file.
			repo.getFile(path, -1L, fileProperty, fileOutputStream);
		} catch (Exception e) {
			LOG.error("Error while fetching files from SVN", e);
			throw processException("Error while fetching files from SVN", e);
		} finally {
			closeSVNClientManagerQuietly(svnClientManager);
			IOUtils.closeQuietly(fileOutputStream);
		}
	}
}
