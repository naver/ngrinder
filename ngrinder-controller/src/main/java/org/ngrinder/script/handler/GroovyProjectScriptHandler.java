/*
 * Copyright (c) 2012-present NAVER Corp.
 *
 * This file is part of The nGrinder software distribution. Refer to
 * the file LICENSE which is part of The nGrinder distribution for
 * licensing details. The nGrinder distribution is available on the
 * Internet at https://naver.github.io/ngrinder
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ngrinder.script.handler;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.ngrinder.common.exception.NGrinderRuntimeException;
import org.ngrinder.common.util.PathUtils;
import org.ngrinder.common.util.PropertiesWrapper;
import org.ngrinder.model.PerfTest;
import org.ngrinder.model.User;
import org.ngrinder.script.model.FileEntry;
import org.ngrinder.script.model.FileType;
import org.ngrinder.script.repository.FileEntryRepository;
import org.ngrinder.script.repository.GitHubFileEntryRepository;
import org.tmatesoft.svn.core.wc.SVNRevision;

import java.io.File;
import java.io.IOException;
import java.util.List;

import static org.apache.commons.io.FileUtils.deleteQuietly;
import static org.ngrinder.common.util.CollectionUtils.newArrayList;
import static org.ngrinder.common.util.LoggingUtils.format;
import static org.ngrinder.script.model.FileType.DIR;
import static oshi.util.ExecutingCommand.runNative;

/**
 * Groovy project {@link ScriptHandler}.
 *
 * @since 3.5.3
 */
@Getter
@Slf4j
public abstract class GroovyProjectScriptHandler extends GroovyScriptHandler implements ProjectHandler {

	protected static String RESOURCES = "src/main/resources/";
	protected static String JAVA = "src/main/java/";
	protected static String GROOVY = "src/main/groovy/";
	protected static String LIB = "lib/";

	private final String buildScriptName;

	public GroovyProjectScriptHandler(String key, String extension, String title,
									  String codeMirrorKey, String buildScriptName, boolean creatable) {
		super(key, extension, title, codeMirrorKey, creatable);
		this.buildScriptName = buildScriptName;
	}

	/**
	 * Return a command to copy dependencies specified in the build script to dist directory.
	 * */
	protected abstract String getCopyDependenciesCommand(File distDir);

	/**
	 * Check if copy dependencies is successful.
	 *
	 * @param results Result of executing command created by {@link #getCopyDependenciesCommand} with OSHI.
	 */
	protected abstract boolean isSuccess(List<String> results);

	@Override
	public boolean canHandle(FileEntry fileEntry) {
		String path = fileEntry.getPath();
		if (!FilenameUtils.isExtension(path, "groovy")) {
			return false;
		}

		//noinspection SimplifiableIfStatement
		if (!isGroovyProjectScriptPath(path)) {
			return false;
		}

		if (isGitHubFileEntry(fileEntry)) {
			return StringUtils.equals(fileEntry.getProperties().get("type"), getKey());
		} else {
			if (fileEntry.getCreatedBy() == null) {
				return false;
			}
			return getFileEntryRepository().hasOne(fileEntry.getCreatedBy(), getBasePath(path) + buildScriptName);
		}
	}

	@Override
	public Integer displayOrder() {
		return 400;
	}

	@Override
	protected Integer order() {
		return 200;
	}

	@Override
	public String getScriptExecutePath(String path) {
		if (path.contains(JAVA)) {
			return path.substring(path.lastIndexOf(JAVA) + JAVA.length());
		} else {
			return path.substring(path.lastIndexOf(GROOVY) + GROOVY.length());
		}
	}

	@Override
	public FileEntry getDefaultQuickTestFilePath(String path) {
		FileEntry fileEntry = new FileEntry();
		fileEntry.setPath(path + "/" + JAVA + "TestRunner.groovy");
		return fileEntry;
	}

