package com.roy.football.match.main.OFH.parser;

import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.roy.football.match.OFN.response.OFNMatchData;
import com.roy.football.match.main.OFH.parser.OFHKey.Match;
import com.roy.football.match.util.StringUtil;

public class Parser {
	private static Pattern KEY_VALUE_REG = Pattern.compile("\\b(\\w+)\\b\\s*=\\s*(.+?);");
	
	public void parse (String rawData) {
		Matcher matcher = KEY_VALUE_REG.matcher(rawData);
		
		OFNMatchData ofnMatchData = new OFNMatchData();

		while (matcher.find()) {
			String key = matcher.group(1);
			String val = matcher.group(2);
			generateMatchData(key, val, ofnMatchData);
		}
		
		System.out.println(ofnMatchData);
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
					break;
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
		String data3 = "var a = ddd; var mm=4; var nn = {abc:1}; var b=}; var c = 4";
		new Parser().parse(data3);
	}
}
