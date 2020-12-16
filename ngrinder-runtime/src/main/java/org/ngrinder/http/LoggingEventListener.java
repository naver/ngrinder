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

import okhttp3.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.util.List;

public class LoggingEventListener extends EventListener {
	private static final Logger logger = LoggerFactory.getLogger(LoggingEventListener.class);

	public static final Factory FACTORY = call -> new LoggingEventListener();

	private long startTime;

	private String hash(Call call) {
		return Integer.toHexString(call.hashCode());
	}

	@Override
	public void callStart(@NotNull Call call) {
		startTime = System.currentTimeMillis();
		logger.debug(String.format("[%s] callStart", hash(call)));
	}

	@Override
	public void callEnd(@NotNull Call call) {
		logger.debug(String.format("[%s] callEnd %dms", hash(call), System.currentTimeMillis() - startTime));
	}

	@Override
	public void dnsStart(@NotNull Call call, @NotNull String domainName) {
		logger.debug(String.format("[%s] dnsStart %dms", hash(call), System.currentTimeMillis() - startTime));
	}

	@Override
	public void dnsEnd(@NotNull Call call, @NotNull String domainName, @NotNull List<InetAddress> inetAddressList) {
		logger.debug(String.format("[%s] dnsEnd %dms", hash(call), System.currentTimeMillis() - startTime));
	}

	@Override
	public void connectStart(@NotNull Call call, @NotNull InetSocketAddress inetSocketAddress, @NotNull Proxy proxy) {
		logger.debug(String.format("[%s] connectStart %dms", hash(call), System.currentTimeMillis() - startTime));
	}

	@Override
	public void connectEnd(@NotNull Call call, @NotNull InetSocketAddress inetSocketAddress, @NotNull Proxy proxy, @Nullable Protocol protocol) {
		logger.debug(String.format("[%s] connectEnd %dms", hash(call), System.currentTimeMillis() - startTime));
	}

	@Override
	public void requestHeadersStart(@NotNull Call call) {
		logger.debug(String.format("[%s] requestHeadersStart %dms", hash(call), System.currentTimeMillis() - startTime));
	}

	@Override
	public void requestHeadersEnd(@NotNull Call call, @NotNull Request request) {
		logger.debug(String.format("[%s] requestHeadersEnd %dms", hash(call), System.currentTimeMillis() - startTime));
	}

	@Override
	public void requestBodyStart(@NotNull Call call) {
		logger.debug(String.format("[%s] requestBodyStart %dms", hash(call), System.currentTimeMillis() - startTime));
	}

	@Override
	public void requestBodyEnd(@NotNull Call call, long byteCount) {
		logger.debug(String.format("[%s] requestBodyEnd %dms", hash(call), System.currentTimeMillis() - startTime));
	}

	@Override
	public void responseHeadersStart(@NotNull Call call) {
		logger.debug(String.format("[%s] responseHeadersStart %dms", hash(call), System.currentTimeMillis() - startTime));
	}

	@Override
	public void responseHeadersEnd(@NotNull Call call, @NotNull Response response) {
		logger.debug(String.format("[%s] responseHeadersEnd %dms", hash(call), System.currentTimeMillis() - startTime));
	}

	@Override
	public void responseBodyStart(@NotNull Call call) {
		logger.debug(String.format("[%s] responseBodyStart %dms", hash(call), System.currentTimeMillis() - startTime));
	}

	@Override
	public void responseBodyEnd(@NotNull Call call, long byteCount) {
		logger.debug(String.format("[%s] responseBodyEnd %dms", hash(call), System.currentTimeMillis() - startTime));
	}
}
