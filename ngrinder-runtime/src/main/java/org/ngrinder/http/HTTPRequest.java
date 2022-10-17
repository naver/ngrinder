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

import HTTPClient.NVPair;
import net.grinder.plugin.http.HTTPPlugin;
import net.grinder.script.InvalidContextException;
import net.grinder.script.NoSuchStatisticException;
import net.grinder.script.Statistics;
import net.grinder.statistics.StatisticsIndexMap;
import org.apache.hc.client5.http.cookie.*;
import org.apache.hc.client5.http.impl.cookie.RFC6265StrictSpec;
import org.apache.hc.core5.http.*;
import org.apache.hc.core5.http.message.BasicHeader;
import org.apache.hc.core5.http.message.BasicNameValuePair;
import org.apache.hc.core5.http.nio.AsyncClientEndpoint;
import org.apache.hc.core5.http.nio.AsyncEntityProducer;
import org.apache.hc.core5.http.nio.AsyncRequestProducer;
import org.apache.hc.core5.http.nio.AsyncResponseConsumer;
import org.apache.hc.core5.http.nio.entity.BasicAsyncEntityConsumer;
import org.apache.hc.core5.http.nio.support.AsyncRequestBuilder;
import org.apache.hc.core5.http.nio.support.BasicResponseConsumer;
import org.apache.hc.core5.http2.HttpVersionPolicy;
import org.apache.hc.core5.util.Timeout;
import org.ngrinder.http.cookie.ThreadContextCookieStore;
import org.ngrinder.http.method.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.HttpURLConnection;
import java.net.URI;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import static java.util.Collections.emptyList;
import static org.ngrinder.http.util.ContentTypeUtils.getContentType;
import static org.ngrinder.http.util.JsonUtils.toJson;
import static org.ngrinder.http.util.PairListConvertUtils.convert;

public class HTTPRequest implements HTTPHead, HTTPGet, HTTPPost, HTTPPut, HTTPPatch, HTTPDelete {

	private static final Logger LOGGER = LoggerFactory.getLogger(HTTPRequest.class);

	private static final CookieStore COOKIE_STORE = ThreadContextCookieStore.INSTANCE;
	private static final CookieSpec COOKIE_SPEC = new RFC6265StrictSpec();

	private HTTPRequester requester;

	private CookieOrigin cookieOrigin;

	private int readBytes = -1;

	private HttpVersionPolicy versionPolicy = HttpVersionPolicy.NEGOTIATE;

	private List<Header> headers = emptyList();

	static {
		// noinspection ResultOfMethodCallIgnored
		HTTPPlugin.getPlugin();    // Ensure plugin is loaded
	}

	public HTTPRequest() {
		requester = new HTTPRequester.Builder().build();
	}

	@Override
	public HTTPResponse HEAD(String uri, List<NameValuePair> params, List<Header> headers) {
		return doRequest(uri, createRequestWithParam("HEAD", uri, params, headers));
	}

	@Override
	public HTTPResponse GET(String uri, List<NameValuePair> params, List<Header> headers) {
		return doRequest(uri, createRequestWithParam("GET", uri, params, headers));
	}

	@Override
	public HTTPResponse POST(String uri, byte[] content, List<Header> headers) {
		return doRequest(uri, createRequestWithBody("POST", uri, content, headers));
	}

	@Override
	public HTTPResponse POST(String uri, List<NameValuePair> params, List<Header> headers) {
		return doRequest(uri, createRequestWithParam("POST", uri, params, headers));
	}

	@Override
	public HTTPResponse POST(String uri, Map<?, ?> params, List<Header> headers) {
		final List<Header> actualHeaders = headers.isEmpty() ? this.headers : headers;
		if (getContentType(actualHeaders).isSameMimeType(ContentType.APPLICATION_JSON)) {
			return POST(uri, toJson(params).getBytes(), actualHeaders);
		}

		return POST(uri, convert((Map<String, String>) params, BasicNameValuePair::new), actualHeaders);
	}

	@Override
	public HTTPResponse POST(String uri, AsyncEntityProducer asyncEntityProducer, List<Header> headers) {
		return doRequest(uri, createRequestWithEntity("POST", uri, asyncEntityProducer, headers));
	}

	@Override
	public HTTPResponse PUT(String uri, byte[] content, List<Header> headers) {
		return doRequest(uri, createRequestWithBody("PUT", uri, content, headers));
	}

