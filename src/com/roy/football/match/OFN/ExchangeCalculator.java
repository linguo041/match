package com.roy.football.match.OFN;

import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;

import com.roy.football.match.okooo.MatchExchangeData;
import com.roy.football.match.okooo.OkoooMatchCrawler;
import com.roy.football.match.util.DateUtil;

public class ExchangeCalculator{
	private OkoooMatchCrawler exchangeCrawler = new OkoooMatchCrawler();
	private Map<Integer, Long> okoooMatches = null;
	
	public MatchExchangeData calculate(Long ofnMatchId) {
		try {
//			ofnMatchId = 160507024l;
			long matchOrder = getMatchOrder(ofnMatchId);
			
			Map<Integer, Long> okMatches = getOkoooMatches();
			
			Long okMatchId= okMatches.get((int)matchOrder);
			
			return exchangeCrawler.getExchangeData(okMatchId);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return null;
	}
	
	private synchronized Map<Integer, Long> getOkoooMatches () {
		if (okoooMatches == null) {
			okoooMatches = exchangeCrawler.craw();
		}
		
		return okoooMatches;
	}
	
	private long getMatchOrder (Long ofnMatchId) throws ParseException {
		Date matchDate = DateUtil.parseSimpleDate("20" + ofnMatchId / 1000);
		
		Calendar calendar = Calendar.getInstance();
		
		calendar.setTime(matchDate);
		int week = calendar.get(Calendar.DAY_OF_WEEK) - 1;
		long matchOrder = ofnMatchId % 1000;
		
		return (week == 0 ? 7 : week) * 1000 + matchOrder;
	}
	
	public static void main (String [] args) throws ParseException {
		new ExchangeCalculator().getMatchOrder(160508053l);
	}
}
