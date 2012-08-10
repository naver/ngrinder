/*
 * Copyright (C) 2012 - 2012 NHN Corporation
 * All rights reserved.
 *
 * This file is part of The nGrinder software distribution. Refer to
 * the file LICENSE which is part of The nGrinder distribution for
 * licensing details. The nGrinder distribution is available on the
 * Internet at http://nhnopensource.org/ngrinder
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS
 * FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE
 * COPYRIGHT HOLDERS OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
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
 * @since
 */
public class EncodingUtilTest {

	/**
	 * Test method for {@link org.ngrinder.common.util.EncodingUtil#getAutoDecodedString(byte[], java.lang.String)}.
	 * @throws IOException 
	 */
	@Test
	public void testGetAutoDecodedString() throws IOException {
		String testStr = "12345678ikbsdfghjklsdfghjklzxcvbnm,.:LGF)(&^%^RYVG";
		String rtnEncode = EncodingUtil.detectEncoding(testStr.getBytes(), "UTF-8");
		assertThat(rtnEncode, is("UTF-8"));
	}
	
	@Test
	public void testGetAutoDecodedStringChinese() throws IOException {
		String testStr = "12345678ikbsdfghjklsd你好lzxcvbnm,.:LGF)(&^%^RYVG";
		String rtnEncode = EncodingUtil.detectEncoding(testStr.getBytes(), "UTF-8");
		assertThat(rtnEncode, is("UTF-8"));
	}

	@Test
	public void testDetectEncoding() throws IOException {
		String testStr = "12345678ikbsdfghjklsd你好lzxcvbnm,.:LGF)(&^%^RYVG";
		String rtnStr = EncodingUtil.getAutoDecodedString(testStr.getBytes(), "UTF-8");
		assertThat(rtnStr, is(testStr));
	}

}
