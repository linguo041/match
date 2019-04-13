package com.roy.football.match.OFN;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.roy.football.match.OFN.parser.OFNResultCrawler;
import com.roy.football.match.OFN.response.Company;
import com.roy.football.match.OFN.response.MatchResult;
import com.roy.football.match.base.ResultGroup;
import com.roy.football.match.jpa.EntityConverter;
import com.roy.football.match.jpa.entities.calculation.EAsiaPk;
import com.roy.football.match.jpa.entities.calculation.EDaXiaoPk;
import com.roy.football.match.jpa.entities.calculation.EMatch;
import com.roy.football.match.jpa.entities.calculation.EMatchResult;
import com.roy.football.match.jpa.entities.calculation.EMatchResultDetail;
import com.roy.football.match.jpa.repositories.AsiaPkRepository;
import com.roy.football.match.jpa.repositories.DaXiaoPkRepository;
import com.roy.football.match.jpa.repositories.MatchRepository;
import com.roy.football.match.jpa.repositories.MatchResultRepository;
import com.roy.football.match.util.MatchUtil;

@Service
public class MatchResultCalculator {
	@Autowired
	private OFNResultCrawler ofnResultCrawler;
	
	@Autowired
	private MatchResultRepository matchResultRepository;
	
	@Autowired
	private AsiaPkRepository asiaPkRepository;
	
	@Autowired
	private DaXiaoPkRepository daXiaoPkRepository;
	
	@Autowired
	private MatchRepository matchRepository;

	@Transactional
	public void calculateAndPersist (EMatch match, Integer hostScore, Integer guestScore) {
		calculateAndPersist(match, hostScore, guestScore, false);
	}
	
	public void calculateAndPersist (EMatch match, Integer hostScore, Integer guestScore, boolean recraw) {
		Long ofnMatchId = match.getOfnMatchId();
		
		if (!MatchUtil.isMatchFinishedNow(match.getMatchTime())) {
			return;
		}
		
		EMatchResult dbResult = matchResultRepository.findOne(ofnMatchId);
		
		if (recraw || dbResult == null) {
			MatchResult result = ofnResultCrawler.craw(ofnMatchId);
			if (result == null && hostScore != null) {
				result = new MatchResult();
				result.setHostScore(hostScore);
				result.setGuestScore(guestScore);
			}
			
			if (result != null) {
				EMatchResult matchResult = EntityConverter.toEMatchResult(ofnMatchId, result);

				matchResult.setPkResult(checkPkResult(ofnMatchId, result));
				matchResult.setDxResult(checkDaXiaoResult(ofnMatchId, result));
				matchResult.setPlResult(checkPlResult(result));
				
				EMatchResultDetail detail = matchResult.getEMatchResultDetail();
				detail.setHostId(match.getHostId());
				detail.setHostName(match.getHostName());
				detail.setGuestId(match.getGuestId());
				detail.setGuestName(match.getGuestName());
				detail.setLeague(match.getLeague());
				
				matchResultRepository.save(matchResult);
				
				if (CalculationType.resulted != match.getPhase()) {
					match.setPhase(CalculationType.resulted);
					matchRepository.save(match);
				}
			}
		}
	}
	
	private ResultGroup checkPkResult (Long ofnMatchId, MatchResult result) {
		EAsiaPk asiaPk = asiaPkRepository.findByOfnMatchIdAndCompany(ofnMatchId, Company.Aomen);
		if (asiaPk != null) {
			float pk = asiaPk.getCurrentPk();
			float pkResult = result.getHostScore() - result.getGuestScore();
			if (pkResult > pk) {
				return ResultGroup.RangThree;
			} else if (pkResult == pk) {
				return ResultGroup.One;
			} else {
				return ResultGroup.RangZero;
			}
		}
		
		return null;
	}
	
	private ResultGroup checkDaXiaoResult (Long ofnMatchId, MatchResult result) {
		EDaXiaoPk daxiaoPk = daXiaoPkRepository.findOne(ofnMatchId);
		if (daxiaoPk != null) {
			float pk = daxiaoPk.getCurrentPk();
			float daoXiaoResult = result.getHostScore() + result.getGuestScore();
			if (daoXiaoResult > pk) {
				return ResultGroup.Three;
			} else if (daoXiaoResult == pk) {
				return ResultGroup.One;
			} else {
				return ResultGroup.Zero;
			}
		}
		
		return null;
	}
	
	private ResultGroup checkPlResult (MatchResult result) {
		float res = result.getHostScore() - result.getGuestScore();
		
		if (res > 0) {
			return ResultGroup.Three;
		} else if (res == 0) {
			return ResultGroup.One;
		} else {
			return ResultGroup.Zero;
		}
	}
}
