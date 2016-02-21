package com.roy.football.match.main.OFH.parser;

import java.io.IOException;
import java.io.StringReader;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import com.roy.football.match.OFN.response.AsiaData;
import com.roy.football.match.OFN.response.ClubDatas;
import com.roy.football.match.OFN.response.EuroData;
import com.roy.football.match.OFN.response.JinCaiSummary;
import com.roy.football.match.OFN.response.OFNMatchData;
import com.roy.football.match.OFN.response.JinCaiSummary.JinCaiMatch;
import com.roy.football.match.httpRequest.HttpRequestException;
import com.roy.football.match.httpRequest.HttpRequestService;
import com.roy.football.match.main.OFH.parser.OFHKey.Match;
import com.roy.football.match.util.StringUtil;
import com.roy.football.match.util.XmlParseException;
import com.roy.football.match.util.XmlParser;

public class Parser {
	private final static String JIN_CAI_URL = "http://www.159cai.com/cpdata/omi/jczq/odds/odds.xml";
	private final static String ANALYSIS_URL_PREFIX = "http://odds.159cai.com/match/analysis/";

	private final static Pattern KEY_VALUE_REG = Pattern.compile("\\b(\\w+)\\b\\s*=\\s*(.+?);");
	
	public Parser(HttpRequestService httpService) {
		this.httpService = httpService;
	}
	
	
	public List<JinCaiMatch> parseJinCaiMatches () {

		try {
			String resData = this.httpService.doHttpRequest(JIN_CAI_URL, HttpRequestService.GET_METHOD, null, null);
			
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

	
	public EuroData parseEuroData (Long oddsmid) {
		return null;
	}
	
	public AsiaData parseAsiaData (Long oddsmid) {
		return null;
	}
	
	private void generateMatchData (String key, String val, OFNMatchData ofnMatchData) {
		if (StringUtil.isEmpty(key) ||StringUtil.isEmpty(val)) {
			return;
		}
		
		val = getQuotedString(val);
		
		Match match = Match.shortKeyOf(key);
		
		if (match != null) {
			switch (match) {
				case MatchId : ofnMatchData.setMatchId(Long.parseLong(val));
					break;
				case MatchTime :
					Date matchDate = new Date(Long.parseLong(val));
					ofnMatchData.setMatchTime(matchDate);
					break;
				case HostName : 
					ofnMatchData.setHostName(val);
					break;
				case HostId :
					ofnMatchData.setHostId(Long.parseLong(val));
					break;
				case GuestName :
					ofnMatchData.setGuestName(val);
					break;
				case GuestId :
					ofnMatchData.setGuestId(Long.parseLong(val));
					break;
				case BaseData :
					ClubDatas clubDatas = OFHConverter.convertClubDatas(val);
					ofnMatchData.setBaseData(clubDatas);
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
	
	public HttpRequestService getHttpService() {
		return httpService;
	}

	public void setHttpService(HttpRequestService httpService) {
		this.httpService = httpService;
	}

	private HttpRequestService httpService;
}
