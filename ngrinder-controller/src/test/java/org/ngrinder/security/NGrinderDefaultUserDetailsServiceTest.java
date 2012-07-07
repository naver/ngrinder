package org.ngrinder.security;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;

import org.junit.Before;
import org.junit.Test;
import org.ngrinder.AbstractNGNinderTransactionalTest;
import org.ngrinder.infra.plugin.MockPluginManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

public class NGrinderDefaultUserDetailsServiceTest extends AbstractNGNinderTransactionalTest {
	@Autowired
	public MockPluginManager mockPluginManager;

	@Autowired
	public NGrinderUserDetailsService userDetailsService;

	@Before
	public void before() {
		mockPluginManager.initPluginFramework();
	}

	@Test
	public void testPluginAuth() {
		// if passing a default user, admin,
		SecuredUser loadUserByUsername = (SecuredUser) userDetailsService.loadUserByUsername("admin");
		// Default should be resolved by DefaultLoginPlugins
		assertThat("admin should not be null", loadUserByUsername, notNullValue());
		assertThat("default user(admin) should be retrieved from DefaultLoginPlugin",
				loadUserByUsername.getUserInfoProviderClass(), is(DefaultLoginPlugin.class.getName()));
	}

	@Test(expected = UsernameNotFoundException.class)
	public void testUnknownUser() {
		// if passing a unknown user,

		userDetailsService.loadUserByUsername("unknown");
		// It should throw out the Exception
	}
}
