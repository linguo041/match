package com.roy.football.match.crawler;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import com.roy.football.match.crawler.CrawlerEnum.CrawlerKey;
import com.roy.football.match.util.DocumentParser;

public class FetchWorker {
	public FetchWorker(){
		
	}
	/**
	 * do the task in multi task
	 * @param executorService
	 * @param matchArray
	 * @param i
	 * @param allTeamNews
	 */
	public void doMultiTask(ExecutorService executorService,JSONArray matchArray,int i,final JSONArray allTeamNews) {
		final JSONObject matchJson;
		try {
			matchJson = matchArray.getJSONObject(i);
			Runnable task = new Runnable() {
				public void run() {
					String againstNo;
					try {
						againstNo = matchJson.getString(CrawlerKey.AGAINSTNO.getKey());
						String team = matchJson.getString(CrawlerKey.HOME.getKey())+ "-"+
								matchJson.getString(CrawlerKey.AWAY.getKey());
						JSONObject brokNewsJsonObj = new JSONObject();
						DocumentParser.getDetailNewsByMatch(againstNo,brokNewsJsonObj,DocumentParser.BROKEN_NEWS_URL);
						brokNewsJsonObj.put(CrawlerKey.TEAM.getKey(), team);
						brokNewsJsonObj.put(CrawlerKey.MATCH_DATE.getKey(), againstNo);
						allTeamNews.put(brokNewsJsonObj);
					} catch (JSONException e) {
						e.printStackTrace();
					}
				}
			};
			executorService.submit(task);
		} catch (JSONException e1) {
			e1.printStackTrace();
		}
		
	}
	public void waitTaskFinish(ExecutorService executorService){
		executorService.shutdown();
		try {
			// 请求关闭、发生超时或者当前线程中断，无论哪一个首先发生之后，都将导致阻塞，直到所有任务完成执行
			// 设置最长等待10秒
			while (!executorService.awaitTermination(10, TimeUnit.SECONDS)) {  
				System.out.println("线程池没有关闭");  
			}
		} catch (InterruptedException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}  
		System.out.println("线程池已经关闭"); 
	}
}
