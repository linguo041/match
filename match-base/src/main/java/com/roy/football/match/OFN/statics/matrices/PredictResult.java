package com.roy.football.match.OFN.statics.matrices;

import com.roy.football.match.base.ResultGroup;

public class PredictResult {

	@Override
	public String toString() {
		return "PredictScore [hostScore=" + hostScore + ", guestScore="
				+ guestScore + ", pk=" + pk + ", currentPk=" + currentPk
				+ ", daxiao=" + daxiao + ", currentDaxiao=" + currentDaxiao
				+ ", kpResult=" + kpResult + "]";
	}

	public float getHostScore() {
		return hostScore;
	}
	public void setHostScore(float hostScore) {
		this.hostScore = hostScore;
	}
	public float getGuestScore() {
		return guestScore;
	}
	public void setGuestScore(float guestScore) {
		this.guestScore = guestScore;
	}
	public ResultGroup getPk() {
		return pk;
	}
	public void setPk(ResultGroup pk) {
		this.pk = pk;
	}
	public float getCurrentPk() {
		return currentPk;
	}
	public void setCurrentPk(float currentPk) {
		this.currentPk = currentPk;
	}
	public ResultGroup getDaxiao() {
		return daxiao;
	}
	public void setDaxiao(ResultGroup daxiao) {
		this.daxiao = daxiao;
	}
	public float getCurrentDaxiao() {
		return currentDaxiao;
	}
	public void setCurrentDaxiao(float currentDaxiao) {
		this.currentDaxiao = currentDaxiao;
	}
	public OFNKillPromoteResult getKpResult() {
		return kpResult;
	}
	public void setKpResult(OFNKillPromoteResult kpResult) {
		this.kpResult = kpResult;
	}

	private float hostScore;
	private float guestScore;
	private ResultGroup pk;
	private float currentPk;
	private ResultGroup daxiao;
	private float currentDaxiao;
	private OFNKillPromoteResult kpResult;
}
