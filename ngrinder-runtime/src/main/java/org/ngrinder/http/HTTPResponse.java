/*
 * Copyright (c) 2012-present NAVER Corp.
 *
 * This file is part of The nGrinder software distribution. Refer to
 * the file LICENSE which is part of The nGrinder distribution for
 * licensing details. The nGrinder distribution is available on the
 * Internet at https://naver.github.io/ngrinder
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ngrinder.http;

import kotlin.text.Charsets;
import okhttp3.MediaType;
import okhttp3.Response;
import org.ngrinder.http.util.JsonUtils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.Map;

public class HTTPResponse extends ResponseProxy {
	private static final Charset DEFAULT_CHARSET = Charsets.UTF_8;

	private final Response response;
	private final int statusCode;

	private byte[] bytes;

	private HTTPResponse(Response response) {
		super(response);
		this.response = response;
		this.statusCode = response.code();
	}

	public Response response() {
		return response;
	}

	public InputStream getInputStream() {
		return body().byteStream();
	}

	public byte[] bytes() {
		if (bytes != null) {
			return bytes;
		}

		if (body() == null) {
			return bytes = new byte[0];
		}

		try {
			return bytes = body().bytes();
		} catch (IOException e) {
			throw new RuntimeException(e);
		} finally {
			body().close();
		}
	}

	public Map<?, ?> json() {
		return JsonUtils.deserialize(bytes());
	}

	public String string() {
		Charset charset = getCharset();
		return new String(bytes(), charset);
	}

	private Charset getCharset() {
		MediaType contentType = body().contentType();
		if (contentType == null) {
			return DEFAULT_CHARSET;
		}
		return contentType.charset(DEFAULT_CHARSET);
	}

	public int getStatusCode() {
		return statusCode;
	}

	public static HTTPResponse of(Response response) {
		if (response == null) {
			return null;
		}
		return new HTTPResponse(response);
	}
}
