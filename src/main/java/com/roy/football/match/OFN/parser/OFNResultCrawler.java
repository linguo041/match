package com.roy.football.match.OFN.parser;

import java.io.IOException;
import java.text.NumberFormat;
import java.text.ParseException;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;

import com.roy.football.match.OFN.response.MatchResult;
import com.roy.football.match.service.HistoryMatchCalculationService;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class OFNResultCrawler {
	private final static String DETAIL_URL_PREIX = "http://bf.159cai.com/detail/index/";
	private final static NumberFormat NF_FORMAT = NumberFormat.getPercentInstance();
	
	public MatchResult craw (long matchId) {
		MatchResult res = new MatchResult();
		
		try {
			Document doc = Jsoup.connect(DETAIL_URL_PREIX + matchId).get();
			
			parseScore(res, doc);
			parseOther(res, doc);
			
			return res;
		} catch (Exception e) {
			log.warn(String.format("Unable to parse match result %s.", matchId), e);
		}
		
		return null;
	}
	
	private void parseScore (MatchResult res, Document doc) {
		Elements eles = doc.select("h1.score");
		
		for (Element ele : eles) {
			String scoreText = ele.text();
			
			String[] tokens = scoreText.split("-");
			
			Integer hostScore = Integer.parseInt(tokens[0].trim());
			Integer guestScore = Integer.parseInt(tokens[1].trim());
			
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
			res.setHostShot(parseIntNum(hostShot));
			res.setGuestShot(parseIntNum(guestShot));
			
			Element shotOnTargetEle = ele.select("tr:nth-child(2)").first();
			String hostShotOnTarget = shotOnTargetEle.child(1).text();
			String guestShotOnTarget = shotOnTargetEle.child(3).text();
			res.setHostShotOnTarget(parseIntNum(hostShotOnTarget));
			res.setGuestShotOnTarget(parseIntNum(guestShotOnTarget));
			
			Element faultEle = ele.select("tr:nth-child(3)").first();
			String hostFault = faultEle.child(1).text();
			String guestFault = faultEle.child(3).text();
			res.setHostFault(parseIntNum(hostFault));
			res.setGuestFault(parseIntNum(guestFault));
			
			Element cornerEle = ele.select("tr:nth-child(4)").first();
			String hostCorner = cornerEle.child(1).text();
			String guestCorner = cornerEle.child(3).text();
			res.setHostCorner(parseIntNum(hostCorner));
			res.setGuestCorner(parseIntNum(guestCorner));
			
			Element offsideEle = ele.select("tr:nth-child(5)").first();
			String hostOffside = offsideEle.child(1).text();
			String guestOffside = offsideEle.child(3).text();
			res.setHostOffside(parseIntNum(hostOffside));
			res.setGuestOffside(parseIntNum(guestOffside));
			
			Element yellowCardEle = ele.select("tr:nth-child(6)").first();
			String hostYellowCard = yellowCardEle.child(1).text();
			String guestYellowCard = yellowCardEle.child(3).text();
			res.setHostYellowCard(parseIntNum(hostYellowCard));
			res.setGuestYellowCard(parseIntNum(guestYellowCard));
			
			Element timeEle = ele.select("tr:nth-child(7)").first();
			String hostTime = timeEle.child(1).text();
			String guestTime = timeEle.child(3).text();
			res.setHostTime(parsePercentNum(hostTime));
			res.setGuestTime(parsePercentNum(guestTime));
			
			Element saveEle = ele.select("tr:nth-child(8)").first();
			String hostSave = saveEle.child(1).text();
			String guestSave = saveEle.child(3).text();
			res.setHostSave(parseIntNum(hostSave));
			res.setGuestSave(parseIntNum(guestSave));
		}
	}
	
	private Float parsePercentNum(String num) {
		try {
			return ((Double)NF_FORMAT.parse(num)).floatValue();
		} catch (Exception e) {
			log.warn("unable to parse percent number {}.", num);
		}
		
		return null;
	}
	
	private Integer parseIntNum(String num) {
		try {
			return Integer.parseInt(num.trim());
		} catch (Exception e) {
			log.warn("unable to parse int number {}.", num);
		}
		
		return null;
	}
	
	public static void main (String args[]) {
		OFNResultCrawler crawler = new OFNResultCrawler();
		System.out.println(crawler.craw(961356));
		
	}

}
