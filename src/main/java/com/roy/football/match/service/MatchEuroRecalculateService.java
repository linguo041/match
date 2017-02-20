package com.roy.football.match.service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.collect.Maps;
import com.roy.football.match.OFN.EuroCalculator;
import com.roy.football.match.OFN.out.OFNExcelData;
import com.roy.football.match.OFN.response.Company;
import com.roy.football.match.OFN.response.EuroPl;
import com.roy.football.match.OFN.response.OFNMatchData;
import com.roy.football.match.OFN.response.JinCaiSummary.JinCaiMatch;
import com.roy.football.match.OFN.statics.matrices.EuroMatrices;
import com.roy.football.match.fivemillion.FMParser;
import com.roy.football.match.fivemillion.FmMatch;
import com.roy.football.match.jpa.EntiryReverseConverter;
import com.roy.football.match.jpa.EntityConverter;
import com.roy.football.match.jpa.entities.calculation.EEuroPlState;
import com.roy.football.match.jpa.entities.calculation.EMatch;
import com.roy.football.match.jpa.repositories.EuroPlStateRepository;
import com.roy.football.match.jpa.repositories.MatchRepository;
import com.roy.football.match.sina.AicaiMatch;
import com.roy.football.match.sina.AicaiParser;
import com.roy.football.match.util.DateUtil;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class MatchEuroRecalculateService {

	@Autowired
	private MatchRepository matchRepository;
	
	@Autowired
	private AicaiParser aicaiParser;
	
	@Autowired
	private EuroCalculator euroCalculator;
	
	@Autowired
	private EuroPlStateRepository euroPlStateRepository;
	
	@Autowired
	public ExecutorService calculateExecutorService;
	
	public void recalculate () {
		List<EMatch> ematches = matchRepository.findMatchesWithoutEuro();
		Map<String, List<AicaiMatch>> fmMatchMap = Maps.newHashMap();
		int totalCalculated = 0;
		List<Future <Boolean>> futures = new ArrayList<Future <Boolean>>();
		
		log.info("total {} matches to recalculate", ematches.size());
		
		for (EMatch ematch : ematches) {
			OFNMatchData ofnMatch = EntiryReverseConverter.fromEMatch(ematch);
			
			Future <Boolean> f = calculateExecutorService.submit(new Callable<Boolean>(){
				@Override
				public Boolean call() throws Exception {
					return calAndPersist(ofnMatch, fmMatchMap);
				}
			});
			
			futures.add(f);
		}
		
		for (Future <Boolean> f : futures) {
			try {
				totalCalculated += f.get() ? 1 : 0;
			} catch (InterruptedException | ExecutionException e) {
				log.error("Unable to calculate match.", e);
			}
		}
		
		log.info("recalculated total: " + totalCalculated);
	}
	
	private boolean calAndPersist (OFNMatchData ofnMatch, Map<String, List<AicaiMatch>> fmMatchMap) {
		List<AicaiMatch> matches = getFmMatches(ofnMatch, fmMatchMap);
		
		String hostName = ofnMatch.getHostName();
		String guestName = ofnMatch.getGuestName();
		
		if (matches != null) {
			for (AicaiMatch fmMatch : matches) {					
				if (fmMatch.getHomeTeam().contains(hostName) || fmMatch.getGuestTeam().contains(guestName)) {
					log.info(String.format("Found ofnMatch [Id: %s, host: %s, guest: %s];"
							+ " fivemillion [Id: %s, host: %s, guest: %s]",
							ofnMatch.getMatchId(), hostName, guestName,
							fmMatch.getFixId(), fmMatch.getHomeTeam(), fmMatch.getGuestTeam()));
					
					Map<Company, List <EuroPl>> companyEus = Maps.newHashMapWithExpectedSize(Company.values().length);
					
					for (Company company : Company.values()) {
						List <EuroPl> eus = aicaiParser.parseEuroData(fmMatch.getFixId(), company);
						
						companyEus.put(company, eus);
					}
					
					ofnMatch.setEuroPls(companyEus);
					
					EuroMatrices euMatrics = euroCalculator.calucate(ofnMatch);
					
					EEuroPlState eEuroPlState = EntityConverter.toEEuroPlState(ofnMatch.getMatchId(), euMatrics);
					euroPlStateRepository.save(eEuroPlState);
					return true;
				}
			}
		}
		
		return false;
	}
	
	private synchronized List<AicaiMatch> getFmMatches (OFNMatchData ofnMatch, Map<String, List<AicaiMatch>> fmMatchMap) {
		Date matchDate = ofnMatch.getMatchTime();
		String matchDateStr = DateUtil.formatHarfYearDate(new Date(matchDate.getTime() - 12*3600*1000));
		
		List<AicaiMatch> matches = fmMatchMap.get(matchDateStr);
		
		if (matches == null) {
			matches = aicaiParser.parseMatchData(matchDateStr);
			fmMatchMap.put(matchDateStr,  matches);
		}
		
		return matches;
	}
}
