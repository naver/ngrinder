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
package org.ngrinder.http.method;

import okhttp3.Headers;
import org.ngrinder.http.HTTPResponse;

import java.util.HashMap;
import java.util.Map;

public interface HTTPRequestGet {
	HTTPResponse GET(String url, Map<?, ?> map, Headers headers);

	default HTTPResponse GET(String url) {
		return GET(url, Headers.of());
	}

	default HTTPResponse GET(String url, Headers headers) {
		return GET(url, new HashMap<>(), headers);
	}

	default HTTPResponse GET(String url, Map<?, ?> map) {
		return GET(url, map, Headers.of());
	}
}
