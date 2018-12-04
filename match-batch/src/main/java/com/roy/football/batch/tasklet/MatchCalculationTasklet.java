package com.roy.football.batch.tasklet;

import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;

import com.roy.football.match.crawler.controller.OFNMatchService;

public class MatchCalculationTasklet implements Tasklet{
	
	@Autowired
	private OFNMatchService ofnMatchService;

	public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
		ofnMatchService.process();
		return null;
	}

}
