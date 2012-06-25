package com.nhncorp.ngrinder.script.util;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nhncorp.ngrinder.core.NGrinderConstants;
import com.nhncorp.ngrinder.script.model.Library;
import com.nhncorp.ngrinder.script.model.Script;

/**
 * Script util
 * 
 * @author Liu Zhifei
 * @date 2012-6-13
 */
public final class ScriptUtil {

	private static final Logger LOG = LoggerFactory.getLogger(ScriptUtil.class);

	private ScriptUtil() {
	}

	public static void createScriptPath(long id) {
		StringBuilder sb = new StringBuilder();
		sb.append(NGrinderConstants.PATH_SCRIPT);
		sb.append(File.separator);
		sb.append(NGrinderConstants.PREFIX_SCRIPT);
		sb.append(id);
		sb.append(File.separator);

		String scriptPath = sb.toString();
		File dir = new File(scriptPath);
		if (!dir.exists()) {
			dir.mkdirs();
		}
		dir = new File(scriptPath + NGrinderConstants.PATH_LIB);
		if (!dir.exists()) {
			dir.mkdir();
		}
		dir = new File(scriptPath + NGrinderConstants.PATH_CACHE);
		if (!dir.exists()) {
			dir.mkdir();
		}
		dir = new File(scriptPath + NGrinderConstants.PATH_LOG);
		if (!dir.exists()) {
			dir.mkdir();
		}
		dir = new File(scriptPath + NGrinderConstants.PATH_REPORT);
		if (!dir.exists()) {
			dir.mkdir();
		}
		dir = new File(scriptPath + NGrinderConstants.PATH_HISTORY);
		if (!dir.exists()) {
			dir.mkdir();
		}
		dir = new File(scriptPath + NGrinderConstants.PATH_IMAGES);
		if (!dir.exists()) {
			dir.mkdir();
		}
	}

	/**
	 * get the script path
	 * 
	 * @param scriptDirectory
	 * @param id
	 * @return the script file path
	 */
	public static String getScriptPath(long id) {
		StringBuilder sb = new StringBuilder();
		sb.append(NGrinderConstants.PATH_SCRIPT);
		sb.append(File.separator);
		sb.append(NGrinderConstants.PREFIX_SCRIPT);
		sb.append(id);
		sb.append(File.separator);

		return sb.toString();
	}

	public static String getContent(Script script) {
		String scriptPath = ScriptUtil.getScriptPath(script.getId());
		String content = null;
		if (null != script) {
			String scriptFilePath = scriptPath + script.getFileName();
			try {
				content = FileUtils.readFileToString(new File(scriptFilePath), NGrinderConstants.ENCODE_UTF8);
			} catch (IOException e) {
				LOG.error("Failed to load script file content, path: " + scriptFilePath + ", error: " + e.getMessage(),
						e);
			}
			script.setContent(content);
		}
		return content;
	}

	public static String getHistoryContent(Script script, String historyName) {
		String scriptPath = ScriptUtil.getScriptPath(script.getId());
		String historyContent = null;
		if (null != script) {
			StringBuilder scriptHistoryFilePath = new StringBuilder(scriptPath);
			scriptHistoryFilePath.append(NGrinderConstants.PATH_HISTORY).append(File.separator);
			scriptHistoryFilePath.append(historyName);
			try {
				historyContent = FileUtils.readFileToString(new File(scriptHistoryFilePath.toString()),
						NGrinderConstants.ENCODE_UTF8);
			} catch (IOException e) {
				LOG.error("Failed to load script file history content, path: " + scriptHistoryFilePath + ", error: "
						+ e.getMessage(), e);
			}
			script.setHistoryContent(historyContent);
		}
		return historyContent;
	}

	public static void saveScriptFile(Script script) {
		String scriptPath = ScriptUtil.getScriptPath(script.getId());
		String scriptFilePath = scriptPath + script.getFileName();
		try {
			if (null != script.getContent()) {
				FileUtils.writeStringToFile(new File(scriptFilePath), script.getContent(),
						NGrinderConstants.ENCODE_UTF8);
			} else {
				FileUtils.writeByteArrayToFile(new File(scriptFilePath), script.getContentBytes());
			}
		} catch (IOException e) {
			LOG.error("Write script file failed.", e);
		}
	}

