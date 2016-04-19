package com.roy.football.match.OFN.statics.matrices;

import java.util.List;
import java.util.Set;

import com.roy.football.match.OFN.response.EuroPl;
import com.roy.football.match.base.League;
import com.roy.football.match.base.MatchData;
import com.roy.football.match.base.ResultGroup;
import com.roy.football.match.base.TeamLabel;
import com.roy.football.match.base.TeamLevel;
import com.roy.football.match.process.CalculateResult;

public class OFNCalculateResult implements CalculateResult, MatchData {

	@Override
	public String toString() {
		return "OFNCalculateResult [league=" + league + ", clubMatrices="
				+ clubMatrices + ", hostLevel=" + hostLevel + ", hostLabels="
				+ hostLabels + ", guestLevel=" + guestLevel + ", guestLabels="
				+ guestLabels + ", attackComp=" + attackComp + ", defendComp="
				+ defendComp + ", jiaoShou=" + jiaoShou + ", matchState="
				+ matchState + ", hotPoint=" + hotPoint + ", pkMatrices="
				+ pkMatrices + ", dxMatrices=" + dxMatrices
				+ ", predictPanKou=" + predictPanKou + ", predictResult="
				+ predictResult + ", euroMatrices=" + euroMatrices
				+ ", jinCai=" + jinCai + "]";
	}

	public ClubMatrices getClubMatrices() {
		return clubMatrices;
	}
	public void setClubMatrices(ClubMatrices clubMatrices) {
		this.clubMatrices = clubMatrices;
	}
	public TeamLevel getGuestLevel() {
		return guestLevel;
	}
	public void setGuestLevel(TeamLevel guestLevel) {
		this.guestLevel = guestLevel;
	}
	public TeamLevel getHostLevel() {
		return hostLevel;
	}
	public void setHostLevel(TeamLevel hostLevel) {
		this.hostLevel = hostLevel;
	}
	public List<TeamLabel> getHostLabels() {
		return hostLabels;
	}
	public void setHostLabels(List<TeamLabel> hostLabels) {
		this.hostLabels = hostLabels;
	}
	public List<TeamLabel> getGuestLabels() {
		return guestLabels;
	}
	public void setGuestLabels(List<TeamLabel> guestLabels) {
		this.guestLabels = guestLabels;
	}

	public JiaoShouMatrices getJiaoShou() {
		return jiaoShou;
	}
	public void setJiaoShou(JiaoShouMatrices jiaoShou) {
		this.jiaoShou = jiaoShou;
	}
	public MatchState getMatchState() {
		return matchState;
	}
	public void setMatchState(MatchState matchState) {
		this.matchState = matchState;
	}

	public PankouMatrices getPkMatrices() {
		return pkMatrices;
	}
	public void setPkMatrices(PankouMatrices pkMatrices) {
		this.pkMatrices = pkMatrices;
	}

	public Float getPredictPanKou() {
		return predictPanKou;
	}

	public void setPredictPanKou(Float predictPanKou) {
		this.predictPanKou = predictPanKou;
	}

	public EuroMatrices getEuroMatrices() {
		return euroMatrices;
	}

	public void setEuroMatrices(EuroMatrices euroMatrices) {
		this.euroMatrices = euroMatrices;
	}

	public League getLeague() {
		return league;
	}

	public void setLeague(League league) {
		this.league = league;
	}

	public EuroPl getJinCai() {
		return jinCai;
	}

	public void setJinCai(EuroPl jinCai) {
		this.jinCai = jinCai;
	}

	public Float getHotPoint() {
		return hotPoint;
	}

	public void setHotPoint(Float hotPoint) {
		this.hotPoint = hotPoint;
	}


	public Float getAttackComp() {
		return attackComp;
	}

	public void setAttackComp(Float attackComp) {
		this.attackComp = attackComp;
	}


	public Float getDefendComp() {
		return defendComp;
	}

	public void setDefendComp(Float defendComp) {
		this.defendComp = defendComp;
	}


	public DaxiaoMatrices getDxMatrices() {
		return dxMatrices;
	}

	public void setDxMatrices(DaxiaoMatrices dxMatrices) {
		this.dxMatrices = dxMatrices;
	}

	public PredictResult getPredictResult() {
		return predictResult;
	}

	public void setPredictResult(PredictResult predictResult) {
		this.predictResult = predictResult;
	}

	private League league;
	private ClubMatrices clubMatrices;
	private TeamLevel hostLevel;
	private List<TeamLabel> hostLabels;
	private TeamLevel guestLevel;
	private List<TeamLabel> guestLabels;
	private Float attackComp;
	private Float defendComp;
	private JiaoShouMatrices jiaoShou;
	private MatchState matchState;
	private Float hotPoint;
	private PankouMatrices pkMatrices;
	private DaxiaoMatrices dxMatrices;
	private Float predictPanKou;
	private PredictResult predictResult;
	private EuroMatrices euroMatrices;
	private EuroPl jinCai;
}
