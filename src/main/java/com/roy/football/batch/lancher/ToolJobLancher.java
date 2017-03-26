package com.roy.football.batch.lancher;

import javax.batch.operations.JobRestartException;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersInvalidException;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import com.roy.football.batch.configuration.HistoryMatchCalculationJobConfiguration;
import com.roy.football.batch.configuration.ToolJobConfiguration;

public class ToolJobLancher {
	@Autowired
    JobLauncher jobLauncher;
 
    @Autowired
    @Qualifier("toolJob")
    Job toolJob;
 
    public static void main(String... args) throws JobParametersInvalidException, JobExecutionAlreadyRunningException, JobRestartException, JobInstanceAlreadyCompleteException, org.springframework.batch.core.repository.JobRestartException, InterruptedException {
 
        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(ToolJobConfiguration.class);
 
        ToolJobLancher main = context.getBean(ToolJobLancher.class);
 
        JobExecution jobExecution = main.jobLauncher.run(main.toolJob, new JobParameters());
 
        // do not close, since the sub-thread would use.
        Thread.sleep(5000);
        context.close();
 
    }
}
