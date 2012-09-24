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

import java.io.IOException;
import java.nio.charset.Charset;

import com.ibm.icu.text.CharsetDetector;
import com.ibm.icu.text.CharsetMatch;

/**
 * Encoding detection utility from byte array
 * 
 * @author JunHo Yoon
 * @since 3.0
 */
public abstract class EncodingUtil {

	public static String getAutoDecodedString(byte[] data, String defaultEncoding) throws IOException {
		return new String(data, detectEncoding(data, defaultEncoding));
	}

	/**
	 * 
	 * @param data
	 * @param defaultEncoding
	 * @return encoding name;
	 * @throws IOException
	 */
	public static String detectEncoding(byte[] data, String defaultEncoding) throws IOException {
		CharsetDetector detector = new CharsetDetector();
		detector.setText(data);
		CharsetMatch cm = detector.detect();
		String estimatedEncoding = cm.getName();
		boolean isReliable = Charset.isSupported(estimatedEncoding) && cm.getConfidence() >= 50;
		return isReliable ? estimatedEncoding : defaultEncoding;
	}
}
