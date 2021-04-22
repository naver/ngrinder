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

import net.grinder.common.GrinderProperties;
import net.grinder.script.Grinder;
import net.grinder.util.Pair;
import org.apache.commons.lang.time.StopWatch;
import org.apache.hc.core5.concurrent.FutureCallback;
import org.apache.hc.core5.function.Callback;
import org.apache.hc.core5.function.Decorator;
import org.apache.hc.core5.function.Supplier;
import org.apache.hc.core5.http.HttpHost;
import org.apache.hc.core5.http.config.CharCodingConfig;
import org.apache.hc.core5.http.config.Http1Config;
import org.apache.hc.core5.http.impl.HttpProcessors;
import org.apache.hc.core5.http.impl.bootstrap.HttpAsyncRequester;
import org.apache.hc.core5.http.impl.nio.ClientHttp1StreamDuplexerFactory;
import org.apache.hc.core5.http.nio.AsyncClientEndpoint;
import org.apache.hc.core5.http.nio.AsyncPushConsumer;
import org.apache.hc.core5.http.protocol.RequestHandlerRegistry;
import org.apache.hc.core5.http2.HttpVersionPolicy;
import org.apache.hc.core5.http2.config.H2Config;
import org.apache.hc.core5.http2.impl.H2Processors;
import org.apache.hc.core5.http2.impl.nio.ClientH2StreamMultiplexerFactory;
import org.apache.hc.core5.http2.impl.nio.ClientHttpProtocolNegotiatorFactory;
import org.apache.hc.core5.http2.nio.support.DefaultAsyncPushConsumerFactory;
import org.apache.hc.core5.http2.ssl.H2ClientTlsStrategy;
import org.apache.hc.core5.reactor.IOEventHandlerFactory;
import org.apache.hc.core5.reactor.IOReactorConfig;
import org.apache.hc.core5.reactor.IOSession;
import org.apache.hc.core5.reactor.IOSessionListener;
import org.apache.hc.core5.util.Timeout;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Future;

public class HTTPRequester extends HttpAsyncRequester {
	private static final ThreadAwareConnPool<HttpHost, IOSession> connPool = new ThreadAwareConnPool<>();

	private HttpVersionPolicy versionPolicy;

	public HTTPRequester(IOReactorConfig ioReactorConfig,
						 IOEventHandlerFactory ioEventHandlerFactory,
						 Decorator<IOSession> ioSessionDecorator,
						 Callback<Exception> exceptionCallback,
						 IOSessionListener ioSessionListener,
						 HttpVersionPolicy versionPolicy) {
		super(ioReactorConfig, ioEventHandlerFactory, ioSessionDecorator, exceptionCallback, ioSessionListener, connPool);
		this.versionPolicy = versionPolicy == null ? HttpVersionPolicy.NEGOTIATE : versionPolicy;
	}

	public static void reset() {
		connPool.clear();
	}

	@Override
	protected Future<AsyncClientEndpoint> doConnect(HttpHost host, Timeout timeout, Object attachment, FutureCallback<AsyncClientEndpoint> callback) {
		return super.doConnect(host, timeout, attachment != null ? attachment : versionPolicy, callback);
	}

	public void setVersionPolicy(HttpVersionPolicy versionPolicy) {
		this.versionPolicy = versionPolicy;
	}

	public static class Builder {

		private HttpVersionPolicy versionPolicy;
		private long readBytes;

		Builder() {
			this.versionPolicy = HttpVersionPolicy.NEGOTIATE;
			this.readBytes = -1;
		}

		public Builder setReadBytes(long readBytes) {
			this.readBytes = readBytes;
			return this;
		}

		public Builder setVersionPolicy(HttpVersionPolicy versionPolicy) {
			this.versionPolicy = versionPolicy;
			return this;
		}

