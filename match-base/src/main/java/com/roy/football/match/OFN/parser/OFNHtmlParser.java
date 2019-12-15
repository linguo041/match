package com.roy.football.match.OFN.parser;

import java.io.IOException;
import java.io.StringReader;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Component;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
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
import com.roy.football.match.OFN.response.OFNMatchGeneralData;
import com.roy.football.match.OFN.response.OFNResponseWrapper;
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
public class OFNHtmlParser {
	private final static String LEAGUE_URL = "http://info.159cai.com/league/";
//	private final static String JIN_CAI_URL = "http://www.159cai.com/cpdata/omi/jczq/odds/odds.xml";
	private final static String JIN_CAI_URL = "http://m.159cai.com/cpdata/omi/jczq/odds/odds.xml";
//	private final static String DETAIL_URL_PREIX = "http://odds.159cai.com/json/match/oddshistory";
//	private final static String JIN_CAI_BF_URL = "http://bf.159cai.com/mcache/livejcjs/";
	private final static String JIN_CAI_BF_URL = "http://mcache.iuliao.com/mcache/livejcjs/";
//	private final static String OFN_HOST = "odds.159cai.com";
	private final static String OFN_HOST = "www.iuliao.com";
	
	private final static String MATCH_DETAIL_URL = "https://www.iuliao.com/odds/match/";
	private final static String MATCH_ODDS_HISTORY_URL = "https://www.iuliao.com/json/match/oddshistory";
	private final static String MATCH_TEAM_URL = "https://www.iuliao.com/json/match/standingteam?mid=";
	private final static String MATCH_RECENT_URL = "https://www.iuliao.com/json/match/recentmatch?mid=";
	private final static String MATCH_JIAOSHOU_URL = "https://www.iuliao.com/json/match/historywars?mid=";

	private final static Pattern KEY_VALUE_REG = Pattern.compile("\\b(\\w+)\\b\\s*=\\s*(.+?);");
	private final static Pattern BF_KEY_VALUE_REG = Pattern.compile("zcdz\\['(\\d+)'\\]=\\[(.+?)\\];");
	
	private final static String HOME_PANKOU = "H";
	private final static String AWAY_PANKOU = "A";

	public List<JinCaiMatch> parseJinCaiMatches () throws MatchParseException {

		try {
			Map<String, String> headers = new HashMap<String, String>();
			
			String resData = HttpRequestService.getInstance().doHttpRequest(JIN_CAI_URL, HttpRequestService.GET_METHOD, null, headers);
			
			JinCaiSummary response = XmlParser.parseXmlToObject(
					new StringReader(resData),
					JinCaiSummary.class, "xml");

			return response.getRows();
		} catch (HttpRequestException | XmlParseException e) {
			throw new MatchParseException("unable to parse jincai matches", e);
		}
	}
	
	public List<JinCaiMatch> parseJinCaiMatchesBf () throws MatchParseException {
		Date today = new Date();
		String todayStr = DateUtil.formatSimpleDate(today);
		
		Date tomorrow = DateUtil.tomorrow(today);
		String tomorrowStr = DateUtil.formatSimpleDate(tomorrow);
		
		Date yestoday = DateUtil.yesterday(today);
		String yestodayStr = DateUtil.formatSimpleDate(yestoday);
		
		Date beforeYest = DateUtil.yesterday(yestoday);
		String beforeYestStr = DateUtil.formatSimpleDate(beforeYest);
		
//		List<JinCaiMatch> todayMatches = parseJinCaiMatchesBf(todayStr);
//		List<JinCaiMatch> beforeYestodayMatches = parseJinCaiMatchesBf(beforeYestStr);
//		List<JinCaiMatch> yestodayMatches = parseJinCaiMatchesBf(yestodayStr);
//		List<JinCaiMatch> tomorrowMatches = parseJinCaiMatchesBf(tomorrowStr);
		
		List<JinCaiMatch> todayMatches = Lists.newArrayList();
		for (int i = 1; i <= 30; i++) {
			todayMatches.addAll(parseJinCaiMatchesBf(String.format("201910%02d", i)));
		}
		
//		todayMatches.addAll(beforeYestodayMatches);
//		todayMatches.addAll(yestodayMatches);
//		todayMatches.addAll(tomorrowMatches);
		return todayMatches;
	}
	
