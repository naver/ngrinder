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
import org.apache.commons.lang.time.StopWatch;
import org.conscrypt.Conscrypt;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.security.Security;
import java.util.List;

public class HTTPRequest2 {
	static {
		// To support ALPN
		Security.insertProviderAt(Conscrypt.newProvider(), 1);

		// Ensure plugin is loaded
		HTTPPlugin.getPlugin();
	}

	private static final Logger LOGGER = LoggerFactory.getLogger(HTTPRequest2.class);

	private final OkHttpClient client;

	public HTTPRequest2() {
		client = new OkHttpClient()
			.newBuilder()
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
		// TODO: Implement aggregate statistics
	}

	private static class ConnectionTimeAggregateListener extends EventListener {
		private final Logger LOGGER = LoggerFactory.getLogger(ConnectionTimeAggregateListener.class);

		private final StopWatch dnsStopWatch = new StopWatch();
		private final StopWatch connectStopWatch = new StopWatch();

		@Override
		public void dnsStart(@NotNull Call call, @NotNull String domainName) {
			dnsStopWatch.start();
		}

		@Override
		public void dnsEnd(@NotNull Call call, @NotNull String domainName, @NotNull List<InetAddress> inetAddressList) {
			dnsStopWatch.stop();
			long dnsTime = dnsStopWatch.getTime();
			dnsStopWatch.reset();

			LOGGER.debug("DNS time : " + dnsTime);
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

			LOGGER.debug("Connect time : " + connectTime);
			accumulate(StatisticsIndexMap.HTTP_PLUGIN_CONNECT_TIME_KEY, connectTime);
		}

		private void accumulate(String key, long time) {
			Statistics statistics = HTTPPlugin.getPlugin()
				.getPluginProcessContext()
				.getScriptContext()
				.getStatistics();

			if (statistics.isTestInProgress()) {
				try {
					Statistics.StatisticsForTest statisticsForTest = statistics.getForCurrentTest();
					statisticsForTest.addLong(key, time);
				} catch (Exception e) {
					LOGGER.error("Fail to accumulate HTTP statistics", e);
				}
			}
		}
	}
}

