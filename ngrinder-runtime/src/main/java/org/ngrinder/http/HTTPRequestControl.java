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

public class HTTPRequestControl {
	private static long callTimeout;
	private static long connectTimeout;
	private static long readTimeout;
	private static long writeTimeout;

	public static long getCallTimeout() {
		return callTimeout;
	}

	public static void setCallTimeout(long callTimeout) {
		HTTPRequestControl.callTimeout = callTimeout;
	}

	public static long getConnectTimeout() {
		return connectTimeout;
	}

	public static void setConnectTimeout(long connectTimeout) {
		HTTPRequestControl.connectTimeout = connectTimeout;
	}

	public static long getReadTimeout() {
		return readTimeout;
	}

	public static void setReadTimeout(long readTimeout) {
		HTTPRequestControl.readTimeout = readTimeout;
	}

	public static long getWriteTimeout() {
		return writeTimeout;
	}

	public static void setWriteTimeout(long writeTimeout) {
		HTTPRequestControl.writeTimeout = writeTimeout;
	}
}
