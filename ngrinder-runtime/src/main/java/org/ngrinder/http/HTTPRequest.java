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

import okhttp3.Headers;
import okhttp3.Protocol;
import okhttp3.RequestBody;

import java.util.List;
import java.util.Map;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;

/**
 * Wrapper class for InternalHTTPRequest.
 * Make byte code instrumentation only once for outer method call
 */
public class HTTPRequest {
	private static final List<Protocol> DEFAULT_PROTOCOLS = asList(Protocol.HTTP_2, Protocol.HTTP_1_1);

	private final InternalHTTPRequest internalHTTPRequest;

	private HTTPRequest(List<Protocol> protocols) {
		internalHTTPRequest = new InternalHTTPRequest(protocols);
	}

	public HTTPResponse GET(String url) {
		return internalHTTPRequest.GET(url);
	}

	public HTTPResponse GET(String url, Headers headers) {
		return internalHTTPRequest.GET(url, headers);
	}

	public HTTPResponse GET(String url, Map<?, ?> map, Headers headers) {
		return internalHTTPRequest.GET(url, map, headers);
	}

	public HTTPResponse POST(String url) {
		return internalHTTPRequest.POST(url);
	}

	public HTTPResponse POST(String url, byte[] data) {
		return internalHTTPRequest.POST(url, data);
	}

	public HTTPResponse POST(String url, byte[] data, Headers headers) {
		return internalHTTPRequest.POST(url, data, headers);
	}

	public HTTPResponse POST(String url, Map<?, ?> map) {
		return internalHTTPRequest.POST(url, map);
	}

	public HTTPResponse POST(String url, Map<?, ?> map, Headers headers) {
		return internalHTTPRequest.POST(url, map, headers);
	}

	public HTTPResponse POST(String url, RequestBody body) {
		return internalHTTPRequest.POST(url, body);
	}

	public HTTPResponse POST(String url, RequestBody body, Headers headers) {
		return internalHTTPRequest.POST(url, body, headers);
	}

	public static HTTPRequest create() {
		return isOverJava8() ? new HTTPRequest(DEFAULT_PROTOCOLS) : createHTTPRequest();
	}

	public static HTTPRequest createHTTPRequest() {
		return new HTTPRequest(singletonList(Protocol.HTTP_1_1));
	}

	private static boolean isOverJava8() {
		String jvmVersion = getJvmVersion();
		return jvmVersion != null && !jvmVersion.startsWith("1.");
	}

	private static String getJvmVersion() {
		return System.getProperty("java.specification.version");
	}
}
