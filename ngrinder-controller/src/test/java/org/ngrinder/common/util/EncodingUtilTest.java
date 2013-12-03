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

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.*;

import java.io.IOException;

import org.junit.Test;

/**
 * Class description.
 *
 * @author Mavlarn
 */
public class EncodingUtilTest {

	/**
	 * Test method for {@link org.ngrinder.common.util.EncodingUtil#getAutoDecodedString(byte[], java.lang.String)}.
	 *
	 * @throws IOException
	 */
	@Test
	public void testGetAutoDecodedString() throws IOException {
		String testStr = "12345678ikbsdfghjklsdfghjklzxcvbnm,.:LGF)(&^%^RYVG";
		String rtnEncode = EncodingUtil.detectEncoding(testStr.getBytes("UTF-8"), "UTF-8");
		assertThat(rtnEncode, is("UTF-8"));
	}

	@Test
	public void testGetAutoDecodedStringChinese() throws IOException {
		String testStr = "12345678ikbsdfghjklsd你好lzxcvbnm,.:LGF)(&^%^RYVG";
		String rtnEncode = EncodingUtil.detectEncoding(testStr.getBytes("EUC-KR"), "EUC-KR");
		assertThat(rtnEncode, is("EUC-KR"));
	}

	@Test
	public void testDetectEncoding() throws IOException {
		String testStr = "12345678ikbsdfghjklsd你好lzxcvbnm,.:LGF)(&^%^RYVG";
		String rtnStr = EncodingUtil.getAutoDecodedString(testStr.getBytes("UTF-8"), "UTF-8");
		assertThat(rtnStr, is(testStr));
	}

}
