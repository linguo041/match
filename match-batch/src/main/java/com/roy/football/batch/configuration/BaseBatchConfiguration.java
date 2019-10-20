package com.roy.football.batch.configuration;

import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.launch.support.SimpleJobLauncher;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.repository.support.MapJobRepositoryFactoryBean;
import org.springframework.batch.support.transaction.ResourcelessTransactionManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableBatchProcessing
public class BaseBatchConfiguration {
    
    @Bean
	public ResourcelessTransactionManager resourcelessTransactionManager () {
		return new ResourcelessTransactionManager();
	}

	@Bean
	public JobRepository jobRepository (ResourcelessTransactionManager resourcelessTransactionManager) {
		MapJobRepositoryFactoryBean mapJobRepositoryFactoryBean = new MapJobRepositoryFactoryBean();
		mapJobRepositoryFactoryBean.setTransactionManager(resourcelessTransactionManager);

		try {
			return mapJobRepositoryFactoryBean.getObject();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	@Bean
	public JobLauncher jobLauncher (JobRepository jobRepository) {
		SimpleJobLauncher jobLauncher = new SimpleJobLauncher();
		jobLauncher.setJobRepository(jobRepository);
//		jobLauncher.setTaskExecutor(taskExecutor);
		
		return jobLauncher;
	}
}
