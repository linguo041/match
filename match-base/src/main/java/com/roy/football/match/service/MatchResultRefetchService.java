package com.roy.football.match.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import com.google.common.collect.Lists;
import com.mysema.query.jpa.impl.JPAQueryFactory;
import com.roy.football.match.OFN.CalculationType;
import com.roy.football.match.OFN.MatchResultCalculator;
import com.roy.football.match.jpa.entities.calculation.EMatch;
import com.roy.football.match.util.DateUtil;

import static com.roy.football.match.jpa.entities.calculation.QEMatch.eMatch;
import static com.roy.football.match.jpa.entities.calculation.QEMatchResultDetail.eMatchResultDetail;

import java.text.ParseException;
import java.util.List;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class MatchResultRefetchService {
	
	@Autowired
	private MatchResultCalculator matchResultCalculator;
	
	@Autowired
	private JPAQueryFactory jpaQueryFactory;
	
	public void recrawMatches () {
		List<EMatch> matches = getMatchesWithoutResDetails();
		
		if (CollectionUtils.isEmpty(matches)) {
			log.info("No proper match found to recraw result.");
			return;
		}
		
		matches.stream().forEach(match -> matchResultCalculator.calculateAndPersist(match, null, null, true));
	}
	
	public List<EMatch> getMatchesWithoutResDetails () {
		try {
			return jpaQueryFactory
				.from(eMatch).join(eMatchResultDetail)
					.on(eMatch.ofnMatchId.eq(eMatchResultDetail.ofnMatchId))
				.where(eMatch.phase.eq(CalculationType.resulted)
						.and(eMatch.matchTime.after(DateUtil.parseSimpleDateWithDash("2015-10-01")))
						.and(eMatchResultDetail.hostCorner.isNull().or(eMatchResultDetail.hostTime.isNull())))
				.list(eMatch);
		} catch (ParseException e) {
			log.warn("Parse date with error.");
		}
		return Lists.newArrayList();
	}

}
