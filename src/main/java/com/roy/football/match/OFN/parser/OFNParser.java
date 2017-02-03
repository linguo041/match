package com.roy.football.match.OFN.parser;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Component;

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
import com.roy.football.match.util.GsonConverter;
import com.roy.football.match.util.MatchUtil;
import com.roy.football.match.util.StringUtil;
import com.roy.football.match.util.XmlParseException;
import com.roy.football.match.util.XmlParser;

@Component
public class OFNParser {
	private final static String JIN_CAI_URL = "http://www.159cai.com/cpdata/omi/jczq/odds/odds.xml";
	private final static String ANALYSIS_URL_PREFIX = "http://odds.159cai.com/match/analysis/";
	private final static String DETAIL_URL_PREIX = "http://odds.159cai.com/match/detial";

	private final static Pattern KEY_VALUE_REG = Pattern.compile("\\b(\\w+)\\b\\s*=\\s*(.+?);");
	
	private final static String HOME_PANKOU = "H";
	private final static String AWAY_PANKOU = "A";

	public List<JinCaiMatch> parseJinCaiMatches () {

		try {
			Map<String, String> headers = new HashMap<String, String>();
			
			String resData = HttpRequestService.getInstance().doHttpRequest(JIN_CAI_URL, HttpRequestService.GET_METHOD, null, headers);
			
			JinCaiSummary response = XmlParser.parseXmlToObject(
					new StringReader(resData),
					JinCaiSummary.class, "xml");

			return response.getRows();
		} catch (HttpRequestException | XmlParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return null;
	}
	
	public OFNMatchData parseMatchData (Long oddsmid) {
		OFNMatchData ofnMatchData = null;

		try {
			Document doc = Jsoup.connect(ANALYSIS_URL_PREFIX + oddsmid).get();
			
			Element script = doc.select("script").last();
			String jsData = script.data();

			ofnMatchData = new OFNMatchData();
			
			parseScore(doc, ofnMatchData);
			Matcher matcher = KEY_VALUE_REG.matcher(jsData);

			while (matcher.find()) {
				String key = matcher.group(1);
				String val = matcher.group(2);
				generateMatchData(key, val, ofnMatchData);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return ofnMatchData;
	}
	
	public void parseScore (Document doc, OFNMatchData ofnMatchData) {
		Elements eles = doc.select(".spanVS");
		
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

	
	public List <EuroPl> parseEuroData (Long oddsmid, Company company) {
		try {
			Map<String, String> headers = new HashMap<String, String>();

			String resData = HttpRequestService.getInstance().doHttpRequest(DETAIL_URL_PREIX
						+ "?cid=" + company.getCompanyId() + "&mid=" + oddsmid + "&etype=" + EType.eruo,
					HttpRequestService.GET_METHOD, null, headers);
			
			String[][] datas = GsonConverter.convertJSonToObjectUseNormal(resData, new TypeToken<String[][]>(){});
			
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
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	public List<AsiaPl> parseAsiaData (Long oddsmid, Company company) {
		try {
			Map<String, String> headers = new HashMap<String, String>();

			String resData = HttpRequestService.getInstance().doHttpRequest(DETAIL_URL_PREIX
						+ "?cid=" + company.getCompanyId() + "&mid=" + oddsmid + "&etype=" + EType.asia,
					HttpRequestService.GET_METHOD, null, headers);
			
			String[][] datas = GsonConverter.convertJSonToObjectUseNormal(resData, new TypeToken<String[][]>(){});
			
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
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	public List<AsiaPl> parseDaxiaoData (Long oddsmid, Company company) {
		try {
			Map<String, String> headers = new HashMap<String, String>();

			String resData = HttpRequestService.getInstance().doHttpRequest(DETAIL_URL_PREIX
						+ "?cid=" + company.getCompanyId() + "&mid=" + oddsmid + "&etype=" + EType.shangxia,
					HttpRequestService.GET_METHOD, null, headers);
			
			String[][] datas = GsonConverter.convertJSonToObjectUseNormal(resData, new TypeToken<String[][]>(){});
			
			if (datas != null && datas.length > 0) {
				List <AsiaPl> asiaPls = new ArrayList<AsiaPl>();
				
				for (String[] as : datas) {
					AsiaPl asia = new AsiaPl();
					asia.sethWin(Float.parseFloat(as[0]));
					asia.setaWin(Float.parseFloat(as[2]) - 1);
					
					Float pankouVal = Float.parseFloat(as[1]) / 4;
					asia.setPanKou(pankouVal);
					
					asia.setPkDate(MatchUtil.parseFromOFHString(as[3]));
					
					asiaPls.add(asia);
				}
				
				return asiaPls;
			}
			
		} catch (HttpRequestException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	private void generateMatchData (String key, String val, OFNMatchData ofnMatchData) {
		if (StringUtil.isEmpty(key) ||StringUtil.isEmpty(val)) {
			return;
		}

		Match match = Match.shortKeyOf(key);
		
		if (match != null) {
			switch (match) {
				case MatchId :
					val = getQuotedString(val);
					ofnMatchData.setMatchId(Long.parseLong(val));
					break;
				case MatchTime :
					val = getQuotedString(val);
					if (!StringUtil.isEmpty(val)) {
						Date matchDate = new Date(Long.parseLong(val)*1000);
						ofnMatchData.setMatchTime(matchDate);
					}
					break;
				case HostName :
					val = getQuotedString(val);
					ofnMatchData.setHostName(val);
					break;
				case HostId :
					val = getQuotedString(val);
					ofnMatchData.setHostId(Long.parseLong(val));
					break;
				case GuestName :
					val = getQuotedString(val);
					ofnMatchData.setGuestName(val);
					break;
				case GuestId :
					val = getQuotedString(val);
					ofnMatchData.setGuestId(Long.parseLong(val));
					break;
				case BaseData :
					ClubDatas clubDatas = OFHConverter.convertClubDatas(val);
					ofnMatchData.setBaseData(clubDatas);
					break;
				case OldMatch :
					List<FinishedMatch> jiaoShou = OFHConverter.convertJiaoShouMatch(val);
					ofnMatchData.setJiaoShou(jiaoShou);
					break;
				case HostOldMatch :
					List<FinishedMatch> hostMatches = OFHConverter.convertJiaoShouMatch(val);
					ofnMatchData.setHostMatches(hostMatches);
					break;
				case GuestOldMatch :
					List<FinishedMatch> guestMatches = OFHConverter.convertJiaoShouMatch(val);
					ofnMatchData.setGuestMatches(guestMatches);
					break;
					//TODO - 
				default :
					break;
					
				
			}
		}
	}
	
	private String getQuotedString (String quoteStr) {
		quoteStr = quoteStr.trim();
		int end = quoteStr.length() - 1;
		return quoteStr.substring(1, end);
	}

	public static void main (String [] args) {
		String instr = "[[\"4.00\",\"3.75\",\"1.75\",\"1454880574\"],[\"3.60\",\"3.75\",\"1.83\",\"1455221779\"],[\"4.20\",\"3.80\",\"1.70\",\"1455434403\"],[\"3.60\",\"3.75\",\"1.83\",\"1455435003\"],[\"4.20\",\"3.80\",\"1.70\",\"1455435312\"]]";
		
		String out[][] = GsonConverter.convertJSonToObjectUseNormal(instr, new TypeToken<String[][]>(){});
		
		System.out.println(out);
	}
}