	public List<JinCaiMatch> parseJinCaiMatchesBf (String dateStr) throws MatchParseException {
		try {
			Map<String, String> headers = new HashMap<String, String>();
			
			String resData = HttpRequestService.getInstance().doHttpRequest(JIN_CAI_BF_URL + dateStr + ".js", HttpRequestService.GET_METHOD, null, headers);
			
			Matcher matcher = BF_KEY_VALUE_REG.matcher(resData);

			List<JinCaiMatch> matches = Lists.newArrayList();
			while (matcher.find()) {
				JinCaiMatch match = new JinCaiMatch();
				String key = matcher.group(1);
				String val = matcher.group(2);

				match.setXid(Long.parseLong(key));
				String[] arr = val.split("^'|','|'$");
//				System.out.println(Lists.newArrayList(arr));
				
				match.setOddsmid(Long.parseLong(arr[1]));
				match.setHtid(Long.parseLong(arr[18]));
				match.setHn(arr[5]);
				match.setGtid(Long.parseLong(arr[19]));
				match.setGn(arr[6]);
				match.setLid(Long.parseLong(arr[28]));
				match.setLn(arr[2]);
				match.setMtime(DateUtil.parseCommaDate(arr[4]));
				matches.add(match);
			}
			
			return matches;
		} catch (HttpRequestException | ParseException e) {
			throw new MatchParseException("unable to parse jincai matches", e);
		}
	}
	
	public OFNMatchData parseMatchData (Long oddsmid) throws MatchParseException {
		OFNMatchData ofnMatchData = null;

		try {
			Document doc = Jsoup.connect(MATCH_DETAIL_URL + oddsmid).get();
			
			Element script = doc.select("script").first();
			String jsData = script.data();

			ofnMatchData = new OFNMatchData();
			
			parseScore(doc, ofnMatchData);
			Matcher matcher = KEY_VALUE_REG.matcher(jsData);

			while (matcher.find()) {
				String key = matcher.group(1);
				String val = matcher.group(2);

				if ("match".equals(key)) {
					OFNMatchGeneralData mgData = GsonConverter.convertJSonToObjectUseNormal(val, OFNMatchGeneralData.class);
					
					try {
						String xidStr = mgData.getXidStr();
						ofnMatchData.setMatchDayId(Long.parseLong(xidStr));
					} catch (Exception e) {
						
					}
					
					ofnMatchData.setHostId(mgData.getHtid());
					ofnMatchData.setHostName(mgData.getHome());
					ofnMatchData.setGuestId(mgData.getAtid());
					ofnMatchData.setGuestName(mgData.getAway());
					
					ofnMatchData.setMatchId(mgData.getMid());
					ofnMatchData.setMatchTime(new Date(mgData.getMtime() * 1000));
				}
			}
		} catch (IOException e) {
			throw new MatchParseException("Parse match detail: ofn_match_id:"+ oddsmid, e);
		}
		
		parseClubDatas(oddsmid, ofnMatchData);
		parseRecentMatches(oddsmid, ofnMatchData);
		parseJiaoShouMatches(oddsmid, ofnMatchData);
		
		return ofnMatchData;
	}
	
	public void parseScore (Document doc, OFNMatchData ofnMatchData) {
		Elements eles = doc.select(".odds-score");
		
		if (eles != null && eles.size() > 0) {
			Element ele = eles.first();
			String scoreText =  ele.text();
			String scores[] = scoreText.split(":");
			
			if (scores != null && scores.length > 1) {
				ofnMatchData.setHostScore(Integer.parseInt(scores[0]));
				ofnMatchData.setGuestScore(Integer.parseInt(scores[1]));
			}
		}
	}
	
