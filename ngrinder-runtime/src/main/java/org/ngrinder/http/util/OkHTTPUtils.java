package org.ngrinder.http.util;

import okhttp3.Headers;
import okhttp3.MediaType;
import okhttp3.RequestBody;

import static org.ngrinder.http.HTTPRequest.DEFAULT_MEDIA_TYPE;

public class OkHTTPUtils {

	private OkHTTPUtils() {}

	public static RequestBody createRequestBody(byte[] data, Headers headers) {
		String contentType = headers.get("Content-Type");
		MediaType mediaType = contentType == null ? DEFAULT_MEDIA_TYPE : MediaType.parse(contentType);
		return RequestBody.create(data, mediaType);
	}
}
