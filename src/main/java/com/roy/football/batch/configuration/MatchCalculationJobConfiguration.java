package com.roy.football.batch.configuration;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import com.roy.football.batch.BatchPackageScanned;
import com.roy.football.batch.tasklet.MatchCalculationTasklet;
import com.roy.football.match.MatchConfiguration;
import com.roy.football.match.jpa.configure.MatchJpaConfiguration;


@Configuration
@Import({BaseBatchConfiguration.class, MatchConfiguration.class, MatchJpaConfiguration.class})
@ComponentScan(basePackageClasses = { BatchPackageScanned.class})
public class MatchCalculationJobConfiguration {
	@Bean
	public Tasklet matchCalculationTasklet () {
		return new MatchCalculationTasklet();
	}
	
	@Bean
    public Step step1(StepBuilderFactory stepBuilderFactory, Tasklet matchCalculationTasklet) {
		return stepBuilderFactory.get("step1")
				.tasklet(matchCalculationTasklet)
				.build();
	}

	@Bean
    public Job matchCalculationJob(JobBuilderFactory jobs, Step s1) {
		return jobs.get("matchCalculateJob")
				.incrementer(new RunIdIncrementer())
				.flow(s1)
				.end()
				.build();
	}
}
