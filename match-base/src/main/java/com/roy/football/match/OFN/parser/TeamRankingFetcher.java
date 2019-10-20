package com.roy.football.match.OFN.parser;

import java.util.Iterator;
import java.util.List;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import com.google.common.collect.Lists;
import com.roy.football.match.util.StringUtil;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class TeamRankingFetcher {

	private final static String TEAM_URL_PREIX = "http://footballdatabase.com/ranking/world/";
	
	@Autowired
	private Translater translater;
	
	@Autowired
    Environment env;

	public List<TeamRanking> fetch (int page) {
		List<TeamRanking> teams = Lists.newArrayList();
		
		try {
			Document doc = Jsoup.connect(TEAM_URL_PREIX + page).get();
			
			Element ele = doc.select(".table-responsive tbody").first();
			Elements lines = ele.select("tr");
			Iterator<Element> iterator = lines.iterator();
			
			while (iterator.hasNext()) {
				Element tr = iterator.next();
				
				if (tr.childNodeSize() >= 3) {
					int rank = Integer.parseInt(tr.child(0).text());
					String enName = removeContryFromName(tr.child(1).child(0).attr("title"));
					String contry = tr.child(1).child(1).text();
					int point = Integer.parseInt(tr.child(2).text());

					String cnName = env.getProperty(enName);
					if (StringUtil.isEmpty(cnName)) {
						cnName = translater.translate(enName);
						Thread.sleep(200L);
					}
					
					teams.add(new TeamRanking(rank, enName, cnName, contry, point));
				}
			}
		} catch (Exception e) {
			log.error(String.format("Unable to fetch club names for {}", page), e);
		}
		return teams;
	}
	
	private String removeContryFromName (String enName) {
		return enName.substring(0, enName.indexOf('(')-1);
	}
	
	@Data
	@AllArgsConstructor
	public static class TeamRanking {
		private Integer rank;
		private String enName;
		private String cnName;
		private String contry;
		private Integer point;
	}
	
	public static void main (String args []) {
		TeamRankingFetcher f = new TeamRankingFetcher();
		System.out.println(f.fetch(1));
	}
}
