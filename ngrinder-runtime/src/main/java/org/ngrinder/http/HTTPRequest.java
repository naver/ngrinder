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
import org.apache.hc.core5.concurrent.FutureCallback;
import org.apache.hc.core5.http.*;
import org.apache.hc.core5.http.nio.AsyncClientEndpoint;
import org.apache.hc.core5.http.nio.AsyncRequestProducer;
import org.apache.hc.core5.http.nio.AsyncResponseConsumer;
import org.apache.hc.core5.http.nio.entity.BasicAsyncEntityConsumer;
import org.apache.hc.core5.http.nio.support.AsyncRequestBuilder;
import org.apache.hc.core5.http.nio.support.BasicResponseConsumer;
import org.apache.hc.core5.util.Timeout;
import org.ngrinder.http.method.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.HttpURLConnection;
import java.net.URI;
import java.util.List;
import java.util.concurrent.Future;

import static org.ngrinder.http.util.ContentTypeUtils.getContentType;

public class HTTPRequest implements HTTPHead, HTTPGet, HTTPPost, HTTPPut, HTTPPatch, HTTPDelete {

	private static final Logger LOGGER = LoggerFactory.getLogger(HTTPRequest.class);

	private static final AsyncResponseConsumer<Message<HttpResponse, byte[]>> DEFAULT_CONSUMER = new BasicResponseConsumer<>(new BasicAsyncEntityConsumer());
	private static final FutureCallback<Message<HttpResponse, byte[]>> DEFAULT_CALLBACK = new SimpleFutureCallback<>();

	private static final HTTPRequester requester = new HTTPRequester();

	static {
		// noinspection ResultOfMethodCallIgnored
		HTTPPlugin.getPlugin();    // Ensure plugin is loaded

		requester.start();
	}

	private HTTPRequest() {

	}

	public static HTTPRequest create() {
		return new HTTPRequest();
	}

	@Override
	public HTTPResponse HEAD(String uri, List<NameValuePair> params, List<Header> headers) {
		return doRequest(uri, createRequest("HEAD", uri, params, headers));
	}

	@Override
	public HTTPResponse GET(String uri, List<NameValuePair> params, List<Header> headers) {
		return doRequest(uri, createRequest("GET", uri, params, headers));
	}

	@Override
	public HTTPResponse POST(String uri, byte[] content, List<Header> headers) {
		return doRequest(uri, createRequestWithBody("POST", uri, content, headers));
	}

	@Override
	public HTTPResponse POST(String uri, List<NameValuePair> params, List<Header> headers) {
		return doRequest(uri, createRequest("POST", uri, params, headers));
	}

	@Override
	public HTTPResponse PUT(String uri, byte[] content, List<Header> headers) {
		return doRequest(uri, createRequestWithBody("PUT", uri, content, headers));
	}

	@Override
	public HTTPResponse PUT(String uri, List<NameValuePair> params, List<Header> headers) {
		return doRequest(uri, createRequest("PUT", uri, params, headers));
	}

	@Override
	public HTTPResponse PATCH(String uri, byte[] content, List<Header> headers) {
		return doRequest(uri, createRequestWithBody("PATCH", uri, content, headers));
	}

	@Override
	public HTTPResponse PATCH(String uri, List<NameValuePair> params, List<Header> headers) {
		return doRequest(uri, createRequest("PATCH", uri, params, headers));
	}

	@Override
	public HTTPResponse DELETE(String uri, List<NameValuePair> params, List<Header> headers) {
		return doRequest(uri, createRequest("DELETE", uri, params, headers));
	}

	private HTTPResponse doRequest(String uri, AsyncRequestProducer producer) {
		try {
			final HttpHost httpHost = HttpHost.create(URI.create(uri));
			final Timeout connectionTimeout = Timeout.ofMilliseconds(HTTPRequestControl.getConnectionTimeout());

			Future<AsyncClientEndpoint> endpointFuture = requester.connect(httpHost, connectionTimeout);
			AsyncClientEndpoint endpoint = endpointFuture.get();

			Future<Message<HttpResponse, byte[]>> messageFuture = endpoint.execute(producer, DEFAULT_CONSUMER, DEFAULT_CALLBACK);
			Message<HttpResponse, byte[]> message = messageFuture.get();

			endpoint.releaseAndReuse();

			aggregate(message);
			summarize(uri, message);

			return HTTPResponse.of(message);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private void aggregate(Message<HttpResponse, byte[]> message) {
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
				StatisticsIndexMap.HTTP_PLUGIN_RESPONSE_STATUS_KEY, message.getHead().getCode());

			if (message.getHead().getCode() >= HttpURLConnection.HTTP_BAD_REQUEST) {
				statisticsForTest.addLong(
					StatisticsIndexMap.HTTP_PLUGIN_RESPONSE_ERRORS_KEY, 1);
			}

			statisticsForTest.addLong(
				StatisticsIndexMap.HTTP_PLUGIN_RESPONSE_LENGTH_KEY, message.getBody() == null ? 0 : message.getBody().length);
		} catch (Exception e) {
			LOGGER.error("Fail to aggregate HTTP statistics", e);
		}
	}

	private void summarize(String uri, Message<HttpResponse, byte[]> message) {
		Logger logger = HTTPPlugin.getPlugin()
			.getPluginProcessContext()
			.getScriptContext()
			.getLogger();

		logger.info("{} -> {} {}, {} bytes",
			uri,
			message.getHead().getCode(),
			message.getHead().getReasonPhrase(),
			message.getBody() == null ? 0 : message.getBody().length);
	}

	private AsyncRequestProducer createRequest(String method, String uri, List<NameValuePair> params, List<Header> headers) {
		AsyncRequestBuilder builder = AsyncRequestBuilder
			.create(method)
			.setUri(uri);

		params.forEach(builder::addParameter);
		headers.forEach(builder::addHeader);

		return builder.build();
	}

	private AsyncRequestProducer createRequestWithBody(String method, String uri, byte[] content, List<Header> headers) {
		AsyncRequestBuilder builder = AsyncRequestBuilder
			.create(method)
			.setUri(uri)
			.setEntity(content, getContentType(headers));

		headers.forEach(builder::addHeader);

		return builder.build();
	}
}
