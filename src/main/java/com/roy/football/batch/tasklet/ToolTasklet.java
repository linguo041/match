package com.roy.football.batch.tasklet;

import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;

import com.roy.football.match.base.League;
import com.roy.football.match.crawler.controller.OFNMatchService;
import com.roy.football.match.jpa.EntityConverter;
import com.roy.football.match.jpa.entities.calculation.ELeague;
import com.roy.football.match.jpa.repositories.ELeagueRepository;
import com.roy.football.match.service.HistoryMatchCalculationService;
import com.roy.football.match.service.MatchEuroRecalculateService;
import com.roy.football.match.service.TeamService;

public class ToolTasklet implements Tasklet{
	
	@Autowired
	private ELeagueRepository eLeagueRepository;
	
	@Autowired 
	private MatchEuroRecalculateService matchEuroRecalculateService;
	
	@Autowired 
	private TeamService teamService;
	
	@Autowired
	private OFNMatchService ofnMatchService;


	@Override
	public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {

//		recalculateMissedEuro();
//		fetchTeamRanking();
//		fetchTeamName();
		processOneMatch();
		
		return null;
	}
	
	private void processOneMatch () {
		ofnMatchService.processMatch(1046629L, 170315008L, League.RiLianBei);
	}
	
	private void saveLeagues () {
		for (League le : League.values()) {
			ELeague eleague = EntityConverter.toEleague(le);
			
			eLeagueRepository.save(eleague);
		}
	}

	private void recalculateMissedEuro () {
		matchEuroRecalculateService.recalculate();
	}
	
	private void fetchTeamName () {
		teamService.fetchTeamName();
	}
	
	private void fetchTeamRanking () {
		teamService.fetchTeamRanking();;
	}

}
