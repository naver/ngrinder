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
package org.ngrinder.common.util;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;

/**
 * Utility class for path manipulation.
 *
 * @author JunHo Yoon
 * @since 3.0
 */
public abstract class PathUtils {

	public static final int MAX_PATH_LENGTH = 40;

	/**
	 * Remove prepended / from the given path.
	 *
	 * @param path path containing /
	 * @return / removed path
	 */
	public static String removePrependedSlash(String path) {
		if (path.startsWith("/")) {
			return path.substring(1);
		}
		return path;
	}

	/**
	 * Join two path without appending '/'.
	 *
	 * @param path1 path1
	 * @param path2 path2
	 * @return joined path
	 */
	public static String join(String path1, String path2) {
		path1 = trimPathSeparatorBothSides(path1);
		path2 = trimPathSeparatorBothSides(path2);
		return FilenameUtils.normalizeNoEndSeparator(path1 + "/" + path2, true);
	}

	/**
	 * Trim both leading and tailing of the path separator '/' from the given path.
	 *
	 * @param path the given path
	 * @return a path which is removed the path separator both sides
	 */
	public static String trimPathSeparatorBothSides(String path) {
		int len = path.length();
		int st = 0;
		int off = 0;
		char[] val = path.toCharArray();
		while ((st < len) && (val[off + st] == '/')) {
			st++;
		}
		while ((st < len) && (val[off + len - 1] == '/')) {
			len--;
		}
		return ((st > 0) || (len < path.length())) ? path.substring(st, len) : path;
	}

	/**
	 * Remove prepended / on the given path.
	 *
	 * @param path path containing /
	 * @return / removed path
	 */
	public static String removeDuplicatedPrependedSlash(String path) {
		if (path.startsWith("//")) {
			return path.substring(1);
		}
		return path;
	}

	/**
	 * Get the shorten displayable path from the given path.
	 *
	 * @param path path
	 * @return shortPath
	 */
	public static String getShortPath(String path) {
		if (path.length() >= MAX_PATH_LENGTH && StringUtils.contains(path, "/")) {
			String start = path.substring(0, path.indexOf("/") + 1);
			String end = path.substring(path.lastIndexOf("/"), path.length());
			return start + "..." + end;
		} else {
			return path;
		}
	}
}
