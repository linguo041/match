package com.roy.football.batch.configuration;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.launch.support.SimpleJobLauncher;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.repository.support.MapJobRepositoryFactoryBean;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.support.transaction.ResourcelessTransactionManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import com.roy.football.batch.BatchPackageScanned;
import com.roy.football.batch.lancher.HistoryMatchCalculationJobLancher;
import com.roy.football.batch.tasklet.HistoryMatchCalculationTasklet;
import com.roy.football.batch.tasklet.MatchCalculationTasklet;
import com.roy.football.match.MatchConfiguration;
import com.roy.football.match.jpa.configure.MatchJpaConfiguration;

@Configuration
@Import({BaseBatchConfiguration.class, MatchConfiguration.class, MatchJpaConfiguration.class})
//@ComponentScan(basePackageClasses = { BatchPackageScanned.class})
public class HistoryMatchCalculationJobConfiguration {
	@Bean
	public Tasklet historyMatchCalculationTasklet () {
		return new HistoryMatchCalculationTasklet();
	}
	
	@Bean
    public Step step1(StepBuilderFactory stepBuilderFactory, Tasklet historyMatchCalculationTasklet) {
		return stepBuilderFactory.get("step1")
				.tasklet(historyMatchCalculationTasklet)
				.build();
	}

	@Bean
    public Job historyMatchCalculationJob(JobBuilderFactory jobs, Step step1) {
		return jobs.get("historyMatchCalculationJob")
				.incrementer(new RunIdIncrementer())
				.flow(step1)
				.end()
				.build();
	}
	
	@Bean HistoryMatchCalculationJobLancher historyMatchCalculationJobLancher() {
		return new HistoryMatchCalculationJobLancher();
	}
}
