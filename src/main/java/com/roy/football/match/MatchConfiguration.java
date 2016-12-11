package com.roy.football.match;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
@PropertySource("classpath:/Match.properties")
@ComponentScan(basePackageClasses = { MatchPackageScanned.class})
public class MatchConfiguration {
	
	@Value("${match.calculation.poolSize}")
	private int calculateThreadSize;

	@Bean
	public ExecutorService calculateExecutorService () {
		return Executors.newFixedThreadPool(calculateThreadSize);
	}
}
