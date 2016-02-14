package com.roy.football.match;

public class AoMen {
	
	public PanKou getOrigPankou() {
		return origPankou;
	}
	public void setOrigPankou(PanKou origPankou) {
		this.origPankou = origPankou;
	}
	public PanKou getLastPanKou() {
		return lastPanKou;
	}
	public void setLastPanKou(PanKou lastPanKou) {
		this.lastPanKou = lastPanKou;
	}
	public PanKou getLatest10h() {
		return latest10h;
	}
	public void setLatest10h(PanKou latest10h) {
		this.latest10h = latest10h;
	}
	public PanKou getMainPankou() {
		return mainPankou;
	}
	public void setMainPankou(PanKou mainPankou) {
		this.mainPankou = mainPankou;
	}
	private PanKou origPankou;
	private PanKou lastPanKou;
	private PanKou latest10h;
	private PanKou mainPankou;
}
