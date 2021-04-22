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

import org.apache.hc.core5.http.Header;
import org.apache.hc.core5.http.nio.DataStreamChannel;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.List;

/**
 * This DataStreamChannel implementation delegates all the actions
 * except the {@link #endStream()} and {@link #endStream(List)} methods.
 * And let you know the end of delegated channel with the {@link #isEnd()} method.
 */
public class DelegateDataStreamChannel implements DataStreamChannel {

	private final DataStreamChannel delegate;
	private boolean end;

	private DelegateDataStreamChannel(DataStreamChannel delegate) {
		this.delegate = delegate;
	}

	public static DelegateDataStreamChannel from(DataStreamChannel channel) {
		return new DelegateDataStreamChannel(channel);
	}

	@Override
	public void requestOutput() {
		delegate.requestOutput();
	}

	@Override
	public int write(ByteBuffer src) throws IOException {
		return delegate.write(src);
	}

	@Override
	public void endStream() throws IOException {
		end = true;
	}

	@Override
	public void endStream(List<? extends Header> trailers) throws IOException {
		end = true;
	}

	public boolean isEnd() {
		return end;
	}
}
