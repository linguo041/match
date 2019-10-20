package com.roy.football.web;

import java.util.List;

import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport;

import com.roy.football.match.MatchConfiguration;
import com.roy.football.match.jpa.configure.MatchJpaConfiguration;

@Configuration
@Import({MatchConfiguration.class, MatchJpaConfiguration.class})
@ComponentScan(basePackageClasses = { WebPackageScanned.class})
public class WebConfiguration extends WebMvcConfigurationSupport {
	
	@Override
    protected void addInterceptors(InterceptorRegistry registry) {
//        registry.addInterceptor(matchInterceptor());
    }

	@Override
	protected void addResourceHandlers(ResourceHandlerRegistry registry) {
		registry.addResourceHandler("/webjars/**").addResourceLocations("classpath:/META-INF/resources/webjars/");
		registry.addResourceHandler("/custom-resources/**").addResourceLocations("/WEB-INF/custom-resources/");

		super.addResourceHandlers(registry);
	}
	
	@Override
	protected void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
		converters.add(new MappingJackson2HttpMessageConverter());
	}

	@Bean
    public MessageSource messageSource() {
        ReloadableResourceBundleMessageSource messageSource = new ReloadableResourceBundleMessageSource();
        messageSource.setBasenames("classpath:vitamin/messages/messages");
        messageSource.setDefaultEncoding("UTF-8");
        return messageSource;
    }
}
