package com.roy.football.match.crawler.controller;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.roy.football.match.OFN.response.AsiaData;
import com.roy.football.match.OFN.response.EuroData;
import com.roy.football.match.OFN.response.JinCaiSummary;
import com.roy.football.match.OFN.response.JinCaiSummary.JinCaiMatch;
import com.roy.football.match.OFN.response.OFNMatchData;
import com.roy.football.match.httpRequest.HttpRequestException;
import com.roy.football.match.httpRequest.HttpRequestService;
import com.roy.football.match.main.OFH.parser.Parser;
import com.roy.football.match.util.MatchUtil;
import com.roy.football.match.util.XmlParseException;
import com.roy.football.match.util.XmlParser;

public class SimpleOFNController {

	public SimpleOFNController(HttpRequestService httpService) {
		this.httpService = httpService;
	}
	
	public void process () {
		List <OFNMatchData> ofnMatches = new ArrayList <OFNMatchData> ();
		
		List<JinCaiMatch> jinCaiMatches = parser.parseJinCaiMatches();
		
		if (jinCaiMatches != null && jinCaiMatches.size() > 0) {
			for (JinCaiMatch jcMatch : jinCaiMatches) {
				OFNMatchData ofnMatch = parser.parseMatchData(jcMatch.getOddsmid());
				EuroData euroData = parser.parseEuroData(jcMatch.getOddsmid(), 442l);
				AsiaData asiaData = parser.parseAsiaData(jcMatch.getOddsmid());
			}
		}
	}


	
	public HttpRequestService getHttpService() {
		return httpService;
	}

	public void setHttpService(HttpRequestService httpService) {
		this.httpService = httpService;
	}

	private HttpRequestService httpService;
	private Parser parser = new Parser(httpService);
}
