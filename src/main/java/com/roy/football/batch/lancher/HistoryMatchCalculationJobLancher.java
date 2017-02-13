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

public class HistoryMatchCalculationJobLancher {
	@Autowired
    JobLauncher jobLauncher;
 
    @Autowired
    @Qualifier("historyMatchCalculationJob")
    Job historyMatchCalculationJob;
 
    public static void main(String... args) throws JobParametersInvalidException, JobExecutionAlreadyRunningException, JobRestartException, JobInstanceAlreadyCompleteException, org.springframework.batch.core.repository.JobRestartException {
 
        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(HistoryMatchCalculationJobConfiguration.class);
 
        HistoryMatchCalculationJobLancher main = context.getBean(HistoryMatchCalculationJobLancher.class);
 
        JobExecution jobExecution = main.jobLauncher.run(main.historyMatchCalculationJob, new JobParameters());
 
        // do not close, since the sub-thread would use.
        context.close();
 
    }
}
