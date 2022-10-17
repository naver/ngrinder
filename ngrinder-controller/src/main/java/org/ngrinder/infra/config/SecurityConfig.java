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

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import org.ngrinder.infra.plugin.PluginManager;
import org.ngrinder.security.NGrinderAuthenticationPreAuthProvider;
import org.ngrinder.security.NGrinderAuthenticationProvider;
import org.ngrinder.security.NGrinderUserDetailsService;
import org.ngrinder.security.NgrinderUsernamePasswordAuthenticationFilter;
import org.ngrinder.security.PluggablePreAuthFilter;
import org.ngrinder.user.repository.UserRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.access.AccessDecisionVoter;
import org.springframework.security.access.vote.AbstractAccessDecisionManager;
import org.springframework.security.access.vote.AffirmativeBased;
import org.springframework.security.access.vote.AuthenticatedVoter;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.expression.DefaultWebSecurityExpressionHandler;
import org.springframework.security.web.authentication.DelegatingAuthenticationEntryPoint;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.preauth.AbstractPreAuthenticatedProcessingFilter;
import org.springframework.security.web.authentication.rememberme.TokenBasedRememberMeServices;
import org.springframework.security.web.authentication.www.BasicAuthenticationEntryPoint;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.ELRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;

import com.google.common.collect.Maps;

import lombok.RequiredArgsConstructor;

@Configuration
@EnableWebSecurity
@Order(2)
@RequiredArgsConstructor
public class SecurityConfig extends WebSecurityConfigurerAdapter {

	private final AuthenticatedVoter authenticatedVoter;

	private final NGrinderUserDetailsService ngrinderUserDetailsService;

	private final PluginManager pluginManager;

	private final UserRepository userRepository;

	private final NGrinderAuthenticationProvider nGrinderAuthenticationProvider;

	private final NGrinderAuthenticationPreAuthProvider nGrinderAuthenticationPreAuthProvider;

	private final DefaultWebSecurityExpressionHandler defaultWebSecurityExpressionHandler;

	private final PluggablePreAuthFilter pluggablePreAuthFilter;

	private static final String REMEMBER_ME_KEY = "ngrinder";
	private static final String REMEMBER_ME_COOKIE_NAME = "ngrinder-remember-me";

	@Bean
	@Override
	protected AuthenticationManager authenticationManager() throws Exception {
		return super.authenticationManager();
	}

	@Bean
	public AbstractAccessDecisionManager accessDecisionManager() {
		List<AccessDecisionVoter<?>> decisionVoters = new ArrayList<>();
		decisionVoters.add(authenticatedVoter);
		return new AffirmativeBased(decisionVoters);
	}

	@Bean
	public TokenBasedRememberMeServices rememberMeServices() {
		TokenBasedRememberMeServices tokenBasedRememberMeServices = new TokenBasedRememberMeServices(REMEMBER_ME_KEY, ngrinderUserDetailsService);
		tokenBasedRememberMeServices.setCookieName(REMEMBER_ME_COOKIE_NAME);
		return tokenBasedRememberMeServices;
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
			.addFilterAt(new BasicAuthenticationFilter(authenticationManager(), httpBasicAuthenticationEntryPoint()), BasicAuthenticationFilter.class)
			.addFilterAt(ngrinderUsernamePasswordAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
			.addFilterAt(pluggablePreAuthFilter, AbstractPreAuthenticatedProcessingFilter.class)
			.logout()
			.logoutRequestMatcher(new AntPathRequestMatcher("/logout"))
			.logoutSuccessUrl("/")
			.deleteCookies("JSESSIONID", "switchUser")
			.invalidateHttpSession(true)
			.and()
			.sessionManagement()
			.sessionFixation().newSession()
			.and()
			.rememberMe()
			.rememberMeCookieName(REMEMBER_ME_COOKIE_NAME)
			.key(REMEMBER_ME_KEY)
			.userDetailsService(ngrinderUserDetailsService)
			.and()
			.csrf().disable().exceptionHandling().authenticationEntryPoint(delegatingAuthenticationEntryPoint())
			.and()
			.headers().frameOptions().sameOrigin();
	}

	/**
	 * configure static resource and login page
	 * @param web WebSecurity
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
