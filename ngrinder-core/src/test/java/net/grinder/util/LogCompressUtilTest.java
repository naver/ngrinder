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
package net.grinder.util;

import org.apache.commons.io.FileUtils;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class LogCompressUtilTest {
	@Test
	public void testLogCompressDecompress() throws IOException {
		File file = new File(LogCompressUtilTest.class.getResource("/grinder1.properties").getFile());
		byte[] zippedContent = LogCompressUtils.compress(file);
		File createTempFile2 = File.createTempFile("a22aa", ".zip");
		createTempFile2.deleteOnExit();
		FileUtils.writeByteArrayToFile(createTempFile2, zippedContent);
		File createTempFile = File.createTempFile("a22", "tmp");
		LogCompressUtils.decompress(zippedContent, createTempFile);
		assertThat(createTempFile.exists(), is(true));
		byte[] unzippedContent = FileUtils.readFileToByteArray(createTempFile);
		assertThat(unzippedContent, is(FileUtils.readFileToByteArray(file)));
	}

}
