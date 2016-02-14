package com.roy.football.match;

public class Club {
	
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public League getLeague() {
		return league;
	}
	public void setLeague(League league) {
		this.league = league;
	}
	public Integer getRank() {
		return rank;
	}
	public void setRank(Integer rank) {
		this.rank = rank;
	}
	public Integer getClubLevel() {
		return clubLevel;
	}
	public void setClubLevel(Integer clubLevel) {
		this.clubLevel = clubLevel;
	}
	
	private String name;
	private League league;
	private Integer rank;
	private Integer clubLevel;
}
