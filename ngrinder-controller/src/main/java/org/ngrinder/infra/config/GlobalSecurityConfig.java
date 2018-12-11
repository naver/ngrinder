package org.ngrinder.infra.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.expression.method.DefaultMethodSecurityExpressionHandler;
import org.springframework.security.access.expression.method.MethodSecurityExpressionHandler;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.method.configuration.GlobalMethodSecurityConfiguration;

import static org.ngrinder.common.util.TypeConvertUtils.cast;

@Configuration
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class GlobalSecurityConfig extends GlobalMethodSecurityConfiguration {

	@Override
	protected MethodSecurityExpressionHandler createExpressionHandler() {
		DefaultMethodSecurityExpressionHandler handler = cast(super.createExpressionHandler());
		handler.setDefaultRolePrefix("");
		return handler;
	}
}
