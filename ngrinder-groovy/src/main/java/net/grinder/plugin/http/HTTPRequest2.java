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

import net.grinder.script.Statistics;
import net.grinder.statistics.StatisticsIndexMap;
import okhttp3.*;
import okio.BufferedSource;
import org.apache.commons.lang.time.StopWatch;
import org.conscrypt.Conscrypt;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.security.Security;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toList;

public class HTTPRequest2 {
	static {
		// To support ALPN
		Security.insertProviderAt(Conscrypt.newProvider(), 1);

		// Ensure plugin is loaded
		HTTPPlugin.getPlugin();
	}

	private static final Logger LOGGER = LoggerFactory.getLogger(HTTPRequest2.class);

	private static final List<Protocol> DEFAULT_PROTOCOLS = asList(Protocol.HTTP_2, Protocol.HTTP_1_1);

	private final OkHttpClient client;

	public HTTPRequest2() {
		this(null);
	}

	public HTTPRequest2(List<Protocol> protocols) {
		if (protocols == null || protocols.isEmpty()) {
			protocols = DEFAULT_PROTOCOLS;
		}

		client = new OkHttpClient()
			.newBuilder()
			.protocols(protocols)
			.cookieJar(new ThreadContextCookieJar())
			.eventListener(new ConnectionTimeAggregateListener())
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
			if (body != null) {
				BufferedSource source = body.source();
				source.request(Long.MAX_VALUE);

				statisticsForTest.addLong(
					StatisticsIndexMap.HTTP_PLUGIN_RESPONSE_LENGTH_KEY, source.getBuffer().snapshot().size());
			}
		} catch (Exception e) {
			LOGGER.error("Fail to aggregate HTTP statistics", e);
		}
	}

	private static class ThreadContextCookieJar implements CookieJar {
		private static final ThreadLocal<List<Cookie>> cookieJar = ThreadLocal.withInitial(ArrayList::new);

		@Override
		public void saveFromResponse(@NotNull HttpUrl httpUrl, @NotNull List<Cookie> list) {
			synchronized (cookieJar) {
				cookieJar.set(merge(cookieJar.get(), list));
			}
		}

		@NotNull
		@Override
		public List<Cookie> loadForRequest(@NotNull HttpUrl httpUrl) {
			return cookieJar.get();
		}

		private List<Cookie> merge(List<Cookie> origin, List<Cookie> cookies) {
			Predicate<Cookie> cookieNameNotContains = originCookie ->
				cookies.stream().anyMatch(cookie -> !cookie.name().equalsIgnoreCase(originCookie.name()));

			List<Cookie> merged = origin.stream()
				.filter(cookieNameNotContains)
				.collect(toList());
			merged.addAll(cookies);

			return merged;
		}
	}

	private static class ConnectionTimeAggregateListener extends EventListener {
		private final Logger LOGGER = LoggerFactory.getLogger(ConnectionTimeAggregateListener.class);

		private final StopWatch dnsStopWatch = new StopWatch();
		private final StopWatch connectStopWatch = new StopWatch();
		private final StopWatch timeToFirstByteStopWatch = new StopWatch();

		@Override
		public void dnsStart(@NotNull Call call, @NotNull String domainName) {
			dnsStopWatch.start();
		}

		@Override
		public void dnsEnd(@NotNull Call call, @NotNull String domainName, @NotNull List<InetAddress> inetAddressList) {
			dnsStopWatch.stop();
			long dnsTime = dnsStopWatch.getTime();
			dnsStopWatch.reset();

			accumulate(StatisticsIndexMap.HTTP_PLUGIN_DNS_TIME_KEY, dnsTime);
		}

		@Override
		public void connectStart(@NotNull Call call, @NotNull InetSocketAddress inetSocketAddress, @NotNull Proxy proxy) {
			connectStopWatch.start();
		}

		@Override
		public void connectEnd(@NotNull Call call, @NotNull InetSocketAddress inetSocketAddress, @NotNull Proxy proxy, @Nullable Protocol protocol) {
			connectStopWatch.stop();
			long connectTime = connectStopWatch.getTime();
			connectStopWatch.reset();

			accumulate(StatisticsIndexMap.HTTP_PLUGIN_CONNECT_TIME_KEY, connectTime);
		}

		@Override
		public void connectionAcquired(@NotNull Call call, @NotNull Connection connection) {
			timeToFirstByteStopWatch.start();
		}

		@Override
		public void responseHeadersStart(@NotNull Call call) {
			try {
				timeToFirstByteStopWatch.stop();
				long timeToFirstByte = timeToFirstByteStopWatch.getTime();
				timeToFirstByteStopWatch.reset();

				accumulate(StatisticsIndexMap.HTTP_PLUGIN_FIRST_BYTE_TIME_KEY, timeToFirstByte);
			} catch (IllegalStateException e) {
				// Do nothing.
				// responseHeadersStart event can be occurred multiple times when redirecting to the same server
			}
		}

		private void accumulate(String key, long time) {
			Statistics statistics = HTTPPlugin.getPlugin()
				.getPluginProcessContext()
				.getScriptContext()
				.getStatistics();

			if (!statistics.isTestInProgress()) {
				return;
			}

			try {
				Statistics.StatisticsForTest statisticsForTest = statistics.getForCurrentTest();
				statisticsForTest.addLong(key, time);
			} catch (Exception e) {
				LOGGER.error("Fail to accumulate HTTP statistics", e);
			}
		}
	}
}

