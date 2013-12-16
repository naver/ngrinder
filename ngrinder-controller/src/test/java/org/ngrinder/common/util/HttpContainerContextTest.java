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

import org.apache.commons.lang.StringUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.ngrinder.AbstractNGrinderTransactionalTest;
import org.ngrinder.infra.config.Config;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.web.servletapi.SecurityContextHolderAwareRequestWrapper;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import static org.junit.Assert.assertTrue;

/**
 * Class description.
 *
 * @author Mavlarn
 */
public class HttpContainerContextTest extends AbstractNGrinderTransactionalTest {

	@Autowired
	private HttpContainerContext httpContainerContext;

	@Autowired
	private Config config;

	@Before
	public void setMockContext() {
		MockHttpServletRequest req = new MockHttpServletRequest();
		req.addHeader("User-Agent", "Win");
		SecurityContextHolderAwareRequestWrapper reqWrapper = new SecurityContextHolderAwareRequestWrapper(req, "U");
		RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(reqWrapper));
	}

	@After
	public void resetContext() {
		RequestContextHolder.resetRequestAttributes();
	}

	@Test
	public void testGetCurrentRequestUrlFromUserRequest() {
		String requestUrl = httpContainerContext.getCurrentContextUrlFromUserRequest();
		assertTrue(requestUrl.startsWith("http://"));

		String httpUrl = config.getControllerProperties().getProperty(PROP_CONTROLLER_URL, "");
		if (StringUtils.isNotBlank(httpUrl)) {
			config.getControllerProperties().addProperty(PROP_CONTROLLER_URL, "");
		} else {
			config.getControllerProperties().addProperty(PROP_CONTROLLER_URL, "http://aa.com");
		}
		requestUrl = httpContainerContext.getCurrentContextUrlFromUserRequest();
		assertTrue(requestUrl.startsWith("http://"));

		// reset the system properties.
		config.getControllerProperties().addProperty(PROP_CONTROLLER_URL, requestUrl);
	}

	/**
	 * Test method for
	 * {@link org.ngrinder.common.util.HttpContainerContext#isUnixUser()}.
	 */
	@Test
	public void testIsUnixUser() {
		boolean isUnix = httpContainerContext.isUnixUser();
		assertTrue(!isUnix);
	}

}
