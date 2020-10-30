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

import okhttp3.Response;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

public class HTTPResponse extends ResponseProxy {
	private final Response response;

	private String body = null;

	private HTTPResponse(Response response) {
		super(response);
		this.response = response;
	}

	public Response response() {
		return response;
	}

	public int statusCode() {
		return code();
	}

	public InputStream byteStream() {
		return response.body().byteStream();
	}

	public Map<?, ?> json() {
		return JsonUtils.deserialize(string());
	}

	public String string() {
		if (body != null || response.body() == null) {
			return body;
		}

		try {
			return body = response.body().string();
		} catch (IOException e) {
			throw new RuntimeException(e);
		} finally {
			response.body().close();
		}
	}

	public static HTTPResponse of(Response response) {
		if (response == null) {
			return null;
		}
		return new HTTPResponse(response);
	}
}
