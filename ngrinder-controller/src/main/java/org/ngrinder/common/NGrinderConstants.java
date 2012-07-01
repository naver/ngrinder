package org.ngrinder.common;

public final class NGrinderConstants {

	private NGrinderConstants() {
		super();
	}

	public static final String START = "start";
	public static final String STOP = "stop";
	public static final String END = "end";

	/**
	 * .ngrinder<br>
	 * |project<br>
	 * |--|u_username1<br>
	 * |--|--|s_scriptid1<br>
	 * |--|--|--|script.properties<br>
	 * |--|--|--|histories<br>
	 * |--|--|--|reports<br>
	 * |--|--|--|logs<br>
	 * |--|--|s_scriptid2<br>
	 * |--|--|libs<br>
	 * |--|u_username2<br>
	 * 
	 */

	// project
	public static final String PATH_PROJECT = getWebRoot() + "project";

	public static final String PREFIX_USER = "u_";

	public static final String PATH_LIB = "libs";

	// script
	// public static final String PATH_SCRIPT = getWebRoot() + "scripts";

	public static final String PREFIX_SCRIPT = "s_";

	public static final String SCRIPT_PROPERTIES = "script.properties";

	public static final String PATH_LOG = "logs";

	public static final String PATH_REPORT = "reports";

	public static final String PATH_HISTORY = "histories";

	public static final String CACHE_NAME = "cache";

	public static final String COMMA = ",";

	public static final String ENCODE_UTF8 = "UTF-8";

	public static final String PY_EXTENTION = ".py";
	public static final String JS_EXTENTION = ".js";

	public static final String GRINDER_PROPERTIES = "grinder.properties";

	public static final String JSON_SUCCESS = "success";
	public static final String JSON_MESSAGE = "message";

	public static String getWebRoot() {
		String path = NGrinderConstants.class.getProtectionDomain().getCodeSource().getLocation().getPath();
		if (path.indexOf("WEB-INF") > 0) {
			path = path.substring(0, path.indexOf("WEB-INF/classes"));
		} else if (path.indexOf("classes") > 0) {
			path = path.substring(0, path.indexOf("classes"));
		}
		return path;
	}
}
