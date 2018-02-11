package com.roy.football.match.process.machineLearning.linear;

import static com.roy.football.match.jpa.entities.calculation.QEAsiaPk.eAsiaPk;
import static com.roy.football.match.jpa.entities.calculation.QEJiaoShou.eJiaoShou;
import static com.roy.football.match.jpa.entities.calculation.QELatestMatchState.eLatestMatchState;
import static com.roy.football.match.jpa.entities.calculation.QEMatch.eMatch;
import static com.roy.football.match.jpa.entities.calculation.QEMatchClubState.eMatchClubState;
import static com.roy.football.match.jpa.entities.calculation.QEMatchResult.eMatchResult;

import java.text.ParseException;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.mysema.commons.lang.Pair;
import com.mysema.query.Tuple;
import com.roy.football.match.base.League;
import com.roy.football.match.jpa.service.MatchComplexQueryService;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class PreditPkByBaseAnalyzer extends AbstractFactorAnalyzer {
	private static final String LOG_FORMAT_HEADER = "pk, host_att_guest_def, gust_att_host_def";
	private static final String LOG_FORMAT_CONTENT = "%-10f      %-5f    %-5f";
	

	@Autowired
	private MatchComplexQueryService matchComplexQueryService;
	
	@Override
	public Pair<double[], double[][]> getEvaluatedData(String fromDate, String endDate, League le)
			throws ParseException {
		List<Tuple> tuples = matchComplexQueryService.findBaseAndPkMatches(fromDate, endDate, le);
		
		double[][] input = new double[tuples.size()][2];
		double[] expected = new double[tuples.size()];
		
		log.info("Total matches: {}", tuples.size());
		
		if (tuples != null && !tuples.isEmpty()) {
			System.out.println(LOG_FORMAT_HEADER);
			for (int ii = 0; ii < tuples.size(); ii++) {
				Tuple tuple = tuples.get(ii);
//				double realScore = tuple.get(eMatchResult.hostScore) + tuple.get(eMatchResult.guestScore);
//				double baseScore = tuple.get(eMatchClubState.hostAttGuestDefInx) + tuple.get(eMatchClubState.guestAttHostDefInx);
//				double latestScore = tuple.get(eLatestMatchState.hostAttackToGuest) + tuple.get(eLatestMatchState.guestAttackToHost);
//				double jsScore = tuple.get(eJiaoShou.hgoalPerMatch) + tuple.get(eJiaoShou.ggoalPerMatch);
//				double [] each = {baseScore, latestScore, jsScore};
				double [] each = {tuple.get(eMatchClubState.hostAttGuestDefInx), tuple.get(eMatchClubState.guestAttHostDefInx)};
				input[ii] = each;
				expected[ii] = tuple.get(eAsiaPk.originPk);
				
				System.out.println(String.format(LOG_FORMAT_CONTENT, tuple.get(eAsiaPk.originPk),
						tuple.get(eMatchClubState.hostAttGuestDefInx), tuple.get(eMatchClubState.guestAttHostDefInx)));
			}
		}
		
		return Pair.of(expected, input);
	}

}
