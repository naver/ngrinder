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

import HTTPClient.NVPair;
import org.apache.hc.core5.http.Header;
import org.apache.hc.core5.http.NameValuePair;
import org.apache.hc.core5.http.message.BasicHeader;
import org.apache.hc.core5.http.message.BasicNameValuePair;
import org.apache.hc.core5.http.nio.AsyncEntityProducer;
import org.ngrinder.http.HTTPResponse;

import java.util.List;
import java.util.Map;

import static java.util.Collections.emptyList;
import static org.ngrinder.http.util.PairListConvertUtils.convert;

public interface HTTPPost {
	HTTPResponse POST(String uri, byte[] content, List<Header> headers);

	HTTPResponse POST(String uri, List<NameValuePair> params, List<Header> headers);

	HTTPResponse POST(String uri, Map<?, ?> params, List<Header> headers);

	HTTPResponse POST(String uri, AsyncEntityProducer asyncEntityProducer, List<Header> headers);

	default HTTPResponse POST(String uri) {
		return POST(uri, new byte[0], emptyList());
	}

	default HTTPResponse POST(String uri, Map<?, ?> params) {
		return POST(uri, params, emptyList());
	}

	default HTTPResponse POST(String uri, Map<?, ?> params, Map<String, String> headers) {
		return POST(uri, params, convert(headers, BasicHeader::new));
	}

	default HTTPResponse POST(String uri, NVPair[] params) {
		return POST(uri, convert(params, BasicNameValuePair::new), emptyList());
	}

	default HTTPResponse POST(String uri, NVPair[] params, NVPair[] headers) {
		return POST(uri, convert(params, BasicNameValuePair::new), convert(headers, BasicHeader::new));
	}

	default HTTPResponse POST(String uri, byte[] content) {
		return POST(uri, content, emptyList());
	}

	default HTTPResponse POST(String uri, byte[] content, Map<String, String> headers) {
		return POST(uri, content, convert(headers, BasicHeader::new));
	}

	default HTTPResponse POST(String uri, AsyncEntityProducer asyncEntityProducer) {
		return POST(uri, asyncEntityProducer, emptyList());
	}

	default HTTPResponse POST(String uri, AsyncEntityProducer asyncEntityProducer, Map<String, String> headers) {
		return POST(uri, asyncEntityProducer, convert(headers, BasicHeader::new));
	}
}