	@Override
	public HTTPResponse PUT(String uri, List<NameValuePair> params, List<Header> headers) {
		return doRequest(uri, createRequestWithParam("PUT", uri, params, headers));
	}

	@Override
	public HTTPResponse PUT(String uri, Map<?, ?> params, List<Header> headers) {
		final List<Header> actualHeaders = headers.isEmpty() ? this.headers : headers;
		if (getContentType(actualHeaders).isSameMimeType(ContentType.APPLICATION_JSON)) {
			return PUT(uri, toJson(params).getBytes(), headers);
		}
		return PUT(uri, convert((Map<String, String>) params, BasicNameValuePair::new), actualHeaders);
	}

	@Override
	public HTTPResponse PUT(String uri, AsyncEntityProducer asyncEntityProducer, List<Header> headers) {
		return doRequest(uri, createRequestWithEntity("PUT", uri, asyncEntityProducer, headers));
	}

	@Override
	public HTTPResponse PATCH(String uri, byte[] content, List<Header> headers) {
		return doRequest(uri, createRequestWithBody("PATCH", uri, content, headers));
	}

	@Override
	public HTTPResponse PATCH(String uri, List<NameValuePair> params, List<Header> headers) {
		return doRequest(uri, createRequestWithParam("PATCH", uri, params, headers));
	}

	@Override
	public HTTPResponse PATCH(String uri, Map<?, ?> params, List<Header> headers) {
		final List<Header> actualHeaders = headers.isEmpty() ? this.headers : headers;
		if (getContentType(actualHeaders).isSameMimeType(ContentType.APPLICATION_JSON)) {
			return PATCH(uri, toJson(params).getBytes(), headers);
		}
		return PATCH(uri, convert((Map<String, String>) params, BasicNameValuePair::new), actualHeaders);
	}

	@Override
	public HTTPResponse PATCH(String uri, AsyncEntityProducer asyncEntityProducer, List<Header> headers) {
		return doRequest(uri, createRequestWithEntity("PATCH", uri, asyncEntityProducer, headers));
	}

	@Override
	public HTTPResponse DELETE(String uri, List<NameValuePair> params, List<Header> headers) {
		return doRequest(uri, createRequestWithParam("DELETE", uri, params, headers));
	}

	private HTTPResponse doRequest(String uri, AsyncRequestProducer producer) {
		AsyncClientEndpoint endpoint = getEndpoint(uri);
		try {
			AsyncResponseConsumer<Message<HttpResponse, byte[]>> consumer = new BasicResponseConsumer<>(new BasicAsyncEntityConsumer());

			Future<Message<HttpResponse, byte[]>> messageFuture = endpoint.execute(producer, consumer, null);
			Message<HttpResponse, byte[]> message = messageFuture.get();

			endpoint.releaseAndReuse();

			processResponseCookies(message.getHead().headerIterator("Set-Cookie"));

			aggregate(message);
			summarize(uri, message);

			return HTTPResponse.of(message);
		} catch (Exception e) {
			endpoint.releaseAndDiscard();
			throw new RuntimeException(e);
		}
	}

	private AsyncClientEndpoint getEndpoint(String uri) {
		try {
			final HttpHost httpHost = HttpHost.create(URI.create(uri));
			final Timeout connectionTimeout = Timeout.ofMilliseconds(HTTPRequestControl.getConnectionTimeout());

			long start = System.currentTimeMillis();
			AsyncClientEndpoint endpoint = requester.connect(httpHost, connectionTimeout).get();
			long end = System.currentTimeMillis();

			Statistics statistics = getStatistics();
			if (statistics.isTestInProgress()) {
				Statistics.StatisticsForTest statisticsForTest = statistics.getForCurrentTest();

				statisticsForTest.setLong(
					StatisticsIndexMap.HTTP_PLUGIN_CONNECT_TIME_KEY, end - start);
			}

			return endpoint;
		} catch (InterruptedException | ExecutionException | InvalidContextException | NoSuchStatisticException e) {
			throw new RuntimeException(e);
		}
	}

	private Statistics getStatistics() {
		return HTTPPlugin.getPlugin()
			.getPluginProcessContext()
			.getScriptContext()
			.getStatistics();
	}

