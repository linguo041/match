package com.roy.football.match.OFN.parser;

import java.io.IOException;
import java.text.NumberFormat;
import java.text.ParseException;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.roy.football.match.OFN.response.MatchResult;

public class OFNResultCrawler {
	private final static String DETAIL_URL_PREIX = "http://bf.159cai.com/detail/index/";
	private final static NumberFormat NF_FORMAT = NumberFormat.getPercentInstance();
	
	public MatchResult craw (long matchId) {
		MatchResult res = new MatchResult();
		
		try {
			Document doc = Jsoup.connect(DETAIL_URL_PREIX + matchId).get();
			
			parseScore(res, doc);
			parseOther(res, doc);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return res;
	}
	
	private void parseScore (MatchResult res, Document doc) {
		Elements eles = doc.select("h1.score");
		
		for (Element ele : eles) {
			String scoreText = ele.text();
			
			String[] tokens = scoreText.split("-");
			
			int hostScore = Integer.parseInt(tokens[0].trim());
			int guestScore = Integer.parseInt(tokens[1].trim());
			
			res.setHostScore(hostScore);
			res.setGuestScore(guestScore);
		}
	}
	
	private void parseOther (MatchResult res, Document doc) {
		Elements eles = doc.select(".zs_mactecdate_con table");
		
		for (Element ele : eles) {
			Element shotEle = ele.select("tr:nth-child(1)").first();
			String hostShot = shotEle.child(1).text();
			String guestShot = shotEle.child(3).text();
			res.setHostShot(Integer.parseInt(hostShot.trim()));
			res.setGuestShot(Integer.parseInt(guestShot.trim()));
			
			Element shotOnTargetEle = ele.select("tr:nth-child(2)").first();
			String hostShotOnTarget = shotOnTargetEle.child(1).text();
			String guestShotOnTarget = shotOnTargetEle.child(3).text();
			res.setHostShotOnTarget(Integer.parseInt(hostShotOnTarget.trim()));
			res.setGuestShotOnTarget(Integer.parseInt(guestShotOnTarget.trim()));
			
			Element faultEle = ele.select("tr:nth-child(3)").first();
			String hostFault = faultEle.child(1).text();
			String guestFault = faultEle.child(3).text();
			res.setHostFault(Integer.parseInt(hostFault.trim()));
			res.setGuestFault(Integer.parseInt(guestFault.trim()));
			
			Element cornerEle = ele.select("tr:nth-child(4)").first();
			String hostCorner = cornerEle.child(1).text();
			String guestCorner = cornerEle.child(3).text();
			res.setHostCorner(Integer.parseInt(hostCorner.trim()));
			res.setGuestCorner(Integer.parseInt(guestCorner.trim()));
			
			Element offsideEle = ele.select("tr:nth-child(5)").first();
			String hostOffside = offsideEle.child(1).text();
			String guestOffside = offsideEle.child(3).text();
			res.setHostOffside(Integer.parseInt(hostOffside.trim()));
			res.setGuestOffside(Integer.parseInt(guestOffside.trim()));
			
			Element yellowCardEle = ele.select("tr:nth-child(6)").first();
			String hostYellowCard = yellowCardEle.child(1).text();
			String guestYellowCard = yellowCardEle.child(3).text();
			res.setHostYellowCard(Integer.parseInt(hostYellowCard.trim()));
			res.setGuestYellowCard(Integer.parseInt(guestYellowCard.trim()));
			
			Element timeEle = ele.select("tr:nth-child(7)").first();
			String hostTime = timeEle.child(1).text();
			String guestTime = timeEle.child(3).text();
			res.setHostTime(parsePercentNum(hostTime));
			res.setGuestTime(parsePercentNum(guestTime));
			
			Element saveEle = ele.select("tr:nth-child(8)").first();
			String hostSave = saveEle.child(1).text();
			String guestSave = saveEle.child(3).text();
			res.setHostSave(Integer.parseInt(hostSave.trim()));
			res.setGuestSave(Integer.parseInt(guestSave.trim()));
		}
	}
	
	private Float parsePercentNum(String num) {
		try {
			return ((Double)NF_FORMAT.parse(num)).floatValue();
		} catch (ParseException e) {
		}
		
		return null;
	}
	
	public static void main (String args[]) {
		OFNResultCrawler crawler = new OFNResultCrawler();
		System.out.println(crawler.craw(959891));
		
	}

}
