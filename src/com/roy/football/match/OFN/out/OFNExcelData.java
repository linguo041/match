package com.roy.football.match.OFN.out;

import com.roy.football.match.OFN.out.header.Header;
import com.roy.football.match.base.TeamLabel;
import com.roy.football.match.base.TeamLevel;

public class OFNExcelData {



	public Long getMatchDayId() {
		return matchDayId;
	}
	public void setMatchDayId(Long matchDayId) {
		this.matchDayId = matchDayId;
	}
	public String getMatchTime() {
		return matchTime;
	}
	public void setMatchTime(String matchTime) {
		this.matchTime = matchTime;
	}
	public String getOriginPanKou() {
		return originPanKou;
	}
	public void setOriginPanKou(String originPanKou) {
		this.originPanKou = originPanKou;
	}
	public String getPredictPanKou() {
		return predictPanKou;
	}
	public void setPredictPanKou(String predictPanKou) {
		this.predictPanKou = predictPanKou;
	}
	public String getKill() {
		return kill;
	}
	public void setKill(String kill) {
		this.kill = kill;
	}
	public String getPromote() {
		return promote;
	}
	public void setPromote(String promote) {
		this.promote = promote;
	}
	
	public String getPkKillRate() {
		return pkKillRate;
	}

	public void setPkKillRate(String pkKillRate) {
		this.pkKillRate = pkKillRate;
	}

	public String getResult() {
		return result;
	}

	public void setResult(String result) {
		this.result = result;
	}

	public String getPredictScore() {
		return predictScore;
	}

	public void setPredictScore(String predictScore) {
		this.predictScore = predictScore;
	}
	public String getHostGuestComp() {
		return hostGuestComp;
	}
	public void setHostGuestComp(String hostGuestComp) {
		this.hostGuestComp = hostGuestComp;
	}
	public String getStateVariation() {
		return stateVariation;
	}
	public void setStateVariation(String stateVariation) {
		this.stateVariation = stateVariation;
	}
	public String getPlMatrix() {
		return plMatrix;
	}
	public void setPlMatrix(String plMatrix) {
		this.plMatrix = plMatrix;
	}
	public String getBifa() {
		return bifa;
	}

	public void setBifa(String bifa) {
		this.bifa = bifa;
	}

	public String getJincai() {
		return jincai;
	}

	public void setJincai(String jincai) {
		this.jincai = jincai;
	}

	public String getMatchInfor() {
		return matchInfor;
	}

	public void setMatchInfor(String matchInfor) {
		this.matchInfor = matchInfor;
	}
	

	public String getLeagueName() {
		return leagueName;
	}

	public void setLeagueName(String leagueName) {
		this.leagueName = leagueName;
	}

	public String getLevel() {
		return level;
	}
	public void setLevel(String level) {
		this.level = level;
	}

	public String getJincaiJY() {
		return jincaiJY;
	}
	public void setJincaiJY(String jincaiJY) {
		this.jincaiJY = jincaiJY;
	}
	public String getAomen() {
		return aomen;
	}
	public void setAomen(String aomen) {
		this.aomen = aomen;
	}



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
	@Header(order=136, title="am_avg am_chg")
	private String aomen;
	@Header(order=137, title="avg jc chg")
	private String jincai;
	@Header(order=138, title="bf jc")
	private String bifa;
	@Header(order=139, title="exg jc_gain")
	private String jincaiJY;
	@Header(order=140, title="Kill[pk |pl ~ex]")
	private String kill;
	@Header(order=150, title="Promote")
	private String promote;
	@Header(order=160, title="Predict_S")
	private String predictScore;
	@Header(order=170, title="Result")
	private String result;
}
