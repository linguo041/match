package com.roy.football.match.OFN.out;

import com.roy.football.match.OFN.out.header.Header;
import com.roy.football.match.base.TeamLabel;
import com.roy.football.match.base.TeamLevel;

public class OFNExcelData {

	@Override
	public String toString() {
		return "OFNExcelData [matchDayId=" + matchDayId + ", matchTime="
				+ matchTime + ", leagueName=" + leagueName + ", hostName="
				+ hostName + ", guestName=" + guestName + ", hostLevel="
				+ hostLevel + ", guestLevel=" + guestLevel + ", baseComp="
				+ baseComp + ", jsComp=" + jsComp + ", stateComp=" + stateComp
				+ ", originPanKou=" + originPanKou + ", predictPanKou="
				+ predictPanKou + ", stateVariation=" + stateVariation
				+ ", pkKillRate=" + pkKillRate + ", kill=" + kill
				+ ", promote=" + promote + ", predictScore=" + predictScore
				+ ", result=" + result + "]";
	}

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
	public String getLeagueName() {
		return leagueName;
	}
	public void setLeagueName(String leagueName) {
		this.leagueName = leagueName;
	}
	public String getHostName() {
		return hostName;
	}
	public void setHostName(String hostName) {
		this.hostName = hostName;
	}
	public String getGuestName() {
		return guestName;
	}
	public void setGuestName(String guestName) {
		this.guestName = guestName;
	}
	public String getHostLevel() {
		return hostLevel;
	}
	public void setHostLevel(String hostLevel) {
		this.hostLevel = hostLevel;
	}
	public String getGuestLevel() {
		return guestLevel;
	}
	public void setGuestLevel(String guestLevel) {
		this.guestLevel = guestLevel;
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

	public String getBaseComp() {
		return baseComp;
	}
	public void setBaseComp(String baseComp) {
		this.baseComp = baseComp;
	}
	public String getStateComp() {
		return stateComp;
	}
	public void setStateComp(String stateComp) {
		this.stateComp = stateComp;
	}
	public String getStateVariation() {
		return stateVariation;
	}
	public void setStateVariation(String stateVariation) {
		this.stateVariation = stateVariation;
	}
	public String getJsComp() {
		return jsComp;
	}
	public void setJsComp(String jsComp) {
		this.jsComp = jsComp;
	}



	@Header(order=10, title="Match Id")
	private Long matchDayId;
	@Header(order=20, title="Match Time")
	private String matchTime;
	@Header(order=30, title="League Name")
	private String leagueName;
	@Header(order=40, title="Host Name")
	private String hostName;
	@Header(order=50, title="Guest Name")
	private String guestName;
	@Header(order=60, title="Host Level [winRt, winGoal]")
	private String hostLevel;
	@Header(order=70, title="Guest Level [winRt, winGoal]")
	private String guestLevel;
	@Header(order=80, title="Base [host : guest]")
	private String baseComp;
	@Header(order=85, title="JS [host : guest]")
	private String jsComp;
	@Header(order=90, title="State [host : guest]")
	private String stateComp;
	@Header(order=100, title="Main, Latest [PK]")
	private String originPanKou;
	@Header(order=110, title="Predict PK [Latest]")
	private String predictPanKou;
	@Header(order=120, title="Hot | h_goal_chg, g_goal_chg")
	private String stateVariation;
	@Header(order=130, title="K_PK[Up, Down]")
	private String pkKillRate;
	@Header(order=140, title="Kill[pk | pl]")
	private String kill;
	@Header(order=150, title="Promote")
	private String promote;
	@Header(order=160, title="Predict_S")
	private String predictScore;
	@Header(order=170, title="Result")
	private String result;
	
}
