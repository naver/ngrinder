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

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import freemarker.template.Configuration;
import freemarker.template.DefaultObjectWrapper;
import freemarker.template.Template;
import freemarker.template.TemplateNotFoundException;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.ngrinder.common.constant.ControllerConstants;
import org.ngrinder.common.exception.NGrinderRuntimeException;
import org.ngrinder.common.exception.PerfTestPrepareException;
import org.ngrinder.common.util.PathUtils;
import org.ngrinder.common.util.PropertiesWrapper;
import org.ngrinder.infra.config.Config;
import org.ngrinder.model.PerfTest;
import org.ngrinder.model.User;
import org.ngrinder.script.model.FileEntry;
import org.ngrinder.script.model.FileType;
import org.ngrinder.script.repository.FileEntryRepository;
import org.ngrinder.script.repository.GitHubFileEntryRepository;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.util.List;
import java.util.Map;

import static freemarker.template.Configuration.DEFAULT_INCOMPATIBLE_IMPROVEMENTS;
import static org.apache.commons.io.FilenameUtils.getFullPath;
import static org.apache.commons.lang.StringUtils.startsWithIgnoreCase;
import static org.ngrinder.common.util.CollectionUtils.newArrayList;
import static org.ngrinder.common.util.ExceptionUtils.processException;
import static org.ngrinder.common.util.FileUtils.copyResourceToFile;
import static org.ngrinder.common.util.LoggingUtils.format;

/**
 * Script per language handler. This is the superclass for all sub
 * {@link ScriptHandler}s which implements the specific processing of each
 * language.
 *
 * @since 3.2
 */
@Getter
@Slf4j
public abstract class ScriptHandler implements ControllerConstants {

	private final String codemirrorKey;
	private final String title;
	private final String extension;
	private final String key;
	private final boolean creatable;

	/**
	 * Constructor.
	 *
	 * @param key           key of the script handler
	 * @param extension     extension
	 * @param title         title of the handler
	 * @param codeMirrorKey code mirror key
	 */
	public ScriptHandler(String key, String extension, String title, String codeMirrorKey, boolean creatable) {
		this.key = key;
		this.extension = extension;
		this.title = title;
		this.codemirrorKey = codeMirrorKey;
		this.creatable = creatable;
	}

	@Autowired
	@JsonIgnore
	private FileEntryRepository fileEntryRepository;

	@Autowired
	@JsonIgnore
	private GitHubFileEntryRepository gitHubFileEntryRepository;

	@Autowired
	@JsonIgnore
	private Config config;

	/**
	 * Get the display order of {@link ScriptHandler}s.
	 *
	 * @return order
	 */
	public abstract Integer displayOrder();

	/**
	 * Check if the given fileEntry can be handled by this handler.
	 *
	 * @param fileEntry fileEntry to be checked
	 * @return true if the given fileEntry can be handled
	 */
	public boolean canHandle(FileEntry fileEntry) {
		return FilenameUtils.isExtension(fileEntry.getPath(), getExtension());
	}

	/**
	 * Get the handler resolution order.
	 * <p/>
	 * Less is more prioritized.
	 *
	 * @return the order of handler resolution
	 */
	protected abstract Integer order();

	@SuppressWarnings("unused")
	@JsonProperty
	public boolean isValidatable() {
		return true;
	}

	/**
	 * Return if it's project handler which implements {@link ProjectHandler}.
	 *
	 * @return true if it is.
	 */

	@SuppressWarnings("UnusedDeclaration")
	@JsonProperty
	public boolean isProjectHandler() {
		return (this instanceof ProjectHandler);
	}

	/**
	 * Prepare the distribution.
	 *
	 * @param perfTest         current running test.
	 * @param user             user who will distribute the script.
	 * @param scriptEntry      script to be distributed.
	 * @param distDir          distribution target dir.
	 * @param properties       properties set which is used for detailed distribution control.
	 * @param processingResult processing result holder.
	 */
	public void prepareDist(PerfTest perfTest,
							User user,
							FileEntry scriptEntry, File distDir, PropertiesWrapper properties,
							ProcessingResultPrintStream processingResult) {
		prepareDefaultFile(distDir, properties);
		List<FileEntry> fileEntries = getLibAndResourceEntries(user, scriptEntry, -1);
		if (scriptEntry.getRevision() != 0) {
			fileEntries.add(scriptEntry);
		}

		String basePath = getBasePath(scriptEntry);
		// Distribute each files in that folder.
		try {
			for (FileEntry each : fileEntries) {
				// Directory is not subject to be distributed.
				if (each.getFileType() == FileType.DIR) {
					continue;
				}
				File toDir = new File(distDir, calcDistSubPath(basePath, each));
				processingResult.printf("%s is being written.\n", each.getPath());
				log.info(format(perfTest, "{} is being written in {}", each.getPath(), toDir));
				if (isGitHubFileEntry(each)) {
					gitHubFileEntryRepository.writeContentTo(each.getPath(), toDir);
				} else {
					fileEntryRepository.writeContentTo(user, each.getPath(), toDir);
				}
			}
		} catch (IOException ex) {
			throw new PerfTestPrepareException("Fail to copy perftest files from distribution folder.\n" +
				"If you change your branch configuration, please click script refresh button before running test.", ex);
		}
		processingResult.setSuccess(true);
		prepareDistMore(perfTest, user, scriptEntry, distDir, properties, processingResult);
	}

	protected boolean isGitHubFileEntry(FileEntry fileEntry) {
		Map<String, String> properties = fileEntry.getProperties();
		return properties != null && StringUtils.equals(properties.get("scm"), "github");
	}

