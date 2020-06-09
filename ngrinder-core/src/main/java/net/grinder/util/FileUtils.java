package net.grinder.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import static java.nio.file.Files.walk;
import static java.nio.file.Paths.get;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;
import static org.apache.commons.codec.digest.DigestUtils.md5Hex;
import static org.ngrinder.common.util.StreamUtils.exceptionWrapper;

/**
 * Convenient File utilities.
 *
 * @since 3.5.1
 */
public class FileUtils {
	/**
	 * @param directoryPath Directory path for searching files.
	 * */
	public static List<File> getAllFilesInDirectory(String directoryPath) throws IOException {
		try (Stream<Path> walk = walk(get(directoryPath))) {
			return walk
				.filter(Files::isRegularFile)
				.map(Path::toFile)
				.collect(toList());
		}
	}

	public static List<File> getAllFilesInDirectory(File directory) throws IOException {
		return getAllFilesInDirectory(directory.getPath());
	}

	public static Set<String> getAllFilesMd5ChecksumInDirectory(File directory) throws IOException {
		return getMd5(getAllFilesInDirectory(directory));
	}

	public static Set<String> getMd5(List<File> files) {
		return files
			.stream()
			.map(exceptionWrapper(FileUtils::getMd5))
			.collect(toSet());
	}

	public static String getMd5(File file) throws IOException {
		try (FileInputStream fileInputStream = new FileInputStream(file)) {
			return md5Hex(fileInputStream);
		}
	}
}
