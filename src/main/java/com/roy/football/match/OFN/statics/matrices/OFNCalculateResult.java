package com.roy.football.match.OFN.statics.matrices;

import com.roy.football.match.base.League;
import com.roy.football.match.base.MatchData;
import com.roy.football.match.okooo.MatchExchangeData;
import com.roy.football.match.process.CalculateResult;

public class OFNCalculateResult implements CalculateResult, MatchData {

	@Override
	public String toString() {
		return "OFNCalculateResult [league=" + league + ", clubMatrices="
				+ clubMatrices + ", jiaoShou=" + jiaoShou + ", matchState="
				+ matchState + ", pkMatrices="
				+ pkMatrices + ", dxMatrices=" + dxMatrices
				+ ", predictPanKou=" + predictPanKou + ", predictResult="
				+ predictResult + ", euroMatrices=" + euroMatrices
				+ "]";
	}

	public ClubMatrices getClubMatrices() {
		return clubMatrices;
	}
	public void setClubMatrices(ClubMatrices clubMatrices) {
		this.clubMatrices = clubMatrices;
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

	public MatchExchangeData getExchanges() {
		return exchanges;
	}

	public void setExchanges(MatchExchangeData exchanges) {
		this.exchanges = exchanges;
	}

	private League league;
	private ClubMatrices clubMatrices;
	private JiaoShouMatrices jiaoShou;
	private MatchState matchState;
	private PankouMatrices pkMatrices;
	private DaxiaoMatrices dxMatrices;
	private Float predictPanKou;
	private PredictResult predictResult;
	private EuroMatrices euroMatrices;
	private MatchExchangeData exchanges;
}
