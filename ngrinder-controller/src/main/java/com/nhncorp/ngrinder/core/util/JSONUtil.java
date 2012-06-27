package com.nhncorp.ngrinder.core.util;

import org.json.simple.JSONObject;

import com.nhncorp.ngrinder.core.NGrinderConstants;


@SuppressWarnings("unchecked")
public class JSONUtil {
	
	private static String successJson;
	private static String errorJson;
	
	static {
		JSONObject rtnJson = new JSONObject();
		rtnJson.put(NGrinderConstants.JSON_SUCCESS, true);
		successJson = rtnJson.toJSONString();
		rtnJson.put(NGrinderConstants.JSON_SUCCESS, false);
		errorJson = rtnJson.toJSONString();
	}
	
	public static String returnSuccess(String message) {
		JSONObject rtnJson = new JSONObject();
		rtnJson.put(NGrinderConstants.JSON_SUCCESS, true);
		rtnJson.put(NGrinderConstants.JSON_MESSAGE, message);
		return rtnJson.toJSONString();
	}
	
	public static String returnError(String message) {
		JSONObject rtnJson = new JSONObject();
		rtnJson.put(NGrinderConstants.JSON_SUCCESS, false);
		rtnJson.put(NGrinderConstants.JSON_MESSAGE, message);
		return rtnJson.toJSONString();
	}

	public static String returnSuccess() {
		return successJson;
	}

	public static String returnError() {
		return errorJson;
	}

}
