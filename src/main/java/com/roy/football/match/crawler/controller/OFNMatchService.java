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
import com.roy.football.match.OFN.out.OFNExcelData;
import com.roy.football.match.OFN.out.OFNOutputFormater;
import com.roy.football.match.OFN.out.PoiWriter;
import com.roy.football.match.OFN.parser.MatchParseException;
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
import com.roy.football.match.jpa.service.MatchPersistService;
import com.roy.football.match.okooo.OkoooMatchCrawler;
import com.roy.football.match.util.DateUtil;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class OFNMatchService {
	
	@Autowired
	private OFNParser parser;
	@Autowired
	private EWJincaiParser ewJincaiParser;
	@Autowired
	private OkoooMatchCrawler okoooMatchCrawler;
	@Autowired
	private OFNCalcucator calculator;
	@Autowired
	private OFNOutputFormater outputFormater;
	
	@Autowired
	private MatchPersistService matchPersistService;

	@Autowired
	public ExecutorService calculateExecutorService;
	
	public void process () throws MatchParseException {	
		List <OFNExcelData> excelDatas = new ArrayList <OFNExcelData> ();
		
		List<JinCaiMatch> jinCaiMatches = parser.parseJinCaiMatches();
		
		Collections.sort(jinCaiMatches);

		if (jinCaiMatches != null && jinCaiMatches.size() > 0) {
			Date now = new Date();
			List<Future <OFNExcelData>> futures = new ArrayList<Future <OFNExcelData>>();
			
			for (JinCaiMatch jcMatch : jinCaiMatches) {

				if (true/*filterValidMatch(jcMatch, now)*/) {
					Future <OFNExcelData> f = calculateExecutorService.submit(new Callable<OFNExcelData>(){
						@Override
						public OFNExcelData call() throws Exception {
							return parseAndCalculate(jcMatch);
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
					log.error("Unable to calculate match.", e);
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
		
		OFNExcelData data = parseAndCalculate(jcMatch);
		writeExcel(Lists.newArrayList(data));
	}
	
	private OFNExcelData parseAndCalculate (JinCaiMatch jcMatch) {
		Long oddsmid = jcMatch.getOddsmid();
		Long matchDayId = jcMatch.getXid();
		League league = League.getLeagueById(jcMatch.getLid());
		EuroPl euroAverage = new EuroPl(jcMatch.getOh(), jcMatch.getOd(), jcMatch.getOa(), null);
		
		try {
			// get match base data
			OFNMatchData ofnMatch = parser.parseMatchData(oddsmid);
			ofnMatch.setMatchDayId(matchDayId);
			ofnMatch.setLeague(league);
			ofnMatch.setEuroAvg(euroAverage);
			
			if (matchDayId != null) {
				ofnMatch.setOkoooMatchId(okoooMatchCrawler.getOkoooMatchId(matchDayId));
			}

			// get euro peilv
			Map<Company, List<EuroPl>> euroMap = new HashMap<Company, List<EuroPl>>();
			for (Company comp : Company.values()) {
				if (comp == Company.Jincai) {
					List<EuroPl> euroPls = getJincaiPls(oddsmid, matchDayId);
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
			
			List<AsiaPl> ysb = parser.parseAsiaData(oddsmid, Company.YiShenBo);
			ofnMatch.setYsb(ysb);
			
			List<AsiaPl> daxiaopls = parser.parseDaxiaoData(oddsmid, Company.Aomen);
			ofnMatch.setDaxiao(daxiaopls);

			// calculate
			OFNCalculateResult calculateResult = calculator.calucate(ofnMatch);
			calculator.predict(calculateResult);
			
			// TODO - split
			if (matchPersistService != null) {
				matchPersistService.save(ofnMatch, calculateResult);
				
				matchPersistService.saveHistoryMatch(ofnMatch.getHostMatches());
				matchPersistService.saveHistoryMatch(ofnMatch.getGuestMatches());
			}

			return outputFormater.format(ofnMatch, calculateResult);
		} catch (Exception e) {
			log.error(String.format("Unable to calculate match %s.", jcMatch), e);
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

			String fileName = System.getProperty("user.dir") + "/data/match-" + DateUtil.formatSimpleDate(new Date())+".xlsx";
			
			File file = new File (fileName);
			
			if (file.exists()) {
				file.delete();
			}
			if (file.createNewFile()) {
				workBook.write(new FileOutputStream(file));
			}
		} catch (IOException e) {
			log.error("Unable to write excel.", e);
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
	
	private List<EuroPl> getJincaiPls (Long oddsmid, Long matchDayId) throws MatchParseException {
		if (matchDayId != null) {
			return ewJincaiParser.getJincaiEuro(matchDayId);
		} else {
			return parser.parseEuroData(oddsmid, Company.Jincai);
		}
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
