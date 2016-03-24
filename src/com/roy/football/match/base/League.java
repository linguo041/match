package com.roy.football.match.base;

public enum League {
	Friendly(166), YingChao(92, 20), YingGuang(177, 24), YingJia(178, 24),
	DeJia(39, 18), DeYi(140, 18),
	YiJia(34, 20),
	ShuChao(76, 12),
	FaJia(93, 20), FaYi(171, 20),
	HeJia(99, 18), HeYi(202, 19), PuChao(88, 18),
	RiLian(102, 18), AoChao(339, 10),
	OuGuan(74, 16), OuLian(58, 16),
	YaGuan(139, 16),
	JieFangZhe(159, 16)
	;
	
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
