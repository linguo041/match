package com.roy.football.match.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.roy.football.match.OFN.CalculationType;
import com.roy.football.match.OFN.MatchResultCalculator;
import com.roy.football.match.OFN.OFNCalcucator;
import com.roy.football.match.OFN.out.OFNExcelData;
import com.roy.football.match.OFN.out.OFNOutputFormater;
import com.roy.football.match.OFN.parser.OFNHtmlParser;
import com.roy.football.match.OFN.parser.OFNParser;
import com.roy.football.match.OFN.parser.OFNResultCrawler;
import com.roy.football.match.OFN.response.AsiaPl;
import com.roy.football.match.OFN.response.Company;
import com.roy.football.match.OFN.response.EuroPl;
import com.roy.football.match.OFN.response.OFNMatchData;
import com.roy.football.match.OFN.response.JinCaiSummary.JinCaiMatch;
import com.roy.football.match.OFN.response.MatchResult;
import com.roy.football.match.OFN.statics.matrices.OFNCalculateResult;
import com.roy.football.match.base.League;
import com.roy.football.match.eightwin.EWJincaiParser;
import com.roy.football.match.fivemillion.FMParser;
import com.roy.football.match.fivemillion.FmRawMatch;
import com.roy.football.match.jpa.entities.calculation.EMatch;
import com.roy.football.match.jpa.entities.calculation.EMatchResult;
import com.roy.football.match.jpa.repositories.MatchRepository;
import com.roy.football.match.jpa.repositories.MatchResultRepository;
import com.roy.football.match.jpa.service.MatchPersistService;
import com.roy.football.match.okooo.OkoooMatchCrawler;
import com.roy.football.match.util.MatchUtil;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class HistoryMatchCalculationService {
//	@Autowired
//	private OFNParser parser;
	@Autowired
	private OFNHtmlParser parser;
	@Autowired
	private EWJincaiParser ewJincaiParser;
	@Autowired
	private OkoooMatchCrawler okoooMatchCrawler;
	@Autowired
	private OFNCalcucator calculator;
	
	@Autowired
	private MatchPersistService matchPersistService;

	@Autowired
	public ExecutorService calculateExecutorService;
	
	@Autowired
	private MatchRepository matchRepository;
	
	@Autowired
	private MatchResultCalculator matchResultCalculator;
	
	@Autowired
	private FMParser fmParser;
	
	public void process () {
		List<EMatch> ematches = matchRepository.findMatchesWithoutResult();
		
		if (ematches != null && ematches.size() > 0) {
			List<Future<Void>> futures = Lists.newArrayList();
			
			for (EMatch ematch : ematches) {
				if (ematch != null && ematch.getMatchTime().getTime() < new Date().getTime()) {
					futures.add(calculateExecutorService.submit(new Callable<Void>(){
						@Override
						public Void call() throws Exception {
							parseAndCalculate(ematch);
							Thread.sleep(1000);
							return null;
						}
					}));
				}
			}
			
			for (Future<Void> f : futures) {
				try {
					f.get(30, TimeUnit.SECONDS);
				} catch (Exception e) {
					log.error("Unable to parse and calculate match.");
				}
			}
		}
	}
	
	private void parseAndCalculateResult (EMatch match) {
		matchResultCalculator.calculateAndPersist(match, null, null);
	}
	
	private void parseAndCalculate (EMatch match) {
		Long oddsmid = match.getOfnMatchId();
		log.debug("start to parse and calculate match {}", match);
		
		boolean finished = MatchUtil.isMatchFinishedNow(match.getMatchTime());
		
		if (!finished) {
			return;
		}
		
		try {
			// get match base data
			OFNMatchData ofnMatch = parser.parseMatchData(oddsmid);
			ofnMatch.setLeague(match.getLeague());
			
			Long matchDayId = match.getMatchDayId();
			if (matchDayId != null) {
				ofnMatch.setOkoooMatchId(okoooMatchCrawler.getOkoooMatchId(matchDayId));
				ofnMatch.setMatchDayId(matchDayId);
				
				FmRawMatch fmMatch = fmParser.getFmMatch(matchDayId);
				if (fmMatch != null) {
					String hostName = fmMatch.getHomeName().replace(" ", "");
					String guestName = fmMatch.getAwayName().replace(" ", "");
					if (ofnMatch.getHostName().contains(hostName) || ofnMatch.getGuestName().contains(guestName)) {
						ofnMatch.setFmMatchId(fmMatch.getFmId());
					}
				}
			}

			// get euro peilv
			Map<Company, List<EuroPl>> euroMap = new HashMap<Company, List<EuroPl>>();
			for (Company comp : Company.values()) {
//				List<EuroPl> euroPls = parser.parseEuroData(oddsmid, comp);
				List<EuroPl> euroPls = parseEuroData(ofnMatch, comp);
				if (euroPls != null && euroPls.size() > 0) {
					euroMap.put(comp, euroPls);
				}
			}

			ofnMatch.setEuroPls(euroMap);

			// get asia peilv
			List<AsiaPl> asiapls = parseAsiaData(ofnMatch, Company.Aomen);
			ofnMatch.setAoMen(asiapls);
						
			List<AsiaPl> ysb = parseAsiaData(ofnMatch, Company.YiShenBo);
			ofnMatch.setYsb(ysb);
						
			List<AsiaPl> daxiaopls = parseDaxiaoData(ofnMatch, Company.Aomen);
			ofnMatch.setDaxiao(daxiaopls);

			// calculate
			OFNCalculateResult calculateResult = calculator.calucate(ofnMatch);
			
			matchPersistService.save(ofnMatch, calculateResult, finished);
			
			matchResultCalculator.calculateAndPersist(match, ofnMatch.getHostScore(), ofnMatch.getGuestScore());
		} catch (Exception e) {
			log.error(String.format("Unable to parse and calculate match %s.", oddsmid), e);
		}		
	}
	
	private List<EuroPl> parseEuroData (OFNMatchData ofnMatch, Company company) {
//		return parser.parseEuroData(ofnMatch.getMatchId(), comp);
		sleep(200);
		return fmParser.parseEuroData(ofnMatch.getFmMatchId(), company);
	}
	
	private List<AsiaPl> parseAsiaData (OFNMatchData ofnMatch, Company company) {
		sleep(500);
//		return parser.parseAsiaData(ofnMatch.getMatchId(), Company.Aomen);
		return fmParser.parseAsiaData(ofnMatch.getFmMatchId(), company);
	}
	
	private List<AsiaPl> parseDaxiaoData (OFNMatchData ofnMatch, Company company) {
		sleep(500);
//		return parser.parseDaxiaoData(ofnMatch.getMatchId(), Company.Aomen);
		return fmParser.parseDaxiaoData(ofnMatch.getFmMatchId(), company);
	}
	
	private void sleep (long time) {
		try {
			Thread.currentThread().sleep(time);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
