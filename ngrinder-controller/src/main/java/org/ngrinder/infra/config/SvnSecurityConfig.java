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
package org.ngrinder.infra.config;

import lombok.RequiredArgsConstructor;
import org.ngrinder.security.*;
import org.ngrinder.user.service.UserService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.access.AccessDecisionVoter;
import org.springframework.security.access.vote.AuthenticatedVoter;
import org.springframework.security.access.vote.RoleVoter;
import org.springframework.security.access.vote.UnanimousBased;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.userdetails.UserDetailsByNameServiceWrapper;
import org.springframework.security.crypto.password.ShaPasswordEncoder;
import org.springframework.security.web.access.expression.DefaultWebSecurityExpressionHandler;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;
import org.springframework.security.web.firewall.DefaultHttpFirewall;
import org.springframework.security.web.firewall.HttpFirewall;

import java.util.ArrayList;
import java.util.List;

import static org.ngrinder.common.constant.ControllerConstants.PROP_CONTROLLER_USER_PASSWORD_SHA256;

/**
 * Some User want to have more secured password. Provide the enhanced pw with sha256 if a user
 * specifies ngrinder.security.sha256 in system.conf
 */
@Configuration
@EnableWebSecurity
@Order(1)
@RequiredArgsConstructor
public class SvnSecurityConfig extends WebSecurityConfigurerAdapter {

	private final Config config;

	private final NGrinderUserDetailsService ngrinderUserDetailsService;

	private final UserSwitchPermissionVoter userSwitchPermissionVoter;

	private final SvnHttpBasicEntryPoint svnHttpBasicEntryPoint;

	private final NGrinderAuthenticationProvider nGrinderAuthenticationProvider;

	private final UserService userService;

	/**
	 * Provide the appropriate shaPasswordEncoder depending on the ngrinder.security.sha256 config.
	 *
	 * @return ShaPasswordEncoder with "SHA-256" algorithm if ngrinder.security.sha256=true. Otherwise
	 *         returns with "SHA-1"
	 */
	@Bean
	public ShaPasswordEncoder shaPasswordEncoder() {
		boolean useEnhancedEncoding = config.getControllerProperties().getPropertyBoolean(PROP_CONTROLLER_USER_PASSWORD_SHA256);
		return useEnhancedEncoding ? new ShaPasswordEncoder("SHA-256") : new ShaPasswordEncoder("SHA-1");
	}

	/**
	 * Generic Web
	 * @return AuthenticatedVoter
	 */
	@Bean
	public AuthenticatedVoter authenticatedVoter() {
		return new AuthenticatedVoter();
	}

	@Bean
	public DefaultWebSecurityExpressionHandler webSecurityExpressionHandler() {
		DefaultWebSecurityExpressionHandler defaultWebSecurityExpressionHandler = new DefaultWebSecurityExpressionHandler();
		defaultWebSecurityExpressionHandler.setDefaultRolePrefix("");
		return defaultWebSecurityExpressionHandler;
	}

	@Bean
	public RoleVoter roleVoter() {
		RoleVoter roleVoter = new RoleVoter();
		roleVoter.setRolePrefix("");
		return roleVoter;
	}

	/**
	 * Svn AccessDecisionManager
	 * @return UnanimousBased
	 */
	@Bean
	public UnanimousBased svnAccessDecisionManager() {
		List<AccessDecisionVoter<?>> decisionVoters = new ArrayList<>();
		decisionVoters.add(authenticatedVoter());
		decisionVoters.add(userSwitchPermissionVoter);
		return new UnanimousBased(decisionVoters);
	}

	@Override
	protected void configure(HttpSecurity http) throws Exception {
		//SVN
		http
			.requestMatchers().antMatchers("/svn/**")
			.and()
			.authorizeRequests()
			.antMatchers("/svn/**")
			.hasAnyRole("A", "S", "U")
			.accessDecisionManager(svnAccessDecisionManager())
			.and()
			.httpBasic().authenticationEntryPoint(svnHttpBasicEntryPoint)
			.realmName("svn")
			.and()
			.csrf().disable();
	}

	@Override
	public void configure(WebSecurity web) {
		web.expressionHandler(webSecurityExpressionHandler());
	}

	@Override
	protected void configure(AuthenticationManagerBuilder auth) {
		auth.authenticationProvider(ngrinderPreAuthProvider()).authenticationProvider(nGrinderAuthenticationProvider);
	}

	@Bean
	public NGrinderAuthenticationPreAuthProvider ngrinderPreAuthProvider() {
		NGrinderAuthenticationPreAuthProvider nGrinderAuthenticationPreAuthProvider = new NGrinderAuthenticationPreAuthProvider();
		nGrinderAuthenticationPreAuthProvider.setPreAuthenticatedUserDetailsService(userDetailsServiceWrapper());
		nGrinderAuthenticationPreAuthProvider.setUserService(userService);
		return nGrinderAuthenticationPreAuthProvider;
	}

	@Bean
	public UserDetailsByNameServiceWrapper<PreAuthenticatedAuthenticationToken> userDetailsServiceWrapper() {
		UserDetailsByNameServiceWrapper<org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken> userDetailsByNameServiceWrapper = new UserDetailsByNameServiceWrapper<>();
		userDetailsByNameServiceWrapper.setUserDetailsService(ngrinderUserDetailsService);
		return userDetailsByNameServiceWrapper;
	}

	@Bean
	public HttpFirewall defaultHttpFirewall() {
		DefaultHttpFirewall firewall = new DefaultHttpFirewall();
		firewall.setAllowUrlEncodedSlash(true);
		return firewall;
	}

}
