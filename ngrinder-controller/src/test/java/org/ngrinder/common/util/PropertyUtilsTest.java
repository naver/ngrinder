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

import org.junit.Test;

import java.io.IOException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class PropertyUtilsTest {

	/**
	 * Test method for {@link PropertyUtils#getAutoDecodedString(byte[], java.lang.String)}.
	 *
	 * @throws IOException
	 */
	@Test
	public void testGetAutoDecodedString() throws IOException {
		String testStr = "12345678ikbsdfghjklsdfghjklzxcvbnm,.:LGF)(&^%^RYVG";
		String rtnEncode = PropertyUtils.detectEncoding(testStr.getBytes("UTF-8"), "UTF-8");
		assertThat(rtnEncode, is("UTF-8"));
	}

	@Test
	public void testGetAutoDecodedStringChinese() throws IOException {
		String testStr = "12345678ikbsdfghjklsd你好lzxcvbnm,.:LGF)(&^%^RYVG";
		String rtnEncode = PropertyUtils.detectEncoding(testStr.getBytes("EUC-KR"), "EUC-KR");
		assertThat(rtnEncode, is("EUC-KR"));
	}

	@Test
	public void testDetectEncoding() throws IOException {
		String testStr = "12345678ikbsdfghjklsd你好lzxcvbnm,.:LGF)(&^%^RYVG";
		String rtnStr = PropertyUtils.getAutoDecodedString(testStr.getBytes("UTF-8"), "UTF-8");
		assertThat(rtnStr, is(testStr));
	}

}
