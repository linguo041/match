package com.roy.football.match.OFN.parser;

import java.io.IOException;
import java.text.NumberFormat;
import java.text.ParseException;

import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.helper.StringUtil;
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
	private final static String DETAIL_URL_PREIX = "https://www.iuliao.com/live/matchlive/";
	private final static NumberFormat NF_FORMAT = NumberFormat.getPercentInstance();
	private final static String TIME = "控球";
	private final static String SHOT = "射门";
	private final static String SHOT_ON_TARGET = "射正";
	private final static String CORNER = "角球";
	private final static String SAVE = "扑救";
	private final static String FAULT = "犯规";
	private final static String YELLOW_CARD = "黄牌";
	private final static String OFFSIDE = "越位";
	
	public MatchResult craw (long matchId) {
		MatchResult res = new MatchResult();
		
		try {
			Document doc = Jsoup.connect(DETAIL_URL_PREIX + matchId).get();
			
			parseScore(res, doc);
			parseOther(res, doc);
			
			return res;
		} catch (Exception e) {
			log.warn(String.format("Unable to parse match result %s, with error: %s", matchId, e.getMessage()));
		}
		
		return null;
	}
	
	private void parseScore (MatchResult res, Document doc) {
		Elements eles = doc.select(".live-score");
		Elements hEles = eles.select(".live-score-home");
		Elements aEles = eles.select(".live-score-away");
		
		for (Element ele : hEles) {
			String hScoretext = ele.text();
			Integer hostScore = Integer.parseInt(hScoretext.trim());
			res.setHostScore(hostScore);
		}
		
		for (Element ele : aEles) {
			String aScoretext = ele.text();
			Integer awayScore = Integer.parseInt(aScoretext.trim());
			res.setGuestScore(awayScore);
		}
	}
	
	private void parseOther (MatchResult res, Document doc) {
		Elements eles = doc.select(".match-statistics table td");
		
		for (Element ele : eles) {
			String action = ele.select(".match-action").first().text();
			String homeNum = ele.select(".percent-num-home").first().text();
			String awayNum = ele.select(".percent-num-away").first().text();
			
			if (TIME.equals(action)) {
				res.setHostTime(parsePercentNum(homeNum));
				res.setGuestTime(parsePercentNum(awayNum));
			} else if (SHOT.equals(action)) {
				res.setHostShot(parseIntNum(homeNum));
				res.setGuestShot(parseIntNum(awayNum));
			} else if (SHOT_ON_TARGET.equals(action)) {
				res.setHostShotOnTarget(parseIntNum(homeNum));
				res.setGuestShotOnTarget(parseIntNum(awayNum));
			} else if (CORNER.equals(action)) {
				res.setHostCorner(parseIntNum(homeNum));
				res.setGuestCorner(parseIntNum(awayNum));
			} else if (SAVE.equals(action)) {
				res.setHostSave(parseIntNum(homeNum));
				res.setGuestSave(parseIntNum(awayNum));
			} else if (FAULT.equals(action)) {
				res.setHostFault(parseIntNum(homeNum));
				res.setGuestFault(parseIntNum(awayNum));
			} else if (YELLOW_CARD.equals(action)) {
				res.setHostYellowCard(parseIntNum(homeNum));
				res.setGuestYellowCard(parseIntNum(awayNum));
			} else if (OFFSIDE.equals(action)) {
				res.setHostOffside(parseIntNum(homeNum));
				res.setGuestOffside(parseIntNum(awayNum));
			}
		}
	}
	
	private Float parsePercentNum(String num) {
		try {
			if ("0".equals(num)) {
				return 0f;
			}
			
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
		System.out.println(crawler.craw(1192861));
		
		System.out.println(crawler.parsePercentNum("0"));
	}

}
