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

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public class ThreadContextHTTPClient {
	private static final Map<HTTPRequest, Supplier<OkHttpClient>> supplierMap = new HashMap<>();
	private static final ThreadLocal<Map<HTTPRequest, OkHttpClient>> clientMapThreadLocal = ThreadLocal.withInitial(HashMap::new);

	public static void init(HTTPRequest httpRequest, Supplier<OkHttpClient> clientSupplier) {
		supplierMap.put(httpRequest, clientSupplier);
	}

	public static OkHttpClient of(HTTPRequest request) {
		OkHttpClient client = clientMapThreadLocal.get().get(request);
		if (client == null) {
			client = supplierMap.get(request).get();
			clientMapThreadLocal.get().put(request, client);
		}
		return client;
	}

	public static void reset() {
		Map<HTTPRequest, OkHttpClient> clientByHTTPRequest = clientMapThreadLocal.get();
		clientByHTTPRequest.values().forEach(client -> client.connectionPool().evictAll());
	}
}
