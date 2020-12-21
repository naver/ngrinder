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
import org.ngrinder.http.method.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.function.Supplier;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.joining;

@SuppressWarnings("unused")
public class HTTPRequest implements HTTPRequestGet, HTTPRequestPost, HTTPRequestPut,
	HTTPRequestDelete, HTTPRequestHead, HTTPRequestPatch {

	static {
		// Ensure plugin is loaded
		// noinspection ResultOfMethodCallIgnored
		HTTPPlugin.getPlugin();
	}
	public static MediaType DEFAULT_MEDIA_TYPE = MediaType.get("application/json; charset=utf-8");

	private static final Logger LOGGER = LoggerFactory.getLogger(HTTPRequest.class);
	private static final List<Protocol> DEFAULT_PROTOCOLS = asList(Protocol.HTTP_2, Protocol.HTTP_1_1);

	private static final CookieJar threadContextCookieJar = new ThreadContextCookieJar();

	HTTPRequest(List<Protocol> protocols) {
		Supplier<OkHttpClient> clientSupplier = () -> new OkHttpClient()
			.newBuilder()
			.protocols(protocols)
			.cookieJar(threadContextCookieJar)
			.eventListenerFactory(ConnectionTimeAggregateListener.FACTORY)
			.hostnameVerifier((s, sslSession) -> true)
			.callTimeout(HTTPRequestControl.getCallTimeout(), TimeUnit.MILLISECONDS)
			.connectTimeout(HTTPRequestControl.getConnectTimeout(), TimeUnit.MILLISECONDS)
			.readTimeout(HTTPRequestControl.getReadTimeout(), TimeUnit.MILLISECONDS)
			.writeTimeout(HTTPRequestControl.getWriteTimeout(), TimeUnit.MILLISECONDS)
			.build();

		ThreadContextHTTPClient.init(clientSupplier);
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

	@Override
	public HTTPResponse GET(String url, Map<?, ?> map, Headers headers) {
		Request request = getRequest(url + toUrlParam(map), "GET", null, headers);
		return doRequest(request);
	}

	@Override
	public HTTPResponse HEAD(String url, Map<?, ?> map, Headers headers) {
		Request request = getRequest(url + toUrlParam(map), "HEAD", null, headers);
		return doRequest(request);
	}

	@Override
	public HTTPResponse POST(String url, RequestBody body, Headers headers) {
		Request request = getRequest(url, "POST", body, headers);
		return doRequest(request);
	}

	@Override
	public HTTPResponse PUT(String url, RequestBody body, Headers headers) {
		Request request = getRequest(url, "PUT", body, headers);
		return doRequest(request);
	}

	@Override
	public HTTPResponse PATCH(String url, RequestBody body, Headers headers) {
		Request request = getRequest(url, "PATCH", body, headers);
		return doRequest(request);
	}

	@Override
	public HTTPResponse DELETE(String url, RequestBody body, Headers headers) {
		Request request = getRequest(url, "DELETE", body, headers);
		return doRequest(request);
	}


	private String toUrlParam(Map<?, ?> map) {
		return "?" + map.entrySet()
			.stream()
			.map(entry -> entry.getKey() + "=" + entry.getValue())
			.collect(joining("&"));
	}

	private Request getRequest(String url, String method, RequestBody body, Headers headers) {
		return new Request.Builder()
			.url(url)
			.method(method, body)
			.headers(headers)
			.build();
	}

	private HTTPResponse doRequest(Request request) {
		Response response;
		try {
			response = ThreadContextHTTPClient.get()
				.newCall(request)
				.execute();

			getThreadContext().pauseClock();

			aggregate(response);
			summarize(response);

			getThreadContext().resumeClock();
		} catch (IOException e) {
			throw new RuntimeException("Fail to get response " + request, e);
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
		int maxDepth = 0;
		Response priorResponse = response.priorResponse();
		while (priorResponse != null) {
			maxDepth++;
			priorResponse = priorResponse.priorResponse();
		}

		summarize(response, maxDepth);
	}

	private void summarize(Response response, int depth) {
		Logger logger = HTTPPlugin.getPlugin()
			.getPluginProcessContext()
			.getScriptContext()
			.getLogger();

		Function<Response, String> generateMessage = res ->
			String.format("%s -> %s %s, %d bytes", res.request().url(), res.code(), res.message(), getBodyLength(res.body()));

		StringBuilder depthIndicatorBuilder = new StringBuilder();
		Response priorResponse = response.priorResponse();
		if (priorResponse != null) {
			for (int i = 0; i < depth - 1; i++) {
				depthIndicatorBuilder.append("    ");
			}
			depthIndicatorBuilder.append("└── ");
			summarize(priorResponse, depth - 1);
		}

		logger.info(depthIndicatorBuilder.toString() + generateMessage.apply(response));
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

}
