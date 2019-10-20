package com.roy.football.match.OFN.statics.matrices;

import com.roy.football.match.OFN.response.AsiaPl;
import com.roy.football.match.base.MatchData;
import com.roy.football.match.process.CalculateResult;

public class PankouMatrices implements CalculateResult, MatchData{

	@Override
	public String toString() {
		return "PankouMatrices [originPk=" + originPk + ", mainPk=" + mainPk
				+ ", currentPk=" + currentPk + ", hwinChangeRate="
				+ hwinChangeRate + ", awinChangeRate=" + awinChangeRate
				+ ", hours=" + hours + "]";
	}

	public AsiaPl getOriginPk() {
		return originPk;
	}
	public void setOriginPk(AsiaPl originPk) {
		this.originPk = originPk;
	}
	public AsiaPl getMainPk() {
		return mainPk;
	}
	public void setMainPk(AsiaPl mainPk) {
		this.mainPk = mainPk;
	}
	public AsiaPl getCurrentPk() {
		return currentPk;
	}
	public void setCurrentPk(AsiaPl currentPk) {
		this.currentPk = currentPk;
	}

	public Float getHwinChangeRate() {
		return hwinChangeRate;
	}

	public void setHwinChangeRate(Float hwinChangeRate) {
		this.hwinChangeRate = hwinChangeRate;
	}

	public Float getAwinChangeRate() {
		return awinChangeRate;
	}

	public void setAwinChangeRate(Float awinChangeRate) {
		this.awinChangeRate = awinChangeRate;
	}

	public Float getHours() {
		return hours;
	}

	public void setHours(Float hours) {
		this.hours = hours;
	}

	private AsiaPl originPk;
	private AsiaPl mainPk;
	private AsiaPl currentPk;
	private Float hwinChangeRate;
	private Float awinChangeRate;
	private Float hours;
}
