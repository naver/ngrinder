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

import static org.apache.commons.lang.StringUtils.startsWithIgnoreCase;
import static org.ngrinder.common.util.CollectionUtils.newArrayList;

import java.io.File;
import java.io.StringWriter;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FilenameUtils;
import org.ngrinder.common.util.FileUtil;
import org.ngrinder.common.util.PropertiesWrapper;
import org.ngrinder.model.User;
import org.ngrinder.script.model.FileEntry;
import org.ngrinder.script.model.FileType;
import org.ngrinder.script.repository.FileEntryRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;

import freemarker.template.Configuration;
import freemarker.template.DefaultObjectWrapper;
import freemarker.template.Template;

/**
 * Script per language handler. This is the superclass for all sub {@link ScriptHandler}s which
 * implements the specific processing of each language.
 * 
 * @author JunHo Yoon
 * @since 3.2
 */
public abstract class ScriptHandler {
	protected static final Logger LOGGER = LoggerFactory.getLogger(JythonScriptHandler.class);
	private final String codemirrorKey;
	private final String title;
	private final String extension;
	private final String key;

	/**
	 * Constructor.
	 * 
	 * @param key
	 *            key of the script handler
	 * @param extension
	 *            extension
	 * @param title
	 *            title of the handler
	 * @param codemirrorKey
	 *            code minrror key
	 */
	public ScriptHandler(String key, String extension, String title, String codemirrorKey) {
		this.key = key;
		this.extension = extension;
		this.title = title;
		this.codemirrorKey = codemirrorKey;
	}

	@Autowired
	private FileEntryRepository fileEntryRepository;

	/**
	 * Get the display order of {@link ScriptHandler}s.
	 * 
	 * @return order
	 */
	public abstract Integer displayOrder();

	public String getCodemirrorKey() {
		return codemirrorKey;
	}

	/**
	 * Check if the given fileEntry can be handled by this handler.
	 * 
	 * @param fileEntry
	 *            fileEntry to be checked
	 * @return true if handleable
	 */
	public boolean canHandle(FileEntry fileEntry) {
		return FilenameUtils.isExtension(fileEntry.getPath(), getExtension());
	}

	public String getExtension() {
		return extension;
	}

	/**
	 * Get the handler resolution order.
	 * 
	 * Less is more prioritized.
	 * 
	 * @return the order of handler resolution
	 */
	protected abstract Integer order();

	public boolean isValidatable() {
		return true;
	}

	/**
	 * Prepare the distribution.
	 * 
	 * @param testcaseId
	 *            id of the test case. This is for the log identification.
	 * @param user
	 *            user who will distribute the script.
	 * @param scriptEntry
	 *            script to be distributed.
	 * @param distDir
	 *            distribution target dir
	 * @param properties
	 *            properties set which is used for detailed distribution control
	 */
	public void prepareDist(String testcaseId, User user, //
					FileEntry scriptEntry, File distDir, PropertiesWrapper properties) {
		prepareDefaultFile(distDir, properties);
		List<FileEntry> fileEntries = getLibAndResourceEntries(user, scriptEntry, -1);
		fileEntries.add(scriptEntry);
		String basePath = getBasePath(scriptEntry);
		// Distribute each files in that folder.
		for (FileEntry each : fileEntries) {
			// Directory is not subject to be distributed.
			if (each.getFileType() == FileType.DIR) {
				continue;
			}
			File toDir = new File(distDir, calcDistSubPath(basePath, each));
			LOGGER.info("{} is being written in {} for test {}", new Object[] { each.getPath(), toDir, testcaseId });
			getFileEntryRepository().writeContentTo(user, each.getPath(), toDir);
		}
		prepareDistMore(testcaseId, user, scriptEntry, distDir, properties);
	}

	/**
	 * Prepare script creation. This method is subject to be extended by the subclasses.
	 * 
	 * This method is the perfect place if it's necessary to include additional files.
	 * 
	 * @param user
	 *            user
	 * @param path
	 *            the path where the env preparation is required.
	 * @return true if process more.
	 * @throws IOException
	 */
	public boolean prepareScriptEnv(User user, String path) {
		return true;
	}

