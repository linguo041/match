package com.roy.football.batch.configuration;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.PropertySource;

import com.roy.football.batch.lancher.ToolJobLancher;
import com.roy.football.batch.tasklet.ToolTasklet;
import com.roy.football.match.MatchConfiguration;
import com.roy.football.match.jpa.configure.MatchJpaConfiguration;

@Configuration
@PropertySource("classpath:/translate.properties")
@Import({BaseBatchConfiguration.class, MatchConfiguration.class, MatchJpaConfiguration.class})
//@ComponentScan(basePackageClasses = { BatchPackageScanned.class})
public class ToolJobConfiguration {
	@Bean
	public Tasklet toolTasklet () {
		return new ToolTasklet();
	}
	
	@Bean
    public Step step1(StepBuilderFactory stepBuilderFactory, Tasklet toolTasklet) {
		return stepBuilderFactory.get("step1")
				.tasklet(toolTasklet)
				.build();
	}

	@Bean
    public Job toolJob(JobBuilderFactory jobs, Step step1) {
		return jobs.get("toolJob")
				.incrementer(new RunIdIncrementer())
				.flow(step1)
				.end()
				.build();
	}
	
	@Bean ToolJobLancher toolJobLancher() {
		return new ToolJobLancher();
	}
}
