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
import org.apache.hc.core5.http.nio.DataStreamChannel;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicReference;

import static org.ngrinder.http.multipart.MultipartEntity.BOUNDARY;
import static org.ngrinder.http.multipart.MultipartEntity.NEWLINE;

public class MultipartEntityProducer implements AsyncEntityProducer {
	private static final String BOUNDARY_END = "--" + BOUNDARY + "--" + NEWLINE;

	private final List<MultipartEntity> multipartEntities;
	private final ContentType contentType;
	private final long length;

	private final ConcurrentLinkedQueue<MultipartEntity> entityQueue;
	private MultipartEntity currentEntity;
	private final AtomicReference<Exception> exception;

	public MultipartEntityProducer(List<MultipartEntity> multipartEntities) {
		this(multipartEntities, ContentType.MULTIPART_FORM_DATA);
	}

	public MultipartEntityProducer(List<MultipartEntity> multipartEntities, ContentType contentType) {
		this.multipartEntities = multipartEntities;
		this.contentType = contentType;
		this.length = calculateLength();

		this.entityQueue = new ConcurrentLinkedQueue<>(multipartEntities);
		this.exception = new AtomicReference<>(null);
		this.currentEntity = entityQueue.remove();
	}

	private long calculateLength() {
		long totalContentLength = multipartEntities.stream()
			.map(MultipartEntity::getContentLength)
			.reduce(Long::sum)
			.orElse(0L);
		long newLineEachEntity = (long) multipartEntities.size() * NEWLINE.length();
		long lastBoundary = BOUNDARY_END.length();

		return totalContentLength + newLineEachEntity + lastBoundary;
	}

	@Override
	public boolean isRepeatable() {
		return true;
	}

	@Override
	public void failed(Exception cause) {
		if (exception.compareAndSet(null, cause)) {
			releaseResources();
		}
	}

	@Override
	public long getContentLength() {
		return length;
	}

	@Override
	public String getContentType() {
		return withBoundary(contentType);
	}

	private String withBoundary(ContentType contentType) {
		return contentType + "; boundary=" + BOUNDARY;
	}

	@Override
	public String getContentEncoding() {
		return null;
	}

	@Override
	public boolean isChunked() {
		return false;
	}

	@Override
	public Set<String> getTrailerNames() {
		return null;
	}

	@Override
	public int available() {
		return Integer.MAX_VALUE;
	}

	@Override
	public void produce(DataStreamChannel channel) throws IOException {
		if (currentEntity.hasRemaining()) {
			currentEntity.produce(channel);
		}

		if (!currentEntity.hasRemaining()) {
			currentEntity = entityQueue.poll();
		}

		if (currentEntity == null) {
			channel.write(ByteBuffer.wrap(BOUNDARY_END.getBytes()));
			channel.endStream();
		}
	}

	@Override
	public void releaseResources() {
		multipartEntities.forEach(MultipartEntity::releaseResources);
	}
}
