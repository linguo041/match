package com.roy.football.match.OFN.response;

import java.util.Date;

import com.roy.football.match.base.MatchData;

public class AsiaPl implements MatchData {

	@Override
	public String toString() {
		return "AsiaPl [hWin=" + hWin + ", aWin=" + aWin + ", panKou=" + panKou
				+ ", pkDate=" + pkDate + "]";
	}

	public Float gethWin() {
		return hWin;
	}
	public void sethWin(Float hWin) {
		this.hWin = hWin;
	}
	public Float getaWin() {
		return aWin;
	}
	public void setaWin(Float aWin) {
		this.aWin = aWin;
	}
	public Float getPanKou() {
		return panKou;
	}
	public void setPanKou(Float panKou) {
		this.panKou = panKou;
	}
	public Date getPkDate() {
		return pkDate;
	}
	public void setPkDate(Date pkDate) {
		this.pkDate = pkDate;
	}

	private Float hWin;
	private Float aWin;
	private Float panKou;
	private Date pkDate;
}
