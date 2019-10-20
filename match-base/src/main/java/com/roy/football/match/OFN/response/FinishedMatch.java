package com.roy.football.match.OFN.response;

import java.util.Date;

import com.google.gson.annotations.SerializedName;
import com.roy.football.match.base.MatchData;

import lombok.Data;

@Data
public class FinishedMatch implements MatchData, Comparable <FinishedMatch> {
	
	@Override
	public int compareTo(FinishedMatch o) {
//		return (int) (this.getMatchTime().getTime() - o.getMatchTime().getTime());
		return (int)(o.getMatchId() - this.getMatchId());
	}

	@SerializedName("mid")
	private Long matchId;
	@SerializedName("lid")
	private Long leagueId;
	@SerializedName("lgname")
	private String leagueName;
	@SerializedName("mtime")
	private Date matchTime;
	@SerializedName("htid")
	private Long hostId;
	@SerializedName("home")
	private String hostName;
	@SerializedName("atid")
	private Long guestId;
	@SerializedName("away")
	private String guestName;
	@SerializedName("bc")
	private String bc;
	@SerializedName("asiapk")
	private Float asiaPanKou;   //
	@SerializedName("asiapl")
	private String asiaPanLu;    // win/lose pankou
	@SerializedName("lasiapk")
	private Float lastApk;   //
	@SerializedName("lasiapl")
	private String lastApl;    // latest win/lose pankou
	@SerializedName("daxiaopk")
	private String daxiaoPanKou; // daxiao pankou string
	@SerializedName("hscore")
	private Integer hscore;
	@SerializedName("ascore")
	private Integer ascore;
}
