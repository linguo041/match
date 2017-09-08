package com.roy.football.match.OFN.out;

import com.roy.football.match.OFN.out.header.Header;
import com.roy.football.match.base.TeamLabel;
import com.roy.football.match.base.TeamLevel;

import lombok.Data;

@Data
public class OFNExcelData {

	@Header(order=10, title="Match Id")
	private Long matchDayId;
	@Header(order=20, title="Match Time")
	private String matchTime;
	@Header(order=30, title="League Name")
	private String leagueName;
	@Header(order=40, title="Match Team")
	private String matchInfor;
	@Header(order=60, title="Level [winRt, winGoal]")
	private String level;
//	@Header(order=70, title="Guest Level [winRt, winGoal]")
//	private String guestLevel;
	@Header(order=80, title="H:G Base State JS")
	private String hostGuestComp;
	@Header(order=90, title="Hot | goal_chg{H:G} ")
	private String stateVariation;
	@Header(order=100, title="Main, curr")
	private String originPanKou;
	@Header(order=110, title="Predict PK [Latest]")
	private String predictPanKou;
	@Header(order=130, title="K_PK[Up, Down]")
	private String pkKillRate;
	@Header(order=135, title="Main_Avg Main_chg")
	private String plMatrix;
	@Header(order=138, title="in_avg in_chg")
	private String inter;
	@Header(order=140, title="am_avg am_chg")
	private String aomen;
	@Header(order=142, title="avg jc chg")
	private String jincai;
	@Header(order=145, title="bf jc")
	private String bifa;
	@Header(order=148, title="exg jc_gain")
	private String jincaiJY;
	@Header(order=150, title="Kill[~pk !pl @plpk ~ex]")
	private String kill;
	@Header(order=160, title="Promote")
	private String promote;
	@Header(order=170, title="Predict_S")
	private String predictScore;
	@Header(order=180, title="Result")
	private String result;
}