	public List <EuroPl> parseEuroData (Long oddsmid, Company company) throws MatchParseException {
		try {
			Map<String, String> headers = new HashMap<String, String>();

			String resData = HttpRequestService.getInstance().doHttpRequest(MATCH_ODDS_HISTORY_URL
						+ "?cid=" + company.getCompanyId() + "&mid=" + oddsmid + "&etype=" + EType.eruo,
					HttpRequestService.GET_METHOD, null, headers);
			
			if (!StringUtil.isEmpty(resData)) {
				String[][] datas = GsonConverter.convertJSonToObjectUseNormal(resData,
						new TypeToken<OFNResponseWrapper<String[][]>>(){}).getData();
				
				if (datas != null && datas.length > 0) {
					List <EuroPl> euroPls = new ArrayList<EuroPl>();
					
					for (String[] eu : datas) {
						EuroPl pl = new EuroPl(Float.parseFloat(eu[0]),
								Float.parseFloat(eu[1]),
								Float.parseFloat(eu[2]),
								MatchUtil.parseFromOFHString(eu[3]));
						
						euroPls.add(pl);
					}
					
					return euroPls;
				}
			}
			
			log.warn(String.format("Euro data of match id %d & company %s is empty", oddsmid, company));
		} catch (HttpRequestException e) {
			throw new MatchParseException(String.format("Parse euro pl: ofn_match_id: %d, company: %s", oddsmid, company), e);
		}
		return null;
	}
	
	public List<AsiaPl> parseAsiaData (Long oddsmid, Company company) throws MatchParseException {
		try {
			Map<String, String> headers = new HashMap<String, String>();

			String resData = HttpRequestService.getInstance().doHttpRequest(MATCH_ODDS_HISTORY_URL
						+ "?cid=" + company.getCompanyId() + "&mid=" + oddsmid + "&etype=" + EType.asia,
					HttpRequestService.GET_METHOD, null, headers);
			
			if (!StringUtil.isEmpty(resData)) {
				String[][] datas = GsonConverter.convertJSonToObjectUseNormal(resData,
						new TypeToken<OFNResponseWrapper<String[][]>>(){}).getData();
				
				if (datas != null && datas.length > 0) {
					List <AsiaPl> asiaPls = new ArrayList<AsiaPl>();
					
					for (String[] as : datas) {
						AsiaPl asia = new AsiaPl();
						asia.sethWin(Float.parseFloat(as[0]));
						asia.setaWin(Float.parseFloat(as[2]));
						
						Double pankouVal = (Float.parseFloat(as[1]) - 1) * 0.25;
						
						if (HOME_PANKOU.equalsIgnoreCase(as[3])) {
							asia.setPanKou(pankouVal.floatValue());
						} else if (AWAY_PANKOU.equalsIgnoreCase(as[3])) {
							asia.setPanKou(-1 * pankouVal.floatValue());
						}
						
						asia.setPkDate(MatchUtil.parseFromOFHString(as[4]));
						
						asiaPls.add(asia);
					}
					
					return asiaPls;
				}
			}
			
			log.warn(String.format("Asia data of match id %d & company %s is empty", oddsmid, company));
		} catch (HttpRequestException e) {
			throw new MatchParseException(String.format("Parse asia pk: ofn_match_id: %d, company: %s", oddsmid, company), e);
		}
		return null;
	}
	
	public List<AsiaPl> parseDaxiaoData (Long oddsmid, Company company) throws MatchParseException {
		try {
			Map<String, String> headers = new HashMap<String, String>();

			String resData = HttpRequestService.getInstance().doHttpRequest(MATCH_ODDS_HISTORY_URL 
						+ "?cid=" + company.getCompanyId() + "&mid=" + oddsmid + "&etype=" + EType.shangxia,
					HttpRequestService.GET_METHOD, null, headers);
			
			String[][] datas = GsonConverter.convertJSonToObjectUseNormal(resData,
					new TypeToken<OFNResponseWrapper<String[][]>>(){}).getData();
			
			if (datas != null && datas.length > 0) {
				List <AsiaPl> asiaPls = new ArrayList<AsiaPl>();
				
				for (String[] as : datas) {
					AsiaPl asia = new AsiaPl();
					asia.sethWin(Float.parseFloat(as[0]));
					asia.setaWin(Float.parseFloat(as[2]));
					
					Float pankouVal = Float.parseFloat(as[1]) / 4;
					asia.setPanKou(pankouVal);
					
					asia.setPkDate(MatchUtil.parseFromOFHString(as[3]));
					
					asiaPls.add(asia);
				}
				
				return asiaPls;
			}
			
		} catch (HttpRequestException e) {
			throw new MatchParseException(String.format("Parse asia daxiao pk: ofn_match_id: %d, company: %s", oddsmid, company), e);
		}
		return null;
	}
	
