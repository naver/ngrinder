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
package org.ngrinder.script.service;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;
import org.ngrinder.common.exception.NGrinderRuntimeException;
import org.ngrinder.model.User;
import org.ngrinder.script.handler.JythonScriptHandler;

public class FileEntryServiceTest {

	private FileEntryService fileEntryService = new FileEntryService();

	@Test
	public void testFileTemplate() {
		User user = new User();
		user.setUserName("JunHo Yoon");
		String content = fileEntryService.loadTemplate(user, new JythonScriptHandler(), "http://helloworld/myname/is",
						"hello");
		assertThat(content, containsString("JunHo Yoon"));
		assertThat(content, containsString("http://helloworld/myname/is"));
	}

	@Test
	public void testFileNameFromUrl() {
		assertThat(fileEntryService.getPathFromUrl("http://helloworld"), is("helloworld"));
		assertThat(fileEntryService.getPathFromUrl("http://helloworld.com"), is("helloworld.com"));
		assertThat(fileEntryService.getPathFromUrl("http://helloworld.com/wewe.nhn"), is("helloworld.com/wewe.nhn"));
		assertThat(fileEntryService.getPathFromUrl("http://helloworld.com/wewe.nhn?wow=%dd"),
						is("helloworld.com/wewe.nhn"));

	}

	@Test
	public void testPathDivide() {
		String[] dividePathAndFile = fileEntryService.dividePathAndFile("helloworld.com/hello");
		assertThat(dividePathAndFile[0], is("helloworld.com"));
		assertThat(dividePathAndFile[1], is("hello"));

		dividePathAndFile = fileEntryService.dividePathAndFile("helloworld.com");
		assertThat(dividePathAndFile[0], is(""));
		assertThat(dividePathAndFile[1], is("helloworld.com"));
	}

	@Test(expected = NGrinderRuntimeException.class)
	public void testFileNameFromInvalidUrl() {
		fileEntryService.getPathFromUrl("htt22p://helloworld22");
	}

}
