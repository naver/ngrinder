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
import static org.junit.Assert.assertThat;

import java.io.File;
import java.io.IOException;

import javax.servlet.http.HttpServletResponse;

import org.junit.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mock.web.MockHttpServletResponse;

/**
 * Class description.
 *
 * @author Mavlarn
 * @since
 */
public class FileDownloadUtilsTest {

	@Test
	public void testDownloadFileHttpServletResponseString() throws IOException {
		File downFile = new ClassPathResource("TEST_USER.zip").getFile();
		String filePath = downFile.getAbsolutePath();
		MockHttpServletResponse resp = new MockHttpServletResponse();
		FileDownloadUtils.downloadFile(resp, filePath);
		String lengthHeader = resp.getHeader("Content-Length");

		assertThat(lengthHeader, is(String.valueOf(downFile.length())));
	}
	
	@Test
	public void testDownloadNotExistFile() throws IOException {
		File downFile = null;
		HttpServletResponse resp = new MockHttpServletResponse();
		boolean result = FileDownloadUtils.downloadFile(resp, downFile);
		assertThat(result, is(false));
		
		downFile = new File("Not-existed-file");
		result = FileDownloadUtils.downloadFile(resp, downFile);
		assertThat(result, is(false));
	}

}