	private void parseClubDatas (Long oddsmid, OFNMatchData ofnMatchData) throws MatchParseException {
		try {
			Map<String, String> headers = new HashMap<String, String>();
			
			String resData = HttpRequestService.getInstance().doHttpRequest(MATCH_TEAM_URL+oddsmid,
					HttpRequestService.GET_METHOD, null, headers);
			
			ofnMatchData.setBaseData(OFHConverter.convertWrappedClubDatas(resData));
		} catch (HttpRequestException e) {
			throw new MatchParseException("unable to parse jincai matches", e);
		}
	}
	
	private void parseRecentMatches (Long oddsmid, OFNMatchData ofnMatchData) throws MatchParseException {
		try {
			Map<String, String> headers = new HashMap<String, String>();
			
			String resData = HttpRequestService.getInstance().doHttpRequest(MATCH_RECENT_URL+oddsmid,
					HttpRequestService.GET_METHOD, null, headers);
			
			JsonParser parser = new JsonParser();
			JsonObject rootObj = parser.parse(resData.toString()).getAsJsonObject();   
			JsonObject dataObj = rootObj.getAsJsonObject("data");
			JsonObject homeRecent = dataObj.getAsJsonObject("home");
			JsonObject awayRecent = dataObj.getAsJsonObject("away");
			
			JsonArray homeMatchArray = homeRecent.getAsJsonArray("matchs");
			JsonArray awayMatchArray = awayRecent.getAsJsonArray("matchs");
			List<FinishedMatch> hostMatches = OFHConverter.convertJiaoShouMatch(homeMatchArray.toString());
			List<FinishedMatch> awayMatches = OFHConverter.convertJiaoShouMatch(awayMatchArray.toString());

			ofnMatchData.setHostMatches(hostMatches);
			ofnMatchData.setGuestMatches(awayMatches);
		} catch (HttpRequestException e) {
			throw new MatchParseException("unable to parse jincai matches", e);
		}
	}
	
	private void parseJiaoShouMatches (Long oddsmid, OFNMatchData ofnMatchData) throws MatchParseException {
		try {
			Map<String, String> headers = new HashMap<String, String>();
			
			String resData = HttpRequestService.getInstance().doHttpRequest(MATCH_JIAOSHOU_URL+oddsmid,
					HttpRequestService.GET_METHOD, null, headers);
			
			JsonParser parser = new JsonParser();
			JsonObject rootObj = parser.parse(resData).getAsJsonObject();   
			JsonObject dataObj = rootObj.getAsJsonObject("data");
			JsonArray jsArray = dataObj.getAsJsonArray("matchs");
			JsonObject asiaObj = dataObj.getAsJsonObject("asia");
			
			Set<Map.Entry<String, JsonElement>> matchSet = asiaObj.entrySet();
			Map<Long, AsiaPl> asiaPls = Maps.newHashMap();
			
			matchSet.stream().forEach(entry -> {
				Long matchId = Long.parseLong(entry.getKey());
				JsonObject matchAsia = (JsonObject)entry.getValue();
				
				if (matchAsia.has("442")) {
					JsonObject asiaData = matchAsia.getAsJsonObject("442");
					
					AsiaPl asia = new AsiaPl();
					asia.sethWin(asiaData.get("ab").getAsFloat());
					asia.setaWin(asiaData.get("be").getAsFloat());
					
					Double pankouVal = (asiaData.get("bets").getAsInt() - 1) * 0.25;
					String pkType = asiaData.get("tp").getAsString();
					
					if (HOME_PANKOU.equalsIgnoreCase(pkType)) {
						asia.setPanKou(pankouVal.floatValue());
					} else if (AWAY_PANKOU.equalsIgnoreCase(pkType)) {
						asia.setPanKou(-1 * pankouVal.floatValue());
					}
					
					asiaPls.put(matchId, asia);
				}
			});
			
			List<FinishedMatch> jsMatches = OFHConverter.convertJiaoShouMatch(jsArray.toString());
			
			jsMatches.stream().forEach(jsMatch -> {
				AsiaPl pl = asiaPls.get(jsMatch.getMatchId());
				if (pl != null) {
					jsMatch.setAsiaPanKou(pl.getPanKou());
					
					float panlu = jsMatch.getHscore() - jsMatch.getAscore() - pl.getPanKou();
					panlu = ofnMatchData.getHostId().equals(jsMatch.getHostId()) ? panlu : panlu * -1;
					if (panlu > 0.0f) {
						jsMatch.setAsiaPanLu(MatchUtil.UNICODE_WIN);
					} else if (panlu < 0.0f) {
						jsMatch.setAsiaPanLu(MatchUtil.UNICODE_LOSE);
					} else {
						jsMatch.setAsiaPanLu(MatchUtil.UNICODE_DRAW);
					}
				}
			});

			ofnMatchData.setJiaoShou(jsMatches);
		} catch (HttpRequestException e) {
			throw new MatchParseException("unable to parse jincai matches", e);
		} catch (Exception e1) {
			log.error(e1.getMessage());
		}
	}
	
