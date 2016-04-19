package com.roy.football.match.OFN.statics.matrices;

import com.roy.football.match.OFN.response.AsiaPl;
import com.roy.football.match.base.MatchData;
import com.roy.football.match.process.CalculateResult;

public class DaxiaoMatrices implements CalculateResult, MatchData{

	@Override
	public String toString() {
		return "DaxiaoMatrices [originPk=" + originPk + ", mainPk=" + mainPk
				+ ", currentPk=" + currentPk + ", daChangeRate=" + daChangeRate
				+ ", xiaoChangeRate=" + xiaoChangeRate + ", hours=" + hours
				+ "]";
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
	public Float getDaChangeRate() {
		return daChangeRate;
	}
	public void setDaChangeRate(Float daChangeRate) {
		this.daChangeRate = daChangeRate;
	}
	public Float getXiaoChangeRate() {
		return xiaoChangeRate;
	}
	public void setXiaoChangeRate(Float xiaoChangeRate) {
		this.xiaoChangeRate = xiaoChangeRate;
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
	private Float daChangeRate;
	private Float xiaoChangeRate;
	private Float hours;
}
