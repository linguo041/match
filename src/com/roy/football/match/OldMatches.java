package com.roy.football.match;

public class OldMatches {
	
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
	public Asia getRecentPankou() {
		return recentPankou;
	}
	public void setRecentPankou(Asia recentPankou) {
		this.recentPankou = recentPankou;
	}
	private Integer hostWins;
	private Integer draws;
	private Integer hostLoses;
	private Integer hostWinsAsHost;
	private Asia recentPankou;
}
