package com.roy.football.match.OFN.statics.matrices;

import java.util.List;

import com.roy.football.match.base.MatchData;
import com.roy.football.match.base.ResultGroup;
import com.roy.football.match.base.TeamLabel;
import com.roy.football.match.base.TeamLevel;
import com.roy.football.match.process.CalculateResult;

public class OFNCalculateResult implements CalculateResult, MatchData {

	@Override
	public String toString() {
		return "OFNCalculateResult [clubMatrices=" + clubMatrices
				+ ", hostLevel=" + hostLevel + ", hostLabels=" + hostLabels
				+ ", guestLevel=" + guestLevel + ", guestLabels=" + guestLabels
				+ ", jiaoShou=" + jiaoShou + ", matchState=" + matchState
				+ ", pkMatrices=" + pkMatrices + ", predictPanKou="
				+ predictPanKou + ", euroMatrices=" + euroMatrices + ", kill="
				+ kill + ", promote=" + promote + "]";
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

	public ResultGroup getKill() {
		return kill;
	}

	public void setKill(ResultGroup kill) {
		this.kill = kill;
	}

	public ResultGroup getPromote() {
		return promote;
	}

	public void setPromote(ResultGroup promote) {
		this.promote = promote;
	}

	private ClubMatrices clubMatrices;
	private TeamLevel hostLevel;
	private List<TeamLabel> hostLabels;
	private TeamLevel guestLevel;
	private List<TeamLabel> guestLabels;
	private JiaoShouMatrices jiaoShou;
	private MatchState matchState;
	private PankouMatrices pkMatrices;
	private Float predictPanKou;
	private EuroMatrices euroMatrices;
	private ResultGroup kill;
	private ResultGroup promote;
}