	/**
	 * Prepare the distribution more. This method is subject to be extended by the subclass.
	 * 
	 * @param testcaseId
	 *            testcase id. This is for the log identification.
	 * @param user
	 *            user
	 * @param script
	 *            script entry to be distributed.
	 * @param distDir
	 *            distribution directory
	 * @param properties
	 *            properties.
	 */
	protected void prepareDistMore(String testcaseId, User user, FileEntry script, File distDir,
					PropertiesWrapper properties) {

	}

	/**
	 * Get the appropriated distribution path for the given file entry.
	 * 
	 * @param basePath
	 *            distribution base path
	 * @param fileEntry
	 *            fileEntry to be distributed
	 * @return the resolved destination path.
	 */
	protected String calcDistSubPath(String basePath, FileEntry fileEntry) {
		String path = FilenameUtils.getPath(fileEntry.getPath());
		path = path.substring(basePath.length());
		return path;
	}

	/**
	 * Get all resources and lib entries belonging to the given user and scriptEntry.
	 * 
	 * @param user
	 *            user
	 * @param scriptEntry
	 *            script entry
	 * @param revision
	 *            revision of the script entry.
	 * @return file entry list
	 */
	public List<FileEntry> getLibAndResourceEntries(User user, FileEntry scriptEntry, long revision) {
		String path = FilenameUtils.getPath(scriptEntry.getPath());
		List<FileEntry> fileList = newArrayList();
		for (FileEntry eachFileEntry : getFileEntryRepository().findAll(user, path + "lib/", revision)) {
			// Skip jython 2.5... it's already included.
			if (startsWithIgnoreCase(eachFileEntry.getFileName(), "jython-2.5.")
							|| startsWithIgnoreCase(eachFileEntry.getFileName(), "jython-standalone-2.5.")) {
				continue;
			}
			FileType fileType = eachFileEntry.getFileType();
			if (fileType.isLibDistribtable()) {
				fileList.add(eachFileEntry);
			}
		}
		for (FileEntry eachFileEntry : getFileEntryRepository().findAll(user, path + "resources/", revision)) {
			FileType fileType = eachFileEntry.getFileType();
			if (fileType.isResourceDistributable()) {
				fileList.add(eachFileEntry);
			}
		}
		return fileList;
	}

	protected void prepareDefaultFile(File distDir, PropertiesWrapper properties) {
		if (properties.getPropertyBoolean("ngrinder.dist.logback", true)) {
			FileUtil.copyResourceToFile("/logback/logback-worker.xml", new File(distDir, "logback-worker.xml"));
		}
	}

	protected String getBasePath(FileEntry script) {
		return FilenameUtils.getPath(script.getPath());
	}

	/**
	 * Check syntax errors for the given content.
	 * 
	 * @param content
	 *            content
	 * @return syntax error messages. null if none.
	 */
	public abstract String checkSyntaxErrors(String content);

	/**
	 * Get the initial script with the given value map.
	 * 
	 * @param map
	 *            map of initial script referencing values.
	 * @return generated string
	 */
	public String getInitialScript(Map<String, Object> map) {
		try {
			Configuration freemarkerConfig = new Configuration();
			ClassPathResource cpr = new ClassPathResource("script_template");
			freemarkerConfig.setDirectoryForTemplateLoading(cpr.getFile());
			freemarkerConfig.setObjectWrapper(new DefaultObjectWrapper());
			Template template = freemarkerConfig.getTemplate("basic_template_" + getExtension() + ".ftl");
			StringWriter writer = new StringWriter();
			template.process(map, writer);
			return writer.toString();
		} catch (Exception e) {
			LOGGER.error("Error while fetching template for quick start", e);
		}
		return "";
	}

	public String getTitle() {
		return title;
	}

	public String getKey() {
		return key;
	}

	FileEntryRepository getFileEntryRepository() {
		return fileEntryRepository;
	}

	void setFileEntryRepository(FileEntryRepository fileEntryRepository) {
		this.fileEntryRepository = fileEntryRepository;
	}
}
