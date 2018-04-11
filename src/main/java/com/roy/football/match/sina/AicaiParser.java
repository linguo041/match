package com.roy.football.match.sina;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
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

import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
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
import com.roy.football.match.sina.AicaiEuroResult.AicaiEuro;
import com.roy.football.match.sina.AicaiEuroResult.EuropDetailOddsVoList;
import com.roy.football.match.util.DateUtil;
import com.roy.football.match.util.GsonConverter;
import com.roy.football.match.util.MatchUtil;
import com.roy.football.match.util.StringUtil;
import com.roy.football.match.util.XmlParseException;
import com.roy.football.match.util.XmlParser;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class AicaiParser {
	private final static String AICAI_JCZQ = "http://www.aicai.com/lotnew/jc/getMatchByDate.htm";
	private final static String EURO_URL = "http://live.aicai.com/xiyaou/odds!getOddsTrack.htm";


	private static Pattern pattern = Pattern.compile("'\\d{3}':(\\{.+?\\})");
	
	public List<AicaiMatch> parseMatchData (String dateStr) {
		List<AicaiMatch> matches = Lists.newArrayList();

		try {
			Map<String, String> headers = new HashMap<String, String>();
			headers.put("Content-Type", "application/json");
			headers.put("User-Agent", "Mozilla/5.0");

			String resData = HttpRequestService.getInstance().doHttpRequest(AICAI_JCZQ
						+ "?lotteryType=jczq&cate=gd&dataStr=" + dateStr,
					HttpRequestService.GET_METHOD, null, headers);

			Matcher matcher = pattern.matcher(resData);

			while (matcher.find()) {
				String res = matcher.group(1);
				AicaiMatch acMatch = GsonConverter.convertJSonToObjectUseNormal(res, new TypeToken<AicaiMatch>(){});
				matches.add(acMatch);
			}
		} catch (Exception e) {
			log.error(String.format("unable to parse five million matches"), e);
		}
		
		return matches;
	}

	public List <EuroPl> parseEuroData (Long acMatchId, Company company) {
		try {
			Map<String, String> headers = new HashMap<String, String>();
			headers.put("Content-Type", "application/json");
			headers.put("User-Agent", "Mozilla/5.0");

			String resData = HttpRequestService.getInstance().doHttpRequest(EURO_URL
						+ "?betId=" + acMatchId + "&companyId=" + company.getAcCompanyId(),
					HttpRequestService.GET_METHOD, null, headers);
			
			AicaiEuroResult res = GsonConverter.convertJSonToObjectUseNormal(resData, new TypeToken<AicaiEuroResult>(){});
			
			if (res != null && res.getResult() != null) {
				List<AicaiEuro> euroList = res.getResult().getEuropDetailOddsVoList();
				if (euroList != null && euroList.size() > 0) {
					List <EuroPl> euroPls = new ArrayList<EuroPl>();
					
					for (AicaiEuro eu : euroList) {
						EuroPl pl = new EuroPl(eu.getWinOdds() / 10000f,
								eu.getDrowOdds() / 10000f,
								eu.getLoseOdds() / 10000f,
								new Date(eu.getCreateTime().getTime()));
						
						euroPls.add(pl);
					}
					
					return euroPls;
				}
			}	
		} catch (Exception e) {
			log.error(String.format("unable to parse match euro data: match [%s], company [%s]", acMatchId, company), e);
		}
		return null;
	}

	public static void main (String [] args) {
		AicaiParser pp = new AicaiParser();
		String matchDateStr = DateUtil.formatHarfYearDate(new Date());
		System.out.println(pp.parseMatchData("180408"));
//		System.out.println(pp.parseEuroData(1251549L, Company.Jincai));
		
	}
}
