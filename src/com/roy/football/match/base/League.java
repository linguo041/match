package com.roy.football.match.base;

import com.roy.football.match.OFN.response.Company;

public enum League {
	Friendly(166),
	Country(61),
	YingChao(92, 20, Company.William), YingGuang(177, 24, Company.William), YingJia(178, 24, Company.William), YingYi(106, 24, Company.William), YingZhuZong(55, 20, Company.William),
	ShuChao(76, 12, Company.William), ShuGuang(236, 10, Company.William), ShuZhuZong(145, 10, Company.William),
	XiJia(85, 20),
	DeJia(39, 18), DeYi(140, 18), DeBei(52, 18),
	YiJia(34, 20, Company.SNAI), YiBei(332, 20, Company.SNAI),
	FaJia(93, 20), FaYi(171, 20), FaBei(101, 20),
	HeJia(99, 18), HeYi(202, 19),
	PuChao(88, 18), PuLianBei(251, 18),
	OuGuan(74, 16), OuLian(58, 16),
	EChao(165, 16), RussiaCup(232, 16),
	NorChao(104, 16), NorCup (227, 16),
	Sweden(103, 16, Company.Sweden),

	RiLian(102, 18, Company.Aomen), RiYi(347, 22, Company.Aomen),
	RiLianBei(158, 8, Company.Aomen),
	HanZhiLian(250, 12, Company.Aomen),
	AoChao(339, 10, Company.Aomen),
	YaGuan(139, 16, Company.Aomen),

	BrazilJia(160, 20), BrazilCup(266, 20),
	America(107, 20),
	Argintina(108, 30),
	Maxico(191, 18),
	ChiLi(192, 16),
	ShenBaoluo(261, 20),
	JieFangZhe(159, 16)
	;
	
	League(int leagueId) {
		this.leagueId = leagueId;
	}
	
	League(int leagueId, int clubNum) {
		this.leagueId = leagueId;
		this.clubNum = clubNum;
	}
	
	League(int leagueId, int clubNum, Company comp) {
		this.leagueId = leagueId;
		this.clubNum = clubNum;
		this.majorCompany = comp;
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

	public Company getMajorCompany() {
		return majorCompany;
	}

	public void setMajorCompany(Company majorCompany) {
		this.majorCompany = majorCompany;
	}

	private Company majorCompany;
	private long leagueId;
	private int clubNum;
}
