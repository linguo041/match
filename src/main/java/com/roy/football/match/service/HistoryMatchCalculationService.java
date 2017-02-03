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
import com.roy.football.match.jpa.entities.calculation.EMatch;
import com.roy.football.match.jpa.entities.calculation.EMatchResult;
import com.roy.football.match.jpa.repositories.MatchRepository;
import com.roy.football.match.jpa.repositories.MatchResultRepository;
import com.roy.football.match.jpa.service.MatchPersistService;
import com.roy.football.match.okooo.OkoooMatchCrawler;

@Service
public class HistoryMatchCalculationService {
	@Autowired
	private OFNParser parser;
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
							return null;
						}
					}));
				}
			}
			
			try {
				for (Future<Void> f : futures) {
					f.get();
				}
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	private void parseAndCalculateResult (EMatch match) {
		matchResultCalculator.calculateAndPersist(match, null, null);
	}
	
	private void parseAndCalculate (EMatch match) {
		Long oddsmid = match.getOfnMatchId();
		
		try {
			// get match base data
			OFNMatchData ofnMatch = parser.parseMatchData(oddsmid);
			ofnMatch.setLeague(match.getLeague());
			
			Long matchDayId = match.getMatchDayId();
			if (matchDayId != null) {
				ofnMatch.setOkoooMatchId(okoooMatchCrawler.getOkoooMatchId(matchDayId));
			}

			// get euro peilv
			Map<Company, List<EuroPl>> euroMap = new HashMap<Company, List<EuroPl>>();
			for (Company comp : Company.values()) {
				List<EuroPl> euroPls = parser.parseEuroData(oddsmid, comp);
				euroMap.put(comp, euroPls);
			}

			ofnMatch.setEuroPls(euroMap);

			// get asia peilv
			List<AsiaPl> asiapls = parser.parseAsiaData(oddsmid, Company.Aomen);
			ofnMatch.setAoMen(asiapls);
			
			List<AsiaPl> daxiaopls = parser.parseDaxiaoData(oddsmid, Company.Aomen);
			ofnMatch.setDaxiao(daxiaopls);

			// calculate
			OFNCalculateResult calculateResult = calculator.calucate(ofnMatch);
			
			matchPersistService.save(ofnMatch, calculateResult);
			
			matchResultCalculator.calculateAndPersist(match, ofnMatch.getHostScore(), ofnMatch.getGuestScore());
		} catch (Exception e) {
			e.printStackTrace();
		}		
	}
}
