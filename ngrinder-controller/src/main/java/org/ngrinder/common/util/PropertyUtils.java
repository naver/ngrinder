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

import com.ibm.icu.text.CharsetDetector;
import com.ibm.icu.text.CharsetMatch;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.nio.charset.Charset;
import java.util.Properties;

import static org.ngrinder.common.util.ExceptionUtils.processException;

/**
 * Automatic encoding detection utility.
 *
 * @since 3.0
 */
public abstract class PropertyUtils {

	private static final int MINIMAL_CONFIDENCE_LEVEL = 70;

	/**
	 * Decode the byte array with auto encoding detection feature.
	 *
	 * @param data            byte array
	 * @param defaultEncoding the default encoding if no encoding is sure.
	 * @return decoded string
	 * @throws IOException occurs when the decoding is failed.
	 */
	public static String getAutoDecodedString(byte[] data, String defaultEncoding) throws IOException {
		return new String(data, detectEncoding(data, defaultEncoding));
	}

	/**
	 * Detect encoding of given data.
	 *
	 * @param data            byte array
	 * @param defaultEncoding the default encoding if no encoding is sure.
	 * @return encoding name detected encoding name
	 */
	public static String detectEncoding(byte[] data, String defaultEncoding) {
		CharsetDetector detector = new CharsetDetector();
		detector.setText(data);
		CharsetMatch cm = detector.detect();
		String estimatedEncoding = cm.getName();
		boolean isReliable = Charset.isSupported(estimatedEncoding) && cm.getConfidence() >= MINIMAL_CONFIDENCE_LEVEL;
		return isReliable ? estimatedEncoding : defaultEncoding;
	}

	public static Properties loadProperties(File file) {
		try {
			if (file.exists()) {
				byte[] propByte = FileUtils.readFileToByteArray(file);
				String propString = PropertyUtils.getAutoDecodedString(propByte, "UTF-8");
				Properties prop = new Properties();
				prop.load(new StringReader(propString));
				return prop;
			} else {
				// default empty properties.
				return new Properties();
			}
		} catch (IOException e) {
			throw processException("Fail to load property file " + file.getAbsolutePath(), e);
		}
	}
}
