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

import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.Header;
import org.apache.hc.core5.http.NameValuePair;
import org.apache.hc.core5.http.message.BasicHeader;
import org.apache.hc.core5.http.message.BasicNameValuePair;
import org.ngrinder.http.HTTPResponse;

import java.util.List;
import java.util.Map;

import static java.util.Collections.emptyList;
import static org.ngrinder.http.util.ContentTypeUtils.getContentType;
import static org.ngrinder.http.util.JsonUtils.toJson;
import static org.ngrinder.http.util.MapToPairListConvertUtils.convert;
import static org.ngrinder.http.util.TypeConvertUtils.cast;

public interface HTTPPatch {
	HTTPResponse PATCH(String uri, byte[] content, List<Header> headers);

	HTTPResponse PATCH(String uri, List<NameValuePair> params, List<Header> headers);

	default HTTPResponse PATCH(String uri) {
		return PATCH(uri, new byte[0], emptyList());
	}

	default HTTPResponse PATCH(String uri, Map<?, ?> params) {
		return PATCH(uri, toJson(params).getBytes(), emptyList());
	}

	default HTTPResponse PATCH(String uri, Map<?, ?> params, Map<String, String> headers) {
		List<Header> headerList = convert(headers, BasicHeader::new);

		if (getContentType(headerList).isSameMimeType(ContentType.APPLICATION_JSON)) {
			return PATCH(uri, toJson(params).getBytes(), headers);
		}
		return PATCH(uri, convert(cast(params), BasicNameValuePair::new), headerList);
	}

	default HTTPResponse PATCH(String uri, byte[] content) {
		return PATCH(uri, content, emptyList());
	}

	default HTTPResponse PATCH(String uri, byte[] content, Map<String, String> headers) {
		return PATCH(uri, content, convert(headers, BasicHeader::new));
	}
}
