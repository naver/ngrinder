package org.ngrinder.infra.config;

import org.springframework.boot.autoconfigure.web.servlet.DispatcherServletPath;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.support.HttpRequestHandlerServlet;
import org.springframework.web.servlet.DispatcherServlet;

@Configuration
public class ServletConfig {
	@Bean
	public ServletRegistrationBean<DispatcherServlet> appServletRegisterBean() {
		ServletRegistrationBean<DispatcherServlet> appServletRegistrationBean = new ServletRegistrationBean<>(dispatcherServlet(), dispatcherServletPath().getPath());
		appServletRegistrationBean.setLoadOnStartup(1);
		appServletRegistrationBean.setName("appServlet");
		return appServletRegistrationBean;
	}

	@Bean
	public DispatcherServlet dispatcherServlet() {
		return new DispatcherServlet();
	}

	@Bean
	public ServletRegistrationBean<HttpRequestHandlerServlet> svnDavServletRegisterBean() {
		ServletRegistrationBean<HttpRequestHandlerServlet> svnDavServletRegistrationBean = new ServletRegistrationBean<>(new HttpRequestHandlerServlet(), "/svn/*");
		svnDavServletRegistrationBean.setLoadOnStartup(1);
		svnDavServletRegistrationBean.setName("svnDavServlet");
		return svnDavServletRegistrationBean;
	}

	@Bean
	public DispatcherServletPath dispatcherServletPath() {
		return () -> "/*";
	}
}
