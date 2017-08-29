package com.roy.football.match.base;

import com.roy.football.match.OFN.response.Company;

public enum League {
	Friendly(166, true),
	Country(61, true), 
	EuroYuYuan(175, true),
	EuroJingBiao(87, true),
	AsiaYu(161, Company.Aomen, true),
	BoluoCup(704, true),
	MeiZhouCup(162, true),
	LianHeHui(291, true),
	ANYMOUS1(297, true),
	
	YingChao(92, 20, Company.William), YingGuang(177, 24, Company.William), YingJia(178, 24, Company.William), YingYi(106, 24, Company.William),
	YingZhuZong(55, 10, Company.William), YingLianBei(53, 10, Company.William),
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
	BiJia(100, 16),

	RiLian(102, 18, Company.Aomen), RiYi(347, 22, Company.Aomen),
	RiLianBei(158, 8, Company.Aomen),
	RiXinBei(392, 8, Company.Aomen),
	HanZhiLian(250, 12, Company.Aomen),
	AoChao(339, 10, Company.Aomen),
	AoZhuZhong(1303, 4, Company.Aomen),
	YaGuan(139, 16, Company.Aomen),

	BrazilJia(160, 20), BrazilCup(266, 20),
	America(107, 20), CannadaCup(700), AmericaPublic(569),
	Argintina(108, 30),
	Maxico(191, 18),
	MaxicoCup(1204, 18),
	ChiLi(192, 16),
	ShenBaoluo(261, 20),
	JieFangZhe(159, 16),
	NanQiuBei(216, 4),
	
	AfricaCup(383, 12)
	;
	
	League(int leagueId) {
		this.leagueId = leagueId;
		this.majorCompany = Company.William;
	}
	
	League(int leagueId, boolean state) {
		this.leagueId = leagueId;
		this.majorCompany = Company.William;
		this.state = state;
	}
	
	League(int leagueId, int clubNum) {
		this.leagueId = leagueId;
		this.clubNum = clubNum;
		this.majorCompany = Company.William;
	}
	
	League(int leagueId, Company comp) {
		this.leagueId = leagueId;
		this.majorCompany = comp;
	}
	
	League(int leagueId, Company comp, boolean state) {
		this.leagueId = leagueId;
		this.majorCompany = comp;
		this.state = state;
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

	public boolean isState() {
		return state;
	}

	public void setState(boolean state) {
		this.state = state;
	}



	private Company majorCompany;
	private long leagueId;
	private int clubNum;
	private boolean state;
}