	private void aggregate(Message<HttpResponse, byte[]> message) {
		Statistics statistics = getStatistics();

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

			statisticsForTest.addLong(
				StatisticsIndexMap.HTTP_PLUGIN_FIRST_BYTE_TIME_KEY, TimeToFirstByteHolder.getTotal() + statisticsForTest.getLong(StatisticsIndexMap.HTTP_PLUGIN_CONNECT_TIME_KEY));
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

	private AsyncRequestBuilder createRequest(String method, String uri, List<Header> headers) {
		AsyncRequestBuilder builder = AsyncRequestBuilder
			.create(method)
			.setUri(uri);

		final List<Header> actualHeaders = headers.isEmpty() ? this.headers : headers;
		actualHeaders.forEach(builder::addHeader);
		getMatchedCookies(uri).forEach(builder::addHeader);

		return builder;
	}

	private AsyncRequestProducer createRequestWithParam(String method, String uri, List<NameValuePair> params, List<Header> headers) {
		AsyncRequestBuilder builder = createRequest(method, uri, headers);
		params.forEach(builder::addParameter);

		return builder.build();
	}

	private AsyncRequestProducer createRequestWithBody(String method, String uri, byte[] content, List<Header> headers) {
		AsyncRequestBuilder builder = createRequest(method, uri, headers);
		builder.setEntity(content, getContentType(headers));

		return builder.build();
	}

	private AsyncRequestProducer createRequestWithEntity(String method, String uri, AsyncEntityProducer asyncEntityProducer, List<Header> headers) {
		AsyncRequestBuilder builder = createRequest(method, uri, headers);
		builder.setEntity(asyncEntityProducer);

		return builder.build();
	}

	private List<Header> getMatchedCookies(String uriString) {
		URI uri = URI.create(uriString);

		int port = uri.getPort();
		if (port == -1) {
			port = 80;
		}

		boolean isSecure = uri.getScheme().equalsIgnoreCase("https");

		cookieOrigin = new CookieOrigin(uri.getHost(), port, uri.getPath(), isSecure);

		final List<Cookie> cookies = COOKIE_STORE.getCookies();
		// Find cookies matching the given origin
		final List<Cookie> matchedCookies = new ArrayList<>();
		final Date now = new Date();
		boolean expired = false;
		for (final Cookie cookie : cookies) {
			if (!cookie.isExpired(now)) {
				if (COOKIE_SPEC.match(cookie, cookieOrigin)) {
					if (LOGGER.isDebugEnabled()) {
						LOGGER.debug("Cookie {} match {}", cookie, cookieOrigin);
					}
					matchedCookies.add(cookie);
				}
			} else {
				if (LOGGER.isDebugEnabled()) {
					LOGGER.debug("Cookie {} expired", cookie);
				}
				expired = true;
			}
		}
		// Per RFC 6265, 5.3
		// The user agent must evict all expired cookies if, at any time, an expired cookie
		// exists in the cookie store
		if (expired) {
			COOKIE_STORE.clearExpired(now);
		}
		// Generate Cookie request headers
		if (!matchedCookies.isEmpty()) {
			return COOKIE_SPEC.formatCookies(matchedCookies);
		}
		return emptyList();
	}

	private void processResponseCookies(Iterator<Header> iterator) {
		iterator.forEachRemaining(header -> {
			try {
				List<Cookie> cookies = COOKIE_SPEC.parse(header, cookieOrigin);
				for (Cookie cookie : cookies) {
					try {
						COOKIE_SPEC.validate(cookie, cookieOrigin);
						COOKIE_STORE.addCookie(cookie);
					} catch (MalformedCookieException e) {
						LOGGER.warn("Cookie rejected [{}] {}", cookie, e.getMessage());
					}
				}
			} catch (MalformedCookieException ex) {
				LOGGER.warn("Invalid cookie header: \"{}\". {}", header, ex.getMessage());
			}
		});
	}

	/**
	 * Set version policy one of FORCE_HTTP_1, FORCE_HTTP_2 and NEGOTIATE
	 */
	public void setVersionPolicy(HttpVersionPolicy versionPolicy) {
		this.versionPolicy = versionPolicy;
		requester.setVersionPolicy(versionPolicy);
	}

	public int getReadBytes() {
		return readBytes;
	}

	public void setReadBytes(int readBytes) {
		this.readBytes = readBytes;
		this.requester = new HTTPRequester.Builder()
			.setReadBytes(this.readBytes)
			.setVersionPolicy(this.versionPolicy)
			.build();
	}

	public void setHeaders(List<Header> headers) {
		this.headers = headers;
	}

	public void setHeaders(Map<String, String> headers) {
		setHeaders(convert(headers, BasicHeader::new));
	}

	public void setHeaders(NVPair[] nvPairHeaders) {
		setHeaders(convert(nvPairHeaders, BasicHeader::new));
	}
}
