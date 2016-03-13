package com.roy.football.match.base;

public enum League {
	Friendly(166), YingChao(92, 20), YingGuang(177, 24), YingJia(178, 24), YiJia(34, 20), ShuChao(76, 12), FaYi(171, 20), HeJia(99, 18),
	RiLian(102, 18), AoChao(339, 10);
	
	League(int leagueId) {
		this.leagueId = leagueId;
	}
	
	League(int leagueId, int clubNum) {
		this.leagueId = leagueId;
		this.clubNum = clubNum;
	}
	
	public long getLeagueId() {
		return leagueId;
	}

	public void setLeagueId(long leagueId) {
		this.leagueId = leagueId;
	}

	public int getClubNum() {
		return clubNum;
	}

	public void setClubNum(int clubNum) {
		this.clubNum = clubNum;
	}
	
	public static League getLeagueById (long lid) {
		for (League le : League.values()) {
			if (lid == le.getLeagueId()) {
				return le;
			}
		}
		return null;
	}

	private long leagueId;
	private int clubNum;
}
