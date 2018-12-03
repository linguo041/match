package com.roy.football.match.base;

import com.roy.football.match.OFN.response.Company;

import lombok.Getter;

@Getter
public enum League {
	Friendly(166, true, null),
	Country(61, true, null), 
	EuroJingBiao(87, true, MatchContinent.Euro),
	AsiaYu(161, Company.Aomen, true, MatchContinent.Asia),
	WorldCupEuroYu(175, true, MatchContinent.Euro),
	WorldCupAsiaYu(141, Company.Aomen, true, MatchContinent.Asia),
	WorldCupAfricaYu(157, Company.William, true, MatchContinent.Africa),
	WorldCupSAmericaYu(126, Company.William, true, MatchContinent.America),
	WorldCupNAmericaYu(176, Company.William, true, MatchContinent.America),
	BoluoCup(704, true, MatchContinent.America),
	MeiZhouCup(162, true, MatchContinent.America),
	LianHeHui(291, true, null),
	Euro21Outter(464, true, MatchContinent.Euro),
	WorldCup(149, true, MatchContinent.Euro),
	EuroCountry(1530, true, MatchContinent.Euro),
	ANYMOUS1(297, true, null),
	
	YingChao(92, 20, Company.William, MatchContinent.Euro), YingGuang(177, 24, Company.William, MatchContinent.Euro),
	YingJia(178, 24, Company.William, MatchContinent.Euro), YingYi(106, 24, Company.William, MatchContinent.Euro),
	YingZhuZong(55, 10, Company.William, MatchContinent.Euro), YingLianBei(53, 10, Company.William, MatchContinent.Euro),
	ShuChao(76, 12, Company.William, MatchContinent.Euro), ShuGuang(236, 10, Company.William, MatchContinent.Euro), ShuZhuZong(145, 10, Company.William, MatchContinent.Euro),
	XiJia(85, 20, MatchContinent.Euro), XiBei(54, 10, MatchContinent.Euro),
	DeJia(39, 18, MatchContinent.Euro), DeYi(140, 18, MatchContinent.Euro), DeBei(52, 18, MatchContinent.Euro),
	YiJia(34, 20, Company.SNAI, MatchContinent.Euro), YiBei(332, 20, Company.SNAI, MatchContinent.Euro),
	FaJia(93, 20, MatchContinent.Euro), FaYi(171, 20, MatchContinent.Euro), FaBei(101, 20, MatchContinent.Euro), FaLianBei(62, 12, MatchContinent.Euro),
	HeJia(99, 18, MatchContinent.Euro), HeYi(202, 18, MatchContinent.Euro), HeBei(146, 10, MatchContinent.Euro),
	PuChao(88, 18, MatchContinent.Euro), PuLianBei(251, 18, MatchContinent.Euro),
	OuGuan(74, 16, MatchContinent.Euro), OuLian(58, 16, MatchContinent.Euro),
	EChao(165, 16, MatchContinent.Euro), RussiaCup(232, 16, MatchContinent.Euro),
	NorChao(104, 16, MatchContinent.Euro), NorCup (227, 16, MatchContinent.Euro),
	Sweden(103, 16, Company.Sweden, MatchContinent.Euro),
	BiJia(100, 16, MatchContinent.Euro),

	RiLian(102, 18, Company.Aomen, MatchContinent.Asia), RiYi(347, 22, Company.Aomen, MatchContinent.Asia),
	RiLianBei(158, 8, Company.Aomen, MatchContinent.Asia),
	TianHuangBei(252, 8, Company.Aomen, MatchContinent.Asia),
	RiXinBei(392, 8, Company.Aomen, MatchContinent.Asia),
	HanZhiLian(250, 12, Company.Aomen, MatchContinent.Asia),
	AoChao(339, 10, Company.Aomen, MatchContinent.Asia),
	AoZhuZhong(1303, 4, Company.Aomen, MatchContinent.Asia),
	YaGuan(139, 16, Company.Aomen, MatchContinent.Asia),

	BrazilJia(160, 20, MatchContinent.America), BrazilCup(266, 20, MatchContinent.America),
	America(107, 20, MatchContinent.America), CannadaCup(700, MatchContinent.America), AmericaPublic(569, MatchContinent.America),
	Argintina(108, 30, MatchContinent.America), ArgintinaCup(1152, 8, MatchContinent.America),
	Maxico(191, 18, MatchContinent.America), MaxicoCup(1204, 18, MatchContinent.America),
	ChiLi(192, 16, MatchContinent.America), ChiLiCup(753, 8, MatchContinent.America),
	ShenBaoluo(261, 20, MatchContinent.America),
	JieFangZhe(159, 16, MatchContinent.America),
	NanQiuBei(216, 4, MatchContinent.America),
	
	AfricaCup(383, 12, MatchContinent.Africa)
	;
	
	League(int leagueId, MatchContinent con) {
		this.leagueId = leagueId;
		this.majorCompany = Company.William;
		this.continent = con;
	}
	
	League(int leagueId, boolean state, MatchContinent con) {
		this.leagueId = leagueId;
		this.majorCompany = Company.William;
		this.state = state;
		this.continent = con;
	}
	
	League(int leagueId, int clubNum, MatchContinent con) {
		this.leagueId = leagueId;
		this.clubNum = clubNum;
		this.majorCompany = Company.William;
		this.continent = con;
	}
	
	League(int leagueId, Company comp, MatchContinent con) {
		this.leagueId = leagueId;
		this.majorCompany = comp;
		this.continent = con;
	}
	
	League(int leagueId, Company comp,  boolean state, MatchContinent con) {
		this.leagueId = leagueId;
		this.majorCompany = comp;
		this.state = state;
		this.continent = con;
	}
	
	League(int leagueId, int clubNum, Company comp, MatchContinent con) {
		this.leagueId = leagueId;
		this.clubNum = clubNum;
		this.majorCompany = comp;
		this.continent = con;
	}
	
	public static League getLeagueById (long lid) {
		for (League le : League.values()) {
			if (lid == le.getLeagueId()) {
				return le;
			}
		}
		return null;
	}

	private Company majorCompany;
	private MatchContinent continent;
	private long leagueId;
	private int clubNum;
	private boolean state;
}
