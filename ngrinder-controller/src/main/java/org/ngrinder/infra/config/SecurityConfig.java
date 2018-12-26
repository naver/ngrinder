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

import com.google.common.collect.Maps;
import org.ngrinder.common.constant.ControllerConstants;
import org.ngrinder.infra.plugin.PluginManager;
import org.ngrinder.security.*;
import org.ngrinder.user.repository.UserRepository;
import org.ngrinder.user.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.access.AccessDecisionVoter;
import org.springframework.security.access.vote.*;
import org.springframework.security.authentication.dao.ReflectionSaltSource;
import org.springframework.security.authentication.dao.SaltSource;
import org.springframework.security.authentication.encoding.ShaPasswordEncoder;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.userdetails.UserDetailsByNameServiceWrapper;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.expression.DefaultWebSecurityExpressionHandler;
import org.springframework.security.web.authentication.*;
import org.springframework.security.web.authentication.preauth.AbstractPreAuthenticatedProcessingFilter;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;
import org.springframework.security.web.authentication.rememberme.TokenBasedRememberMeServices;
import org.springframework.security.web.authentication.www.BasicAuthenticationEntryPoint;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.ELRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * Some User want to have more secured password. Provide the enhanced pw with sha256 if a user
 * specifies ngrinder.security.sha256 in system.conf
 *
 * @author JunHo Yoon
 */
@Configuration
@EnableWebSecurity
@Order(1)
public class SecurityConfig extends WebSecurityConfigurerAdapter {

	@Autowired
	private Config config;

	@Autowired
	private NGrinderUserDetailsService ngrinderUserDetailsService;

	@Autowired
	private UserSwitchPermissionVoter userSwitchPermissionVoter;

	@Autowired
	private SvnHttpBasicEntryPoint svnHttpBasicEntryPoint;

	@Autowired
	private NGrinderAuthenticationProvider nGrinderAuthenticationProvider;

	@Autowired
	private UserService userService;

	/**
	 * Provide the appropriate shaPasswordEncoder depending on the ngrinder.security.sha256 config.
	 *
	 * @return {@link ShaPasswordEncoder} with 256 if ngrinder.security.sha256=true. Otherwise
	 *         returns default {@link ShaPasswordEncoder}
	 */
	@Bean(name = "shaPasswordEncoder")
	public ShaPasswordEncoder shaPasswordEncoder() {
		boolean useEnhancedEncoding = config.getControllerProperties().getPropertyBoolean(ControllerConstants.PROP_CONTROLLER_USER_PASSWORD_SHA256);
		return useEnhancedEncoding ? new ShaPasswordEncoder(256) : new ShaPasswordEncoder();
	}

