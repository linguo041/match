package com.roy.football.batch.tasklet;

import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.math3.util.FastMath;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;

import com.google.common.collect.Lists;
import com.mysema.query.jpa.impl.JPAQueryFactory;
import com.roy.football.match.OFN.response.JinCaiSummary.JinCaiMatch;
import com.roy.football.match.base.LatestMatchMatrixType;
import com.roy.football.match.base.League;
import com.roy.football.match.crawler.controller.OFNMatchService;
import com.roy.football.match.jpa.EntityConverter;
import com.roy.football.match.jpa.entities.calculation.ELatestMatchDetail;
import com.roy.football.match.jpa.entities.calculation.ELatestMatchState;
import com.roy.football.match.jpa.entities.calculation.ELeague;
import com.roy.football.match.jpa.entities.calculation.EMatch;
import com.roy.football.match.jpa.entities.calculation.EMatchClubState;
import com.roy.football.match.jpa.repositories.ELeagueRepository;
import com.roy.football.match.jpa.repositories.LatestMatchStateRepository;
import com.roy.football.match.jpa.repositories.MatchClubStateRepository;
import com.roy.football.match.jpa.repositories.MatchRepository;
import com.roy.football.match.jpa.service.MatchComplexQueryService;
import com.roy.football.match.service.HistoryMatchCalculationService;
import com.roy.football.match.service.MatchEuroRecalculateService;
import com.roy.football.match.service.MatchPkService;
import com.roy.football.match.service.MatchResultRefetchService;
import com.roy.football.match.service.PredictScoreFactorClusterService;
import com.roy.football.match.service.TeamService;
import com.roy.football.match.util.DateUtil;

import static com.roy.football.match.jpa.entities.calculation.QEMatch.eMatch;

public class ToolTasklet implements Tasklet{
	
	@Autowired
	private ELeagueRepository eLeagueRepository;
	
	@Autowired 
	private MatchEuroRecalculateService matchEuroRecalculateService;
	
	@Autowired 
	private TeamService teamService;
	
	@Autowired
	private OFNMatchService ofnMatchService;
	
	@Autowired
	private LatestMatchStateRepository latestMatchStateRepository;
	
	@Autowired
	private MatchClubStateRepository matchClubStateRepository;
	
	@Autowired
	private PredictScoreFactorClusterService predictScoreFactorClusterService;
	
	@Autowired
	private MatchResultRefetchService matchResultRefetchService;
	
	@Autowired
	private MatchComplexQueryService matchComplexQueryService;
	
	@Autowired
	private MatchPkService matchPkService;
	
	@Autowired
	private MatchRepository matchRepository;
	
	@Override
	public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {

//		recalculateMissedEuro();
//		fetchTeamRanking();
//		fetchTeamName();
//		saveLeagues();
		processOneMatch();
//		processMatches();
//		recrawResult();
//		checkPkDirection();
//		predictFactor();
//		adjustVariance();
		
		return null;
	}
	
	private void checkPkDirection () {
		matchPkService.checkPKDirection("2017-09-11 19:00:00", "2017-09-14 11:00:00");
	}
	
	private void recrawResult () {
		matchResultRefetchService.recrawMatches();
	}
	
	private void processOneMatch () {
		EMatch  match = matchRepository.findOne(1146922L);
		
		ofnMatchService.processMatches(Lists.newArrayList(JinCaiMatch.fromDBMatch(match)));
	}
	
	private void processMatches () {
		List<EMatch> matches = matchComplexQueryService.findMatchesByDateRange("2017-12-29 12:00:00", "2017-12-31 11:00:00");
		
		if (matches != null && !matches.isEmpty()) {
			List<JinCaiMatch> jcMatches = matches.stream().map(match -> {
				JinCaiMatch jcMatch = new JinCaiMatch();
				jcMatch.setOddsmid(match.getOfnMatchId());
				jcMatch.setXid(match.getMatchDayId());
				jcMatch.setLid(match.getLeague().getLeagueId());
				jcMatch.setMtime(match.getMatchTime());
				return jcMatch;
			}).collect(Collectors.toList());
			
			ofnMatchService.processMatches(jcMatches);
		}
		
	}
	
	private void saveLeagues () {
		for (League le : League.values()) {
			ELeague eleagueInDB = eLeagueRepository.findOne(le.getLeagueId());
			
			if (eleagueInDB != null) {
				eleagueInDB.setMainCompany(le.getMajorCompany());
				eleagueInDB.setContinent(le.getContinent());
				eleagueInDB.setTeamNum(le.getClubNum());
				eleagueInDB.setState(le.isState());
				eLeagueRepository.save(eleagueInDB);
			} else {
				ELeague eleague = EntityConverter.toEleague(le);
				eLeagueRepository.save(eleague);
			}
		}
	}

