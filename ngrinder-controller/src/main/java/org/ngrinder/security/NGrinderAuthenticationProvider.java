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

import org.ngrinder.extension.OnLoginRunnable;
import org.ngrinder.infra.plugin.PluginManager;
import org.ngrinder.model.Role;
import org.ngrinder.model.User;
import org.ngrinder.user.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.dao.AbstractUserDetailsAuthenticationProvider;
import org.springframework.security.authentication.dao.SaltSource;
import org.springframework.security.authentication.encoding.ShaPasswordEncoder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import java.util.Date;

/**
 * nGrinder UserDetailsAuthenticationProvider.
 * 
 * This class validates the user provided ID / Password from login page. Internally it uses the plugins implementing
 * {@link OnLoginRunnable}. If you want to extend user authentification, please create the plugin implementing
 * {@link OnLoginRunnable} interface.
 * 
 * @author JunHo Yoon
 * @since 3.0
 * 
 */
@SuppressWarnings("UnusedDeclaration")
@Service("ngrinderAuthenticationProvider")
public class NGrinderAuthenticationProvider extends AbstractUserDetailsAuthenticationProvider {

	protected static final Logger LOG = LoggerFactory.getLogger(NGrinderAuthenticationProvider.class);

	@Autowired
	private PluginManager pluginManager;

	@Autowired
	private DefaultLoginPlugin defaultLoginPlugin;
	// ~ Instance fields
	// ================================================================================================

	@Autowired
	private ShaPasswordEncoder passwordEncoder;

	@Autowired
	@Qualifier("reflectionSaltSource")
	private SaltSource saltSource;

	@Autowired
	private UserDetailsService userDetailsService;

	@Autowired
	private UserService userService;

	// ~ Methods
	// ========================================================================================================

	@SuppressWarnings("deprecation")
	protected void additionalAuthenticationChecks(UserDetails userDetails,
			UsernamePasswordAuthenticationToken authentication) {

		Authentication authentication2 = SecurityContextHolder.getContext().getAuthentication();
		if (authentication2 != null) {
			return;
		}
		Object salt = null;

		if (this.saltSource != null) {
			salt = this.saltSource.getSalt(userDetails);
		}
		String message = messages.getMessage("AbstractUserDetailsAuthenticationProvider.badCredentials",
				"Bad credentials");
		if (authentication.getCredentials() == null) {
			LOG.debug("Authentication failed: no credentials provided");
			throw new BadCredentialsException(message);
		}

		String presentedPassword = authentication.getCredentials().toString();
		SecuredUser user = ((SecuredUser) userDetails);
		boolean authorized = false;

		for (OnLoginRunnable each : getPluginManager().getEnabledModulesByClass(OnLoginRunnable.class,
				defaultLoginPlugin)) {
			try {
				each.validateUser(user.getUsername(), presentedPassword, user.getPassword(), passwordEncoder, salt);
				LOG.info("{} is logined by {}", user.getUsername(), each.getClass().getName());
				authorized = true;
				break;
			} catch (BadCredentialsException exception) {
				LOG.info("{} is not logined by {}", user.getUsername(), each.getClass().getName());
				authorized = false;
			}
		}

		if (!authorized) {
			throw new BadCredentialsException(message);
		}

		// If It's the first time to login
		if (user.getUser().getId() == null) {
			addNewUserIntoLocal(user);
			LOG.info("{} is saved by password {}", user.getUser().getId(), user.getUser().getPassword());
		}
	}

	/**
	 * Add new user into local db.
	 * 
	 * @param securedUser
	 *            user
	 */
	@Transactional
	public void addNewUserIntoLocal(SecuredUser securedUser) {
		User user = securedUser.getUser();
		user.setAuthProviderClass(securedUser.getUserInfoProviderClass());
		user.setCreatedDate(new Date());
		User newUser = userService.getOne(user.getUserId());
		if (newUser != null) {
			user = newUser.merge(user);
		}
		if (user.getRole() == null) {
			user.setRole(Role.USER);
		}
		User savedUser = userService.save(user);
		securedUser.setUser(savedUser);
	}

