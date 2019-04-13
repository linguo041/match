package com.roy.football.match.fivemillion;

import java.io.IOException;
import java.io.StringReader;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
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
	private final static String EURO_URL = "http://odds.500.com/fenxi/json/ouzhi.php";
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

	public List <EuroPl> parseEuroData (String fmatchId, Company company) {
		try {
			Document doc = Jsoup.connect(EURO_URL
					+ "?cid=" + company.getFmCompanyId() + "&fid=" + fmatchId + "&type=europe").get();
			
			String[][] datas = GsonConverter.convertJSonToObjectUseNormal(doc.body().text(), new TypeToken<String[][]>(){});
			
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
			log.error(String.format("unable to parse match euro data: match [%s], company [%s]", fmatchId, company), e);
		}
		return null;
	}
	
	private Long generateMatchDayId (Long day, String fmMatchDayOrder) {
		return Long.parseLong(day + fmMatchDayOrder.substring(fmMatchDayOrder.length() - 3));
	}

	public static void main (String [] args) {
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
		
		FmRawMatch match1 = p.getFmMatch(190107005L);
		System.out.println(match1);
		FmRawMatch match2 = p.getFmMatch(190107006L);
		System.out.println(match2);
	}
}