	private void recalculateMissedEuro () {
		matchEuroRecalculateService.recalculate();
	}
	
	private void fetchTeamName () {
		teamService.fetchTeamName();
	}
	
	private void fetchTeamRanking () {
		teamService.fetchTeamRanking();
	}
	
	private void predictFactor () {
//		predictScoreFactorClusterService.evaluateBaseFactor();
//		predictScoreFactorClusterService.evaluateAllFactors();
		predictScoreFactorClusterService.evaluatePkByBase();
	}
	
	private void adjustVariance () {
		Iterable<ELatestMatchState> states = latestMatchStateRepository.findAll();
		Iterator<ELatestMatchState> its = states.iterator();
		while (its.hasNext()) {
			ELatestMatchState state = its.next();
			
//			ELatestMatchState state = latestMatchStateRepository.findOne(1092723L);
			
			System.out.println(state.toString());
			
			EMatchClubState club = matchClubStateRepository.findOne(state.getOfnMatchId());
			
			if (club == null) {
				continue;
			}
			
			int levelDiff = club.getHostLevel().ordinal() - club.getGuestLevel().ordinal();
			
			Set<ELatestMatchDetail> details = state.getLatestDetails();
			
			ELatestMatchDetail hostMatches = null;
			ELatestMatchDetail guestMatches = null;
			
			for (ELatestMatchDetail detail : details) {
//				Float gv = detail.getGVariation();
//				Float mv = detail.getMVariation();
				
//				detail.setGVariation((float)FastMath.sqrt(gv));
//				detail.setMVariation((float)FastMath.sqrt(mv));
				
				if (LatestMatchMatrixType.hostHome5 == detail.getType()
						|| LatestMatchMatrixType.host6 == detail.getType() && hostMatches == null) {
					hostMatches = detail;
				}
				if (LatestMatchMatrixType.guestAway5 == detail.getType()
						|| LatestMatchMatrixType.guest6 == detail.getType() && guestMatches == null) {
					guestMatches = detail;
				}
			}
			
			if (hostMatches == null || guestMatches == null) {
				continue;
			}
			
			
			// h_goal = (h_goal_avg - h_variance * (h_goal_avg - g_lose_avg) / h_goal_avg) *(4 - (h_level - g_level))/7
			//        + (g_lose_avg + g_variance * (h_goal_avg - g_lose_avg) / g_lose_avg) *(4 + (h_level - g_level))/7
			float hgoal =  (hostMatches.getMatchGoal() == 0 ? 0 : (hostMatches.getMatchGoal()
								 - hostMatches.getGVariation() * (hostMatches.getMatchGoal() - guestMatches.getMatchMiss())/Math.max(hostMatches.getMatchGoal(), guestMatches.getMatchMiss())
						   ) * (4 - levelDiff)/7)
						 + (guestMatches.getMatchMiss() == 0 ? 0 : (guestMatches.getMatchMiss()
								 + guestMatches.getMVariation() * (hostMatches.getMatchGoal() - guestMatches.getMatchMiss())/Math.max(hostMatches.getMatchGoal(), guestMatches.getMatchMiss())
						   ) * (4 + levelDiff)/7);
			// g_goal = (g_goal_avg - g_variance * (g_goal_avg - h_lose_avg) / g_goal_avg) *(4 + (h_level - g_level))/7
			//        + (h_lose_avg + h_variance * (g_goal_avg - h_lose_avg) / h_lose_avg) *(4 - (h_level - g_level))/7
			float ggoal = (guestMatches.getMatchGoal() == 0 ? 0 : (guestMatches.getMatchGoal()
								- guestMatches.getGVariation() * (guestMatches.getMatchGoal() - hostMatches.getMatchMiss())/Math.max(guestMatches.getMatchGoal(), hostMatches.getMatchMiss())
						  ) * (4 + levelDiff)/7)
						+ (hostMatches.getMatchMiss() == 0 ? 0 : (hostMatches.getMatchMiss()
								+ hostMatches.getMVariation() * (guestMatches.getMatchGoal() - hostMatches.getMatchMiss())/Math.max(guestMatches.getMatchGoal(), hostMatches.getMatchMiss())
						  ) * (4 - levelDiff)/7);
			
			float hvariation = hostMatches.getGVariation() * (4 - levelDiff)/7 + guestMatches.getMVariation() * (4 + levelDiff)/7;
			float gvariation = guestMatches.getGVariation() * (4 + levelDiff)/7 + hostMatches.getMVariation() * (4 - levelDiff)/7;
			
			state.setHostAttackToGuest(hgoal);
			state.setGuestAttackToHost(ggoal);
			state.setHostAttackVariationToGuest(hvariation);
			state.setGuestAttackVariationToHost(gvariation);
			
			latestMatchStateRepository.save(state);
			
			System.out.println(state.toString());
		}
		
	}

}
