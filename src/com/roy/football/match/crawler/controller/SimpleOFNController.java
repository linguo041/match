package com.roy.football.match.crawler.controller;

import java.io.IOException;
import java.io.StringReader;
import java.util.Collections;
import java.util.List;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import com.roy.football.match.OFN.response.JinCaiSummary;
import com.roy.football.match.OFN.response.JinCaiSummary.JinCaiMatch;
import com.roy.football.match.httpRequest.HttpRequestException;
import com.roy.football.match.httpRequest.HttpRequestService;
import com.roy.football.match.util.MatchUtil;
import com.roy.football.match.util.XmlParseException;
import com.roy.football.match.util.XmlParser;

public class SimpleOFNController {
	private final static String JIN_CAI_URL = "http://www.159cai.com/cpdata/omi/jczq/odds/odds.xml";
	private final static String ANALYSIS_URL_PREFIX = "http://odds.159cai.com/match/analysis/";
	
	public SimpleOFNController(HttpRequestService httpService) {
		this.httpService = httpService;
	}
	
	public void process () {
		try {
			String resData = this.httpService.doHttpRequest(JIN_CAI_URL, HttpRequestService.GET_METHOD, null, null);

			JinCaiSummary response = XmlParser.parseXmlToObject(
							new StringReader(resData),
							JinCaiSummary.class, "xml");

			List<JinCaiMatch> matches = response.getRows();

			if (matches != null && matches.size() > 0) {
				Collections.sort(matches);
				
				String matchDay = MatchUtil.getMatchDay();

				
				for (JinCaiMatch match : matches) {
					Long xid = match.getXid();
					if ((xid+"").contains(matchDay)) {
						
						Document doc = Jsoup.connect(ANALYSIS_URL_PREFIX + match.getOddsmid()).get();
						
						Elements e = doc.select("#hawar td:eq(6)");
						System.out.println(e.val());
						break;
					}
				}
			}

			System.out.println(matches);
		} catch (HttpRequestException | XmlParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	
	public HttpRequestService getHttpService() {
		return httpService;
	}

	public void setHttpService(HttpRequestService httpService) {
		this.httpService = httpService;
	}

	private HttpRequestService httpService;
}
