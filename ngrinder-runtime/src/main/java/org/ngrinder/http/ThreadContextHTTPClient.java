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

import okhttp3.OkHttpClient;

import java.util.function.Supplier;

public class ThreadContextHTTPClient {
	private static ThreadLocal<OkHttpClient> okHttpClientThreadLocal;

	public static void init(Supplier<OkHttpClient> clientSupplier) {
		okHttpClientThreadLocal = ThreadLocal.withInitial(clientSupplier);
	}

	public static OkHttpClient get() {
		if (okHttpClientThreadLocal == null) {
			throw new RuntimeException("Cannot find http client on current thread context. You may need to initialize HTTPRequest first.");
		}

		return okHttpClientThreadLocal.get();
	}

	public static void reset() {
		if (okHttpClientThreadLocal == null) {
			return;
		}

		okHttpClientThreadLocal.get()
			.connectionPool()
			.evictAll();
	}
}
