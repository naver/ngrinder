package net.grinder.util;

import org.ngrinder.common.exception.NGrinderRuntimeException;

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
import static org.ngrinder.common.util.PathUtils.getSubPath;
import static org.ngrinder.common.util.StreamUtils.exceptionWrapper;

/**
 * Convenient File utilities.
 *
 * @since 3.5.0
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

	/**
	 * Make file digest to {relative path from base directory}:{md5 checksum of file} format.
	 *
	 * @param baseDir  Base directory for calculate relative path.
	 * @param file     Target file.
	 *
	 * */
	public static String getFileDigest(File baseDir, File file) {
		try {
			return getSubPath(baseDir.getPath(), file.getPath()) + ":" + getMd5(file);
		} catch (IOException e) {
			throw new NGrinderRuntimeException(e);
		}
	}

	public static Set<String> getFilesDigest(File baseDir, List<File> files) {
		return files
			.stream()
			.map(file -> getFileDigest(baseDir, file))
			.collect(toSet());
	}
}
