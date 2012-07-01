package org.ngrinder.script.util;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.ngrinder.common.NGrinderConstants;
import org.ngrinder.script.model.Library;
import org.ngrinder.user.util.UserUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Library util
 * 
 * @author Tobi
 * @since
 * @date 2012-6-28
 */
public class LibraryUtil {
	private static final Logger LOG = LoggerFactory.getLogger(LibraryUtil.class);

	private LibraryUtil() {
	}

	public static void createLibraryPath() {
		StringBuilder sb = new StringBuilder();
		sb.append(NGrinderConstants.PATH_PROJECT);
		sb.append(File.separator);
		sb.append(NGrinderConstants.PREFIX_USER);
		sb.append(UserUtil.getCurrentUser().getName());
		sb.append(File.separator);
		sb.append(NGrinderConstants.PATH_LIB);
		sb.append(File.separator);

		String libPath = sb.toString();
		File dir = new File(libPath);
		if (!dir.exists()) {
			dir.mkdirs();
		}
	}

	public static String getLibraryPath() {
		StringBuilder sb = new StringBuilder();
		sb.append(NGrinderConstants.PATH_PROJECT);
		sb.append(File.separator);
		sb.append(NGrinderConstants.PREFIX_USER);
		sb.append(UserUtil.getCurrentUser().getName());
		sb.append(File.separator);
		sb.append(NGrinderConstants.PATH_LIB);
		sb.append(File.separator);

		return sb.toString();
	}

	public static String getLibFilePath(String libName) {
		return getLibraryPath() + libName;
	}

	public static List<Library> getLibrary() {
		List<Library> librarys = new ArrayList<Library>();

		String libPath = getLibraryPath();
		File libDir = new File(libPath);
		File[] libFiles = libDir.listFiles();
		if (null != libFiles) {
			for (File libFile : libFiles) {
				Library library = new Library();
				library.setFileName(libFile.getName());
				library.setFileSize(libFile.length());
				library.setFileType(libFile.getName().substring(libFile.getName().lastIndexOf('.') + 1));
				librarys.add(library);
			}
		}
		return librarys;
	}

	public static void saveLibraryFile(Library library) {
		String libPath = getLibraryPath();
		String libFilePath = libPath + library.getFileName();
		try {
			FileUtils.writeByteArrayToFile(new File(libFilePath), library.getContentBytes());
		} catch (IOException e) {
			LOG.error("Write script file failed.", e);
		}
	}

	public static void deleteLibraryFile(String libraryName) {
		String libPath = getLibraryPath();
		String libFilePath = libPath + libraryName;
		FileUtils.deleteQuietly(new File(libFilePath));
	}
}
