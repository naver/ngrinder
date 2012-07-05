package org.ngrinder.security;

import org.ngrinder.infra.plugin.OnLoginRunnable;
import org.ngrinder.infra.plugin.PluginManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;

public class NGrinderAuthenticationProvider implements AuthenticationProvider {

	@Autowired
	private PluginManager pluginManager;

	@Override
	public Authentication authenticate(Authentication authentication) throws AuthenticationException {
		for (OnLoginRunnable each : pluginManager.getEnabledModulesByClass(OnLoginRunnable.class)) {
			UserDetails userDetails = each.auth(authentication.getPrincipal(), authentication.getCredentials());
		}
		
		return null;
	}

	@Override
	public boolean supports(Class<?> authentication) {
		return true;
	}

}
