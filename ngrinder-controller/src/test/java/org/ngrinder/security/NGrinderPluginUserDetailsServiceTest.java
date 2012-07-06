package org.ngrinder.security;

import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;

import org.junit.Before;
import org.junit.Test;
import org.ngrinder.NGrinderIocTransactionalTestBase;
import org.ngrinder.infra.plugin.OnLoginRunnable;
import org.ngrinder.infra.plugin.PluginManager;
import org.ngrinder.user.model.Role;
import org.ngrinder.user.model.SecuredUser;
import org.ngrinder.user.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

public class NGrinderPluginUserDetailsServiceTest extends NGrinderIocTransactionalTestBase {

	@Autowired
	public NGrinderAuthenticationProvider authProvider;
	PluginManager manager;

	@Autowired
	private DefaultLoginPlugin defaultLoginPlugin;

	@Autowired
	private NGrinderUserDetailsService ngrinderUserDetailsService;

	MockLoginPlugin mockLoginPlugin;

	@Before
	public void before() {
		mockLoginPlugin = mock(MockLoginPlugin.class);
		manager = mock(PluginManager.class);
		authProvider.setPluginManager(manager);
		ngrinderUserDetailsService.setPluginManager(manager);
	}

	@SuppressWarnings({ "unchecked", "serial" })
	@Test
	public void testSecondAuth() {
		// Given that there is two plugins exist
		Authentication auth = mock(UsernamePasswordAuthenticationToken.class);
		authProvider = spy(authProvider);

		when(auth.getPrincipal()).thenReturn("hello");
		when(auth.getCredentials()).thenReturn("world");

		when(manager.getEnabledModulesByClass(any(OnLoginRunnable.class.getClass()), any(OnLoginRunnable.class)))
				.thenReturn(new ArrayList<OnLoginRunnable>() {
					{
						add(defaultLoginPlugin);
						add(mockLoginPlugin);
					}
				});

		// When user is return by plugin module.
		User user = new User();
		user.setUserName("hello");
		user.setUserId("hello");
		user.setRole(new Role("S"));
		when(mockLoginPlugin.loadUser(anyString())).thenReturn(
				new SecuredUser(user, mockLoginPlugin.getClass().getName()));
		when(mockLoginPlugin.authUser(any(), anyString(), anyString(), anyString(), any())).thenReturn(true);

		// Then, Auth should be succeeded.
		assertThat(authProvider.authenticate(auth), notNullValue());

		// And should be inserted into DB
		verify(authProvider, times(1)).addNewUserInfoLocal(any(SecuredUser.class));

		reset(authProvider);
		// Then, Auth should be succeeded.
		assertThat(authProvider.authenticate(auth), notNullValue());

		// And should be inserted into DB
		
		verify(authProvider, times(0)).addNewUserInfoLocal(any(SecuredUser.class));

	}

	@Test(expected = UsernameNotFoundException.class)
	public void testUnknownUser() {
	}
}
