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
import org.apache.hc.core5.http.nio.DataStreamChannel;
import org.apache.hc.core5.http.nio.entity.BasicAsyncEntityProducer;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * A part of simple text for multipart form data entity.
 *
 * This entity writes multipart header to {@link DataStreamChannel} and
 * internally delegates {@link #produce(DataStreamChannel)} to {@link BasicAsyncEntityProducer}.
 */
public class BasicMultipartEntity implements MultipartEntity {
	private final String name;
	private final ContentType contentType;
	private final BasicAsyncEntityProducer basicEntityProducer;

	private final AtomicBoolean isBegin;
	private final String multipartHeader;

	private boolean remaining = true;

	public BasicMultipartEntity(String name, BasicAsyncEntityProducer basicEntityProducer, ContentType contentType) {
		this.name = name;
		this.contentType = contentType;
		this.basicEntityProducer = basicEntityProducer;

		this.isBegin = new AtomicBoolean(false);
		this.multipartHeader = generateMultipartHeader(name, contentType);
	}

	public BasicMultipartEntity(String name, CharSequence content, ContentType contentType) {
		this(name, new BasicAsyncEntityProducer(content, contentType), contentType);
	}

	public BasicMultipartEntity(String name, CharSequence entity) {
		this(name, entity, ContentType.TEXT_PLAIN);
	}

	public BasicMultipartEntity(String name, byte[] content, ContentType contentType) {
		this(name, new BasicAsyncEntityProducer(content, contentType), contentType);
	}

	public BasicMultipartEntity(String name, byte[] content) {
		this(name, content, ContentType.APPLICATION_OCTET_STREAM);
	}

	private String generateMultipartHeader(String name, ContentType contentType) {
		return "--" + BOUNDARY + NEWLINE
			+ CONTENT_DISPOSITION + name + "\"" + NEWLINE
			+ CONTENT_TYPE + contentType + NEWLINE + NEWLINE;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public void produce(DataStreamChannel channel) throws IOException {
		if (isBegin.compareAndSet(false, true)) {
			channel.write(ByteBuffer.wrap(multipartHeader.getBytes()));
		}

		DelegateDataStreamChannel delegateChannel = DelegateDataStreamChannel.from(channel);
		basicEntityProducer.produce(delegateChannel);

		if (delegateChannel.isEnd()) {
			channel.write(ByteBuffer.wrap(NEWLINE.getBytes()));
			remaining = false;
		}
	}

	@Override
	public ContentType getContentType() {
		return contentType;
	}

	@Override
	public boolean hasRemaining() {
		return remaining;
	}

	@Override
	public long getContentLength() {
		return basicEntityProducer.getContentLength() + multipartHeader.length();
	}

	@Override
	public void releaseResources() {
		basicEntityProducer.releaseResources();
	}
}
