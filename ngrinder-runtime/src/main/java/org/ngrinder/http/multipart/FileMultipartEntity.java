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
import org.apache.hc.core5.http.nio.entity.FileEntityProducer;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * A part of file for multipart form data entity.
 * <p>
 * This entity writes multipart header to {@link DataStreamChannel} and
 * internally delegates {@link #produce(DataStreamChannel)} to {@link FileEntityProducer}.
 */
public class FileMultipartEntity implements MultipartEntity {
	private final String name;
	private final ContentType contentType;
	private final FileEntityProducer fileEntityProducer;

	private final AtomicBoolean isBegin;
	private final String multipartHeader;

	private boolean remaining = true;

	public FileMultipartEntity(String name, File file, ContentType contentType) {
		this.name = name;
		this.contentType = contentType;

		this.fileEntityProducer = new FileEntityProducer(file, contentType);

		this.isBegin = new AtomicBoolean(false);
		this.multipartHeader = generateMultipartHeader(name, file.getName(), contentType);
	}

	public FileMultipartEntity(String name, File file) {
		this(name, file, ContentType.APPLICATION_OCTET_STREAM);
	}

	private String generateMultipartHeader(String name, String fileName, ContentType contentType) {
		return "--" + BOUNDARY + NEWLINE +
			CONTENT_DISPOSITION + name + FILENAME + fileName + "\"" + NEWLINE +
			CONTENT_TYPE + contentType + NEWLINE + NEWLINE;
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
		fileEntityProducer.produce(delegateChannel);

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
		return fileEntityProducer.getContentLength() + multipartHeader.length();
	}

	@Override
	public void releaseResources() {
		fileEntityProducer.releaseResources();
	}
}
