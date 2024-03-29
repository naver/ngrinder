/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ngrinder.common.util;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.ngrinder.common.util.ExceptionUtils.processException;

/**
 * Automatic encoding detection utility.
 *
 * @since 3.5.0
 */
public abstract class EncodingUtils {
	/**
	 * Encode the given path with UTF-8.
	 * "/" is not encoded.
	 *
	 * @param path path
	 * @return encoded path
	 */
	public static String encodePathWithUTF8(String path) {
		try {
			StringBuilder result = new StringBuilder();
			for (char each : path.toCharArray()) {
				if (each == '/') {
					result.append("/");
				} else {
					result.append(URLEncoder.encode(String.valueOf(each), "UTF-8"));
				}
			}
			return result.toString();
		} catch (UnsupportedEncodingException e) {
			throw processException(e);
		}
	}

	/**
	 * Decode the given path with UTF-8.
	 *
	 * @param path path
	 * @return decoded path
	 */
	public static String decodePathWithUTF8(String path) {
		try {
			return URLDecoder.decode(path, UTF_8.name());
		} catch (UnsupportedEncodingException e) {
			throw processException(e);
		}
	}
}