	/**
	 * Prepare script creation. This method is subject to be extended by the
	 * subclasses.
	 * <p/>
	 * This method is the perfect place if it's necessary to include additional
	 * files.
	 *
	 * @param user                  user
	 * @param path                  base path
	 * @param fileName              fileName
	 * @param name                  name
	 * @param url                   url
	 * @param createLibAndResources true if lib and resources should be created
	 * @return true if process more.
	 */
	@SuppressWarnings("UnusedReturnValue")
	public boolean prepareScriptEnv(User user, String path, String fileName, String name, String url,
									boolean createLibAndResources, String scriptContent) {
		return true;
	}

	/**
	 * Prepare the distribution more. This method is subject to be extended by
	 * the subclass.
	 *
	 * @param perfTest         current running test.
	 * @param user             user
	 * @param script           script entry to be distributed.
	 * @param distDir          distribution directory
	 * @param properties       properties
	 * @param processingResult processing result holder
	 */
	protected void prepareDistMore(PerfTest perfTest, User user, FileEntry script, File distDir,
	                               PropertiesWrapper properties, ProcessingResultPrintStream processingResult) {
	}

	/**
	 * Get the appropriated distribution path for the given file entry.
	 *
	 * @param basePath  distribution base path
	 * @param fileEntry fileEntry to be distributed
	 * @return the resolved destination path.
	 */
	protected String calcDistSubPath(String basePath, FileEntry fileEntry) {
		String path = getFullPath(fileEntry.getPath());
		return path.substring(basePath.length());
	}

	/**
	 * Get all resources and lib entries belonging to the given user and
	 * scriptEntry.
	 *
	 * @param user        user
	 * @param scriptEntry script entry
	 * @param revision    revision of the script entry.
	 * @return file entry list
	 */
	public List<FileEntry> getLibAndResourceEntries(User user, FileEntry scriptEntry, long revision) {
		String path = getFullPath(scriptEntry.getPath());
		List<FileEntry> fileList = newArrayList();
		List<FileEntry> libFileEntries;
		List<FileEntry> resourceFileEntries;

		if (isGitHubFileEntry(scriptEntry)) {
			try {
				libFileEntries = gitHubFileEntryRepository.findAll(path + "lib");
				resourceFileEntries = gitHubFileEntryRepository.findAll(path + "resources");
			} catch (IOException e) {
				throw new NGrinderRuntimeException(e);
			}
		} else {
			libFileEntries = fileEntryRepository.findAll(user, path + "lib/", revision, true);
			resourceFileEntries = fileEntryRepository.findAll(user, path + "resources/", revision, true);
		}

		for (FileEntry eachFileEntry : libFileEntries) {
			// Skip jython 2.7... it's already included.
			if (startsWithIgnoreCase(eachFileEntry.getFileName(), "jython-2.7.")
					|| startsWithIgnoreCase(eachFileEntry.getFileName(), "jython-standalone-2.7.")) {
				continue;
			}
			FileType fileType = eachFileEntry.getFileType();
			if (fileType.isLibDistributable()) {
				fileList.add(eachFileEntry);
			}
		}

		for (FileEntry eachFileEntry : resourceFileEntries) {
			FileType fileType = eachFileEntry.getFileType();
			if (fileType.isResourceDistributable()) {
				fileList.add(eachFileEntry);
			}
		}

		return fileList;
	}

	protected void prepareDefaultFile(File distDir, PropertiesWrapper properties) {
		if (properties.getPropertyBoolean(PROP_CONTROLLER_DIST_LOGBACK)) {
			copyResourceToFile("/logback/logback-worker.xml", new File(distDir, "logback-worker.xml"));
		}
	}

	protected String getBasePath(FileEntry script) {
		return getBasePath(script.getPath());
	}

	/**
	 * Get the base path of the given path.
	 *
	 * @param path path
	 * @return base path
	 */
	public String getBasePath(String path) {
		return getFullPath(path);
	}

	/**
	 * Get executable script path.
	 *
	 * @param svnPath path in svn
	 * @return path executable in agent.
	 */
	public String getScriptExecutePath(String svnPath) {
		return FilenameUtils.getName(svnPath);
	}

	/**
	 * Check syntax errors for the given content.
	 *
	 * @param path    path
	 * @param content content
	 * @return syntax error messages. null if none.
	 */
	public abstract String checkSyntaxErrors(String path, String content);

	/**
	 * Get the initial script with the given value map.
	 *
	 * @param values map of initial script referencing values.
	 * @return generated string
	 */
	public String getScriptTemplate(Map<String, Object> values) {
		try {
			Configuration freemarkerConfig = new Configuration(DEFAULT_INCOMPATIBLE_IMPROVEMENTS);
			freemarkerConfig.setObjectWrapper(new DefaultObjectWrapper(DEFAULT_INCOMPATIBLE_IMPROVEMENTS));
			freemarkerConfig.setDirectoryForTemplateLoading(config.getHomeScriptTemplateDirectory());
			Template template = freemarkerConfig.getTemplate(getScriptTemplateName());
			StringWriter writer = new StringWriter();
			template.process(values, writer);
			return writer.toString();
		} catch (Exception e) {
			throw processException("Error while fetching the script template.", e);
		}
	}

	/**
	 * Get the default quick test file.
	 *
	 * @param basePath base path
	 * @return quick test file
	 */
	public FileEntry getDefaultQuickTestFilePath(String basePath) {
		FileEntry fileEntry = new FileEntry();
		fileEntry.setPath(PathUtils.join(basePath, "TestRunner." + getExtension()));
		return fileEntry;
	}

	public String getScriptTemplateName() {
		return "basic_template_" + getExtension() + ".ftl";
	}
}
