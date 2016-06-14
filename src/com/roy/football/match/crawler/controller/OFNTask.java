package com.roy.football.match.crawler.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

import com.roy.football.match.OFN.OFNCalcucator;
import com.roy.football.match.OFN.out.OFNExcelData;
import com.roy.football.match.OFN.out.OFNOutputFormater;
import com.roy.football.match.OFN.parser.OFNParser;
import com.roy.football.match.OFN.response.AsiaPl;
import com.roy.football.match.OFN.response.Company;
import com.roy.football.match.OFN.response.EuroPl;
import com.roy.football.match.OFN.response.JinCaiSummary.JinCaiMatch;
import com.roy.football.match.OFN.response.OFNMatchData;
import com.roy.football.match.OFN.statics.matrices.OFNCalculateResult;
import com.roy.football.match.eightwin.JincaiParser;

public class OFNTask implements Callable<OFNExcelData>{
	
	public OFNTask (OFNParser parser, JincaiParser ewJincaiParser, OFNCalcucator calculator, OFNOutputFormater outputFormater, JinCaiMatch jcMatch) {
		this.parser = parser;
		this.calculator = calculator;
		this.outputFormater = outputFormater;
		this.jcMatch = jcMatch;
		this.ewJincaiParser = ewJincaiParser;
	}

	@Override
	public OFNExcelData call() throws Exception {
		EuroPl euroAg = new EuroPl();
		euroAg.seteWin(jcMatch.getOh());
		euroAg.seteDraw(jcMatch.getOd());
		euroAg.seteLose(jcMatch.getOa());

		return getOFNMatchExcelData(jcMatch.getOddsmid(),
				jcMatch.getXid(), jcMatch.getLid(), jcMatch.getLn(), euroAg);
	}
	
	public OFNExcelData getOFNMatchExcelData (Long oddsmid, Long matchDayId, Long leagueId, String leaueName, EuroPl euroAvg) {
		try {
			// get match base data
			OFNMatchData ofnMatch = parser.parseMatchData(oddsmid);
			ofnMatch.setMatchDayId(matchDayId);
			ofnMatch.setLeagueId(leagueId);
			ofnMatch.setLeagueName(leaueName);
			ofnMatch.setEuroAvg(euroAvg);

			// get euro peilv
			Map<Company, List<EuroPl>> euroMap = new HashMap<Company, List<EuroPl>>();
			for (Company comp : Company.values()) {
				if (comp == Company.Jincai) {
					List<EuroPl> euroPls = ewJincaiParser.getJincaiEuro(matchDayId);
					euroMap.put(comp, euroPls);
				} else {
					List<EuroPl> euroPls = parser.parseEuroData(oddsmid, comp);
					euroMap.put(comp, euroPls);
				}
			}

			ofnMatch.setEuroPls(euroMap);

			// get asia peilv
			List<AsiaPl> asiapls = parser.parseAsiaData(oddsmid, Company.Aomen);
			ofnMatch.setAoMen(asiapls);
			
			List<AsiaPl> daxiaopls = parser.parseDaxiaoData(oddsmid, Company.Aomen);
			ofnMatch.setDaxiao(daxiaopls);

			// calculate
			OFNCalculateResult calculateResult = calculator.calucate(ofnMatch);

			return outputFormater.format(ofnMatch, calculateResult);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	private OFNParser parser;
	private JincaiParser ewJincaiParser;
	private OFNCalcucator calculator;
	private OFNOutputFormater outputFormater;
	private JinCaiMatch jcMatch;
}
