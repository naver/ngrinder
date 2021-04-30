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
package org.ngrinder.http.multipart;

import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.nio.AsyncEntityProducer;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class MultipartEntityBuilder {
	private final List<MultipartEntity> multipartEntities = new ArrayList<>();

	private MultipartEntityBuilder() {
	}

	public static MultipartEntityBuilder create() {
		return new MultipartEntityBuilder();
	}

	public AsyncEntityProducer build() {
		return new MultipartEntityProducer(multipartEntities);
	}

	public MultipartEntityBuilder addEntity(MultipartEntity multipartEntity) {
		multipartEntities.add(multipartEntity);
		return this;
	}

	public MultipartEntityBuilder addEntity(String name, CharSequence value, ContentType contentType) {
		multipartEntities.add(new BasicMultipartEntity(name, value, contentType));
		return this;
	}

	public MultipartEntityBuilder addEntity(String name, CharSequence value) {
		return addEntity(name, value, ContentType.TEXT_PLAIN);
	}

	public MultipartEntityBuilder addEntity(String name, File file, ContentType contentType) {
		multipartEntities.add(new FileMultipartEntity(name, file, contentType));
		return this;
	}

	public MultipartEntityBuilder addEntity(String name, File file) {
		return addEntity(name, file, ContentType.APPLICATION_OCTET_STREAM);
	}
}
