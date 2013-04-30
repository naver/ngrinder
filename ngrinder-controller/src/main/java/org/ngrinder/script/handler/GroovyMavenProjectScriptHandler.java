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
package org.ngrinder.script.handler;

import static org.ngrinder.common.util.CollectionUtils.newArrayList;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.maven.cli.MavenCli;
import org.ngrinder.common.exception.NGrinderRuntimeException;
import org.ngrinder.common.util.PropertiesWrapper;
import org.ngrinder.model.User;
import org.ngrinder.script.model.FileEntry;
import org.ngrinder.script.model.FileType;
import org.ngrinder.script.repository.FileEntryRepository;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import org.tmatesoft.svn.core.wc.SVNRevision;

/**
 * Groovy Maven project {@link ScriptHandler}.
 * 
 * @author JunHo Yoon
 * @since 3.2
 */
@Component
public class GroovyMavenProjectScriptHandler extends GroovyScriptHandler implements ProjectHandler {
	/**
	 * Constructor.
	 */
	public GroovyMavenProjectScriptHandler() {
		super("GroovyMaven", "", "Groovy Maven Project", "groovy");
	}

	private static final String RESOURCES = "/src/main/resources/";
	private static final String JAVA = "/src/main/java/";
	private static final String LIB = "/lib/";

	@Override
	public boolean canHandle(FileEntry fileEntry) {
		if (fileEntry.getCreatedUser() == null) {
			return false;
		}
		String path = fileEntry.getPath();
		if (!path.contains(JAVA) || !FilenameUtils.isExtension(path, "groovy")) {
			return false;
		}

		return getFileEntryRepository().hasFileEntry(fileEntry.getCreatedUser(),
						path.substring(0, path.lastIndexOf(JAVA)) + "/pom.xml");
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
	public List<FileEntry> getLibAndResourceEntries(User user, FileEntry scriptEntry, long revision) {
		List<FileEntry> fileList = newArrayList();
		String basePath = getBasePath(scriptEntry);
		FileEntryRepository fileEntryRepository = getFileEntryRepository();
		for (FileEntry eachFileEntry : fileEntryRepository.findAll(user, basePath + RESOURCES, revision, true)) {
			FileType fileType = eachFileEntry.getFileType();
			if (fileType.isResourceDistributable()) {
				fileList.add(eachFileEntry);
			}
		}

		for (FileEntry eachFileEntry : fileEntryRepository.findAll(user, basePath + JAVA, revision, true)) {
			FileType fileType = eachFileEntry.getFileType();
			if (fileType.isLibDistribtable()) {
				fileList.add(eachFileEntry);
			}
		}

		for (FileEntry eachFileEntry : fileEntryRepository.findAll(user, basePath + LIB, revision)) {
			FileType fileType = eachFileEntry.getFileType();
			if (fileType.isLibDistribtable()) {
				fileList.add(eachFileEntry);
			}
		}
		fileList.add(fileEntryRepository.findOne(user, basePath + "/pom.xml", SVNRevision.create(revision)));
		return fileList;
	}

	@Override
	protected String calcDistSubPath(String basePath, FileEntry each) {
		String calcDistSubPath = super.calcDistSubPath(basePath, each);
		if (calcDistSubPath.startsWith(JAVA)) {
			return calcDistSubPath.substring(JAVA.length() - 1);
		} else if (calcDistSubPath.startsWith(RESOURCES)) {
			return calcDistSubPath.substring(RESOURCES.length() - 1);
		}
		return calcDistSubPath;
	}

	@Override
	protected void prepareDistMore(String identifier, User user, FileEntry script, File distDir,
					PropertiesWrapper properties) {
		String pomPathInSVN = getBasePath(script) + "pom.xml";
		MavenCli cli = new MavenCli();
		ByteArrayOutputStream writer = new ByteArrayOutputStream();
		cli.doMain(new String[] { // goal specification
		"dependency:copy-dependencies", // run dependency goal
				"-DoutputDirectory=./lib", // to the lib folder
				"-DexcludeScope=provided" // but exclude the provided library
		}, distDir.getAbsolutePath(), new PrintStream(writer), new PrintStream(writer));
		LOGGER.info("Files in {} is copied into {}/lib folder", pomPathInSVN, distDir.getAbsolutePath());
		LOGGER.info(writer.toString());
		FileUtils.deleteQuietly(new File(distDir, "pom.xml"));
	}

	@Override
	public boolean prepareScriptEnv(User user, String path) {
		File scriptTemplateDir;
		FileEntryRepository fileEntryRepository = getFileEntryRepository();
		try {
			scriptTemplateDir = new ClassPathResource("/script_template/" + getKey()).getFile();

			for (File each : FileUtils.listFiles(scriptTemplateDir, null, true)) {
				try {
					String substring = each.getPath().substring(scriptTemplateDir.getPath().length());
					byte[] bytes = FileUtils.readFileToByteArray(each);
					FileEntry fileEntry = new FileEntry();
					fileEntry.setContentBytes(bytes);
					fileEntry.setPath(FilenameUtils.normalize(path + substring, true));
					fileEntryRepository.save(user, fileEntry, null);
				} catch (IOException e) {
					throw new NGrinderRuntimeException("Error while saving " + each.getName(), e);
				}
			}
		} catch (IOException e) {
			throw new NGrinderRuntimeException("Error while patching script_template", e);
		}
		return false;
	}

	@Override
	protected String getBasePath(FileEntry script) {
		String path = script.getPath();
		return path.substring(0, path.lastIndexOf(JAVA));
	}

}
