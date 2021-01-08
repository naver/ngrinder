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
package org.ngrinder.http.consumer;

import org.apache.hc.client5.http.async.methods.SimpleHttpResponse;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.HttpException;
import org.apache.hc.core5.http.HttpResponse;
import org.apache.hc.core5.http.nio.AsyncEntityConsumer;
import org.apache.hc.core5.http.nio.support.AbstractAsyncResponseConsumer;
import org.apache.hc.core5.http.protocol.HttpContext;

import java.io.IOException;

public class PartialResponseConsumer extends AbstractAsyncResponseConsumer<SimpleHttpResponse, byte[]> {
	PartialResponseConsumer(final AsyncEntityConsumer<byte[]> entityConsumer) {
		super(entityConsumer);
	}

	public static PartialResponseConsumer create(int size) {
		return new PartialResponseConsumer(new PartialAsyncEntityConsumer(size));
	}

	@Override
	protected SimpleHttpResponse buildResult(HttpResponse response, byte[] entity, ContentType contentType) {
		final SimpleHttpResponse simpleResponse = SimpleHttpResponse.copy(response);
		if (entity != null) {
			simpleResponse.setBody(entity, contentType);
		}
		return simpleResponse;
	}

	@Override
	public void informationResponse(HttpResponse response, HttpContext context) throws HttpException, IOException {

	}
}
