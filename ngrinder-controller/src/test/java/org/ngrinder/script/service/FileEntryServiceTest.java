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
	public void testFileTemplateWithoutOptions() {
		User user = new User();
		user.setUserName("JunHo Yoon");
		String content = fileEntryService.loadTemplate(user, new JythonScriptHandler(), "http://helloworld/myname/is",
						"hello", null);
		assertThat(content, containsString("JunHo Yoon"));
		assertThat(content, containsString("http://helloworld/myname/is"));
	}

	@Test
	public void testFileTemplateWithOptions() {
		String options = "{\"method\":\"POST_TEST\"," +
			"\"headers\":[{\"name\":\"header\",\"value\":\"123\"}," +
			"{\"name\":\"auth\",\"value\":\"no\"}]," +
			"\"params\":[{\"name\":\"pName\",\"value\":\"pValue\"}]," +
			"\"cookies\":[{\"name\":\"cook\",\"value\":\"good\",\"domain\":\"naver.com\",\"path\":\"/home\"}]}";
		User user = new User();
		user.setUserName("Gisoo Gwon");
		String content = fileEntryService.loadTemplate(user, new JythonScriptHandler(), "http://helloworld/myname/is",
						"hello", options);
		System.out.println(content);
		assertThat(content, containsString("Gisoo Gwon"));
		assertThat(content, containsString("http://helloworld/myname/is"));
		assertThat(content, containsString("request1.POST_TEST"));
		assertThat(content, containsString("headers.append(NVPair(\"header\", \"123\"))"));
		assertThat(content, containsString("params.append(NVPair(\"pName\", \"pValue\"))"));
		assertThat(content, containsString("Cookie(\"cook\", \"good\", \"naver.com\", \"/home\""));
	}

	@Test
	public void testFileNameFromUrl() {
		assertThat(fileEntryService.getPathFromUrl("http://helloworld/wow;wow"), is("helloworld/wow_wow"));
		assertThat(fileEntryService.getPathFromUrl("http://hellowor%d/222$wewe"), is("hellowor_d/222_wewe"));
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
