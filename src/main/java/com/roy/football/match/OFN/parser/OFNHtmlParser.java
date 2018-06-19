package com.roy.football.match.OFN.parser;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
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
import com.roy.football.match.util.GsonConverter;
import com.roy.football.match.util.MatchUtil;
import com.roy.football.match.util.StringUtil;
import com.roy.football.match.util.XmlParseException;
import com.roy.football.match.util.XmlParser;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class OFNHtmlParser {
//	private final static String JIN_CAI_URL = "http://www.159cai.com/cpdata/omi/jczq/odds/odds.xml";
	private final static String JIN_CAI_URL = "http://m.159cai.com/cpdata/omi/jczq/odds/odds.xml";
	private final static String DETAIL_URL_PREIX = "http://odds.159cai.com/json/match/oddshistory";

	private final static Pattern KEY_VALUE_REG = Pattern.compile("\\b(\\w+)\\b\\s*=\\s*(.+?);");
	
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
	
	public OFNMatchData parseMatchData (Long oddsmid) throws MatchParseException {
		OFNMatchData ofnMatchData = null;

		try {
			Document doc = Jsoup.connect("http://odds.159cai.com/match/" + oddsmid).get();
			
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
					ofnMatchData.setHostId(mgData.getHtid());
					ofnMatchData.setHostName(mgData.getHome());
					ofnMatchData.setGuestId(mgData.getAtid());
					ofnMatchData.setGuestName(mgData.getAway());
					ofnMatchData.setMatchDayId(mgData.getXid());
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

			String resData = HttpRequestService.getInstance().doHttpRequest(DETAIL_URL_PREIX
						+ "?cid=" + company.getCompanyId() + "&mid=" + oddsmid + "&etype=" + EType.eruo,
					HttpRequestService.GET_METHOD, null, headers);
			
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
			
		} catch (HttpRequestException e) {
			throw new MatchParseException(String.format("Parse euro pl: ofn_match_id: %d, company: %s", oddsmid, company), e);
		}
		return null;
	}
	
	public List<AsiaPl> parseAsiaData (Long oddsmid, Company company) throws MatchParseException {
		try {
			Map<String, String> headers = new HashMap<String, String>();

			String resData = HttpRequestService.getInstance().doHttpRequest(DETAIL_URL_PREIX
						+ "?cid=" + company.getCompanyId() + "&mid=" + oddsmid + "&etype=" + EType.asia,
					HttpRequestService.GET_METHOD, null, headers);
			
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
			
		} catch (HttpRequestException e) {
			throw new MatchParseException(String.format("Parse asia pk: ofn_match_id: %d, company: %s", oddsmid, company), e);
		}
		return null;
	}
	
	public List<AsiaPl> parseDaxiaoData (Long oddsmid, Company company) throws MatchParseException {
		try {
			Map<String, String> headers = new HashMap<String, String>();

			String resData = HttpRequestService.getInstance().doHttpRequest("http://odds.159cai.com/json/match/oddshistory" 
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
			
			String resData = HttpRequestService.getInstance().doHttpRequest("http://odds.159cai.com/json/match/standingteam?mid="+oddsmid,
					HttpRequestService.GET_METHOD, null, headers);
			
			ofnMatchData.setBaseData(OFHConverter.convertWrappedClubDatas(resData));
		} catch (HttpRequestException e) {
			throw new MatchParseException("unable to parse jincai matches", e);
		}
	}
	
	private void parseRecentMatches (Long oddsmid, OFNMatchData ofnMatchData) throws MatchParseException {
		try {
			Map<String, String> headers = new HashMap<String, String>();
			
			String resData = HttpRequestService.getInstance().doHttpRequest("http://odds.159cai.com/json/match/recentmatch?mid="+oddsmid,
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
			
			String resData = HttpRequestService.getInstance().doHttpRequest("http://odds.159cai.com/json/match/historywars?mid="+oddsmid,
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

	public static void main (String [] args) throws MatchParseException {
//		String instr = "[[\"4.00\",\"3.75\",\"1.75\",\"1454880574\"],[\"3.60\",\"3.75\",\"1.83\",\"1455221779\"],[\"4.20\",\"3.80\",\"1.70\",\"1455434403\"],[\"3.60\",\"3.75\",\"1.83\",\"1455435003\"],[\"4.20\",\"3.80\",\"1.70\",\"1455435312\"]]";
//		
//		String out[][] = GsonConverter.convertJSonToObjectUseNormal(instr, new TypeToken<String[][]>(){});
//		
//		System.out.println(out);
		
		OFNHtmlParser ppp = new OFNHtmlParser();
		OFNMatchData ofnMatchData = new OFNMatchData();
//		ppp.parseClubDatas(1074452L, ofnMatchData);
//		ppp.parseRecentMatches(1074452L, ofnMatchData);
		
		System.out.println(ppp.parseEuroData(1074452L, Company.Aomen));
		System.out.println(ppp.parseAsiaData(1074452L, Company.Aomen));
		System.out.println(ppp.parseDaxiaoData(1074452L, Company.Aomen));
//		System.out.println(ppp.parseMatchData(1074452L));
		ofnMatchData.setHostId(1269L);
		ppp.parseJiaoShouMatches(1074452L, ofnMatchData);
		System.out.println(ofnMatchData);
		
	}
}
