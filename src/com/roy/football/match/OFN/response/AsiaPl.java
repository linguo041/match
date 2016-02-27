package com.roy.football.match.OFN.response;

import java.util.Date;

public class AsiaPl {

	@Override
	public String toString() {
		return "AsiaPl [hWin=" + hWin + ", aWin=" + aWin + ", panKou=" + panKou
				+ ", matchDate=" + matchDate + "]";
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
	public Date getMatchDate() {
		return matchDate;
	}
	public void setMatchDate(Date matchDate) {
		this.matchDate = matchDate;
	}

	private Float hWin;
	private Float aWin;
	private Float panKou;
	private Date matchDate;
}