	private String getQuotedString (String quoteStr) {
		quoteStr = quoteStr.trim();
		int end = quoteStr.length() - 1;
		return quoteStr.substring(1, end);
	}

	public static void main (String [] args) throws MatchParseException, Exception {
//		String instr = "{\"euro99\":[\"2.90\",\"3.26\",\"2.36\"],\"ltype\":1,\"cl\":\"9E277D\",\"venues\":\"\",\"except\":\"\",\"hhs\":\"0\",\"weather\":\"\",\"mtime\":1541533500,\"home\":\"\u8bfa\u8328\u90e1\",\"rid\":13,\"lid\":106,\"atid\":633,\"rq\":\"\",\"ln\":\"\u82f1\u4e59\",\"away\":\"\u5965\u5fb7\u6c49\u59c6\",\"htid\":771,\"mid\":1195423,\"sid\":7854,\"has\":\"0\",\"referee\":\"\",\"hs\":\"0\",\"xid\":\"\",\"asiatop\":[\"1.090\",2,\"0.810\",\"H\",1541328210,448],\"exflag\":0,\"as\":\"0\",\"season\":\"2018-2019\",\"oname\":\"\"}";
				
//		OFNMatchGeneralData mgData = GsonConverter.convertJSonToObjectUseNormal(instr, OFNMatchGeneralData.class);
//		String out[][] = GsonConverter.convertJSonToObjectUseNormal(instr, new TypeToken<String[][]>(){});
		
//		System.out.println(mgData);
//
//		OFNHtmlParser ppp = new OFNHtmlParser();
//		System.out.println(ppp.parseMatchData(1201196L));
		
//		OFNMatchData ofnMatchData = new OFNMatchData();
//		ppp.parseClubDatas(1201196L, ofnMatchData);
//		ppp.parseRecentMatches(1201196L, ofnMatchData);
//		ppp.parseJiaoShouMatches(1201196L, ofnMatchData);
//		System.out.println(ofnMatchData);
		
//		System.out.println(ppp.parseEuroData(1201196L, Company.Aomen));
//		System.out.println(ppp.parseAsiaData(1201196L, Company.Aomen));
//		System.out.println(ppp.parseDaxiaoData(1201196L, Company.Aomen));
		
//		ofnMatchData.setHostId(1269L);

		/*
		String tests = " zcdz['180824039']=['1243179','亚运男足','#E03C1C','2018,08,24,17,00,00','印度尼西亚国奥','阿联酋国奥','2','2','0','0','4','','0-1','','5','27℃～28℃ ','90分钟[2-2],120分钟[2-2],点球[3-4]','3141','1156','1','A1','C3','0','0','0.0','1.73','1.97','474','0','1','第二圈','2','0'];";
				
		Matcher matcher = BF_KEY_VALUE_REG.matcher(tests);

		while (matcher.find()) {
			JinCaiMatch match = new JinCaiMatch();
			String key = matcher.group(1);
			String val = matcher.group(2);

			match.setXid(Long.parseLong(key));
			System.out.println(val);
			String[] arr = val.split("^'|','|'$");
			System.out.println(Lists.newArrayList(arr));
			match.setOddsmid(Long.parseLong(arr[1]));
			match.setHtid(Long.parseLong(arr[18]));
			match.setHn(arr[5]);
			match.setGtid(Long.parseLong(arr[19]));
			match.setGn(arr[6]);
			match.setLid(Long.parseLong(arr[28]));
			match.setLn(arr[2]);
			match.setMtime(DateUtil.parseCommaDate(arr[4]));
			System.out.println(match);
		}
		*/
		System.out.println(String.format("aa%02d", 2));
	}
}
