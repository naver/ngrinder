package org.ngrinder.infra.config;

import com.github.jknack.handlebars.springmvc.HandlebarsViewResolver;
import lombok.RequiredArgsConstructor;
import org.ngrinder.infra.interceptor.DefaultSuccessJsonInterceptor;
import org.ngrinder.infra.spring.ApiExceptionHandlerResolver;
import org.ngrinder.infra.spring.RemainedPathMethodArgumentResolver;
import org.ngrinder.infra.spring.UserHandlerMethodArgumentResolver;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.web.ResourceProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.multipart.MultipartResolver;
import org.springframework.web.multipart.commons.CommonsMultipartResolver;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.ViewResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.i18n.LocaleChangeInterceptor;
import org.springframework.web.servlet.mvc.method.annotation.ExceptionHandlerExceptionResolver;

import java.util.List;

@Configuration
@ComponentScan(
	basePackages = {"org.ngrinder"},
	useDefaultFilters = false,
	includeFilters = {@ComponentScan.Filter(type = FilterType.ANNOTATION, value = org.springframework.stereotype.Controller.class)}
)
@RequiredArgsConstructor
public class WebMvcConfig implements WebMvcConfigurer {

	@Value("${server.default-encoding}")
	private String defaultEncoding;

	private final LocaleChangeInterceptor localeChangeInterceptor;

	private final DefaultSuccessJsonInterceptor defaultSuccessJsonInterceptor;

	private final ResourceProperties resourceProperties;

	@Override
	public void addArgumentResolvers(List<HandlerMethodArgumentResolver> argumentResolvers) {
		argumentResolvers.add(userHandlerMethodArgumentResolver());
		argumentResolvers.add(remainedPathMethodArgumentResolver());
		argumentResolvers.add(pageableHandlerMethodArgumentResolver());
	}

	@Bean
	public PageableHandlerMethodArgumentResolver pageableHandlerMethodArgumentResolver() {
		PageableHandlerMethodArgumentResolver pageableHandlerMethodArgumentResolver = new PageableHandlerMethodArgumentResolver();
		pageableHandlerMethodArgumentResolver.setPrefix("page.");
		return pageableHandlerMethodArgumentResolver;
	}

	@Bean
	public RemainedPathMethodArgumentResolver remainedPathMethodArgumentResolver() {
		return new RemainedPathMethodArgumentResolver();
	}

	@Bean
	public UserHandlerMethodArgumentResolver userHandlerMethodArgumentResolver() {
		return new UserHandlerMethodArgumentResolver();
	}

	@Override
	public void configureHandlerExceptionResolvers(List<HandlerExceptionResolver> exceptionResolvers) {
		exceptionResolvers.add(apiExceptionHandlerResolver());
		exceptionResolvers.add(exceptionHandlerExceptionResolver());
	}

	@Bean
	public ApiExceptionHandlerResolver apiExceptionHandlerResolver() {
		ApiExceptionHandlerResolver apiExceptionHandlerResolver = new ApiExceptionHandlerResolver();
		apiExceptionHandlerResolver.setOrder(-1);
		return apiExceptionHandlerResolver;
	}

	@Bean
	public ExceptionHandlerExceptionResolver exceptionHandlerExceptionResolver() {
		ExceptionHandlerExceptionResolver exceptionHandlerExceptionResolver = new ExceptionHandlerExceptionResolver();
		exceptionHandlerExceptionResolver.setOrder(1);
		return exceptionHandlerExceptionResolver;
	}

	@Override
	public void addInterceptors(InterceptorRegistry registry) {
		registry.addInterceptor(localeChangeInterceptor);
		registry.addInterceptor(defaultSuccessJsonInterceptor);
	}

	@Bean
	public MultipartResolver multipartResolver() {
		CommonsMultipartResolver commonsMultipartResolver = new CommonsMultipartResolver();
		commonsMultipartResolver.setDefaultEncoding(defaultEncoding);
		return commonsMultipartResolver;
	}

	@Bean
	public ViewResolver viewResolver() {
		HandlebarsViewResolver viewResolver = new HandlebarsViewResolver();
		viewResolver.setOrder(1);
		viewResolver.setPrefix("classpath:/templates/");
		viewResolver.setSuffix(".html");
		viewResolver.setExposeContextBeansAsAttributes(false);
		viewResolver.setContentType("text/html; charset=" + defaultEncoding);
		viewResolver.setCache(false);
		return viewResolver;
	}

	@Override
	public void addResourceHandlers(ResourceHandlerRegistry registry) {
	    String[] staticPathPatterns = new String[] {"/**/*.js" , "/**/*.png"
			, "/**/*.jpg" , "/**/*.swf" , "/**/*.csv" , "/**/*.css"
			, "/**/*.html" , "/**/*.gif" , "/**/*.ico" , "/**/*.woff2"
			, "/**/*.woff" , "/**/*.ttf"};
		registry.addResourceHandler(staticPathPatterns).addResourceLocations(this.resourceProperties.getStaticLocations()).setCachePeriod(3600);
	}
}
