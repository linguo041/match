package com.roy.football.match.crawler.controller;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.collect.Lists;
import com.roy.football.match.OFN.OFNCalcucator;
import com.roy.football.match.OFN.OkoooExchangeCalculator;
import com.roy.football.match.OFN.out.OFNExcelData;
import com.roy.football.match.OFN.out.OFNOutputFormater;
import com.roy.football.match.OFN.out.PoiWriter;
import com.roy.football.match.OFN.parser.OFNParser;
import com.roy.football.match.OFN.response.AsiaPl;
import com.roy.football.match.OFN.response.Company;
import com.roy.football.match.OFN.response.EuroPl;
import com.roy.football.match.OFN.response.OFNMatchData;
import com.roy.football.match.OFN.response.JinCaiSummary.JinCaiMatch;
import com.roy.football.match.OFN.statics.matrices.OFNCalculateResult;
import com.roy.football.match.base.League;
import com.roy.football.match.context.MatchContext;
import com.roy.football.match.eightwin.EWJincaiParser;
import com.roy.football.match.util.DateUtil;

@Service
public class OFNMatchService {
	
	@Autowired
	private OFNParser parser;
	@Autowired
	private EWJincaiParser ewJincaiParser;
	@Autowired
	private OkoooExchangeCalculator exchangeCalculator;
	@Autowired
	private OFNCalcucator calculator;
	@Autowired
	private OFNOutputFormater outputFormater;

	@Autowired
	public ExecutorService calculateExecutorService;
	
	public void process () {	
		List <OFNExcelData> excelDatas = new ArrayList <OFNExcelData> ();
		
		List<JinCaiMatch> jinCaiMatches = parser.parseJinCaiMatches();
		Map<Integer, Long> okMatches = exchangeCalculator.getOkoooMatches();
		
		Collections.sort(jinCaiMatches);

		if (jinCaiMatches != null && jinCaiMatches.size() > 0) {
			Date now = new Date();
			List<Future <OFNExcelData>> futures = new ArrayList<Future <OFNExcelData>>();
			
			for (JinCaiMatch jcMatch : jinCaiMatches) {
				Long okMatchOrder = exchangeCalculator.getMatchOrder(jcMatch.getXid());

				if (filterValidMatch(jcMatch, now)) {
					Future <OFNExcelData> f = calculateExecutorService.submit(new Callable<OFNExcelData>(){
						@Override
						public OFNExcelData call() throws Exception {
							return parseAndCalculate(jcMatch, okMatches.get(okMatchOrder));
						}
					});
					
					futures.add(f);
				} else {
					// log...
				}
			}

			for (Future <OFNExcelData> f : futures) {
				try {
					excelDatas.add(f.get());
				} catch (InterruptedException | ExecutionException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		
		writeExcel(excelDatas);
	}
	
	public void processMatch (Long oddsmid, Long matchDayId, League league) {
		JinCaiMatch jcMatch = new JinCaiMatch();
		jcMatch.setOddsmid(oddsmid);
		jcMatch.setXid(matchDayId);
		jcMatch.setLid(league.getLeagueId());
		
		Map<Integer, Long> okMatches = exchangeCalculator.getOkoooMatches();
		Long okMatchOrder = exchangeCalculator.getMatchOrder(jcMatch.getXid());
		
		OFNExcelData data = parseAndCalculate(jcMatch, okMatches.get(okMatchOrder));
		writeExcel(Lists.newArrayList(data));
	}
	
	private OFNExcelData parseAndCalculate (JinCaiMatch jcMatch, Long okMatchId) {
		Long oddsmid = jcMatch.getOddsmid();
		Long matchDayId = jcMatch.getXid();
		League league = League.getLeagueById(jcMatch.getLid());
		EuroPl euroAverage = new EuroPl(jcMatch.getOh(), jcMatch.getOd(), jcMatch.getOd(), null);
		
		try {
			// get match base data
			OFNMatchData ofnMatch = parser.parseMatchData(oddsmid);
			ofnMatch.setMatchDayId(matchDayId);
			ofnMatch.setLeague(league);
			ofnMatch.setEuroAvg(euroAverage);
			ofnMatch.setOkoooMatchId(okMatchId);

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

	private void writeExcel (List <OFNExcelData> datas) {
		XSSFWorkbook workBook = new XSSFWorkbook();
		PoiWriter <OFNExcelData> writer = new PoiWriter <OFNExcelData>(OFNExcelData.class);
		
		try {
			if (datas != null && datas.size() > 0) {
				writer.write(datas, workBook);
			}

			String fileName = System.getProperty("user.dir") + "\\data\\match-" + DateUtil.formatSimpleDate(new Date())+".xlsx";
			System.out.println(fileName);
			
			File file = new File (fileName);
			
			if (file.exists()) {
				file.delete();
			}
			if (file.createNewFile()) {
				workBook.write(new FileOutputStream(file));
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {

		}
	}
	
	private long getMatchDayIdPre () {
		Calendar cal = Calendar.getInstance();
		int year = cal.get(Calendar.YEAR);
		int month = cal.get(Calendar.MONTH) + 1;
		int day = cal.get(Calendar.DAY_OF_MONTH);
		
		return (day + month * 100 + (year - 2000) * 10000) * 1000;
	}
	
	private boolean filterValidMatch (JinCaiMatch jcMatch, Date now) {
		long matchDayPre = getMatchDayIdPre();

		// filter not today matches
		if (jcMatch.getXid() < matchDayPre || jcMatch.getXid() > matchDayPre + 999) {
			return false;
		}
		
		// filter started match
		if (jcMatch.getMtime().getTime() <= now.getTime()) {
			return false;
		}
		
		return true;
	} 
} 