	@Bean
	public SaltSource reflectionSaltSource() {
		ReflectionSaltSource reflectionSaltSource = new ReflectionSaltSource();
		reflectionSaltSource.setUserPropertyToUse("username");
		return reflectionSaltSource;
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

	@Configuration
	@Order(2)
	public static class NGrinderHttpConfig extends WebSecurityConfigurerAdapter {
		@Autowired
		private BasicAuthenticationEntryPoint basicAuthenticationEntryPoint;

		@Autowired
		private AuthenticatedVoter authenticatedVoter;

		@Autowired
		private NGrinderUserDetailsService ngrinderUserDetailsService;

		@Autowired
		private PluginManager pluginManager;

		@Autowired
		private UserRepository userRepository;

		@Autowired
		private NGrinderAuthenticationProvider nGrinderAuthenticationProvider;

		@Autowired
		private NGrinderAuthenticationPreAuthProvider nGrinderAuthenticationPreAuthProvider;

		@Autowired
		private DefaultWebSecurityExpressionHandler defaultWebSecurityExpressionHandler;

		@Autowired
		private PluggablePreAuthFilter pluggablePreAuthFilter;

		@Bean
		public AbstractAccessDecisionManager accessDecisionManager() {
			List<AccessDecisionVoter<?>> decisionVoters = new ArrayList<>();
			decisionVoters.add(authenticatedVoter);
			return new AffirmativeBased(decisionVoters);
		}

		@Bean
		public TokenBasedRememberMeServices rememberMeServices() {
			return new TokenBasedRememberMeServices("ngrinder", ngrinderUserDetailsService);
		}

		/**
		 * SuccessHandler
		 * @return SavedRequestAwareAuthenticationSuccessHandler
		 */
		@Bean
		public SavedRequestAwareAuthenticationSuccessHandler loginLogAuthenticationSuccessHandler() {
			SavedRequestAwareAuthenticationSuccessHandler savedRequestAwareAuthenticationSuccessHandler = new SavedRequestAwareAuthenticationSuccessHandler();
			savedRequestAwareAuthenticationSuccessHandler.setDefaultTargetUrl("/home");
			savedRequestAwareAuthenticationSuccessHandler.setAlwaysUseDefaultTargetUrl(true);
			return savedRequestAwareAuthenticationSuccessHandler;
		}

		/**
		 * FailureHandler
		 * @return SimpleUrlAuthenticationFailureHandler
		 */
		@Bean
		public SimpleUrlAuthenticationFailureHandler simpleUrlAuthenticationFailureHandler() {
			SimpleUrlAuthenticationFailureHandler simpleUrlAuthenticationFailureHandler = new SimpleUrlAuthenticationFailureHandler();
			simpleUrlAuthenticationFailureHandler.setDefaultFailureUrl("/login");
			return simpleUrlAuthenticationFailureHandler;
		}

		@Bean
		public DelegatingAuthenticationEntryPoint delegatingAuthenticationEntryPoint() {
			LinkedHashMap<RequestMatcher, AuthenticationEntryPoint> matchers = Maps.newLinkedHashMap();
			matchers.put(new ELRequestMatcher("hasHeader('WWW-Authenticate','Basic')"), httpBasicAuthenticationEntryPoint());
			DelegatingAuthenticationEntryPoint entryPoint = new DelegatingAuthenticationEntryPoint(matchers);
			entryPoint.setDefaultEntryPoint(authenticationProcessingFilterEntryPoint());
			return entryPoint;
		}

		@Bean
		public BasicAuthenticationEntryPoint httpBasicAuthenticationEntryPoint() {
			BasicAuthenticationEntryPoint basicAuthenticationEntryPoint = new BasicAuthenticationEntryPoint();
			basicAuthenticationEntryPoint.setRealmName("ngrinder");
			return basicAuthenticationEntryPoint;
		}

		@Bean
		public LoginUrlAuthenticationEntryPoint authenticationProcessingFilterEntryPoint() {
			return new LoginUrlAuthenticationEntryPoint("/login");
		}

		@Override
		protected void configure(HttpSecurity http) throws Exception {
			NgrinderUsernamePasswordAuthenticationFilter ngrinderUsernamePasswordAuthenticationFilter
				= new NgrinderUsernamePasswordAuthenticationFilter(userRepository);
			ngrinderUsernamePasswordAuthenticationFilter.setRememberMeServices(rememberMeServices());
			ngrinderUsernamePasswordAuthenticationFilter.setFilterProcessesUrl("/form_login");
			ngrinderUsernamePasswordAuthenticationFilter.setAuthenticationSuccessHandler(loginLogAuthenticationSuccessHandler());
			ngrinderUsernamePasswordAuthenticationFilter.setAuthenticationFailureHandler(simpleUrlAuthenticationFailureHandler());
			ngrinderUsernamePasswordAuthenticationFilter.setUsernameParameter("j_username");
			ngrinderUsernamePasswordAuthenticationFilter.setPasswordParameter("j_password");
			ngrinderUsernamePasswordAuthenticationFilter.setAuthenticationManager(authenticationManager());

			pluggablePreAuthFilter.loadPlugins(pluginManager);

			http
				.addFilterAt(new BasicAuthenticationFilter(authenticationManager() ,basicAuthenticationEntryPoint), BasicAuthenticationFilter.class)
				.addFilterAt(ngrinderUsernamePasswordAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
				.addFilterAt(pluggablePreAuthFilter, AbstractPreAuthenticatedProcessingFilter.class)
				.logout()
				.logoutRequestMatcher(new AntPathRequestMatcher("/logout"))
				.logoutSuccessUrl("/")
				.deleteCookies("JSESSIONID","switchUser")
				.invalidateHttpSession(true)
				.and()
				.sessionManagement()
				.invalidSessionUrl("/login")
				.sessionFixation().newSession()
				.and()
				.rememberMe()
				.key("ngrinder")
				.userDetailsService(ngrinderUserDetailsService)
				.and()
				.csrf().disable().exceptionHandling().authenticationEntryPoint(delegatingAuthenticationEntryPoint());
		}

		/**
		 * configure static resource and login page
		 * @param web WebSecurity
		 * @throws Exception
		 */
		@Override
		public void configure(WebSecurity web) {
			web.ignoring()
				.antMatchers(
					"/css/**",
					"/img/**",
					"/js/**",
					"/plugins/**",
					"/login",
					"/perftest/**/report",
					"/perftest/**/monitor",
					"/perftest/**/graph",
					"/check/**",
					"/agent/download/**",
					"/monitor/download/**",
					"/sign_up/**"
				).and().expressionHandler(defaultWebSecurityExpressionHandler);
		}

		@Override
		protected void configure(AuthenticationManagerBuilder auth) {
			auth.authenticationProvider(nGrinderAuthenticationPreAuthProvider).authenticationProvider(nGrinderAuthenticationProvider);
		}
	}
}