	public static void saveScriptHistoryFile(Script script) {
		String scriptPath = ScriptUtil.getScriptPath(script.getId());
		StringBuilder scriptHistoryFilePath = new StringBuilder(scriptPath);
		scriptHistoryFilePath.append(NGrinderConstants.PATH_HISTORY).append(File.separator);
		scriptHistoryFilePath.append(new Date().getTime());
		try {
			FileUtils.writeStringToFile(new File(scriptHistoryFilePath.toString()), script.getContent(),
					NGrinderConstants.ENCODE_UTF8);
		} catch (IOException e) {
			LOG.error("Write script history file failed.", e);
		}
	}

	public static void deleteScript(long id) {
		String scriptPath = ScriptUtil.getScriptPath(id);
		try {
			FileUtils.deleteDirectory(new File(scriptPath));
		} catch (IOException e) {
			LOG.error("delete script directory failed.", e);
		}
	}

	public static List<String> getHistoryFileNames(long id) {
		List<String> historyFileNames = new ArrayList<String>();
		String scriptPath = ScriptUtil.getScriptPath(id);
		StringBuilder scriptHistoryFilePath = new StringBuilder(scriptPath);
		scriptHistoryFilePath.append(NGrinderConstants.PATH_HISTORY).append(File.separator);

		File historyDir = new File(scriptHistoryFilePath.toString());
		File[] historyFiles = historyDir.listFiles();
		for (File historyFile : historyFiles) {
			historyFileNames.add(historyFile.getName());
		}
		return historyFileNames;
	}

	public static void saveScriptCache(long id, String content) {
		String scriptPath = ScriptUtil.getScriptPath(id);
		String scriptCachePath = scriptPath + NGrinderConstants.CACHE_NAME;
		try {
			FileUtils.writeStringToFile(new File(scriptCachePath), content, NGrinderConstants.ENCODE_UTF8);
		} catch (IOException e) {
			LOG.error("Write script cache file failed.", e);
		}
	}

	public static String getScriptCache(long id) {
		String scriptPath = ScriptUtil.getScriptPath(id);
		String scriptCachePath = scriptPath + NGrinderConstants.CACHE_NAME;
		String content = null;
		try {
			content = FileUtils.readFileToString(new File(scriptCachePath));
		} catch (IOException e) {
			LOG.error("There is no cached script file. " + e.getMessage());
		}
		return content;
	}

	public static void deleteScriptCache(long id) {
		String scriptPath = ScriptUtil.getScriptPath(id);
		String scriptCachePath = scriptPath + NGrinderConstants.CACHE_NAME;
		FileUtils.deleteQuietly(new File(scriptCachePath));
	}

	public static List<Library> getScriptLib(long id) {
		List<Library> librarys = new ArrayList<Library>();

		String scriptPath = ScriptUtil.getScriptPath(id);
		String scriptLibPath = scriptPath + NGrinderConstants.PATH_LIB;

		File libDir = new File(scriptLibPath);
		File[] libFiles = libDir.listFiles();
		for (File libFile : libFiles) {
			Library library = new Library();
			library.setFileName(libFile.getName());
			library.setFileSize(libFile.length());
			library.setFileType(libFile.getName().substring(libFile.getName().lastIndexOf('.') + 1));
		}
		return librarys;
	}

	public static void saveScriptLibrary(long scriptId, Library library) {
		String scriptPath = ScriptUtil.getScriptPath(scriptId);
		String scriptLibPath = scriptPath + NGrinderConstants.PATH_LIB;
		try {
			FileUtils.writeByteArrayToFile(new File(scriptLibPath), library.getContentBytes());
		} catch (IOException e) {
			LOG.error("Write script file failed.", e);
		}
	}

	public static void deleteScriptLibrary(long scriptId, String libraryName) {
		String scriptPath = ScriptUtil.getScriptPath(scriptId);
		String scriptLibFilePath = scriptPath + NGrinderConstants.PATH_LIB + File.separator + libraryName;
		FileUtils.deleteQuietly(new File(scriptLibFilePath));
	}

}
