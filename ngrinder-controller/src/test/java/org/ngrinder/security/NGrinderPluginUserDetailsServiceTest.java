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
import org.ngrinder.AbstractNGrinderTransactionalTest;
import org.ngrinder.extension.OnLoginRunnable;
import org.ngrinder.infra.plugin.PluginManager;
import org.ngrinder.model.Role;
import org.ngrinder.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

public class NGrinderPluginUserDetailsServiceTest extends AbstractNGrinderTransactionalTest {

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
		// Given that there exists two plugins.
		Authentication auth = mock(UsernamePasswordAuthenticationToken.class);
		authProvider = spy(authProvider);

		when(auth.getPrincipal()).thenReturn("hello");
		when(auth.getName()).thenReturn("hello");
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
		user.setEmail("helloworld@gmail.com");
		user.setRole(Role.SUPER_USER);
		when(mockLoginPlugin.loadUser(anyString())).thenReturn(user);
		when(mockLoginPlugin.validateUser(anyString(), anyString(), anyString(), any(), any())).thenReturn(true);

		// Then, Auth should be succeeded.
		assertThat(authProvider.authenticate(auth), notNullValue());

		// And should be inserted into DB
		// verify(authProvider, times(1)).addNewUserIntoLocal(any(SecuredUser.class));

		reset(authProvider);
		when(mockLoginPlugin.loadUser("hello")).thenReturn(user);
		// Then, Auth should be succeeded.
		assertThat(authProvider.authenticate(auth), notNullValue());

		// And should not be inserted into DB
		// verify(authProvider, times(0)).addNewUserIntoLocal(any(SecuredUser.class));

	}

}
