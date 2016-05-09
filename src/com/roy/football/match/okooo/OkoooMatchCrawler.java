package com.roy.football.match.okooo;

import java.io.IOException;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.sun.org.apache.bcel.internal.generic.NEW;

public class OkoooMatchCrawler {
	private final static String OKOOO_JINCAI_URL = "http://www.okooo.com/jingcai/";
	private final static String OKOOO_BIFA_URL = "http://www.okooo.com/soccer/match/{matchId}/exchanges/";
//	private final static String OKOOO_MATCH_SELECT = "div.touzhu_1";
	private final static String OKOOO_MATCH_SELECT = "div.touzhu_1[data-end=0]";
	private final static int MAX_BODY_SIZE = 1024*1024*10;
	private final static NumberFormat NF_FORMAT = NumberFormat.getPercentInstance();
	
	public Map<Integer, Long> craw () {
		Map<Integer, Long> matches = new HashMap<Integer, Long>();

		try {
			Document doc = Jsoup.connect(OKOOO_JINCAI_URL).maxBodySize(MAX_BODY_SIZE).get();

			Elements eles = doc.select(OKOOO_MATCH_SELECT);

			Iterator<Element> elIterator = eles.iterator();

			while (elIterator.hasNext()) {
				Element element = elIterator.next();

				String matchIdStr = element.attr("data-mid");
				String matchOrderStr = element.attr("data-morder");

				matches.put(Integer.parseInt(matchOrderStr), Long.parseLong(matchIdStr));
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return matches;
	}
	
	public MatchExchangeData getExchangeData (long matchId) {
		try {
			Document doc = Jsoup.connect(getExchangeUrl(matchId)).get();
			
			Elements eles = doc.select(".bfTable table");
			
			Iterator<Element> elIterator = eles.iterator();
			
			MatchExchangeData exchangeData = new MatchExchangeData();
			
			if (elIterator.hasNext()) {
				parseBaseTable(exchangeData, elIterator.next());
				parseAnalyseTable(exchangeData, elIterator.next());
			}
			
			return exchangeData;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return null;
	}
	
	private String getExchangeUrl (long matchId) {
		return OKOOO_BIFA_URL.replace("{matchId}", matchId+"");
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
		
		exchangeData.setBfWinExchange(Long.parseLong(bfWinExchange));
		exchangeData.setBfDrawExchange(Long.parseLong(bfDrawExchange));
		exchangeData.setBfLoseExchange(Long.parseLong(bfLoseExchange));
		exchangeData.setJcWinExchange(Long.parseLong(jcWinExchange));
		exchangeData.setJcDrawExchange(Long.parseLong(jcDrawExchange));
		exchangeData.setJcLoseExchange(Long.parseLong(jcLoseExchange));
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
		
		exchangeData.setBfWinExgRt(parsePercentNum(bfWinExgRt));
		exchangeData.setBfDrawExgRt(parsePercentNum(bfDrawExgRt));
		exchangeData.setBfLoseExgRt(parsePercentNum(bfLoseExgRt));
		exchangeData.setBfWinGain(Integer.parseInt(bfWinGain));
		exchangeData.setBfDrawGain(Integer.parseInt(bfDrawGain));
		exchangeData.setBfLoseGain(Integer.parseInt(bfLoseGain));
		exchangeData.setJcWinExgRt(parsePercentNum(jcWinExgRt));
		exchangeData.setJcDrawExgRt(parsePercentNum(jcDrawExgRt));
		exchangeData.setJcLoseExgRt(parsePercentNum(jcLoseExgRt));
		exchangeData.setJcWinGain(Integer.parseInt(jcWinGain));
		exchangeData.setJcDrawGain(Integer.parseInt(jcDrawGain));
		exchangeData.setJcLoseGain(Integer.parseInt(jcLoseGain));
	}
	
	private Float parsePercentNum(String num) {
		try {
			return ((Double)NF_FORMAT.parse(num)).floatValue();
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return null;
	}

	
	public static void main (String [] args) {
		System.out.println(new OkoooMatchCrawler().parsePercentNum("6.06%"));
	}
}
