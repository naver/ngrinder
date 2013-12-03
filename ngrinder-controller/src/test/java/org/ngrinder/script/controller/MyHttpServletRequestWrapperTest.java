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
package org.ngrinder.script.controller;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;

import javax.servlet.http.HttpServletRequest;

import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;

public class MyHttpServletRequestWrapperTest {

	MyHttpServletRequestWrapper wrapper;

	@Test
	public void testHandleRequest() {
		HttpServletRequest req = new MockHttpServletRequest("GET", "http://127.0.0.1:80/hello/svn/admin/한글");
		wrapper = new MyHttpServletRequestWrapper(req) {
			public String getRequestURI() {
				return "/hello/svn/admin/한글";
			}

			@Override
			public String getContextPath() {
				return "/hello";
			}
		};
		String path = wrapper.getPathInfo();
		assertThat(path, is("/admin/%ED%95%9C%EA%B8%80"));
	}

	@Test
	public void testHandleRequest3() throws UnsupportedEncodingException {
		String testURI = "/hello/svnadmin/admin/한글";
		testURI = URLEncoder.encode(testURI, "UTF-8");

		HttpServletRequest req = new MockHttpServletRequest("GET", testURI);
		wrapper = new MyHttpServletRequestWrapper(req) {
			public String getRequestURI() {
				return "/hello/svn/admin/한글";
			}

			@Override
			public String getContextPath() {
				return "/hello";
			}
		};
		String path = wrapper.getPathInfo();
		assertThat(path, is("/admin/%ED%95%9C%EA%B8%80"));
	}

	@Test
	public void testHandleRequest2() {
		HttpServletRequest req = new MockHttpServletRequest("GET", "http://127.0.0.1:80/hello/svnadmin/admin");
		wrapper = new MyHttpServletRequestWrapper(req) {
			public String getRequestURI() {
				return "/hello/svnadmin/admin";
			}

			@Override
			public String getContextPath() {
				return "/hello";
			}
		};
		String path = wrapper.getPathInfo();
		assertThat(path, is("admin/admin"));
	}
}
