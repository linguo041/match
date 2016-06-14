package com.roy.football.match.eightwin;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONObject;

import com.roy.football.match.OFN.response.EuroPl;
import com.roy.football.match.httpRequest.HttpRequestException;
import com.roy.football.match.httpRequest.HttpRequestService;
import com.roy.football.match.util.DateUtil;

public class JincaiParser {
	private final static String JINCAI_ODD_HISTORY_URL = "http://www.8win.com/buy/add/odd/history";
	
	public List<EuroPl> getJincaiEuro (Long oddsmid) {
		String ewMatchId = getEightWinMatchIdString(oddsmid);

		try {
			Map<String, String> headers = new HashMap<String, String>();

			String resData = HttpRequestService.getInstance().doHttpRequest(JINCAI_ODD_HISTORY_URL + "?mid=" + ewMatchId + "&pool=had",
					HttpRequestService.GET_METHOD, null, headers);

			return convertJson(resData, ewMatchId);
		} catch (HttpRequestException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	private List<EuroPl> convertJson (String json, String ewMatchId) {
		List<EuroPl> jincaiPls = new ArrayList<EuroPl>();
		
		try {
			JSONObject root = new JSONObject(json);
			
			JSONArray oddsArray = (JSONArray)root.get("odds");
			
			if (oddsArray.length() > 0) {
				// oddsArray.length() is always 1
				JSONObject oddsObj = oddsArray.getJSONObject(0);
				for (int i = oddsObj.length()-1; i >= 0; i--) {
					JSONObject odd = oddsObj.getJSONObject(i+"");

					EuroPl euroPl = new EuroPl();
					euroPl.seteWin(Float.valueOf(odd.getString("h")));
					euroPl.seteDraw(Float.valueOf(odd.getString("d")));
					euroPl.seteLose(Float.valueOf(odd.getString("a")));
					euroPl.seteDate(DateUtil.parseEightWinDate(ewMatchId.substring(0, 4) + "-" + odd.getString("time")));

					jincaiPls.add(euroPl);
				}
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return jincaiPls;
	}
	
	private String getEightWinMatchIdString (Long oddsmid) {
		String oddsmidString = String.valueOf(oddsmid);
		
		String dateStr = "20" + oddsmidString.substring(0, 6);
		try {
			Date dt = DateUtil.parseSimpleDate(dateStr);
			Calendar calendar = Calendar.getInstance();
			calendar.setTime(dt);
			int dayInWeek = calendar.get(Calendar.DAY_OF_WEEK);
			
			dayInWeek = dayInWeek == 1 ? 7 : dayInWeek - 1;
			
			
			return dateStr + dayInWeek + oddsmidString.substring(6);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return null;
	}
	
	public static void main (String args[]) {
		String json = "{\"odds\":[{\"0\":{\"h\":\"1.8\",\"d\":\"3.25\",\"a\":\"3.85\",\"hp\":\"3.1%\",\"dp\":\"3.4%\",\"ap\":\"4.2%\",\"p\":\"92.11%\",\"time\":\"06-13 17:36\",\"lotteryCode\":\"FTBRQSPF\"},\"1\":{\"h\":\"1.83\",\"d\":\"3.2\",\"a\":\"3.8\",\"hp\":\"3.1%\",\"dp\":\"3.4%\",\"ap\":\"4.2%\",\"p\":\"92.18%\",\"time\":\"06-13 17:00\",\"lotteryCode\":\"FTBRQSPF\"},\"2\":{\"h\":\"1.85\",\"d\":\"3.2\",\"a\":\"3.7\",\"hp\":\"3.1%\",\"dp\":\"3.4%\",\"ap\":\"4.2%\",\"p\":\"92.09%\",\"time\":\"06-13 15:35\",\"lotteryCode\":\"FTBRQSPF\"},\"3\":{\"h\":\"1.88\",\"d\":\"3.15\",\"a\":\"3.65\",\"hp\":\"3.1%\",\"dp\":\"3.4%\",\"ap\":\"4.2%\",\"p\":\"92.09%\",\"time\":\"06-13 12:13\",\"lotteryCode\":\"FTBRQSPF\"},\"4\":{\"h\":\"1.91\",\"d\":\"3.15\",\"a\":\"3.55\",\"hp\":\"3.1%\",\"dp\":\"3.4%\",\"ap\":\"4.2%\",\"p\":\"92.15%\",\"time\":\"06-13 10:24\",\"lotteryCode\":\"FTBRQSPF\"},\"5\":{\"h\":\"1.91\",\"d\":\"3.15\",\"a\":\"3.55\",\"hp\":\"3.6%\",\"dp\":\"3.4%\",\"ap\":\"4.2%\",\"p\":\"92.36%\",\"time\":\"06-12 18:55\",\"lotteryCode\":\"FTBRQSPF\"},\"6\":{\"h\":\"1.91\",\"d\":\"3.15\",\"a\":\"3.55\",\"hp\":\"5.6%\",\"dp\":\"3.9%\",\"ap\":\"3.7%\",\"p\":\"93.20%\",\"time\":\"06-10 22:38\",\"lotteryCode\":\"FTBRQSPF\"},\"7\":{\"h\":\"1.91\",\"d\":\"3.15\",\"a\":\"3.55\",\"hp\":\"5.6%\",\"dp\":\"3.9%\",\"ap\":\"3.1%\",\"p\":\"93.06%\",\"time\":\"06-10 22:38\",\"lotteryCode\":\"FTBRQSPF\"},\"8\":{\"h\":\"1.91\",\"d\":\"3.15\",\"a\":\"3.55\",\"hp\":\"5.6%\",\"dp\":\"3.1%\",\"ap\":\"3.1%\",\"p\":\"92.86%\",\"time\":\"06-10 22:38\",\"lotteryCode\":\"FTBRQSPF\"},\"9\":{\"h\":\"1.91\",\"d\":\"3.15\",\"a\":\"3.55\",\"hp\":\"3.1%\",\"dp\":\"3.1%\",\"ap\":\"3.1%\",\"p\":\"91.83%\",\"time\":\"06-10 21:11\",\"lotteryCode\":\"FTBRQSPF\"}}]}";
		System.out.println(new JincaiParser().convertJson(json, "201606131101"));
//		System.out.println(new JincaiParser().getEightWinMatchIdString(160618101l));
	}
}
