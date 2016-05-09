package com.roy.football.match.crawler.controller;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.roy.football.match.OFN.OFNCalcucator;
import com.roy.football.match.OFN.out.OFNExcelData;
import com.roy.football.match.OFN.out.OFNOutputFormater;
import com.roy.football.match.OFN.out.PoiWriter;
import com.roy.football.match.OFN.parser.OFNParser;
import com.roy.football.match.OFN.response.JinCaiSummary.JinCaiMatch;
import com.roy.football.match.base.League;
import com.roy.football.match.context.MatchContext;
import com.roy.football.match.util.DateUtil;

public class SimpleOFNController {

	public SimpleOFNController(MatchContext context) {
		this.setContext(context);
	}
	
	public void process () {	
		List <OFNExcelData> excelDatas = new ArrayList <OFNExcelData> ();
		
		List<JinCaiMatch> jinCaiMatches = parser.parseJinCaiMatches();
		
		Collections.sort(jinCaiMatches);

		if (jinCaiMatches != null && jinCaiMatches.size() > 0) {
			Date now = new Date();
			List<Future <OFNExcelData>> futures = new ArrayList<Future <OFNExcelData>>();
			ExecutorService executor = Executors.newFixedThreadPool(8);
			
			try {
				for (JinCaiMatch jcMatch : jinCaiMatches) {
					long matchDayPre = getMatchDayIdPre();

					if (jcMatch.getXid() < matchDayPre || jcMatch.getXid() > matchDayPre + 999) {
						continue;
					}
					
					if (jcMatch.getMtime().getTime() <= now.getTime()) {
						continue;
					}

					Future <OFNExcelData> f = executor.submit(new OFNTask(parser, calculator, outputFormater, jcMatch));
					
					futures.add(f);
				}

				for (Future <OFNExcelData> f : futures) {
					try {
						excelDatas.add(f.get());
					} catch (InterruptedException | ExecutionException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			} finally {
				executor.shutdown();
				
				try {
					if (!executor.awaitTermination(30, TimeUnit.SECONDS)) {
						executor.shutdownNow();
					}
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

			
		}
		
		writeExcel(excelDatas);
	}
	
	public void processMatch (Long oddsmid, Long matchDayId, League league) {
		List <OFNExcelData> excelDatas = new ArrayList <OFNExcelData> ();
		OFNTask task = new OFNTask(parser, calculator, outputFormater, null);
		excelDatas.add(task.getOFNMatchExcelData(oddsmid, matchDayId, league.getLeagueId(), null, null));
		writeExcel(excelDatas);
	}

	private void writeExcel (List <OFNExcelData> datas) {
		XSSFWorkbook workBook = new XSSFWorkbook();
		
		try {
			if (datas != null && datas.size() > 0) {
				writer.write(datas, workBook);
			}

			File file = new File ("C:\\work-records\\match\\data\\match-" + DateUtil.formatSimpleDate(new Date())+".xlsx");
			
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

	public MatchContext getContext() {
		return context;
	}

	public void setContext(MatchContext context) {
		this.context = context;
	}

	private OFNParser parser = new OFNParser();
	private OFNCalcucator calculator = new OFNCalcucator();
	private OFNOutputFormater outputFormater = new OFNOutputFormater();
	private PoiWriter <OFNExcelData> writer = new PoiWriter <OFNExcelData>(OFNExcelData.class);
	private MatchContext context;
} 
