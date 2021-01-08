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

import org.apache.hc.client5.http.async.methods.SimpleHttpResponse;
import org.apache.hc.core5.concurrent.FutureCallback;

public class SimpleFutureCallback implements FutureCallback<SimpleHttpResponse> {
	private SimpleFutureCallback() {
	}

	public static SimpleFutureCallback create() {
		return new SimpleFutureCallback();
	}

	@Override
	public void completed(SimpleHttpResponse response) {
		aggregate(response);
		summarize(response);
	}

	private void aggregate(SimpleHttpResponse response) {

	}

	private void summarize(SimpleHttpResponse response) {

	}

	@Override
	public void failed(Exception ex) {

	}

	@Override
	public void cancelled() {

	}
}
