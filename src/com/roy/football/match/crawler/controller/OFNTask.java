package com.roy.football.match.crawler.controller;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.roy.football.match.OFN.OFNCalcucator;
import com.roy.football.match.OFN.out.OFNExcelData;
import com.roy.football.match.OFN.out.OFNOutputFormater;
import com.roy.football.match.OFN.out.PoiWriter;
import com.roy.football.match.OFN.parser.OFNParser;
import com.roy.football.match.OFN.response.AsiaPl;
import com.roy.football.match.OFN.response.Company;
import com.roy.football.match.OFN.response.EuroPl;
import com.roy.football.match.OFN.response.JinCaiSummary.JinCaiMatch;
import com.roy.football.match.OFN.response.OFNMatchData;
import com.roy.football.match.OFN.statics.matrices.OFNCalculateResult;
import com.roy.football.match.context.MatchContext;

public class OFNTask implements Callable<OFNExcelData>{
	
	public OFNTask (OFNParser parser, OFNCalcucator calculator, OFNOutputFormater outputFormater, JinCaiMatch jcMatch) {
		this.parser = parser;
		this.calculator = calculator;
		this.outputFormater = outputFormater;
		this.jcMatch = jcMatch;
	}

	@Override
	public OFNExcelData call() throws Exception {

		return getOFNMatchExcelData(jcMatch.getOddsmid(),
				jcMatch.getXid(), jcMatch.getLid(), jcMatch.getLn());
	}
	
	public OFNExcelData getOFNMatchExcelData (Long oddsmid, Long matchDayId, Long leagueId, String leaueName) {
		try {
			// get match base data
			OFNMatchData ofnMatch = parser.parseMatchData(oddsmid);
			ofnMatch.setMatchDayId(matchDayId);
			ofnMatch.setLeagueId(leagueId);
			ofnMatch.setLeagueName(leaueName);

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

			// calculate
			OFNCalculateResult calculateResult = calculator.calucate(ofnMatch);

			return outputFormater.format(ofnMatch, calculateResult);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	private OFNParser parser;
	private OFNCalcucator calculator;
	private OFNOutputFormater outputFormater;
	private JinCaiMatch jcMatch;
}
