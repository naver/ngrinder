package org.ngrinder.security;

import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.junit.Before;
import org.junit.Test;
import org.ngrinder.AbstractNGrinderTransactionalTest;
import org.ngrinder.infra.plugin.MockPluginManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;

public class NGrinderDefaultUserDetailsServiceTest extends AbstractNGrinderTransactionalTest {
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
						loadUserByUsername.getUserInfoProviderClass(), nullValue());
	}

	@Test
	public void testUnknownUser() {
		// if passing a unknown user,
		UserDetails user = null;
		try {
			user = userDetailsService.loadUserByUsername("unknown22");
		} catch (Exception e) {
			return;
		}
		fail("Exception should occus " + ToStringBuilder.reflectionToString(user));
	}
}
