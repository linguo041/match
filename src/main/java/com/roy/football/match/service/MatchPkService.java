package com.roy.football.match.service;

import static com.roy.football.match.jpa.entities.calculation.QEAsiaPk.eAsiaPk;
import static com.roy.football.match.jpa.entities.calculation.QEMatch.eMatch;

import java.text.ParseException;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.mysema.query.Tuple;
import com.mysema.query.jpa.impl.JPAQueryFactory;
import com.roy.football.match.OFN.response.AsiaPl;
import com.roy.football.match.OFN.response.Company;
import com.roy.football.match.base.League;
import com.roy.football.match.jpa.entities.calculation.EAsiaPk;
import com.roy.football.match.util.DateUtil;
import com.roy.football.match.util.PanKouUtil;
import com.roy.football.match.util.PanKouUtil.PKDirection;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class MatchPkService {
	@Autowired
	private JPAQueryFactory jpaQueryFactory;
	
	public List<EAsiaPk> findGoalsPredictedMatches (String fromDate, String endDate) throws ParseException {
		return jpaQueryFactory
			.from(eMatch)
				.join(eAsiaPk).on(eMatch.ofnMatchId.eq(eAsiaPk.ofnMatchId).and(eAsiaPk.company.eq(Company.Aomen)))
			.where(eMatch.matchTime.between(DateUtil.parseSimpleDateWithDash(fromDate), DateUtil.parseSimpleDateWithDash(endDate)))
			.list(eAsiaPk);
	}
	
	public void checkPKDirection (String fromDate, String endDate) {
		List<EAsiaPk> asias = null;
		
		try {
			asias = findGoalsPredictedMatches(fromDate, endDate);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		if (asias != null && !asias.isEmpty()) {
			asias.stream().forEach(asia -> {
				AsiaPl main = new AsiaPl(asia.getMainHWin(), asia.getMainAWin(), asia.getMainPk());
				AsiaPl current = new AsiaPl(asia.getCurrentHWin(), asia.getCurrentAWin(), asia.getCurrentPk());
				
				PKDirection direction = PanKouUtil.getPKDirection(current, main);
				
				System.out.println(String.format("%-5f  %-5f  %-5f",
						main.gethWin(), main.getaWin(), main.getPanKou()));
				System.out.println(String.format("%-5f  %-5f  %-5f %s",
						current.gethWin(), current.getaWin(), current.getPanKou(), direction));
			});
			
			
		}
	}
}
