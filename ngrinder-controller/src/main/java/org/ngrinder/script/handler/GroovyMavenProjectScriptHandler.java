package org.ngrinder.script.handler;

import static org.ngrinder.common.util.CollectionUtils.newArrayList;

import java.io.File;
import java.util.List;

import org.apache.commons.io.FilenameUtils;
import org.ngrinder.common.util.PropertiesWrapper;
import org.ngrinder.model.User;
import org.ngrinder.script.model.FileEntry;
import org.ngrinder.script.model.FileType;
import org.springframework.stereotype.Component;

@Component
public class GroovyMavenProjectScriptHandler extends GroovyScriptHandler {

	@Override
	public boolean canHandle(FileEntry fileEntry) {
		if (fileEntry.getCreatedUser() == null) {
			return false;
		}
		String path = fileEntry.getPath();
		if (!path.contains("/src/main/java/") || !FilenameUtils.isExtension(path, "groovy")) {
			return false;
		}

		return fileEntryRepository.hasFileEntry(fileEntry.getCreatedUser(),
						path.substring(0, path.lastIndexOf("/src/main/java/")) + "/pom.xml");
	}

	@Override
	protected Integer order() {
		return 200;
	}

	@Override
	public List<FileEntry> getLibAndResourceEntries(User user, FileEntry scriptEntry, long revision) {
		List<FileEntry> fileList = newArrayList();
		for (FileEntry eachFileEntry : fileEntryRepository.findAll(user, getBasePath(scriptEntry)
						+ "/src/main/resources/", revision)) {
			FileType fileType = eachFileEntry.getFileType();
			if (fileType.isResourceDistributable()) {
				fileList.add(eachFileEntry);
			}
		}
		return fileList;
	}

	@Override
	protected void prepareDistMore(String identifier, User user, FileEntry script, File distDir,
					PropertiesWrapper properties) {
		super.prepareDistMore(identifier, user, script, distDir, properties);
	}

	@Override
	protected String getBasePath(FileEntry script) {
		String path = script.getPath();
		return path.substring(0, path.lastIndexOf("/src/main/java/"));
	}

}
