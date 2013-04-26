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

public abstract class ScriptHandler {
	protected static final Logger LOGGER = LoggerFactory.getLogger(JythonScriptHandler.class);
	
	/** This is package protected scope due to unit test */
	@Autowired
	FileEntryRepository fileEntryRepository;

	public abstract String getCodemirrorKey();

	public boolean canHandle(FileEntry fileEntry) {
		return FilenameUtils.isExtension(fileEntry.getPath(), getExtension());
	}

	public abstract String getExtension();

	protected abstract Integer order();

	public void prepareDist(String identifier, User user, FileEntry script, File distDir, PropertiesWrapper properties) {
		List<FileEntry> fileEntries = getLibAndResourceEntries(user, script, -1);
		fileEntries.add(script);
		String basePath = getBasePath(script);
		prepareDefaultFile(distDir, properties);
		// Distribute each files in that folder.
		for (FileEntry each : fileEntries) {
			// Directory is not subject to be distributed.
			if (each.getFileType() == FileType.DIR) {
				continue;
			}
			File toDir = new File(distDir, calcDistSubPath(basePath, each));
			LOGGER.info("{} is being written in {} for test {}", new Object[] { each.getPath(), toDir, identifier });
			fileEntryRepository.writeContentTo(user, each.getPath(), toDir);
		}
		prepareDistMore(identifier, user, script, distDir, properties);
	}

	protected void prepareDistMore(String identifier, User user, FileEntry script, File distDir,
					PropertiesWrapper properties) {

	}

	private String calcDistSubPath(String basePath, FileEntry each) {
		String path = FilenameUtils.getPath(each.getPath());
		path = path.substring(basePath.length());
		return path;
	}

	public List<FileEntry> getLibAndResourceEntries(User user, FileEntry scriptEntry, long revision) {
		String path = FilenameUtils.getPath(scriptEntry.getPath());
		List<FileEntry> fileList = newArrayList();
		for (FileEntry eachFileEntry : fileEntryRepository.findAll(user, path + "lib/", revision)) {
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
		for (FileEntry eachFileEntry : fileEntryRepository.findAll(user, path + "resources/", revision)) {
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

	public abstract String checkSyntaxErrors(String content);

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
}
