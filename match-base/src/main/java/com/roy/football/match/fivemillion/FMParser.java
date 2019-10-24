package com.roy.football.match.fivemillion;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.parser.Parser;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Component;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import com.roy.football.match.OFN.EuroCalculator;
import com.roy.football.match.OFN.parser.OFHKey.Match;
import com.roy.football.match.OFN.response.AsiaPl;
import com.roy.football.match.OFN.response.ClubDatas;
import com.roy.football.match.OFN.response.Company;
import com.roy.football.match.OFN.response.EType;
import com.roy.football.match.OFN.response.EuroPl;
import com.roy.football.match.OFN.response.FinishedMatch;
import com.roy.football.match.OFN.response.JinCaiSummary;
import com.roy.football.match.OFN.response.OFNMatchData;
import com.roy.football.match.OFN.response.JinCaiSummary.JinCaiMatch;
import com.roy.football.match.httpRequest.HttpRequestException;
import com.roy.football.match.httpRequest.HttpRequestService;
import com.roy.football.match.service.HistoryMatchCalculationService;
import com.roy.football.match.util.DateUtil;
import com.roy.football.match.util.GsonConverter;
import com.roy.football.match.util.MatchUtil;
import com.roy.football.match.util.StringUtil;
import com.roy.football.match.util.XmlParseException;
import com.roy.football.match.util.XmlParser;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class FMParser {
	private final static String FIVE_M_JCZQ = "http://trade.500.com/jczq/";
	private final static String ANALYSIS_URL_PREFIX = "http://odds.500.com/fenxi/shuju-{fmId}.shtml";
	private final static String EURO_URL = "http://odds.500.com/fenxi1/json/ouzhi.php";
	private final static String ASIA_URL = "http://odds.500.com/fenxi1/inc/yazhiajax.php";
	private final static String DAXIAO_URL = "http://odds.500.com/fenxi1/inc/daxiaoajax.php";
	private final static String FM_JCZQ_URL = "https://ews.500.com/static/ews/jczq/";

	private Cache<Long, FmRawMatch> fmMatches = CacheBuilder.newBuilder()
			.maximumSize(1000)
			.build();
	
	public synchronized FmRawMatch getFmMatch (Long matchDayId) {
		// matchDayId: 190107102
		try {
			FmRawMatch fmMatch = fmMatches.getIfPresent(matchDayId);
			
			if (fmMatch == null) {
				log.info(String.format("No fm match found for match order %s, recraw.", matchDayId));
				
				Long month = 200000 + matchDayId / 100000;
				Long day = 20000000 + matchDayId / 1000;
				Long matchDay = matchDayId / 1000;
				List<FmRawMatch> matches = parseMatchData(month.toString(), day.toString());
				for (FmRawMatch match : matches) {
					fmMatches.put(generateMatchDayId(matchDay, match.getMatchDayOrder()), match);
				}
				
				return fmMatches.getIfPresent(matchDayId);
			}
			
			return fmMatch;
		} catch (Exception e) {
			log.error(String.format("Unable to parse to fm match id from match day id %s", matchDayId), e);
		}
		
		return null;
	}
	
	public List<FmRawMatch> parseMatchData (String month, String day) {
		Map<String, String> headers = new HashMap<String, String>();
		
		try {
			log.info("Get FM matches of " + day);
			// https://ews.500.com/static/ews/jczq/201812/20181214.json
			String resData = HttpRequestService.getInstance().doHttpRequest(FM_JCZQ_URL + month +"/" + day + ".json",
					HttpRequestService.GET_METHOD, null, headers);
			
			if (!StringUtil.isEmpty(resData)) {
				JsonParser parser = new JsonParser();
				JsonObject rootObj = parser.parse(resData).getAsJsonObject();   
				JsonObject dataObj = rootObj.getAsJsonObject("data");
				JsonArray matchArray = dataObj.getAsJsonArray("matches");
				
				Gson gson = new GsonBuilder().create();
				
				return gson.fromJson(matchArray, new TypeToken<List<FmRawMatch>>(){}.getType());
			}
		} catch (HttpRequestException e) {
			log.warn(String.format("Can't get the matches of %s", day));
		}
		
		return Lists.newArrayList();
	}

/*	
	public List<FmRawMatch> parseMatchData (String dateStr) {
		List<FmRawMatch> matches = Lists.newArrayList();

		try {
			Document doc = Jsoup.connect(FIVE_M_JCZQ).data("date", dateStr).get();
			
			Element matchTable = doc.select("table.bet_table").last();
			Elements tableRows = matchTable.select("tr");
			
			Iterator<Element> iterator = tableRows.iterator();
			
			while (iterator.hasNext()) {
				try {
					Element row = iterator.next();
					String fid = row.attr("fid");
					String morderId = row.attr("pname");
					String matchTime = row.select(".match_time").attr("title");
					matchTime = matchTime.substring(matchTime.indexOf("20"));
					String matchHostName = row.select(".left_team a").attr("title");
					String matchGuestName = row.select(".right_team a").attr("title");
					
					FmRawMatch fmMatch = new FmRawMatch();
					fmMatch.setFmMatchId(Long.parseLong(fid));
					fmMatch.setMatchOrderId(Integer.parseInt(morderId));
					fmMatch.setMatchDate(DateUtil.parseEightWinDate(matchTime));
					fmMatch.setHostName(matchHostName);
					fmMatch.setGuestName(matchGuestName);
					
					matches.add(fmMatch);
				} catch (Exception e) {
					log.error(String.format("unable to parse five million match"), e);
				}
			}
		} catch (Exception e) {
			log.error(String.format("unable to parse five million matches"), e);
		}
		
		return matches;
	}
*/

	public List <EuroPl> parseEuroData (Long fmatchId, Company company) {
		Map<String, String> headers = new HashMap<String, String>();
		headers.put("X-Requested-With", "XMLHttpRequest");
		
		try {
//			Document doc = Jsoup.connect(EURO_URL
//					+ "?cid=" + company.getFmCompanyId() + "&fid=" + fmatchId + "&type=europe").get();
			
			String resData = HttpRequestService.getInstance().doHttpRequest(EURO_URL
					+ "?cid=" + company.getFmCompanyId() + "&fid=" + fmatchId + "&type=europe",
					HttpRequestService.GET_METHOD, null, headers);
			
			String[][] datas = GsonConverter.convertJSonToObjectUseNormal(resData, new TypeToken<String[][]>(){});
			
			if (datas != null && datas.length > 0) {
				List <EuroPl> euroPls = new ArrayList<EuroPl>();
				
				for (String[] eu : datas) {
					EuroPl pl = new EuroPl(Float.parseFloat(eu[0]),
							Float.parseFloat(eu[1]),
							Float.parseFloat(eu[2]),
							DateUtil.parseDateWithDataBase(eu[4]));
					
					euroPls.add(pl);
				}
				
				return euroPls;
			}
			
		} catch (Exception e) {
			log.error(String.format("unable to parse match euro data: match [%d], company [%s]", fmatchId, company), e);
		}
		return null;
	}
	
	public List <AsiaPl> parseAsiaData (Long fmatchId, Company company) {
		Map<String, String> headers = new HashMap<String, String>();
		headers.put("X-Requested-With", "XMLHttpRequest");
		String resData = "";
		
		try {
			resData = HttpRequestService.getInstance().doHttpRequest(ASIA_URL
					+ "?id=" + company.getFmCompanyId() + "&fid=" + fmatchId, HttpRequestService.GET_METHOD, null, headers);
			
			String[] datas = GsonConverter.convertJSonToObjectUseNormal(resData, new TypeToken<String[]>(){});
			
			if (datas != null && datas.length > 0) {
				List <AsiaPl> res = Lists.newArrayList();
				
				for (String ele : datas) {
					res.add(parseAsiaPl(ele, true));
				}
				
				return res;
			}
		} catch (Exception e) {
			log.error(String.format("unable to parse match asia data: match [%d], company [%s], res [%s]", fmatchId, company, resData), e);
		}
		return null;
	}
	
	public List <AsiaPl> parseDaxiaoData (Long fmatchId, Company company) {
		Map<String, String> headers = new HashMap<String, String>();
		headers.put("X-Requested-With", "XMLHttpRequest");
		
		try {
			String resData = HttpRequestService.getInstance().doHttpRequest(DAXIAO_URL
					+ "?id=" + company.getFmCompanyId() + "&fid=" + fmatchId, HttpRequestService.GET_METHOD, null, headers);
			
			String[] datas = GsonConverter.convertJSonToObjectUseNormal(resData, new TypeToken<String[]>(){});
			
			if (datas != null && datas.length > 0) {
				List <AsiaPl> res = Lists.newArrayList();
				
				for (String ele : datas) {
					res.add(parseAsiaPl(ele, false));
				}
				
				return res;
			}
		} catch (Exception e) {
			log.error(String.format("unable to parse match asia data: match [%d], company [%s]", fmatchId, company), e);
		}
		return null;
	}
	
	private AsiaPl parseAsiaPl(String asiaStr, boolean pankou) throws ParseException {
		AsiaPl asia = new AsiaPl();
		
		Document doc = Jsoup.parse(asiaStr, "", Parser.xmlParser());
		Elements tds = doc.select("td");
		Iterator<Element> iterator = tds.iterator();
		
		Element downEle = iterator.next();
		asia.setaWin(Float.parseFloat(downEle.text()));
		
		Element pkEle = iterator.next();
		asia.setPanKou(pankou ? parsePanKou(pkEle.text()) : parseDaxiao(pkEle.text()));
		
		Element upEle = iterator.next();
		asia.sethWin(Float.parseFloat(upEle.text()));
		
		Element dateEle = iterator.next();
		asia.setPkDate(DateUtil.parseFiveMDate(dateEle.text()));
		
		return asia;
	}
	
	private String normalize(String pkStr) {
		if (pkStr != null) {
			return pkStr.replaceAll("\\h|降|升", "").trim();
		}
		
		return pkStr;
	}
	
	private Float parsePanKou(String pkInputStr) {
		String pkStr = normalize(pkInputStr);
		switch (pkStr) {
			case "两球": return 2f;
			case "球半/两球": return 1.75f;
			case "球半": return 1.5f;
			case "一球/球半": return 1.25f;
			case "一球": return 1f;
			case "半球/一球": return 0.75f;
			case "半球": return 0.5f;
			case "平手/半球": return 0.25f;
			case "平手": return 0f;
			case "受平手/半球": return -0.25f;
			case "受半球": return -0.5f;
			case "受半球/一球": return -0.75f;
			case "受一球": return -1f;
			case "受一球/球半": return -1.250f;
			case "受球半": return -1.5f;
			case "受球半/两球": return -1.75f;
			case "受两球": return -2f;
			default: 
				log.warn("no yapang pk matched {}.", pkStr);
				return null;
		}
	}
	
	private Float parseDaxiao(String pkInputStr) {
		String pkStr = normalize(pkInputStr);
		switch (pkStr) {
			case "5": return 5f;
			case "4.5/5": return 4.75f;
			case "4.5": return 4.5f;
			case "4/4.5": return 4.25f;
			case "4": return 4f;
			case "3.5/4": return 3.75f;
			case "3.5": return 3.5f;
			case "3/3.5": return 3.25f;
			case "3": return 3f;
			case "2.5/3": return 2.75f;
			case "2.5": return 2.5f;
			case "2/2.5": return 2.25f;
			case "2": return 2f;
			case "1.5/2": return 1.75f;
			case "1.5": return 1.5f;
			case "1/1.5": return 1.25f;
			case "1": return 1f;
			case "0.5/1": return 0.75f;
			case "0.5": return 0.5f;
			case "0/0.5": return 0.25f;
			case "0": return 1f;
			default: 
				log.warn("no daxiao pk matched {}.", pkStr);
				return null;
		}
	}
	
	private Long generateMatchDayId (Long day, String fmMatchDayOrder) {
		return Long.parseLong(day + fmMatchDayOrder.substring(fmMatchDayOrder.length() - 3));
	}

	public static void main (String [] args) throws Exception {
//		String instr = "[[\"4.00\",\"3.75\",\"1.75\",\"1454880574\"],[\"3.60\",\"3.75\",\"1.83\",\"1455221779\"],[\"4.20\",\"3.80\",\"1.70\",\"1455434403\"],[\"3.60\",\"3.75\",\"1.83\",\"1455435003\"],[\"4.20\",\"3.80\",\"1.70\",\"1455435312\"]]";
//		
//		String out[][] = GsonConverter.convertJSonToObjectUseNormal(instr, new TypeToken<String[][]>(){});
//		
//		System.out.println(out);
		FMParser p = new FMParser();
//		List <EuroPl> pls = p.parseEuroData(449928L, Company.Aomen);
//		System.out.println(pls);
		
//		List<FmRawMatch> ms = p.parseMatchData("201901", "20190107");
//		System.out.println(ms);
		
//		System.out.println(p.parsePanKou("半球/一球 降"));
//		System.out.println(p.parsePanKou(" 半球/一球"));
//		System.out.println(p.parseDaxiao(" 2.5 "));
//		System.out.println(p.parseDaxiao("2.5 升"));
		
		tttest();
		
//		parseAsia();
		
//		System.out.println(DateUtil.parseFiveMDate("12-15 12:12"));
		
//		FmRawMatch match1 = p.getFmMatch(190107005L);
//		System.out.println(match1);
//		FmRawMatch match2 = p.getFmMatch(190107006L);
//		System.out.println(match2);
	}
	
	private static void parseAsia () {
//		String asiaData = "[\"<tr><td class='tips_down'>0.960<\\/td><td>\u534a\\u7403<\\/td><td class='tips_up'>0.840<\\/td><td>12-15 12:12<\\/td><\\/tr>\",\"<tr><td class='tips_down'>0.950<\\/td><td>\u534a\\u7403<\\/td><td class='tips_up'>0.850<\\/td><td>12-15 12:12<\\/td><\\/tr>\"]";
//		String asiaData = "[\"<td>\u5e73\u624b\\/\u534a\u7403<\\/td>\"]";
		String asiaData = "[\"<tr><td class='tips_down'>0.72<\\/td><td class=''>2\\/2.5<\\/td><td class='tips_up'>0.98<\\/td><td class=''>12-15 19:34<\\/td><\\/tr>\"]";
		String out[] = GsonConverter.convertJSonToObjectUseNormal(asiaData, new TypeToken<String[]>(){});
		
		for (String ele : out) {
			Document doc =Jsoup.parse(ele, "", Parser.xmlParser());
			System.out.println(doc);
			Elements tableRows = doc.select("td");
			
			Iterator<Element> iterator = tableRows.iterator();
			
			while (iterator.hasNext()) {
				Element row = iterator.next();
				System.out.println(row.text());
			}
			
		}
		
	}
	
	public static void tttest () throws Exception {
		String resData = "[[1.75,4,3.85,92.49,\"2019-10-22 23:45:26\",0,0,-1],[1.75,4,3.9,92.78,\"2019-10-22 21:21:34\",0,0,-1],[1.75,4,4,93.33,\"2019-10-22 10:03:39\",0,1,0],[1.75,3.9,4,92.78,\"2019-10-22 03:15:31\",1,-1,0],[1.73,4,4,92.76,\"2019-10-21 16:15:05\",0,1,-1],[1.73,3.9,4.25,93.48,\"2019-10-21 11:54:16\",1,0,0],[1.7,3.9,4.25,92.6,\"2019-10-21 06:57:00\",0,1,1],[1.7,3.75,3.9,89.98,\"2019-10-21 00:30:49\",0,0,-1],[1.7,3.75,4,90.51,\"2019-10-20 17:01:40\",1,-1,-1],[1.65,3.9,4.25,91.09,\"2019-10-19 15:44:56\",1,0,0],[1.63,3.9,4.25,90.48,\"2019-10-17 20:48:46\",1,0,0],[1.6,3.9,4.25,89.55,\"2019-10-16 00:02:34\",0,0,0]]";
		List <EuroPl> euroPls = new ArrayList<EuroPl>();
		String[][] datas = GsonConverter.convertJSonToObjectUseNormal(resData, new TypeToken<String[][]>(){});
		
		if (datas != null && datas.length > 0) {
			for (String[] eu : datas) {
				EuroPl pl = new EuroPl(Float.parseFloat(eu[0]),
						Float.parseFloat(eu[1]),
						Float.parseFloat(eu[2]),
						DateUtil.parseDateWithDataBase(eu[4]));
				
				euroPls.add(pl);
			}
		}
		
		Collections.<EuroPl>sort(euroPls, (v1, v2) -> {
			return v1.getEDate().compareTo(v2.getEDate());
		});
		
		EuroCalculator euc = new EuroCalculator();
//		System.out.print(euc.getAbsoluteEuroMatrix(euroPls, DateUtil.parseDateWithDataBase("2019-10-23 00:15:00")));
	}
}
