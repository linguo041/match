package com.roy.football.match.util;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import com.roy.football.match.crawler.CrawlerEnum.CrawlerKey;

public class DocumentParser {
	public static final int TIME_OUT = 5000;
	public static final String BROKEN_NEWS_URL = "http://cms.8win.com/zybl/one-";
	public static final String BROKEN_DETAIL_URL = "http://cms.8win.com/zybl/matchList/data.do";
	
	/**
	 * get the Document by the url and the match date
	 * @param url
	 * @param matchDate
	 * @return
	 */
	public static Document getDoc(String url,String matchDate){
		Document doc = null;
		try {
			Map<String, String> requestMap = new HashMap<String,String>();
			requestMap.put("startDate", matchDate);
			doc = Jsoup.connect(url)
					.data(requestMap).ignoreContentType(true).timeout(TIME_OUT).post();
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		return doc;
	}
	
	public static void getDetailNewsByMatch(String matchDate,JSONObject brokNewsJsonObj,String broNewsUrl){
		Document doc = null;
		try {
			doc = Jsoup.connect(broNewsUrl + matchDate).timeout(TIME_OUT).get();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		if (doc == null) {
			return;
		}
		Elements titles = doc.select(".bl-con h3 a ");
		Elements contents = doc.select(".bl-con .abstract ");
		Elements homeAways = doc.select(".bl-con-time .zhuke");
		Elements newTypes = doc.select(".bl-con-time .blType");
		
		if(titles != null && contents != null && titles.size() == contents.size()){
			try {
				for (int i = 0; i < titles.size(); i++) {
					brokNewsJsonObj.put(CrawlerKey.HOMEAWAY.getKey(), homeAways.get(i).text());
					brokNewsJsonObj.put(CrawlerKey.NEWSTYPE.getKey(), newTypes.get(i).text());
					brokNewsJsonObj.put(CrawlerKey.NEWS_TITLE.getKey(),titles.get(i).text());
					brokNewsJsonObj.put(CrawlerKey.NEWS_ABSTRACT.getKey(),contents.get(i).text());
				}
			} catch (JSONException e1) {
				e1.printStackTrace();
			}
		}
	}
	
}
