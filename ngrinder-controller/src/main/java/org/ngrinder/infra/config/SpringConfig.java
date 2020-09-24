package org.ngrinder.infra.config;

import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.annotation.Import;
import org.springframework.context.support.AbstractResourceBasedMessageSource;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

import javax.net.ssl.SSLException;

import static io.netty.handler.ssl.util.InsecureTrustManagerFactory.INSTANCE;
import static org.springframework.web.reactive.function.client.WebClient.builder;

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

	@Bean
	public WebClient webClient() throws SSLException {
		SslContext sslContext = SslContextBuilder
			.forClient()
			.trustManager(INSTANCE)
			.build();
		HttpClient httpClient = HttpClient.create().secure(sslProviderBuilder -> sslProviderBuilder.sslContext(sslContext));
		return builder().clientConnector(new ReactorClientHttpConnector(httpClient)).build();
	}
}
