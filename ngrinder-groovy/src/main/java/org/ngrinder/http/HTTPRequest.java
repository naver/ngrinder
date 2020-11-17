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

import net.grinder.common.GrinderException;
import net.grinder.plugin.http.HTTPPlugin;
import net.grinder.plugin.http.HTTPPluginThreadState;
import net.grinder.plugininterface.PluginThreadContext;
import net.grinder.script.Statistics;
import net.grinder.statistics.StatisticsIndexMap;
import okhttp3.*;
import okio.BufferedSource;
import org.conscrypt.Conscrypt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.security.Security;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;

public class HTTPRequest {
	static {
		// To support ALPN
		Security.insertProviderAt(Conscrypt.newProvider(), 1);

		// Ensure plugin is loaded
		HTTPPlugin.getPlugin();
	}

	private static final Logger LOGGER = LoggerFactory.getLogger(HTTPRequest.class);

	private static final List<Protocol> DEFAULT_PROTOCOLS = asList(Protocol.HTTP_2, Protocol.HTTP_1_1);
	private static final MediaType DEFAULT_MEDIA_TYPE = MediaType.get("application/json; charset=utf-8");

	private static final CookieJar threadContextCookieJar = new ThreadContextCookieJar();

	private final OkHttpClient client;

	private HTTPRequest(List<Protocol> protocols) {
		client = new OkHttpClient()
			.newBuilder()
			.protocols(protocols)
			.cookieJar(threadContextCookieJar)
			.eventListener(new ConnectionTimeAggregateListener())
			.hostnameVerifier((s, sslSession) -> true)
			.build();
	}

	public HTTPResponse GET(String url) {
		return GET(url, Headers.of());
	}

	public HTTPResponse GET(String url, Headers headers) {
		Request request = new Request.Builder()
			.url(url)
			.headers(headers)
			.build();

		return doRequest(request);
	}

	public HTTPResponse POST(String url) {
		return POST(url, "".getBytes());
	}

	public HTTPResponse POST(String url, byte[] data) {
		return POST(url, data, Headers.of());
	}

	public HTTPResponse POST(String url, byte[] data, Headers headers) {
		String contentType = headers.get("Content-Type");
		MediaType mediaType = contentType == null ? DEFAULT_MEDIA_TYPE : MediaType.parse(contentType);
		RequestBody body = RequestBody.create(data, mediaType);
		return POST(url, body, headers);
	}

	public HTTPResponse POST(String url, Map<?, ?> map) {
		return POST(url, map, Headers.of());
	}

	public HTTPResponse POST(String url, Map<?, ?> map, Headers headers) {
		RequestBody body = RequestBody.create(JsonUtils.serialize(map), DEFAULT_MEDIA_TYPE);
		return POST(url, body, headers);
	}

	public HTTPResponse POST(String url, RequestBody body) {
		return POST(url, body, Headers.of());
	}

	public HTTPResponse POST(String url, RequestBody body, Headers headers) {
		Request request = new Request.Builder()
			.url(url)
			.post(body)
			.headers(headers)
			.build();

		return doRequest(request);
	}

	private HTTPResponse doRequest(Request request) {
		Response response = null;
		try {
			response = client.newCall(request).execute();

			getThreadContext().pauseClock();

			aggregate(response);
			summarize(response);

			getThreadContext().resumeClock();
		} catch (IOException e) {
			LOGGER.error("Fail to get response {}", request, e);
		}

		return HTTPResponse.of(response);
	}

	private PluginThreadContext getThreadContext() {
		try {
			HTTPPluginThreadState threadState = (HTTPPluginThreadState) HTTPPlugin.getPlugin()
				.getPluginProcessContext()
				.getPluginThreadListener();
			return threadState.getThreadContext();
		} catch (GrinderException e) {
			throw new RuntimeException(e);
		}
	}

	private void summarize(Response response) {
		Logger logger = HTTPPlugin.getPlugin()
			.getPluginProcessContext()
			.getScriptContext()
			.getLogger();

		Function<Response, String> generateMessage = new Function<Response, String>() {
			@Override
			public String apply(Response response) {
				return response.request().url() + " -> " + response.code() + " " + response.message() + ", " + getBodyLength(response.body()) + " bytes";
			}
		};

		String message = "";
		Response res = response.priorResponse();
		if (res != null) {
			summarize(res);
			message += " L ";
		}

		logger.info(message + generateMessage.apply(response));
	}

	private void aggregate(final Response response) {
		Statistics statistics = HTTPPlugin.getPlugin()
			.getPluginProcessContext()
			.getScriptContext()
			.getStatistics();

		if (!statistics.isTestInProgress()) {
			return;
		}

		try {
			Statistics.StatisticsForTest statisticsForTest = statistics.getForCurrentTest();

			statisticsForTest.setLong(
				StatisticsIndexMap.HTTP_PLUGIN_RESPONSE_STATUS_KEY, response.code());

			if (response.code() >= HttpURLConnection.HTTP_BAD_REQUEST) {
				statisticsForTest.addLong(
					StatisticsIndexMap.HTTP_PLUGIN_RESPONSE_ERRORS_KEY, 1);
			}

			ResponseBody body = response.body();
			statisticsForTest.addLong(
				StatisticsIndexMap.HTTP_PLUGIN_RESPONSE_LENGTH_KEY, getBodyLength(body));
		} catch (Exception e) {
			LOGGER.error("Fail to aggregate HTTP statistics", e);
		}
	}

	private long getBodyLength(ResponseBody body) {
		if (body != null) {
			try {
				BufferedSource source = body.source();
				source.request(Long.MAX_VALUE);
				return source.getBuffer().snapshot().size();
			} catch (IOException e) {
				LOGGER.error("", e);
			}
		}

		return 0L;
	}

	public static HTTPRequest create() {
		return new HTTPRequest(DEFAULT_PROTOCOLS);
	}

	public static HTTPRequest createHTTPRequest() {
		return new HTTPRequest(singletonList(Protocol.HTTP_1_1));
	}

}

