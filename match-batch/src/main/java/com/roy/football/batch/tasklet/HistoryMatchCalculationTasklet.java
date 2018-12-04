package com.roy.football.batch.tasklet;

import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;

import com.roy.football.match.service.HistoryMatchCalculationService;

public class HistoryMatchCalculationTasklet implements Tasklet{

	@Autowired
	private HistoryMatchCalculationService historyMatchCalculationService;

	@Override
	public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
		historyMatchCalculationService.process();
		return null;
	}

}
