package com.roy.football.match.OFN;

import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.roy.football.match.okooo.MatchExchangeData;
import com.roy.football.match.okooo.OkoooMatchCrawler;
import com.roy.football.match.util.DateUtil;

@Component
public class OkoooExchangeCalculator{
	@Autowired
	private OkoooMatchCrawler exchangeCrawler;
	
	public Map<Integer, Long> getOkoooMatches () {
		return exchangeCrawler.craw(false);
	}
	
	public Long getMatchOrder (Long ofnMatchId) {
		try {
			Date matchDate = DateUtil.parseSimpleDate("20" + ofnMatchId / 1000);
			
			Calendar calendar = Calendar.getInstance();
			
			calendar.setTime(matchDate);
			int week = calendar.get(Calendar.DAY_OF_WEEK) - 1;
			long matchOrder = ofnMatchId % 1000;
			
			return (week == 0 ? 7 : week) * 1000 + matchOrder;
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return null;
	}
	
	public MatchExchangeData calculate(Long ofnMatchId, Long okMatchId) {
		if (okMatchId != null) {
			return exchangeCrawler.getExchangeData(okMatchId);
		}
		
		return null;
	}
	
	public static void main (String [] args) throws ParseException {
		System.out.println(new OkoooExchangeCalculator().getMatchOrder(160508053l));
	}
}
