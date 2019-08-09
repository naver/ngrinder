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
	@SuppressWarnings("unchecked")
	public ServletRegistrationBean appServletRegisterBean() {
		ServletRegistrationBean appServletRegistrationBean = new ServletRegistrationBean(dispatcherServlet(), dispatcherServletPath().getPath());
		appServletRegistrationBean.setLoadOnStartup(1);
		appServletRegistrationBean.setName("appServlet");
		return appServletRegistrationBean;
	}

	@Bean
	public DispatcherServlet dispatcherServlet() {
		return new DispatcherServlet();
	}

	@Bean
	@SuppressWarnings("unchecked")
	public ServletRegistrationBean svnDavServletRegisterBean() {
		ServletRegistrationBean SvnDavServletRegistrationBean = new ServletRegistrationBean(new HttpRequestHandlerServlet(), "/svn/*");
		SvnDavServletRegistrationBean.setLoadOnStartup(1);
		SvnDavServletRegistrationBean.setName("svnDavServlet");
		return SvnDavServletRegistrationBean;
	}

	@Bean
	public DispatcherServletPath dispatcherServletPath() {
		return () -> "/*";
	}
}
