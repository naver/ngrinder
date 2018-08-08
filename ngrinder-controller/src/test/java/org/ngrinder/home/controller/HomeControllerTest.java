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
package org.ngrinder.home.controller;

import org.junit.Test;
import org.mockito.Mockito;
import org.ngrinder.AbstractNGrinderTransactionalTest;
import org.ngrinder.model.Role;
import org.ngrinder.model.User;
import org.ngrinder.region.service.RegionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.ui.ModelMap;
import org.springframework.web.servlet.DispatcherServlet;
import org.springframework.web.servlet.i18n.CookieLocaleResolver;
import org.springframework.web.servlet.mvc.support.RedirectAttributesModelMap;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

public class HomeControllerTest extends AbstractNGrinderTransactionalTest {

	@Autowired
	private HomeController homeController;

	@Autowired
	private RegionService regionService;

	@Test
	public void testHome() {
		MockHttpServletResponse res = new MockHttpServletResponse();
		MockHttpServletRequest req = new MockHttpServletRequest();
		CookieLocaleResolver localeResolver = new CookieLocaleResolver();
		req.setAttribute(DispatcherServlet.LOCALE_RESOLVER_ATTRIBUTE, localeResolver);
		User testUser2 = getTestUser();
		testUser2.setUserLanguage("EN");
		ModelMap model = new ModelMap();
		String viewName = homeController.home(testUser2, null, null, model, res, req);
		assertThat(viewName, is("index"));

		User testUserError = Mockito.spy(testUser2);
		Mockito.when(testUserError.getRole()).thenReturn(Role.SYSTEM_USER);
		viewName = homeController.home(testUserError, "Test Error message!", null, model, res, req);
		assertThat(viewName, is("login"));

	}

	@Test
	public void testHealthCheck() {
		MockHttpServletResponse res = new MockHttpServletResponse();
		homeController.healthCheck(res);
		HttpEntity<String> message = homeController.healthCheckSlowly(500, res);
		assertThat(message.getBody(), containsString(regionService.getCurrent()));
	}

	@Test
	public void testLogin() {
		ModelMap model = new ModelMap();
		String viewName = homeController.login(model);
		assertThat(viewName, is("redirect:/"));
	}


	@Test
	public void testErrorPage() {
		String viewName = homeController.error404(new ModelMap());
		assertThat(viewName, startsWith("redirect:/doError"));

		MockHttpServletResponse res = new MockHttpServletResponse();
		MockHttpServletRequest req = new MockHttpServletRequest();
		RedirectAttributesModelMap model = new RedirectAttributesModelMap();
		viewName = homeController.second(getTestUser(), model, res, req);
		assertThat(viewName, is("index"));
	}

}