	@Override
	protected String calcDistSubPath(String basePath, FileEntry each) {
		String calcDistSubPath = super.calcDistSubPath(basePath, each);
		if (calcDistSubPath.startsWith(JAVA)) {
			return calcDistSubPath.substring(JAVA.length() - 1);
		} else if (calcDistSubPath.startsWith(GROOVY)) {
			return calcDistSubPath.substring(GROOVY.length() - 1);
		} else if (calcDistSubPath.startsWith(RESOURCES)) {
			return calcDistSubPath.substring(RESOURCES.length() - 1);
		}
		return calcDistSubPath;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * org.ngrinder.script.handler.ScriptHandler#getBasePath(java.lang.String)
	 */
	@Override
	public String getBasePath(String path) {
		if (path.contains(JAVA)) {
			return path.substring(0, path.lastIndexOf(JAVA));
		} else {
			return path.substring(0, path.lastIndexOf(GROOVY));
		}
	}

	@Override
	protected void prepareDistMore(PerfTest perfTest, User user, FileEntry script, File distDir,
								   PropertiesWrapper properties, ProcessingResultPrintStream processingResult) {
		String buildFilePathInSVN = PathUtils.join(getBasePath(script), getBuildScriptName());
		String copyDependenciesCommand = getCopyDependenciesCommand(distDir);
		processingResult.println("\nCopy dependencies by running '" + copyDependenciesCommand + "'");

		boolean success = isSuccess(runNative(copyDependenciesCommand));

		if (success) {
			processingResult.printf("\nDependencies in %s was copied.\n", buildFilePathInSVN);
			log.info(format(perfTest, "Dependencies in {} is copied into {}/lib folder", buildFilePathInSVN, distDir.getAbsolutePath()));
		} else {
			processingResult.printf("\nDependencies copy in %s is failed.\n", buildFilePathInSVN);
			log.info(format(perfTest, "Dependencies copy in {} is failed.", buildFilePathInSVN));
		}

		deleteUnnecessaryFilesFromDist(distDir);
		processingResult.setSuccess(success);
	}

	@Override
	public List<FileEntry> getLibAndResourceEntries(User user, FileEntry scriptEntry, long revision) {
		List<FileEntry> fileList = newArrayList();
		String basePath = getBasePath(scriptEntry);
		FileEntryRepository fileEntryRepository = getFileEntryRepository();
		GitHubFileEntryRepository gitHubFileEntryRepository = getGitHubFileEntryRepository();

		List<FileEntry> resourcesFileEntry;
		List<FileEntry> javaFileEntry;
		List<FileEntry> groovyFileEntry;
		List<FileEntry> libFileEntry;

		if (isGitHubFileEntry(scriptEntry)) {
			try {
				javaFileEntry = gitHubFileEntryRepository.findAll(basePath + JAVA);
				resourcesFileEntry = gitHubFileEntryRepository.findAll(basePath + RESOURCES);
				groovyFileEntry = gitHubFileEntryRepository.findAll(basePath + GROOVY);
				libFileEntry = gitHubFileEntryRepository.findAll(basePath + LIB);
				fileList.add(gitHubFileEntryRepository.findOne(basePath + buildScriptName));
			} catch (IOException e) {
				throw new NGrinderRuntimeException(e);
			}
		} else {
			resourcesFileEntry = fileEntryRepository.findAll(user, basePath + RESOURCES, revision, true);
			javaFileEntry = fileEntryRepository.findAll(user, basePath + JAVA, revision, true);
			groovyFileEntry = fileEntryRepository.findAll(user, basePath + GROOVY, revision, true);
			libFileEntry = fileEntryRepository.findAll(user, basePath + LIB, revision, true);
			fileList.add(fileEntryRepository.findOne(user, basePath + buildScriptName, SVNRevision.create(revision)));
		}

		for (FileEntry eachFileEntry : resourcesFileEntry) {
			FileType fileType = eachFileEntry.getFileType();
			if (fileType.isResourceDistributable()) {
				fileList.add(eachFileEntry);
			}
		}

		for (FileEntry eachFileEntry : javaFileEntry) {
			FileType fileType = eachFileEntry.getFileType();
			if (fileType.isLibDistributable() && !eachFileEntry.getPath().equals(scriptEntry.getPath())) {
				fileList.add(eachFileEntry);
			}
		}

		for (FileEntry eachFileEntry : groovyFileEntry) {
			FileType fileType = eachFileEntry.getFileType();
			if (fileType.isLibDistributable() && !eachFileEntry.getPath().equals(scriptEntry.getPath())) {
				fileList.add(eachFileEntry);
			}
		}

		for (FileEntry eachFileEntry : libFileEntry) {
			FileType fileType = eachFileEntry.getFileType();
			if (fileType.isLibDistributable()) {
				fileList.add(eachFileEntry);
			}
		}

		return fileList;
	}

	protected void deleteUnnecessaryFilesFromDist(File distDir) {
		deleteQuietly(new File(distDir, getBuildScriptName()));
	}

	protected void createLibraryDirectory(User user, String path) {
		FileEntry fileEntry = new FileEntry();
		fileEntry.setPath(path + "/lib");
		fileEntry.setFileType(DIR);
		fileEntry.setDescription("put private libraries here");
		getFileEntryRepository().save(user, fileEntry, null);
	}

	public String getGroovyProjectPath(String path) {
		if (path.contains(JAVA)) {
			return path.substring(path.lastIndexOf(JAVA));
		} else {
			return path.substring(path.lastIndexOf(GROOVY));
		}
	}

	@SuppressWarnings("BooleanMethodIsAlwaysInverted")
	public boolean isGroovyProjectScriptPath(String path) {
		return path.contains(JAVA) || path.contains(GROOVY);
	}
}
