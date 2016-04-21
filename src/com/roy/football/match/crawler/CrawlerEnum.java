package com.roy.football.match.crawler;


public interface CrawlerEnum {
	
	
	public static enum CrawlerKey {
		AGAINSTNO("againstNo"),HOME("home")
		, AWAY("away"),ARTICLESIDS1("articlesIds1"),
		BROKNEWS("brokNews"),HOMEAWAY("homeaway"),NEWSTYPE("newsType"),MATCH_DATE("matchDate"),
		TEAM("team"),OLDMATCHLIST("oldMatchList"),NEWS_TITLE("newsTitle"),NEWS_ABSTRACT("newsAbs");

		private String key = "";
		public String getKey() {
			return this.key;
		}
		
		private CrawlerKey(String key) {
			this.key = key;
		}
	};
}