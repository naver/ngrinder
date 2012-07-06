package org.ngrinder.security;

import org.ngrinder.infra.plugin.OnLoginRunnable;
import org.ngrinder.infra.plugin.PluginManager;
import org.ngrinder.user.model.SecuredUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service("ngrinderUserDetailsService")
public class NGrinderUserDetailsService implements UserDetailsService {

	@Autowired
	private PluginManager pluginManager;

	@Autowired
	private DefaultLoginPlugin defaultPlugin;

	@Override
	public UserDetails loadUserByUsername(String userId) throws UsernameNotFoundException, DataAccessException {
		for (OnLoginRunnable each : getPluginManager().getEnabledModulesByClass(OnLoginRunnable.class, defaultPlugin)) {
			SecuredUser user = each.loadUser(userId);
			if (user != null) {
				return user;
			}
		}
		throw new UsernameNotFoundException(userId + " is not found.");
	}

	public PluginManager getPluginManager() {
		return pluginManager;
	}

	public void setPluginManager(PluginManager pluginManager) {
		this.pluginManager = pluginManager;
	}
}
