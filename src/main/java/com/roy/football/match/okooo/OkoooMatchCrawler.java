package com.roy.football.match.okooo;

import java.io.IOException;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.roy.football.match.service.HistoryMatchCalculationService;
import com.roy.football.match.util.DateUtil;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class OkoooMatchCrawler {
	private final static String OKOOO_JINCAI_URL = "http://www.okooo.com/jingcai/";
	private final static String OKOOO_BIFA_URL = "http://www.okooo.com/soccer/match/{matchId}/exchanges/";
	private final static String OKOOO_HISTORY_URL = "http://www.okooo.com/soccer/match/{matchId}/history/";
	private final static String OKOOO_ALL_MATCH_SELECT = "div.touzhu_1";
	private final static String OKOOO_MATCH_SELECT = "div.touzhu_1[data-end=0]";
	private final static int MAX_BODY_SIZE = 1024*1024*10;
	private final static NumberFormat NF_FORMAT = NumberFormat.getPercentInstance();
	
	private Cache<Long, Long> okMatches = CacheBuilder.newBuilder()
			.maximumSize(10000)
			.build();
	
	public MatchExchangeData getExchangeData (Long matchId) {
		
		try {
			Document doc = Jsoup.connect(getExchangeUrl(matchId))
					.userAgent("Mozilla")
					.referrer(getExchangeUrlReferer(matchId))
					.get();
			
			Elements eles = doc.select(".bfTable table");
			
			Iterator<Element> elIterator = eles.iterator();
			
			MatchExchangeData exchangeData = new MatchExchangeData();
			
			if (elIterator.hasNext()) {
				parseBaseTable(exchangeData, elIterator.next());
				parseAnalyseTable(exchangeData, elIterator.next());
			}

			return exchangeData;
		} catch (Exception e) {
			log.error(String.format("Unable to parse exchange data from okooo with okooo match id", matchId), e);
		}
		
		return null;
	}
	
	/*
	public Long getOkoooMatchId (Date matchDate) {
		
		try {			
			Calendar calendar = Calendar.getInstance();
			calendar.setTime(matchDate);
			calendar.add(Calendar.HOUR, -12);
			int week = calendar.get(Calendar.DAY_OF_WEEK) - 1;
			
			long matchOrder = (week == 0 ? 7 : week) * 1000 + matchDayId % 1000;
			
			Long okMatchId = okMatches.getIfPresent(matchOrder);
			
			if (okMatchId == null) {
				craw(false, matchDate);
				
				return okMatches.getIfPresent(matchOrder);
			}
			
			return okMatchId;
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return null;
	}
	*/
	
	public synchronized Long getOkoooMatchId (Long matchDayId) {
		
		try {
			Date matchDate = DateUtil.parseSimpleDate("20" + matchDayId / 1000);
			Calendar calendar = Calendar.getInstance();
			calendar.setTime(matchDate);
			int week = calendar.get(Calendar.DAY_OF_WEEK) - 1;
			long matchOrder = (week == 0 ? 7 : week) * 1000 + matchDayId % 1000;

			Long okMatchId = okMatches.getIfPresent(matchOrder);
			
			if (okMatchId == null) {
				log.info(String.format("No ok match found for match order %s, recraw.", matchOrder));
				craw(true, matchDate);
				
				return okMatches.getIfPresent(matchOrder);
			}
			
			return okMatchId;
		} catch (ParseException e) {
			log.error(String.format("Unable to parse to oc match id from match day id %s", matchDayId), e);
		}
		
		return null;
	}
	
	private void craw (boolean all, Date matchDate) {

		try {
			String url = OKOOO_JINCAI_URL;
			
			if (matchDate != null) {
				url = url + DateUtil.formatSimpleDateWithDash(matchDate) + "/";
			}
			
			Document doc = Jsoup.connect(url).maxBodySize(MAX_BODY_SIZE).get();

			Elements eles = doc.select(all ? OKOOO_ALL_MATCH_SELECT : OKOOO_MATCH_SELECT);

			Iterator<Element> elIterator = eles.iterator();

			while (elIterator.hasNext()) {
				Element element = elIterator.next();

				String matchIdStr = element.attr("data-mid");
				String matchOrderStr = element.attr("data-morder");

				okMatches.put(Long.parseLong(matchOrderStr), Long.parseLong(matchIdStr));
				log.info(String.format("match order: %s,    match id: %s", matchOrderStr,  matchIdStr));
			}
		} catch (Exception e) {
			log.error(String.format("Unable to craw to oc matches for %s", matchDate), e);
		}
	}

	private String getExchangeUrl (long matchId) {
		return OKOOO_BIFA_URL.replace("{matchId}", matchId+"");
	}
	
	private String getExchangeUrlReferer (long matchId) {
		return OKOOO_HISTORY_URL.replace("{matchId}", matchId+"");
	}
	
	private void parseBaseTable (MatchExchangeData exchangeData, Element element) {
		Element winElement = element.select("tr:nth-child(3)").first();
		Element drawElement = element.select("tr:nth-child(4)").first();
		Element loseElement = element.select("tr:nth-child(5)").first();
		
		String bfWinExchange = winElement.child(6).text();
		String jcWinExchange = winElement.child(9).text();
		String bfDrawExchange = drawElement.child(6).text();
		String jcDrawExchange = drawElement.child(9).text();
		String bfLoseExchange = loseElement.child(6).text();
		String jcLoseExchange = loseElement.child(9).text();
		
		try {
			exchangeData.setBfWinExchange(Long.parseLong(bfWinExchange));
			exchangeData.setBfDrawExchange(Long.parseLong(bfDrawExchange));
			exchangeData.setBfLoseExchange(Long.parseLong(bfLoseExchange));
		} catch (Exception e) {
			log.error("bf exchange parse error.", e);
		}
		
		try {
			exchangeData.setJcWinExchange(Long.parseLong(jcWinExchange));
			exchangeData.setJcDrawExchange(Long.parseLong(jcDrawExchange));
			exchangeData.setJcLoseExchange(Long.parseLong(jcLoseExchange));
		} catch (Exception e) {
			log.error("jc exchange parse error.", e);
		}
	}
	
	private void parseAnalyseTable (MatchExchangeData exchangeData, Element element) {
		Element winElement = element.select("tr:nth-child(3)").first();
		Element drawElement = element.select("tr:nth-child(4)").first();
		Element loseElement = element.select("tr:nth-child(5)").first();
		
		String bfWinExgRt = winElement.child(3).text();
		String jcWinExgRt = winElement.child(4).text();
		String bfWinGain = winElement.child(10).text();
		String jcWinGain= winElement.child(11).text();
		String bfDrawExgRt = drawElement.child(3).text();
		String jcDrawExgRt = drawElement.child(4).text();
		String bfDrawGain = drawElement.child(10).text();
		String jcDrawGain= drawElement.child(11).text();
		String bfLoseExgRt = loseElement.child(3).text();
		String jcLoseExgRt = loseElement.child(4).text();
		String bfLoseGain = loseElement.child(10).text();
		String jcLoseGain= loseElement.child(11).text();
		
		try {
			exchangeData.setBfWinExgRt(parsePercentNum(bfWinExgRt));
			exchangeData.setBfDrawExgRt(parsePercentNum(bfDrawExgRt));
			exchangeData.setBfLoseExgRt(parsePercentNum(bfLoseExgRt));
			exchangeData.setBfWinGain(Integer.parseInt(bfWinGain));
			exchangeData.setBfDrawGain(Integer.parseInt(bfDrawGain));
			exchangeData.setBfLoseGain(Integer.parseInt(bfLoseGain));
		} catch (Exception e) {
			log.error("bf exchange parse error.", e);
		}
		
		
		try {
			exchangeData.setJcWinExgRt(parsePercentNum(jcWinExgRt));
			exchangeData.setJcDrawExgRt(parsePercentNum(jcDrawExgRt));
			exchangeData.setJcLoseExgRt(parsePercentNum(jcLoseExgRt));
			exchangeData.setJcWinGain(Integer.parseInt(jcWinGain));
			exchangeData.setJcDrawGain(Integer.parseInt(jcDrawGain));
			exchangeData.setJcLoseGain(Integer.parseInt(jcLoseGain));
		} catch (Exception e) {
			log.error("jc exchange parse error.", e);
		}
	}
	
	private Float parsePercentNum(String num) {
		try {
			return ((Double)NF_FORMAT.parse(num)).floatValue();
		} catch (ParseException e) {
		}
		
		return null;
	}

	
	public static void main (String [] args) throws IOException {
		Document doc = Jsoup.connect("http://www.okooo.com/soccer/match/926771/exchanges/").userAgent("Mozilla").get();
		System.out.println(doc);
	}
}
