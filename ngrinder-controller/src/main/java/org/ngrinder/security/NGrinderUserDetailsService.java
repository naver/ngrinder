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
package org.ngrinder.security;

import static org.ngrinder.common.util.Preconditions.checkNotEmpty;

import org.ngrinder.extension.OnLoginRunnable;
import org.ngrinder.infra.plugin.PluginManager;
import org.ngrinder.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * NGrinder {@link UserDetailsService}.
 *
 * This resolves user info using plugins implementing {@link OnLoginRunnable}.
 *
 * @author JunHo Yoon
 */
@Service("ngrinderUserDetailsService")
public class NGrinderUserDetailsService implements UserDetailsService {

	protected static final Logger LOG = LoggerFactory.getLogger(NGrinderUserDetailsService.class);

	@Autowired
	private PluginManager pluginManager;

	@Autowired
	private DefaultLoginPlugin defaultPlugin;

	@Override
	public UserDetails loadUserByUsername(String userId) {
		for (OnLoginRunnable each : getPluginManager().getEnabledModulesByClass(OnLoginRunnable.class, defaultPlugin)) {
			User user = each.loadUser(userId);
			if (user != null) {
				checkNotEmpty(user.getUserId(), "User info's userId provided by " + each.getClass().getName()
						+ " should not be empty");
				checkNotEmpty(user.getUserName(), "User info's userName provided by " + each.getClass().getName()
						+ " should not be empty");
				return new SecuredUser(user, user.getAuthProviderClass());
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
