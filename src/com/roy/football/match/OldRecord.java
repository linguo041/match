package com.roy.football.match;

public class OldRecord {
	
	public Integer getHostWins() {
		return hostWins;
	}
	public void setHostWins(Integer hostWins) {
		this.hostWins = hostWins;
	}
	public Integer getDraws() {
		return draws;
	}
	public void setDraws(Integer draws) {
		this.draws = draws;
	}
	public Integer getHostLoses() {
		return hostLoses;
	}
	public void setHostLoses(Integer hostLoses) {
		this.hostLoses = hostLoses;
	}
	public Integer getHostWinsAsHost() {
		return hostWinsAsHost;
	}
	public void setHostWinsAsHost(Integer hostWinsAsHost) {
		this.hostWinsAsHost = hostWinsAsHost;
	}
	public AoMen getRecentPankou() {
		return recentPankou;
	}
	public void setRecentPankou(AoMen recentPankou) {
		this.recentPankou = recentPankou;
	}
	private Integer hostWins;
	private Integer draws;
	private Integer hostLoses;
	private Integer hostWinsAsHost;
	private AoMen recentPankou;
}
