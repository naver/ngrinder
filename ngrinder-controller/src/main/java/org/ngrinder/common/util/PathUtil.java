package org.ngrinder.common.util;

public class PathUtil {
	public static String removePrependedSlash(String path) {
		if (path.startsWith("/")) {
			return path.substring(1);
		}
		return path;
	}
}
