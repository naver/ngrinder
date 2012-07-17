package org.ngrinder.util;

@SuppressWarnings("unchecked")
public class JSONUtil {

	private static String successJson;
	private static String errorJson;

	public static String returnError(String message) {
		return "temp";
	}

	public static String returnSuccess() {
		return successJson;
	}

	public static String returnError() {
		return errorJson;
	}

}
