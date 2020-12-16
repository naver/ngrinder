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

import okhttp3.*;

class ResponseProxy {
	private final Response response;

	ResponseProxy(Response response) {
		this.response = response;
	}

	public Headers headers() {
		return response.headers();
	}

	public String header(String name) {
		return response.header(name);
	}

	public String header(String name, String defaultValue) {
		return response.header(name, defaultValue);
	}

	public CacheControl cacheControl() {
		return response.cacheControl();
	}

	public int code() {
		return response.code();
	}

	public Protocol protocol() {
		return response.protocol();
	}

	public Request request() {
		return response.request();
	}

	public boolean isRedirect() {
		return response.isRedirect();
	}

	public boolean isSuccessful() {
		return response.isSuccessful();
	}

	public ResponseBody body() {
		return response.body();
	}

	public void close() {
		response.close();
	}
}
