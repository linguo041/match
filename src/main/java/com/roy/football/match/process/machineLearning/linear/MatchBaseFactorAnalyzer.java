package com.roy.football.match.process.machineLearning.linear;

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
public class MatchBaseFactorAnalyzer extends AbstractFactorAnalyzer {
	@Autowired
	private MatchComplexQueryService matchComplexQueryService;

	public Pair<double[], double[][]> getEvaluatedData(String fromDate, String endDate, League le) throws ParseException {
		List<Tuple> tuples = matchComplexQueryService.findGoalsPredictedMatches(fromDate, endDate, le);
		
		double[][] input = new double[tuples.size()][2];
		double[] expected = new double[tuples.size()];
		
		if (tuples != null && !tuples.isEmpty()) {
			for (int ii = 0; ii < tuples.size(); ii++) {
				Tuple tuple = tuples.get(ii);
				double score = tuple.get(eMatchResult.hostScore) - tuple.get(eMatchResult.guestScore);
				double [] each = {tuple.get(eMatchClubState.hostAttGuestDefInx), tuple.get(eMatchClubState.guestAttHostDefInx)};
				input[ii] = each;
				expected[ii] = score;
			}
			
			return Pair.of(expected, input);
		}
		
		return null;
	}
}
