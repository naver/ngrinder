package org.ngrinder.infra.config;

import com.navercorp.lucy.security.xss.servletfilter.XssEscapeServletFilter;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.orm.jpa.support.OpenEntityManagerInViewFilter;
import org.springframework.web.filter.CharacterEncodingFilter;
import org.springframework.web.filter.DelegatingFilterProxy;
import org.springframework.web.filter.HiddenHttpMethodFilter;
import org.springframework.web.filter.HttpPutFormContentFilter;

@Configuration
public class ServletFilterConfig {
	@Bean
	public FilterRegistrationBean characterEncodingFilterRRegisterBean() {
		FilterRegistrationBean registrationBean = new FilterRegistrationBean();
		registrationBean.setFilter(characterEncodingFilter());
		registrationBean.setOrder(1);
		return registrationBean;
	}

	@Bean
	public CharacterEncodingFilter characterEncodingFilter(){
		CharacterEncodingFilter characterEncodingFilter = new CharacterEncodingFilter();
		characterEncodingFilter.setEncoding("UTF-8");
		return characterEncodingFilter;
	}

	@Bean
	public FilterRegistrationBean xssEscapeServletFilter() {
		FilterRegistrationBean xssEscapeServletFilter = new FilterRegistrationBean(new XssEscapeServletFilter());
		String[] urlMappings = {"/login/*", "/perftest/*", "/user/*", "/script/*"};
		xssEscapeServletFilter.addUrlPatterns(urlMappings);
		xssEscapeServletFilter.setOrder(2);
		return xssEscapeServletFilter;
	}

	@Bean
	public FilterRegistrationBean httpPutFormContentFilter() {
		FilterRegistrationBean httpPutFormContentFilter = new FilterRegistrationBean(new HttpPutFormContentFilter());
		httpPutFormContentFilter.addUrlPatterns("/*");
		httpPutFormContentFilter.setOrder(3);
		return httpPutFormContentFilter;
	}

	@Bean
	public FilterRegistrationBean springOpenEntityManagerInViewFilter() {
		FilterRegistrationBean springOpenEntityManagerInViewFilter = new FilterRegistrationBean(new OpenEntityManagerInViewFilter());
		springOpenEntityManagerInViewFilter.addUrlPatterns("/*");
		springOpenEntityManagerInViewFilter.setOrder(4);
		return springOpenEntityManagerInViewFilter;
	}

	@Bean
	public FilterRegistrationBean hiddenHttpMethodFilter() {
		FilterRegistrationBean hiddenHttpMethodFilter = new FilterRegistrationBean(new HiddenHttpMethodFilter());
		hiddenHttpMethodFilter.addUrlPatterns("/*");
		hiddenHttpMethodFilter.setOrder(5);
		return hiddenHttpMethodFilter;
	}

	@Bean
	public FilterRegistrationBean pluggableServletFilterRegister() {
		FilterRegistrationBean pluggableServletFilter = new FilterRegistrationBean(new DelegatingFilterProxy());
		pluggableServletFilter.addUrlPatterns("/*");
		pluggableServletFilter.setName("pluggableServletFilter");
		pluggableServletFilter.setOrder(6);
		return pluggableServletFilter;
	}
}
