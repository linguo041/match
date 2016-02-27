package com.roy.football.match.OFN.response;

import java.util.Date;

public class EuroPl {

	@Override
	public String toString() {
		return "EuroPl [eWin=" + eWin + ", eDraw=" + eDraw + ", eLose=" + eLose
				+ ", eDate=" + eDate + "]";
	}

	public Float geteWin() {
		return eWin;
	}
	public void seteWin(Float eWin) {
		this.eWin = eWin;
	}
	public Float geteDraw() {
		return eDraw;
	}
	public void seteDraw(Float eDraw) {
		this.eDraw = eDraw;
	}
	public Float geteLose() {
		return eLose;
	}
	public void seteLose(Float eLose) {
		this.eLose = eLose;
	}
	public Date geteDate() {
		return eDate;
	}
	public void seteDate(Date eDate) {
		this.eDate = eDate;
	}

	private Float eWin;
	private Float eDraw;
	private Float eLose;
	private Date eDate;
}