	protected void doAfterPropertiesSet() throws Exception {
		Assert.notNull(this.userDetailsService, "A UserDetailsService must be set");
	}

	protected final UserDetails retrieveUser(String username, UsernamePasswordAuthenticationToken authentication) {
		UserDetails loadedUser;

		try {
			loadedUser = this.getUserDetailsService().loadUserByUsername(username);
		} catch (UsernameNotFoundException notFound) {
			throw notFound;
		} catch (Exception repositoryProblem) {
			throw new AuthenticationServiceException(repositoryProblem.getMessage(), repositoryProblem);
		}

		if (loadedUser == null) {
			throw new AuthenticationServiceException(
					"UserDetailsService returned null, which is an interface contract violation");
		}
		return loadedUser;
	}

	/**
	 * Sets the PasswordEncoder instance to be used to encode and validate passwords. If not set, the password will be
	 * compared as plain text.
	 * <p>
	 * For systems which are already using salted password which are encoded with a previous release, the encoder should
	 * be of type {@code org.springframework.security.authentication.encoding.PasswordEncoder} . Otherwise, the
	 * recommended approach is to use {@code org.springframework.security.crypto.password.PasswordEncoder}.
	 * 
	 * @param passwordEncoder
	 *            must be an instance of one of the {@code PasswordEncoder} types.
	 */
	public void setPasswordEncoder(Object passwordEncoder) {
		Assert.notNull(passwordEncoder, "passwordEncoder cannot be null");

		if (passwordEncoder instanceof ShaPasswordEncoder) {
			this.passwordEncoder = (ShaPasswordEncoder) passwordEncoder;
			return;
		}

		if (passwordEncoder instanceof org.springframework.security.crypto.password.PasswordEncoder) {
			final org.springframework.security.crypto.password.PasswordEncoder delegate = cast(passwordEncoder);
			this.passwordEncoder = new ShaPasswordEncoder() {
				public String encodePassword(String rawPass, Object salt) {
					checkSalt(salt);
					return delegate.encode(rawPass);
				}

				public boolean isPasswordValid(String encPass, String rawPass, Object salt) {
					checkSalt(salt);
					return delegate.matches(rawPass, encPass);
				}

				private void checkSalt(Object salt) {
					Assert.isNull(salt, "Salt value must be null when used with crypto module PasswordEncoder");
				}
			};

			return;
		}

		throw new IllegalArgumentException("passwordEncoder must be a PasswordEncoder instance");
	}

	@SuppressWarnings("unchecked")
	private <T> T cast(Object passwordEncoder) {
		return (T) passwordEncoder;
	}

	protected ShaPasswordEncoder getPasswordEncoder() {
		return passwordEncoder;
	}

	/**
	 * The source of salts to use when decoding passwords. <code>null</code> is a valid value, meaning the
	 * <code>DaoAuthenticationProvider</code> will present <code>null</code> to the relevant
	 * <code>PasswordEncoder</code>.
	 * <p>
	 * Instead, it is recommended that you use an encoder which uses a random salt and combines it with the password
	 * field. This is the default approach taken in the {@code org.springframework.security.crypto.password} package.
	 * 
	 * @param saltSource
	 *            to use when attempting to decode passwords via the <code>PasswordEncoder</code>
	 */
	public void setSaltSource(SaltSource saltSource) {
		this.saltSource = saltSource;
	}

	protected SaltSource getSaltSource() {
		return saltSource;
	}

	public void setUserDetailsService(UserDetailsService userDetailsService) {
		this.userDetailsService = userDetailsService;
	}

	protected UserDetailsService getUserDetailsService() {
		return userDetailsService;
	}

	public PluginManager getPluginManager() {
		return pluginManager;
	}

	public void setPluginManager(PluginManager pluginManager) {
		this.pluginManager = pluginManager;
	}
}
