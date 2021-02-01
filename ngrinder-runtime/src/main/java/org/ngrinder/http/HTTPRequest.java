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

import org.apache.hc.core5.concurrent.FutureCallback;
import org.apache.hc.core5.http.*;
import org.apache.hc.core5.http.nio.AsyncClientEndpoint;
import org.apache.hc.core5.http.nio.AsyncRequestProducer;
import org.apache.hc.core5.http.nio.AsyncResponseConsumer;
import org.apache.hc.core5.http.nio.entity.BasicAsyncEntityConsumer;
import org.apache.hc.core5.http.nio.support.AsyncRequestBuilder;
import org.apache.hc.core5.http.nio.support.BasicResponseConsumer;
import org.apache.hc.core5.util.Timeout;
import org.ngrinder.http.method.HTTPGet;
import org.ngrinder.http.method.HTTPHead;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.util.List;
import java.util.concurrent.Future;

public class HTTPRequest implements HTTPHead, HTTPGet {

	private static final Logger LOGGER = LoggerFactory.getLogger(HTTPRequest.class);

	private static final AsyncResponseConsumer<Message<HttpResponse, byte[]>> DEFAULT_CONSUMER = new BasicResponseConsumer<>(new BasicAsyncEntityConsumer());
	private static final FutureCallback<Message<HttpResponse, byte[]>> DEFAULT_CALLBACK = new SimpleFutureCallback<>();

	private static final HTTPRequester requester = new HTTPRequester();

	static {
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

	private HTTPResponse doRequest(String uri, AsyncRequestProducer producer) {
		try {
			final HttpHost httpHost = HttpHost.create(URI.create(uri));
			final Timeout connectionTimeout = Timeout.ofMilliseconds(HTTPRequestControl.getConnectionTimeout());

			Future<AsyncClientEndpoint> endpointFuture = requester.connect(httpHost, connectionTimeout);
			AsyncClientEndpoint endpoint = endpointFuture.get();

			Future<Message<HttpResponse, byte[]>> messageFuture = endpoint.execute(producer, DEFAULT_CONSUMER, DEFAULT_CALLBACK);
			Message<HttpResponse, byte[]> message = messageFuture.get();

			LOGGER.info("{} -> {} {}, {} bytes", uri, message.getHead().getCode(), message.getHead().getReasonPhrase(), message.getBody().length);

			endpoint.releaseAndReuse();

			return HTTPResponse.of(message);
		} catch (Exception e) {
			LOGGER.error("Fail to execute HTTP request", e);
			throw new RuntimeException(e);
		}
	}

	private AsyncRequestProducer createRequest(String method, String uri, List<NameValuePair> params, List<Header> headers) {
		AsyncRequestBuilder builder = AsyncRequestBuilder
			.create(method)
			.setUri(uri);

		params.forEach(builder::addParameter);
		headers.forEach(builder::addHeader);

		return builder.build();
	}
}
