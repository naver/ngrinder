package org.ngrinder.util;

import org.json.simple.JSONObject;

@SuppressWarnings("unchecked")
public class JSONUtil {

	private static String successJson;
	private static String errorJson;

	static {
		JSONObject rtnJson = new JSONObject();
		rtnJson.put("success", true);
		successJson = rtnJson.toJSONString();
		rtnJson.put("success", false);
		errorJson = rtnJson.toJSONString();
	}

	public static String returnError(String message) {
		JSONObject rtnJson = new JSONObject();
		rtnJson.put("success", false);
		rtnJson.put("message", message);
		return rtnJson.toJSONString();
	}

	public static String returnSuccess() {
		return successJson;
	}

	public static String returnError() {
		return errorJson;
	}

}
