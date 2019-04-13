package com.roy.football.match.jpa.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.collect.Lists;
import com.mysema.query.Tuple;
import com.mysema.query.jpa.impl.JPAQueryFactory;
import com.mysema.query.types.Projections;
import com.roy.football.match.OFN.MatchResultAnalyzer;
import com.roy.football.match.OFN.response.Company;
import com.roy.football.match.OFN.response.MatchResult;
import com.roy.football.match.base.League;
import com.roy.football.match.jpa.entities.calculation.EMatch;
import com.roy.football.match.jpa.entities.calculation.EMatchResultDetail;
import com.roy.football.match.util.DateUtil;

import lombok.extern.slf4j.Slf4j;

import java.text.ParseException;
import java.util.Collections;
import java.util.List;

import static com.roy.football.match.jpa.entities.calculation.QEMatch.eMatch;
import static com.roy.football.match.jpa.entities.calculation.QEMatchClubState.eMatchClubState;
import static com.roy.football.match.jpa.entities.calculation.QELatestMatchState.eLatestMatchState;
import static com.roy.football.match.jpa.entities.calculation.QEAsiaPk.eAsiaPk;
import static com.roy.football.match.jpa.entities.calculation.QEJiaoShou.eJiaoShou;
import static com.roy.football.match.jpa.entities.calculation.QEEuroPlCompany.eEuroPlCompany;
import static com.roy.football.match.jpa.entities.calculation.QEMatchResult.eMatchResult;
import static com.roy.football.match.jpa.entities.calculation.QEMatchResultDetail.eMatchResultDetail;

@Service
@Slf4j
@Transactional(readOnly = true, value = "transactionManager")
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
	
	public List<Tuple> findBaseAndPkMatches (String fromDate, String endDate, League le) throws ParseException {
		return jpaQueryFactory
			.from(eMatch)
				.join(eMatchClubState).on(eMatch.ofnMatchId.eq(eMatchClubState.ofnMatchId))
				.join(eLatestMatchState).on(eMatch.ofnMatchId.eq(eLatestMatchState.ofnMatchId))
				.join(eAsiaPk).on(eMatch.ofnMatchId.eq(eAsiaPk.ofnMatchId).and(eAsiaPk.company.eq(Company.Aomen)))
			.where(eMatch.matchTime.between(DateUtil.parseSimpleDateWithDash(fromDate), DateUtil.parseSimpleDateWithDash(endDate))
					.and(eMatch.league.eq(le))
					.and(eMatchClubState.hostAttGuestDefInx.isNotNull())
					.and(eLatestMatchState.hostAttackToGuest.isNotNull()))
			.list(eMatch.ofnMatchId,
					eAsiaPk.originPk,
					eMatchClubState.hostAttGuestDefInx, eMatchClubState.guestAttHostDefInx,
					eLatestMatchState.hostAttackToGuest, eLatestMatchState.guestAttackToHost,
					eLatestMatchState.hostAttackVariationToGuest, eLatestMatchState.guestAttackVariationToHost);
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
	
	public List<EMatchResultDetail> findLatestMatchResult(Long teamId, Boolean host, int limit) {
		try {
			return jpaQueryFactory
					.from(eMatchResultDetail)
						.join(eMatch).on(eMatchResultDetail.ofnMatchId.eq(eMatch.ofnMatchId)
								.and(eMatchResultDetail.league.eq(eMatch.league)))
					.where(host ? eMatchResultDetail.hostId.eq(teamId) : eMatchResultDetail.guestId.eq(teamId))
					.orderBy(eMatch.matchTime.desc())
					.limit(limit)
					.list(eMatchResultDetail);
		} catch (Exception e) {
			log.error("unable to find latest match result of team: {}", teamId);
		}
		
		return Lists.newArrayList();
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
