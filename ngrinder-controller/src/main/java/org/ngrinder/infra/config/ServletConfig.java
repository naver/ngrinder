package org.ngrinder.infra.config;

import org.ngrinder.infra.spring.Redirect404DispatcherServlet;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.support.HttpRequestHandlerServlet;
import org.springframework.web.servlet.DispatcherServlet;

@Configuration
public class ServletConfig {
	@Bean
	public ServletRegistrationBean appServletRegisterBean() {
		ServletRegistrationBean redirect404DispatcherServlet = new ServletRegistrationBean(dispatcherServlet(), "/*");
		redirect404DispatcherServlet.setLoadOnStartup(1);
		redirect404DispatcherServlet.setName("appServlet");
		return redirect404DispatcherServlet;
	}

	@Bean
	public DispatcherServlet dispatcherServlet() {
		return new Redirect404DispatcherServlet();
	}

	@Bean
	public ServletRegistrationBean svnDavServletRegisterBean() {
		ServletRegistrationBean SvnDavServletRegistrationBean = new ServletRegistrationBean(new HttpRequestHandlerServlet(), "/svn/*");
		SvnDavServletRegistrationBean.setLoadOnStartup(1);
		SvnDavServletRegistrationBean.setName("svnDavServlet");
		return SvnDavServletRegistrationBean;
	}
}
