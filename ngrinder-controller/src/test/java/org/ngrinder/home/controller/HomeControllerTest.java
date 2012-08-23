package org.ngrinder.home.controller;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;
import org.ngrinder.AbstractNGrinderTransactionalTest;
import org.ngrinder.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.ui.ModelMap;
import org.springframework.web.servlet.DispatcherServlet;
import org.springframework.web.servlet.i18n.CookieLocaleResolver;

public class HomeControllerTest extends AbstractNGrinderTransactionalTest {

	@Autowired
	private HomeController homeController;

	@Test
	public void testHome() {
		MockHttpServletResponse res = new MockHttpServletResponse();
		MockHttpServletRequest req = new MockHttpServletRequest();
		CookieLocaleResolver localeResolver = new CookieLocaleResolver();
		req.setAttribute(DispatcherServlet.LOCALE_RESOLVER_ATTRIBUTE, localeResolver);
		User testUser2 = getTestUser();
		testUser2.setUserLanguage("EN");
		ModelMap model = new ModelMap();
		String viewName = homeController.home(testUser2, model, res, req);
		assertThat(viewName, is("index"));

	}

	@Test
	public void testLogin() {
		ModelMap model = new ModelMap();
		String viewName = homeController.login(model);
		assertThat(viewName, is("login"));
	}

	@Test
	public void testHelp() {
		ModelMap model = new ModelMap();
		String viewName = homeController.openHelp(model);
		assertThat(viewName, is("help"));
	}

	@Test
	public void testGetTimeZone() {
		ModelMap model = new ModelMap();
		String viewName = homeController.getAllTimeZone(model);
		assertThat(viewName, is("allTimeZone"));
	}


}
