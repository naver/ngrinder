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
import okhttp3.Call;
import okhttp3.Connection;
import okhttp3.EventListener;
import okhttp3.Protocol;
import org.apache.commons.lang.time.StopWatch;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.util.List;

public class ConnectionTimeAggregateListener extends EventListener {
	private static final Logger LOGGER = LoggerFactory.getLogger(ConnectionTimeAggregateListener.class);
	public static final Factory FACTORY = new Factory() {
		@NotNull
		@Override
		public EventListener create(@NotNull Call call) {
			return new ConnectionTimeAggregateListener();
		}
	};

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

	@Override
	public void connectionReleased(@NotNull Call call, @NotNull Connection connection) {
		timeToFirstByteStopWatch.reset();
		// connectionReleased can be occurred immediately without responseHeaderStart event
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
