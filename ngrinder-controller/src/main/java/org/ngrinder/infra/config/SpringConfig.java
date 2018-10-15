package org.ngrinder.infra.config;

import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.support.AbstractResourceBasedMessageSource;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;

@Configurable
@Import({TaskConfig.class })
public class SpringConfig {
	@Bean
	public AbstractResourceBasedMessageSource reloadableResourceBundleMessageSource(){
		return new ReloadableResourceBundleMessageSource();
	}
}
