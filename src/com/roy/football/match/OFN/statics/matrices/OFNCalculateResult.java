package com.roy.football.match.OFN.statics.matrices;

import java.util.List;
import java.util.Set;

import com.roy.football.match.OFN.response.EuroPl;
import com.roy.football.match.base.MatchData;
import com.roy.football.match.base.ResultGroup;
import com.roy.football.match.base.TeamLabel;
import com.roy.football.match.base.TeamLevel;
import com.roy.football.match.process.CalculateResult;

public class OFNCalculateResult implements CalculateResult, MatchData {

	@Override
	public String toString() {
		return "OFNCalculateResult [leagueId=" + leagueId + ", clubMatrices="
				+ clubMatrices + ", hostLevel=" + hostLevel + ", hostLabels="
				+ hostLabels + ", guestLevel=" + guestLevel + ", guestLabels="
				+ guestLabels + ", hostAttack=" + hostAttack + ", hostDefend="
				+ hostDefend + ", guestAttack=" + guestAttack
				+ ", guestDefend=" + guestDefend + ", jiaoShou=" + jiaoShou
				+ ", matchState=" + matchState + ", pkMatrices=" + pkMatrices
				+ ", predictPanKou=" + predictPanKou + ", euroMatrices="
				+ euroMatrices + ", jinCai=" + jinCai + ", hotPoint=" + hotPoint
				+ ", killByPk=" + killByPk + ", killByPl=" + killByPl
				+ ", promote=" + promote + "]";
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

	public Set<ResultGroup> getKillByPk() {
		return killByPk;
	}

	public void setKillByPk(Set<ResultGroup> killByPk) {
		this.killByPk = killByPk;
	}

	public Set<ResultGroup> getKillByPl() {
		return killByPl;
	}

	public void setKillByPl(Set<ResultGroup> killByPl) {
		this.killByPl = killByPl;
	}

	public Set<ResultGroup> getPromote() {
		return promote;
	}

	public void setPromote(Set<ResultGroup> promote) {
		this.promote = promote;
	}

	public long getLeagueId() {
		return leagueId;
	}

	public void setLeagueId(long leagueId) {
		this.leagueId = leagueId;
	}

	public EuroPl getJinCai() {
		return jinCai;
	}

	public void setJinCai(EuroPl jinCai) {
		this.jinCai = jinCai;
	}
	

	public Float getHostAttack() {
		return hostAttack;
	}

	public void setHostAttack(Float hostAttack) {
		this.hostAttack = hostAttack;
	}

	public Float getHostDefend() {
		return hostDefend;
	}

	public void setHostDefend(Float hostDefend) {
		this.hostDefend = hostDefend;
	}

	public Float getGuestAttack() {
		return guestAttack;
	}

	public void setGuestAttack(Float guestAttack) {
		this.guestAttack = guestAttack;
	}

	public Float getGuestDefend() {
		return guestDefend;
	}

	public void setGuestDefend(Float guestDefend) {
		this.guestDefend = guestDefend;
	}


	public Float getHotPoint() {
		return hotPoint;
	}

	public void setHotPoint(Float hotPoint) {
		this.hotPoint = hotPoint;
	}


	private long leagueId;
	private ClubMatrices clubMatrices;
	private TeamLevel hostLevel;
	private List<TeamLabel> hostLabels;
	private TeamLevel guestLevel;
	private List<TeamLabel> guestLabels;
	private Float hostAttack;
	private Float hostDefend;
	private Float guestAttack;
	private Float guestDefend;
	private JiaoShouMatrices jiaoShou;
	private MatchState matchState;
	private Float hotPoint;
	private PankouMatrices pkMatrices;
	private Float predictPanKou;
	private EuroMatrices euroMatrices;
	private EuroPl jinCai;
	private Set<ResultGroup> killByPk;
	private Set<ResultGroup> killByPl;
	private Set<ResultGroup> promote;
}
