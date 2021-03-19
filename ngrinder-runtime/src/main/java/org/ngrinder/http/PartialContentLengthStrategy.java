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

import org.apache.hc.core5.http.ContentLengthStrategy;
import org.apache.hc.core5.http.HttpException;
import org.apache.hc.core5.http.HttpMessage;
import org.apache.hc.core5.http.impl.DefaultContentLengthStrategy;

public class PartialContentLengthStrategy implements ContentLengthStrategy {
	private static final DefaultContentLengthStrategy DELEGATE = new DefaultContentLengthStrategy();

	private long readBytes = -1;

	public PartialContentLengthStrategy(long readBytes) {
		this.readBytes = readBytes;
	}

	@Override
	public long determineLength(HttpMessage message) throws HttpException {
		if (readBytes > -1) {
			return readBytes;    // And connection cannot be reused.
		}
		return DELEGATE.determineLength(message);
	}
}
