package com.roy.football.match.OFN.parser;

import java.io.IOException;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class OFNTeamFetcher {

	private final static String TEAM_URL_PREIX = "http://info.159cai.com/team/index/";
	
	public OfnTeamName fetch (Long teamId) {
		try {
			Document doc = Jsoup.connect(TEAM_URL_PREIX + teamId).get();
			
			Element ele = doc.select("table.clubname").first();
			
			Element line1 = ele.select("tr:nth-child(1)").first();
			Element line2 = ele.select("tr:nth-child(2)").first();
			Element line3 = ele.select("tr:nth-child(3)").first();

			String name = line1.child(0).child(0).text();
			String enName = line2.child(1).text();
			String city = line2.child(3).text();
			String field = line3.child(1).text();
			
			return new OfnTeamName(name, enName, city, field);
		} catch (IOException e) {
			log.error("Unable to fetch club names for {}", teamId);
		}
		return null;
	}
	
	@Data
	@AllArgsConstructor
	public static class OfnTeamName {
		private String name;
		private String enName;
		private String city;
		private String field;
	}
}
