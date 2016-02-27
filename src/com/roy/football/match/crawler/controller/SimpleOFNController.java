package com.roy.football.match.crawler.controller;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.roy.football.match.OFN.OFNCalcucator;
import com.roy.football.match.OFN.parser.Parser;
import com.roy.football.match.OFN.response.AsiaPl;
import com.roy.football.match.OFN.response.Company;
import com.roy.football.match.OFN.response.EuroPl;
import com.roy.football.match.OFN.response.JinCaiSummary.JinCaiMatch;
import com.roy.football.match.OFN.response.OFNMatchData;
import com.roy.football.match.context.MatchContext;

public class SimpleOFNController {

	public SimpleOFNController(MatchContext context) {
		this.setContext(context);
	}
	
	public void process () {
		List <OFNMatchData> ofnMatches = new ArrayList <OFNMatchData> ();
		
		List<JinCaiMatch> jinCaiMatches = parser.parseJinCaiMatches();
		
		Collections.sort(jinCaiMatches);
		
		int i = 0;
		
		if (jinCaiMatches != null && jinCaiMatches.size() > 0) {
			Date now = new Date();

			for (JinCaiMatch jcMatch : jinCaiMatches) {
				
				if (jcMatch.getMtime().getTime() <= now.getTime()) {
					continue;
				}
				
				i++;

				// get match base data
				OFNMatchData ofnMatch = parser.parseMatchData(jcMatch.getOddsmid());
				
				// get euro peilv
				Map<Company, List <EuroPl>> euroMap = new HashMap<Company, List <EuroPl>>();
				for (Company comp : Company.values()) {
					List <EuroPl> euroPls = parser.parseEuroData(jcMatch.getOddsmid(), comp);
					euroMap.put(comp, euroPls);
				}
				
				ofnMatch.setEuroPls(euroMap);
				
				// get asia peilv
				List<AsiaPl> asiapls = parser.parseAsiaData(jcMatch.getOddsmid(), Company.Aomen);
				
				ofnMatch.setAoMen(asiapls);
				
				ofnMatch.setCalculateResult(calculator.calucate(ofnMatch));

				ofnMatches.add(ofnMatch);
				
				if (i > 5) {
					break;
				}
			}
		}
	}

	public MatchContext getContext() {
		return context;
	}

	public void setContext(MatchContext context) {
		this.context = context;
	}

	private Parser parser = new Parser();
	private OFNCalcucator calculator = new OFNCalcucator();
	private MatchContext context;
} 
