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
import okhttp3.RequestBody;
import org.ngrinder.http.HTTPResponse;
import org.ngrinder.http.util.JsonUtils;

import java.util.Map;

import static org.ngrinder.http.HTTPRequest.DEFAULT_MEDIA_TYPE;
import static org.ngrinder.http.util.OkHTTPUtils.createRequestBody;

@SuppressWarnings("unused")
public interface HTTPRequestPatch {

	HTTPResponse PATCH(String url, RequestBody body, Headers headers);

	default HTTPResponse PATCH(String url) {
		return PATCH(url, "".getBytes(), Headers.of());
	}

	default HTTPResponse PATCH(String url, byte[] data) {
		return PATCH(url, data, Headers.of());
	}

	default HTTPResponse PATCH(String url, Map<?, ?> map) {
		return PATCH(url, map, Headers.of());
	}

	default HTTPResponse PATCH(String url, Map<?, ?> map, Headers headers) {
		RequestBody body = RequestBody.create(JsonUtils.serialize(map), DEFAULT_MEDIA_TYPE);
		return PATCH(url, body, headers);
	}

	default HTTPResponse PATCH(String url, byte[] data, Headers headers) {
		RequestBody body = createRequestBody(data, headers);
		return PATCH(url, body, headers);
	}

	default HTTPResponse PATCH(String url, RequestBody body) {
		return PATCH(url, body, Headers.of());
	}
}
