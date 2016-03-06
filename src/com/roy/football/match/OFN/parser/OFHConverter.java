package com.roy.football.match.OFN.parser;

import java.util.Date;
import java.util.List;

import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.roy.football.match.OFN.response.ClubDatas;
import com.roy.football.match.OFN.response.FinishedMatch;
import com.roy.football.match.util.DateDeserializer;
import com.roy.football.match.util.GsonConverter;

public class OFHConverter {
	
	private final static String RSP_OBJECT = ":null";
	private final static String RSP_ARRAY = ":[]";
	private final static String TOKEN_DASH = "\"-\"";
	private final static String TOKEN_QUOTE = "\"\"";
	
	private final static String OFH_JSONC_NAME = "OFH";
	
	public static void registerOFHJsonConverter () {
		GsonBuilder gb = new GsonBuilder().registerTypeAdapter(Date.class, new DateDeserializer());
		
		GsonConverter.useCustomizedGson(OFH_JSONC_NAME, gb);
	}

	public static ClubDatas convertClubDatas (String json) {
		json = json.replace(RSP_ARRAY, RSP_OBJECT);
		return GsonConverter.convertJSonToObjectUseNormal(json, ClubDatas.class);
	}
	
	public static List<FinishedMatch> convertJiaoShouMatch (String json) {
		json = json.replace(RSP_ARRAY, RSP_OBJECT).replace(TOKEN_DASH, TOKEN_QUOTE);
		GsonConverter converter = GsonConverter.useCustomizedGson(OFH_JSONC_NAME);

		return converter.convertJSonToObject(json, new TypeToken<List<FinishedMatch>>(){});
	}
}
