/*
 * Copyright (C) 2012 - 2012 NHN Corporation
 * All rights reserved.
 *
 * This file is part of The nGrinder software distribution. Refer to
 * the file LICENSE which is part of The nGrinder distribution for
 * licensing details. The nGrinder distribution is available on the
 * Internet at http://nhnopensource.org/ngrinder
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS
 * FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE
 * COPYRIGHT HOLDERS OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.ngrinder.security;

import org.apache.commons.lang.StringUtils;
import org.ngrinder.infra.plugin.OnLoginRunnable;
import org.ngrinder.infra.plugin.PluginManager;
import org.ngrinder.model.Role;
import org.ngrinder.model.User;
import org.ngrinder.user.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.dao.AbstractUserDetailsAuthenticationProvider;
import org.springframework.security.authentication.dao.SaltSource;
import org.springframework.security.authentication.encoding.PasswordEncoder;
import org.springframework.security.authentication.encoding.PlaintextPasswordEncoder;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

@Service("ngrinderAuthenticationProvider")
public class NGrinderAuthenticationProvider extends AbstractUserDetailsAuthenticationProvider {

	@Autowired
	private PluginManager pluginManager;

	@Autowired
	private DefaultLoginPlugin defaultLoginPlugin;
	// ~ Instance fields
	// ================================================================================================

	private PasswordEncoder passwordEncoder = new PlaintextPasswordEncoder();

	private SaltSource saltSource;

	@Autowired
	private UserDetailsService userDetailsService;

	@Autowired
	private UserService userService;

	// ~ Methods
	// ========================================================================================================

	@SuppressWarnings("deprecation")
	protected void additionalAuthenticationChecks(UserDetails userDetails,
			UsernamePasswordAuthenticationToken authentication) throws AuthenticationException {
		Object salt = null;

		if (this.saltSource != null) {
			salt = this.saltSource.getSalt(userDetails);
		}

		if (authentication.getCredentials() == null) {
			logger.debug("Authentication failed: no credentials provided");

			throw new BadCredentialsException(messages.getMessage(
					"AbstractUserDetailsAuthenticationProvider.badCredentials", "Bad credentials"), userDetails);
		}

		String presentedPassword = authentication.getCredentials().toString();
		SecuredUser user = ((SecuredUser) userDetails);
		boolean authorized = false;
		for (OnLoginRunnable each : getPluginManager().getEnabledModulesByClass(OnLoginRunnable.class,
				defaultLoginPlugin)) {
			// If no auth provider provided, defaultLoginPlugin is used to
			// verify ID/PW
			if (StringUtils.isEmpty(user.getAuthProviderClass()) && each.equals(defaultLoginPlugin)) {
				each.authUser(passwordEncoder, user.getUsername(), user.getPassword(), presentedPassword, salt);
				authorized = true;
				break;
			}
			//
			else if (each.getClass().getName().equals(user.getAuthProviderClass())) {
				each.authUser(passwordEncoder, user.getUsername(), user.getPassword(), presentedPassword, salt);
				authorized = true;
				if (!DefaultLoginPlugin.class.getName().equals(user.getUserInfoProviderClass())) {
					addNewUserInfoLocal(user);
				}
				break;
			}
		}
		if (!authorized) {
			throw new BadCredentialsException(messages.getMessage(
					"AbstractUserDetailsAuthenticationProvider.badCredentials", "Bad credentials"), user);
		}

	}

	public void addNewUserInfoLocal(SecuredUser user) {
		User userForLocalStore = user.getUser();
		userForLocalStore.setRole(Role.USER);
		userService.saveUser(userForLocalStore);
	}

	protected void doAfterPropertiesSet() throws Exception {
		Assert.notNull(this.userDetailsService, "A UserDetailsService must be set");
	}

	protected final UserDetails retrieveUser(String username, UsernamePasswordAuthenticationToken authentication)
			throws AuthenticationException {
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

		if (passwordEncoder instanceof PasswordEncoder) {
			this.passwordEncoder = (PasswordEncoder) passwordEncoder;
			return;
		}

		if (passwordEncoder instanceof org.springframework.security.crypto.password.PasswordEncoder) {
			final org.springframework.security.crypto.password.PasswordEncoder delegate = (org.springframework.security.crypto.password.PasswordEncoder) passwordEncoder;
			this.passwordEncoder = new PasswordEncoder() {
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

	protected PasswordEncoder getPasswordEncoder() {
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
