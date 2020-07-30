package org.ngrinder.infra.config;

import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.annotation.Import;
import org.springframework.context.support.AbstractResourceBasedMessageSource;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Configuration
@EnableTransactionManagement(proxyTargetClass = true)
@EnableAspectJAutoProxy(proxyTargetClass = true, exposeProxy = true)
@EnableJpaRepositories(basePackages = "org.ngrinder", entityManagerFactoryRef = "emf")
@EnableScheduling
@EnableAsync
@EnableCaching
@Import({TaskConfig.class })
public class SpringConfig {
	@Bean
	public AbstractResourceBasedMessageSource reloadableResourceBundleMessageSource(){
		return new ReloadableResourceBundleMessageSource();
	}
}
