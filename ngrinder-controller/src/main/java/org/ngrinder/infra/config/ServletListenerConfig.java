package org.ngrinder.infra.config;

import net.sf.ehcache.constructs.web.ShutdownListener;
import org.springframework.boot.web.servlet.ServletListenerRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.web.session.HttpSessionEventPublisher;

@Configuration
public class ServletListenerConfig {
	@Bean
	public ServletListenerRegistrationBean<ShutdownListener> shutdownListener() {
		ServletListenerRegistrationBean<ShutdownListener> servletListenerRegistrationBean = new ServletListenerRegistrationBean<>();
		servletListenerRegistrationBean.setListener(new ShutdownListener());
		return servletListenerRegistrationBean;
	}

	@Bean
	public ServletListenerRegistrationBean<HttpSessionEventPublisher> httpSessionEventPublisher() {
		ServletListenerRegistrationBean<HttpSessionEventPublisher> servletListenerRegistrationBean = new ServletListenerRegistrationBean<>();
		servletListenerRegistrationBean.setListener(new HttpSessionEventPublisher());
		return servletListenerRegistrationBean;
	}
}
