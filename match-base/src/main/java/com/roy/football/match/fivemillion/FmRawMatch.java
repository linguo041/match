package com.roy.football.match.fivemillion;

import com.google.gson.annotations.SerializedName;

import lombok.Data;

@Data
public class FmRawMatch {

	@SerializedName("fid")
	private String fmId;
	
	@SerializedName("homeid")
	private String homeId;
	@SerializedName("homesxname")
	private String homeName;
	
	@SerializedName("awayid")
	private String awayId;
	@SerializedName("awaysxname")
	private String awayName;
	
	@SerializedName("matchtime")
	private String matchTime;
	
	@SerializedName("league_id")
	private String leagueId;
	
	@SerializedName("homescore")
	private String homeScore;
	@SerializedName("awayscore")
	private String awayScore;
	
	@SerializedName("order")
	private String matchDayOrder;
	
	@SerializedName("iscrazybet")
	private String isCrazyBet;
}
