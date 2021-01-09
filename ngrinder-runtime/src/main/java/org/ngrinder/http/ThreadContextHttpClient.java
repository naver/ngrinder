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

import org.apache.hc.client5.http.impl.async.CloseableHttpAsyncClient;
import org.apache.hc.client5.http.impl.async.HttpAsyncClients;
import org.apache.hc.client5.http.impl.nio.PoolingAsyncClientConnectionManager;

public class ThreadContextHttpClient {
	private static final ThreadLocal<CloseableHttpAsyncClient> clientThreadLocal = ThreadLocal.withInitial(() -> {
		CloseableHttpAsyncClient client = createHttpAsyncClient();
		client.start();

		return client;
	});

	private static CloseableHttpAsyncClient createHttpAsyncClient() {
		PoolingAsyncClientConnectionManager connectionManager = new ConnectionTimeAggregateConnectionManager();
		SummarizeRedirectStrategy summarizeRedirectStrategy = new SummarizeRedirectStrategy();

		return HttpAsyncClients.custom()
			.setRedirectStrategy(summarizeRedirectStrategy)
			.setConnectionManager(connectionManager)
			.build();
	}

	public static CloseableHttpAsyncClient get() {
		return clientThreadLocal.get();
	}
}
