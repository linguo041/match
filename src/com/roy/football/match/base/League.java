package com.roy.football.match.base;

public enum League {
	Friendly(166), YingChao(92), YingGuang(177), YingJia(178), YiJia(34), RiLian(102), FaYi(171), HeJia(99);
	
	League(int leagueId) {
		this.leagueId = leagueId;
	}
	
	public long getLeagueId() {
		return leagueId;
	}

	public void setLeagueId(long leagueId) {
		this.leagueId = leagueId;
	}

	private long leagueId;
}
