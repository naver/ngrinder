package org.ngrinder.infra.config;

import org.ngrinder.infra.spring.ApiExceptionHandlerResolver;
import org.ngrinder.infra.spring.RemainedPathMethodArgumentResolver;
import org.ngrinder.infra.spring.UserHandlerMethodArgumentResolver;
import org.springframework.beans.factory.annotation.Autowired;
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
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;
import org.springframework.web.servlet.i18n.LocaleChangeInterceptor;
import org.springframework.web.servlet.mvc.method.annotation.ExceptionHandlerExceptionResolver;
import org.springframework.web.servlet.view.freemarker.FreeMarkerConfigurer;
import org.springframework.web.servlet.view.freemarker.FreeMarkerViewResolver;

import javax.servlet.ServletContext;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

@Configuration
@ComponentScan(
	basePackages = {"org.ngrinder"},
	useDefaultFilters = false,
	includeFilters = {@ComponentScan.Filter(type = FilterType.ANNOTATION, value = org.springframework.stereotype.Controller.class)}
)
public class WebMvcConfig extends WebMvcConfigurerAdapter {

	@Value("${ngrinder.version}")
	private String ngrinderVersion;

	@Value("${server.multipart.max-upload-size}")
	private int multipartMaxUploadSize;

	@Value("${server.default-encoding}")
	private String defaultEncoding;

	@Autowired
	private LocaleChangeInterceptor localeChangeInterceptor;

	@Autowired
	private ResourceProperties resourceProperties = new ResourceProperties();

	@Override
	public void addArgumentResolvers(List<HandlerMethodArgumentResolver> argumentResolvers) {
		argumentResolvers.add(userHandlerMethodArgumentResolver());
		argumentResolvers.add(remainedPathMethodArgumentResolver());
		argumentResolvers.add(pageableHandlerMethodArgumentResolver());
		super.addArgumentResolvers(argumentResolvers);
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
	}

	@Bean
	public MultipartResolver multipartResolver() {
		CommonsMultipartResolver commonsMultipartResolver = new CommonsMultipartResolver();
		commonsMultipartResolver.setMaxUploadSize(multipartMaxUploadSize);
		commonsMultipartResolver.setDefaultEncoding(defaultEncoding);
		return commonsMultipartResolver;
	}

	@Bean
	public ViewResolver freemarkerResolver() {
		FreeMarkerViewResolver resolver = new FreeMarkerViewResolver();
		resolver.setOrder(1);
		resolver.setViewClass(org.springframework.web.servlet.view.freemarker.FreeMarkerView.class);
		resolver.setPrefix("");
		resolver.setSuffix(".ftl");
		resolver.setRequestContextAttribute("req");
		Properties properties = new Properties();
		properties.put("nGrinderVersion" , ngrinderVersion);
		resolver.setExposeContextBeansAsAttributes(false);
		resolver.setAttributes(properties);
		resolver.setContentType("text/html; charset=" + defaultEncoding);
		resolver.setExposeSpringMacroHelpers(true);
		resolver.setCache(false);
		return resolver;
	}

	@Bean
	@Autowired
	public FreeMarkerConfigurer freemarkerConfig(ServletContext servletContext) {
		FreeMarkerConfigurer configurer = new NGrinderFreeMarkerConfigure();
		configurer.setTemplateLoaderPath("classpath:/templates/ftl/");
		configurer.setPreferFileSystemAccess(false);
		configurer.setDefaultEncoding(defaultEncoding);
		return configurer;
	}

	@Override
	public void addResourceHandlers(ResourceHandlerRegistry registry) {
	    String staticPathPatterns[] = new String[] {"/**/*.js" , "/**/*.png"
			, "/**/*.jpg" , "/**/*.swf" , "/**/*.csv" , "/**/*.css"
			, "/**/*.html" , "/**/*.gif" , "/**/*.ico" , "/**/*.woff2"
			, "/**/*.woff" , "/**/*.ttf"};
		Integer cachePeriod = this.resourceProperties.getCachePeriod();
		registry.addResourceHandler(staticPathPatterns).addResourceLocations(this.resourceProperties.getStaticLocations()).setCachePeriod(cachePeriod);
	}

	public static class NGrinderFreeMarkerConfigure extends FreeMarkerConfigurer {
		@Override
		public void setServletContext(ServletContext servletContext) {
			super.setServletContext(servletContext);
			List<String> freeMarkerTlds = new ArrayList<String>();
			freeMarkerTlds.add("security.tld");
			this.getTaglibFactory().setClasspathTlds(freeMarkerTlds);
		}
	}
}
