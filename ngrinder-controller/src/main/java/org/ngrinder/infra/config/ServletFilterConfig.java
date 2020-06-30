package org.ngrinder.infra.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.orm.jpa.support.OpenEntityManagerInViewFilter;
import org.springframework.web.filter.*;

import javax.servlet.Filter;

@Configuration
@SuppressWarnings("unchecked")
public class ServletFilterConfig {

	@Value("${server.default-encoding}")
	private String defaultEncoding;

	@Bean
	public Filter characterEncodingFilter() {
		CharacterEncodingFilter characterEncodingFilter = new CharacterEncodingFilter();
		characterEncodingFilter.setForceEncoding(true);
		characterEncodingFilter.setEncoding(defaultEncoding);
		return characterEncodingFilter;
	}

	@Bean
	public FilterRegistrationBean<FormContentFilter> httpPutFormContentFilter() {
		FilterRegistrationBean<FormContentFilter> httpPutFormContentFilter = new FilterRegistrationBean<>(new FormContentFilter());
		httpPutFormContentFilter.addUrlPatterns("/*");
		httpPutFormContentFilter.setOrder(1);
		return httpPutFormContentFilter;
	}

	@Bean
	public FilterRegistrationBean<OpenEntityManagerInViewFilter> springOpenEntityManagerInViewFilter() {
		FilterRegistrationBean<OpenEntityManagerInViewFilter> springOpenEntityManagerInViewFilter = new FilterRegistrationBean<>(new OpenEntityManagerInViewFilter());
		springOpenEntityManagerInViewFilter.addUrlPatterns("/*");
		springOpenEntityManagerInViewFilter.setOrder(2);
		return springOpenEntityManagerInViewFilter;
	}

	@Bean
	public FilterRegistrationBean<HiddenHttpMethodFilter> hiddenHttpMethodFilter() {
		FilterRegistrationBean<HiddenHttpMethodFilter> hiddenHttpMethodFilter = new FilterRegistrationBean<>(new HiddenHttpMethodFilter());
		hiddenHttpMethodFilter.addUrlPatterns("/*");
		hiddenHttpMethodFilter.setOrder(3);
		return hiddenHttpMethodFilter;
	}

	@Bean
	public FilterRegistrationBean<DelegatingFilterProxy> pluggableServletFilterRegister() {
		FilterRegistrationBean<DelegatingFilterProxy> pluggableServletFilter = new FilterRegistrationBean<>(new DelegatingFilterProxy());
		pluggableServletFilter.addUrlPatterns("/*");
		pluggableServletFilter.setName("pluggableServletFilter");
		pluggableServletFilter.setOrder(4);
		return pluggableServletFilter;
	}
}
