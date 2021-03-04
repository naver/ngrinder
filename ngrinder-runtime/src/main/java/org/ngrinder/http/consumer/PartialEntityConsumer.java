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

import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.HttpException;
import org.apache.hc.core5.http.nio.entity.AbstractBinAsyncEntityConsumer;
import org.apache.hc.core5.util.ByteArrayBuffer;

import java.io.IOException;
import java.nio.ByteBuffer;

public class PartialEntityConsumer extends AbstractBinAsyncEntityConsumer<byte[]> {
	private final int size;
	private final ByteArrayBuffer buffer;

	public PartialEntityConsumer(int size) {
		this.size = size;
		this.buffer = new ByteArrayBuffer(size);
	}

	@Override
	protected void streamStart(ContentType contentType) throws HttpException, IOException {
	}

	@Override
	protected byte[] generateContent() throws IOException {
		return buffer.toByteArray();
	}

	@Override
	protected int capacityIncrement() {
		return Integer.MAX_VALUE;
	}

	@Override
	protected void data(ByteBuffer src, boolean endOfStream) throws IOException {
		if (src == null) {
			return;
		}
		if (buffer.isEmpty()) {
			if (src.hasArray()) {
				buffer.append(src.array(), src.arrayOffset() + src.position(), size);
			} else {
				while (src.hasRemaining() && !buffer.isFull()) {
					buffer.append(src.get());
				}
			}
		}
	}

	@Override
	public void releaseResources() {
		buffer.clear();
	}
}