		private IOReactorConfig ioReactorConfig() {
			GrinderProperties properties = Grinder.grinder.getProperties();
			int totalThreadCount = 1;
			if (properties != null) {
				totalThreadCount = properties.getInt("grinder.threads", 1);
			}

			int ioThreadCount = totalThreadCount / 100 + 1;

			return IOReactorConfig.custom()
				.setIoThreadCount(ioThreadCount)
				.setSoTimeout(Timeout.ofMilliseconds(HTTPRequestControl.getSocketTimeout()))
				.build();
		}

		private IOEventHandlerFactory ioEventHandlerFactory() {
			final RequestHandlerRegistry<Supplier<AsyncPushConsumer>> registry = new RequestHandlerRegistry<>();
			final ClientHttp1StreamDuplexerFactory http1StreamHandlerFactory = new ClientHttp1StreamDuplexerFactory(
				HttpProcessors.client(),
				Http1Config.DEFAULT,
				CharCodingConfig.DEFAULT,
				null,
				null,
				null,
				new PartialContentLengthStrategy(this.readBytes),
				null,
				null);
			final ClientH2StreamMultiplexerFactory http2StreamHandlerFactory = new ClientH2StreamMultiplexerFactory(
				H2Processors.client(),
				new DefaultAsyncPushConsumerFactory(registry),
				H2Config.DEFAULT,
				CharCodingConfig.DEFAULT,
				null);
			final SSLContext sslContext;
			try {
				sslContext = SSLContext.getInstance("SSL");
				sslContext.init(null, new TrustManager[]{new TrustAllManager()}, new SecureRandom());
			} catch (GeneralSecurityException e) {
				throw new RuntimeException(e);
			}
			return new ClientHttpProtocolNegotiatorFactory(
				http1StreamHandlerFactory,
				http2StreamHandlerFactory,
				null,
				new H2ClientTlsStrategy(sslContext),
				null);
		}

		private IOSessionListener ioSessionListener() {
			return new IOSessionListener() {
				private final Map<IOSession, Pair<StopWatch, Boolean>> stopWatchAndTlsFlagMap = new HashMap<>();

				@Override
				public void connected(IOSession session) {
					Pair<StopWatch, Boolean> stopWatchAndTlsFlag = stopWatchAndTlsFlagMap.get(session);
					if (stopWatchAndTlsFlag == null) {
						stopWatchAndTlsFlag = Pair.of(new StopWatch(), false);
						stopWatchAndTlsFlag.getFirst().start();
					} else {
						stopWatchAndTlsFlag = Pair.of(stopWatchAndTlsFlag.getFirst(), false);
					}

					stopWatchAndTlsFlagMap.put(session, stopWatchAndTlsFlag);
				}

				@Override
				public void startTls(IOSession session) {
					Pair<StopWatch, Boolean> stopWatchAndTlsFlag = Pair.of(new StopWatch(), true);
					stopWatchAndTlsFlag.getFirst().start();

					stopWatchAndTlsFlagMap.put(session, stopWatchAndTlsFlag);
				}

				@Override
				public void inputReady(IOSession session) {
					Pair<StopWatch, Boolean> stopWatchAndTlsFlag = stopWatchAndTlsFlagMap.get(session);
					if (stopWatchAndTlsFlag == null) {
						return;
					}

					StopWatch stopWatch = stopWatchAndTlsFlag.getFirst();
					boolean isTlsEvent = stopWatchAndTlsFlag.getSecond();

					if (isTlsEvent) {
						return;
					}

					stopWatch.stop();
					long timeToFirstByte = stopWatch.getTime();
					stopWatch.reset();

					TimeToFirstByteHolder.accumulate(timeToFirstByte);
					stopWatchAndTlsFlagMap.remove(session);
				}

				@Override
				public void outputReady(IOSession session) {

				}

				@Override
				public void timeout(IOSession session) {

				}

				@Override
				public void exception(IOSession session, Exception ex) {

				}

				@Override
				public void disconnected(IOSession session) {

				}
			};
		}

		public HTTPRequester build() {
			HTTPRequester requester = new HTTPRequester(
				ioReactorConfig(),
				ioEventHandlerFactory(),
				null,
				null,
				ioSessionListener(),
				versionPolicy);
			requester.start();
			return requester;
		}
	}
}
