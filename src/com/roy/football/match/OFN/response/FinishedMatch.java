package com.roy.football.match.OFN.response;

import java.util.Date;

import com.google.gson.annotations.SerializedName;

public class FinishedMatch {

	public Long getMatchId() {
		return matchId;
	}
	public void setMatchId(Long matchId) {
		this.matchId = matchId;
	}
	public Long getLeagueId() {
		return leagueId;
	}
	public void setLeagueId(Long leagueId) {
		this.leagueId = leagueId;
	}
	public String getLeagueName() {
		return leagueName;
	}
	public void setLeagueName(String leagueName) {
		this.leagueName = leagueName;
	}
	public Date getMatchTime() {
		return matchTime;
	}
	public void setMatchTime(Date matchTime) {
		this.matchTime = matchTime;
	}
	public Long getHostId() {
		return hostId;
	}
	public void setHostId(Long hostId) {
		this.hostId = hostId;
	}
	public String getHostName() {
		return hostName;
	}
	public void setHostName(String hostName) {
		this.hostName = hostName;
	}
	public Long getGuestId() {
		return guestId;
	}
	public void setGuestId(Long guestId) {
		this.guestId = guestId;
	}
	public String getGuestName() {
		return guestName;
	}
	public void setGuestName(String guestName) {
		this.guestName = guestName;
	}
	public String getBc() {
		return bc;
	}
	public void setBc(String bc) {
		this.bc = bc;
	}
	public String getAsiaPanKou() {
		return asiaPanKou;
	}
	public void setAsiaPanKou(String asiaPanKou) {
		this.asiaPanKou = asiaPanKou;
	}
	public String getAsiaPanLu() {
		return asiaPanLu;
	}
	public void setAsiaPanLu(String asiaPanLu) {
		this.asiaPanLu = asiaPanLu;
	}
	public String getDaxiaoPanKou() {
		return daxiaoPanKou;
	}
	public void setDaxiaoPanKou(String daxiaoPanKou) {
		this.daxiaoPanKou = daxiaoPanKou;
	}
	public Integer getHscore() {
		return hscore;
	}
	public void setHscore(Integer hscore) {
		this.hscore = hscore;
	}
	public Integer getAscore() {
		return ascore;
	}
	public void setAscore(Integer ascore) {
		this.ascore = ascore;
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
	private String asiaPanKou;   //
	@SerializedName("asiapl")
	private String asiaPanLu;    // win/lose pankou
	@SerializedName("daxiaopk")
	private String daxiaoPanKou; // daxiao pankou
	@SerializedName("hscore")
	private Integer hscore;
	@SerializedName("ascore")
	private Integer ascore;
	
}
