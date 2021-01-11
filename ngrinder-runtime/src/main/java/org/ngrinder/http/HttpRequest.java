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

import net.grinder.plugin.http.HTTPPlugin;
import net.grinder.script.Statistics;
import net.grinder.statistics.StatisticsIndexMap;
import org.apache.hc.client5.http.async.methods.SimpleHttpResponse;
import org.apache.hc.client5.http.async.methods.SimpleResponseConsumer;
import org.apache.hc.client5.http.impl.async.CloseableHttpAsyncClient;
import org.apache.hc.core5.concurrent.FutureCallback;
import org.apache.hc.core5.http.Header;
import org.apache.hc.core5.http.NameValuePair;
import org.apache.hc.core5.http.nio.AsyncRequestProducer;
import org.apache.hc.core5.http.nio.AsyncResponseConsumer;
import org.apache.hc.core5.http.nio.support.AsyncRequestBuilder;
import org.ngrinder.http.consumer.PartialResponseConsumer;
import org.ngrinder.http.method.HttpGet;
import org.ngrinder.http.method.HttpHead;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.HttpURLConnection;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class HttpRequest implements HttpHead, HttpGet {
	private static final Logger LOGGER = LoggerFactory.getLogger(HttpRequest.class);

	static {
		// noinspection ResultOfMethodCallIgnored
		HTTPPlugin.getPlugin();    // Ensure plugin is loaded
	}

	/**
	 * Indicates how may first bytes to read from the response body.
	 * If readBytes smaller than 0, read entire response body.
	 * It readBytes greater than or equal to 0, read only given bytes from beginning of the response body.
	 */
	private int readBytes = -1;

	private HttpRequest() {
	}

	public static HttpRequest create() {
		return new HttpRequest();
	}

	@Override
	public HttpResponse HEAD(String uri, List<NameValuePair> params, List<Header> headers) {
		return doRequest(createRequest("HEAD", uri, params, headers));
	}

	@Override
	public HttpResponse GET(String uri, List<NameValuePair> params, List<Header> headers) {
		return doRequest(createRequest("GET", uri, params, headers));
	}

	public HttpResponse POST(String uri, List<NameValuePair> params, List<Header> headers) {
		return HttpResponse.of(null);
	}

	public HttpResponse PUT(String uri, List<NameValuePair> params, List<Header> headers) {
		return HttpResponse.of(null);
	}

	public HttpResponse PATCH(String uri, List<NameValuePair> params, List<Header> headers) {
		return HttpResponse.of(null);
	}

	public HttpResponse DELETE(String uri, List<NameValuePair> params, List<Header> headers) {
		return HttpResponse.of(null);
	}

	private AsyncRequestProducer createRequest(String method, String uri, List<NameValuePair> params, List<Header> headers) {
		AsyncRequestBuilder builder = AsyncRequestBuilder
			.create(method)
			.setUri(uri);

		params.forEach(builder::addParameter);
		headers.forEach(builder::addHeader);

		return builder.build();
	}

	private HttpResponse doRequest(AsyncRequestProducer producer) {
		final AsyncResponseConsumer<SimpleHttpResponse> consumer;
		if (getReadBytes() >= 0) {
			consumer = PartialResponseConsumer.create(getReadBytes());
		} else {
			consumer = SimpleResponseConsumer.create();
		}

		FutureCallback<SimpleHttpResponse> futureCallback = SimpleFutureCallback.create();

		CloseableHttpAsyncClient client = ThreadContextHttpClient.get();
		try {
			SimpleHttpResponse response = client.execute(producer, consumer, futureCallback).get();

			aggregate(response);

			return HttpResponse.of(response);
		} catch (InterruptedException | ExecutionException e) {
			throw new RuntimeException("Fail to execute a request " + producer, e);
		}
	}

	private void aggregate(SimpleHttpResponse response) {
		Statistics statistics = HTTPPlugin.getPlugin()
			.getPluginProcessContext()
			.getScriptContext()
			.getStatistics();

		if (!statistics.isTestInProgress()) {
			return;
		}

		try {
			Statistics.StatisticsForTest statisticsForTest = statistics.getForCurrentTest();

			statisticsForTest.setLong(StatisticsIndexMap.HTTP_PLUGIN_RESPONSE_STATUS_KEY, response.getCode());

			if (response.getCode() >= HttpURLConnection.HTTP_BAD_REQUEST) {
				statisticsForTest.addLong(StatisticsIndexMap.HTTP_PLUGIN_RESPONSE_ERRORS_KEY, 1);
			}

			statisticsForTest.addLong(StatisticsIndexMap.HTTP_PLUGIN_RESPONSE_LENGTH_KEY, response.getBodyBytes().length);
		} catch (Exception e) {
			LOGGER.error("Fail to aggregate http statistics", e);
		}
	}

	public int getReadBytes() {
		return readBytes;
	}

	public void setReadBytes(int readBytes) {
		this.readBytes = readBytes;
	}
}
