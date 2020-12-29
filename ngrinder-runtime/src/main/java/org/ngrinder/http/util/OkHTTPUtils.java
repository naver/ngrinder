package org.ngrinder.http.util;

import okhttp3.FormBody;
import okhttp3.Headers;
import okhttp3.MediaType;
import okhttp3.RequestBody;

import java.util.Map;

public class OkHTTPUtils {

	private static final MediaType DEFAULT_MEDIA_TYPE = MediaType.get("application/json; charset=utf-8");

	private OkHTTPUtils() {
	}

	public static RequestBody createRequestBody(Map<?, ?> map, Headers headers) {
		String contentType = headers.get("Content-Type");
		MediaType mediaType = contentType == null ? DEFAULT_MEDIA_TYPE : MediaType.get(contentType);

		RequestBody body;
		if (mediaType.type().equalsIgnoreCase("application") &&
			mediaType.subtype().equalsIgnoreCase("x-www-form-urlencoded")) {
			FormBody.Builder builder = new FormBody.Builder();
			map.forEach((key, value) -> builder.add(key.toString(), value.toString()));
			body = builder.build();
		} else {
			body = RequestBody.create(JsonUtils.serialize(map), mediaType);
		}

		return body;
	}
}
