package com.roy.football.match.ml;

import static com.roy.football.match.jpa.entities.calculation.QEJiaoShou.eJiaoShou;
import static com.roy.football.match.jpa.entities.calculation.QELatestMatchState.eLatestMatchState;
import static com.roy.football.match.jpa.entities.calculation.QEMatch.eMatch;
import static com.roy.football.match.jpa.entities.calculation.QEMatchClubState.eMatchClubState;
import static com.roy.football.match.jpa.entities.calculation.QEMatchResult.eMatchResult;
import static com.roy.football.match.jpa.entities.calculation.QEAsiaPk.eAsiaPk;
import static com.roy.football.match.jpa.entities.calculation.QEEuroPlState.eEuroPlState;
import static com.roy.football.match.jpa.entities.calculation.QEEuroPlCompany.eEuroPlCompany;

import java.text.ParseException;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import com.mysema.query.Tuple;
import com.mysema.query.jpa.impl.JPAQueryFactory;
import com.roy.football.match.base.League;
import com.roy.football.match.util.DateUtil;

public class DataPreparingService {
	@Autowired
	private JPAQueryFactory jpaQueryFactory;
	
	public List<Tuple> findGoalsPredictedMatches (String fromDate, String endDate, League le) throws ParseException {
		return jpaQueryFactory
			.from(eMatch)
				.join(eMatchClubState).on(eMatch.ofnMatchId.eq(eMatchClubState.ofnMatchId))
				.join(eLatestMatchState).on(eMatch.ofnMatchId.eq(eLatestMatchState.ofnMatchId))
//				.join(eJiaoShou).on(eMatch.ofnMatchId.eq(eJiaoShou.ofnMatchId))
				.join(eAsiaPk).on(eMatch.ofnMatchId.eq(eAsiaPk.ofnMatchId))
				.join(eEuroPlState).on(eMatch.ofnMatchId.eq(eEuroPlState.ofnMatchId))
				.join(eEuroPlCompany).on(eMatch.ofnMatchId.eq(eEuroPlCompany.ofnMatchId))
				.join(eMatchResult).on(eMatch.ofnMatchId.eq(eMatchResult.ofnMatchId))
			.where(eMatch.matchTime.between(DateUtil.parseSimpleDateWithDash(fromDate), DateUtil.parseSimpleDateWithDash(endDate))
					.and(eMatch.league.eq(le))
					.and(eMatchClubState.hostAttGuestDefInx.isNotNull())
					.and(eLatestMatchState.hostAttackToGuest.isNotNull())
					.and(eJiaoShou.hgoalPerMatch.isNotNull()))
			.list(eMatch.ofnMatchId, eMatchResult.hostScore, eMatchResult.guestScore,
					// base
					eMatchClubState.hostAttGuestDefInx, eMatchClubState.guestAttHostDefInx,
					// latest matches
					eLatestMatchState.hotPoint, eLatestMatchState.hostAttackToGuest, eLatestMatchState.guestAttackToHost,
					// pk
					eAsiaPk.currentPk, eAsiaPk.hwinChangeRate,
					// EU
					eEuroPlCompany.currentEWin, eEuroPlCompany.currentEDraw, eEuroPlCompany.currentELose,
					eEuroPlCompany.winChange, eEuroPlCompany.drawChange, eEuroPlCompany.loseChange);
	}
}
