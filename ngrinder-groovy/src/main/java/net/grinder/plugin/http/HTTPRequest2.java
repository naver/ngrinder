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
package net.grinder.plugin.http;

import okhttp3.*;
import org.conscrypt.Conscrypt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.security.Security;

public class HTTPRequest2 {
	static {
		// To support ALPN
		Security.insertProviderAt(Conscrypt.newProvider(), 1);
	}

	private static final Logger LOGGER = LoggerFactory.getLogger(HTTPRequest2.class);

	private final OkHttpClient client;

	public HTTPRequest2() {
		client = new OkHttpClient()
			.newBuilder()
			.hostnameVerifier((s, sslSession) -> true)
			.build();
	}

	public Response GET(String url) {
		return GET(url, Headers.of());
	}

	public Response GET(String url, Headers headers) {
		Request request = new Request.Builder()
			.url(url)
			.headers(headers)
			.build();

		return doRequest(request);
	}

	public Response POST(String url) {
		return POST(url, "".getBytes());
	}

	public Response POST(String url, byte[] data) {
		return POST(url, data, Headers.of());
	}

	public Response POST(String url, byte[] data, Headers headers) {
		RequestBody body = RequestBody.create(data);
		Request request = new Request.Builder()
			.url(url)
			.post(body)
			.headers(headers)
			.build();

		return doRequest(request);
	}

	private Response doRequest(Request request) {
		Response response = null;
		try {
			response = client.newCall(request).execute();
			aggregate(response);
		} catch (IOException e) {
			LOGGER.error("Fail to get response {}", request, e);
		}

		return response;
	}

	private void aggregate(final Response response) {
		// TODO: Implement aggregate statistics
	}
}
