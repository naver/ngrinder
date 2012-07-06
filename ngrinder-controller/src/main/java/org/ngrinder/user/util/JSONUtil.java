package org.ngrinder.user.util;

import java.util.List;

import com.google.gson.Gson;

public class JSONUtil {

	private static Gson gson;

	static {
		gson = new Gson();
	}

	public static String toJson(List<?> list) {
		return gson.toJson(list);
	}

}
