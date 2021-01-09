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
import org.apache.hc.client5.http.impl.nio.PoolingAsyncClientConnectionManager;
import org.apache.hc.client5.http.nio.AsyncConnectionEndpoint;
import org.apache.hc.core5.concurrent.FutureCallback;
import org.apache.hc.core5.http.protocol.HttpContext;
import org.apache.hc.core5.reactor.ConnectionInitiator;
import org.apache.hc.core5.util.Timeout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Future;

public class ConnectionTimeAggregateConnectionManager extends PoolingAsyncClientConnectionManager {
	private static final Logger LOGGER = LoggerFactory.getLogger(ConnectionTimeAggregateConnectionManager.class);

	@Override
	public Future<AsyncConnectionEndpoint> connect(AsyncConnectionEndpoint endpoint, ConnectionInitiator connectionInitiator, Timeout connectTimeout, Object attachment, HttpContext context, FutureCallback<AsyncConnectionEndpoint> callback) {
		long start = System.currentTimeMillis();
		Future<AsyncConnectionEndpoint> future = super.connect(endpoint, connectionInitiator, connectTimeout, attachment, context, callback);
		long end = System.currentTimeMillis();

		try {
			if (getStatistics().isTestInProgress()) {
				Statistics.StatisticsForTest statisticsForTest = getStatistics().getForCurrentTest();
				statisticsForTest.addLong(StatisticsIndexMap.HTTP_PLUGIN_CONNECT_TIME_KEY, end - start);
			}
		} catch (Exception e) {
			LOGGER.error("Fail to aggregate connect time statistics", e);
		}

		return future;
	}

	private static Statistics getStatistics() {
		return HTTPPlugin.getPlugin()
			.getPluginProcessContext()
			.getScriptContext()
			.getStatistics();
	}
}
