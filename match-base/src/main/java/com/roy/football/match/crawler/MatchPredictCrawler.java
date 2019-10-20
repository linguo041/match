package com.roy.football.match.crawler;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.jsoup.nodes.Document;

import com.roy.football.match.crawler.CrawlerEnum.CrawlerKey;
import com.roy.football.match.util.DocumentParser;
import com.roy.football.match.util.FileUtil;


public class MatchPredictCrawler implements BrokenNewsFetch {
	
	public void getBrokenNews(String matchDate){
		final int nThreads = 10;
		ExecutorService executorService = Executors.newFixedThreadPool(nThreads);  
		
		Document doc = DocumentParser.getDoc(DocumentParser.BROKEN_DETAIL_URL,matchDate);
		if(doc == null) return ;
		
		String contengJson = doc.select("body").text();
		final JSONArray allTeamNews = new JSONArray();
			
		if(contengJson == null) return;
		try {
			JSONObject jsonObject = new JSONObject(contengJson);
			String matchListStr = jsonObject.getString(CrawlerKey.OLDMATCHLIST.getKey());
			if(matchListStr == null ) return ;
			
			JSONArray matchArray = new JSONArray(new String(matchListStr));
			FetchWorker worker = new FetchWorker();
			for (int i = 0; i < matchArray.length(); i++) {
				worker.doMultiTask(executorService,matchArray,i,allTeamNews);
			}
			worker.waitTaskFinish(executorService);
		} catch (JSONException e2) {
			e2.printStackTrace();
		}
		System.out.println(allTeamNews.toString());
//		FileUtil.writeToFile(allTeamNews.toString(), "c:/match-2016-4-16.txt");
	}
	
	
	public static void main(String[] args) {
		new MatchPredictCrawler().getBrokenNews("2016-04-17");
	}
}
