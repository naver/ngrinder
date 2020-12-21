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

import org.junit.Test;
import org.ngrinder.common.exception.NGrinderRuntimeException;
import org.ngrinder.model.User;
import org.ngrinder.script.handler.JythonScriptHandler;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;

public class FileEntryServiceTest {

	private FileEntryService fileEntryService = new FileEntryService(null, null, null, null);

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
		assertThat(content, containsString("headers = Headers.of("));
		assertThat(content, containsString("\"header\", \"123\","));
		assertThat(content, containsString(")"));
		assertThat(content, containsString("params = {"));
		assertThat(content, containsString("\"pName\": \"pValue\","));
		assertThat(content, containsString("}"));
		assertThat(content, containsString("Cookie.Builder()"));
		assertThat(content, containsString(".name(\"cook\")"));
		assertThat(content, containsString(".value(\"good\")"));
		assertThat(content, containsString(".domain(\"naver.com\")"));
		assertThat(content, containsString(".path(\"/home\")"));
		assertThat(content, containsString(".build(),"));
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
