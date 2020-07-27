package org.ngrinder.script.repository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.ngrinder.script.model.FileEntry;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.List;

import static java.io.File.separator;
import static java.nio.file.Files.walk;
import static java.nio.file.Paths.get;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.io.FileUtils.copyFileToDirectory;
import static org.ngrinder.common.util.CollectionUtils.buildMap;
import static org.ngrinder.common.util.CollectionUtils.newArrayList;
import static org.ngrinder.common.util.NoOp.noOp;
import static org.ngrinder.script.model.FileType.getFileTypeByName;

/**
 * @since 3.5.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class GitHubFileEntryRepository {

	/**
	 * Copy file to the given path.
	 *
	 * This method only work for the file not dir.
	 *
	 * @param path      path of resource.
	 * @param toPathDir file dir path to write.
	 */
	public void writeContentTo(String path, File toPathDir) throws IOException {
		File script = new File(path);
		if (script.exists() && script.isDirectory()) {
			return;
		}
		copyFileToDirectory(script, toPathDir);
	}

	/**
	 * find lib and resource files.
	 *
	 * @param rootPath rootPath for finding.
	 */
	public List<FileEntry> findAll(String rootPath) throws IOException {
		List<FileEntry> fileEntries = newArrayList();
		try {
			fileEntries = walk(get(rootPath))
				.filter(Files::isRegularFile)
				.map(this::convertPathToFileEntry)
				.collect(toList());
		} catch (NoSuchFileException e) {
			noOp();
		}
		return fileEntries;
	}

	public FileEntry findOne(String path) throws NoSuchFileException {
		File file = new File(path);
		if (!file.exists()) {
			throw new NoSuchFileException(path + " not exist.");
		}
		FileEntry fileEntry = new FileEntry();
		fileEntry.setPath(path);
		fileEntry.setProperties(buildMap("scm", "github"));
		return fileEntry;
	}


	private FileEntry convertPathToFileEntry(Path path) {
		FileEntry fileEntry = new FileEntry();
		fileEntry.setFileType(getFileTypeByName(path.toString()));
		fileEntry.setPath(path.toString().replace(separator, "/"));
		fileEntry.setProperties(buildMap("scm", "github"));
		return fileEntry;
	}
}
