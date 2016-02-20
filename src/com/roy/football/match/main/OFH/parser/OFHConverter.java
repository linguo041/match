package com.roy.football.match.main.OFH.parser;

import com.roy.football.match.OFN.response.ClubDatas;
import com.roy.football.match.util.GsonConverter;

public class OFHConverter {

	public static ClubDatas convertClubDatas (String json) {
		return GsonConverter.convertJSonToObjectUseNormal(json, ClubDatas.class);
	}
}
