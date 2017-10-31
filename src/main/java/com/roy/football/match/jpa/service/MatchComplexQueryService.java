package com.roy.football.match.jpa.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.collect.Lists;
import com.mysema.query.Tuple;
import com.mysema.query.jpa.impl.JPAQueryFactory;
import com.roy.football.match.OFN.response.Company;
import com.roy.football.match.base.League;
import com.roy.football.match.jpa.entities.calculation.EMatch;
import com.roy.football.match.util.DateUtil;
import java.text.ParseException;
import java.util.Collections;
import java.util.List;

import static com.roy.football.match.jpa.entities.calculation.QEMatch.eMatch;
import static com.roy.football.match.jpa.entities.calculation.QEMatchClubState.eMatchClubState;
import static com.roy.football.match.jpa.entities.calculation.QELatestMatchState.eLatestMatchState;
import static com.roy.football.match.jpa.entities.calculation.QEJiaoShou.eJiaoShou;
import static com.roy.football.match.jpa.entities.calculation.QEEuroPlCompany.eEuroPlCompany;
import static com.roy.football.match.jpa.entities.calculation.QEMatchResult.eMatchResult;

@Service
public class MatchComplexQueryService {
	@Autowired
	private JPAQueryFactory jpaQueryFactory;
	
	public List<Tuple> findGoalsPredictedMatches (String fromDate, String endDate, League le) throws ParseException {
		return jpaQueryFactory
			.from(eMatch)
				.join(eMatchClubState).on(eMatch.ofnMatchId.eq(eMatchClubState.ofnMatchId))
				.join(eLatestMatchState).on(eMatch.ofnMatchId.eq(eLatestMatchState.ofnMatchId))
				.join(eJiaoShou).on(eMatch.ofnMatchId.eq(eJiaoShou.ofnMatchId))
				.join(eMatchResult).on(eMatch.ofnMatchId.eq(eMatchResult.ofnMatchId))
			.where(eMatch.matchTime.between(DateUtil.parseSimpleDateWithDash(fromDate), DateUtil.parseSimpleDateWithDash(endDate))
					.and(eMatch.league.eq(le))
					.and(eMatchClubState.hostAttGuestDefInx.isNotNull())
					.and(eLatestMatchState.hostAttackToGuest.isNotNull())
					.and(eJiaoShou.hgoalPerMatch.isNotNull()))
			.list(eMatch.ofnMatchId, eMatchResult.hostScore, eMatchResult.guestScore,
					eMatchClubState.hostAttGuestDefInx, eMatchClubState.guestAttHostDefInx,
					eLatestMatchState.hostAttackToGuest, eLatestMatchState.guestAttackToHost,
					eLatestMatchState.hostAttackVariationToGuest, eLatestMatchState.guestAttackVariationToHost,
					eJiaoShou.hgoalPerMatch, eJiaoShou.ggoalPerMatch);
	}
	
	public List<Tuple> findMatchesWithJCLatestPl (String fromDate, String endDate, League le) throws ParseException {
		return jpaQueryFactory
			.from(eMatch)
				.join(eMatchClubState).on(eMatch.ofnMatchId.eq(eMatchClubState.ofnMatchId))
				.join(eEuroPlCompany).on(eMatch.ofnMatchId.eq(eEuroPlCompany.ofnMatchId).and(eEuroPlCompany.company.eq(Company.Jincai)))
				.join(eMatchResult).on(eMatch.ofnMatchId.eq(eMatchResult.ofnMatchId))
			.where(eMatch.matchTime.between(DateUtil.parseSimpleDateWithDash(fromDate), DateUtil.parseSimpleDateWithDash(endDate))
					.and(eMatch.league.eq(le))
					.and(eMatchClubState.hostAttGuestDefInx.isNotNull())
					.and(eLatestMatchState.hostAttackToGuest.isNotNull())
					.and(eJiaoShou.hgoalPerMatch.isNotNull()))
			.list(eMatch.ofnMatchId, eMatchResult.hostScore, eMatchResult.guestScore,
					eMatchClubState.hostAttGuestDefInx, eMatchClubState.guestAttHostDefInx,
					eLatestMatchState.hostAttackToGuest, eLatestMatchState.guestAttackToHost,
					eLatestMatchState.hostAttackVariationToGuest, eLatestMatchState.guestAttackVariationToHost,
					eJiaoShou.hgoalPerMatch, eJiaoShou.ggoalPerMatch);
	}
	
	public List<EMatch> findMatchesByDateRange (String from, String to) {
		try {
			return jpaQueryFactory.from(eMatch)
					.where(eMatch.matchTime.between(
							DateUtil.parseDateWithDataBase(from),
							DateUtil.parseDateWithDataBase(to)))
					.list(eMatch);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return Collections.EMPTY_LIST;
	}
}
