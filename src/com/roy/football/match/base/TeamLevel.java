package com.roy.football.match.base;

public enum TeamLevel {
	Top(0.5f, 1.5f, 4, 0.7f, 1.4f, 1.4f),
	Strong(0.35f, 1f, 10, 0.6f, 1.2f, 1.6f),
	Nomal(0.3f, -1f, 15, 0.5f, 1f, 1.8f),
	Weak(-1f, -100f, 100, -1f, 0.01f, 100f);
	
	TeamLevel(Float winRateStd, Float netGoalStd, Integer pm,
			Float winDrawRateStd, Float goalStd, Float loseStd) {
		this.winRateStd = winRateStd;
		this.netGoalStd = netGoalStd;
		this.pm = pm;
		this.winDrawRateStd = winDrawRateStd;
		this.goalStd = goalStd;
		this.loseStd = loseStd;
	}

	public Float getWinRateStd() {
		return winRateStd;
	}
	public void setWinRateStd(Float winRateStd) {
		this.winRateStd = winRateStd;
	}
	public Float getNetGoalStd() {
		return netGoalStd;
	}
	public void setNetGoalStd(Float netGoalStd) {
		this.netGoalStd = netGoalStd;
	}
	public Integer getPm() {
		return pm;
	}
	public void setPm(Integer pm) {
		this.pm = pm;
	}
	public Float getWinDrawRateStd() {
		return winDrawRateStd;
	}
	public void setWinDrawRateStd(Float winDrawRateStd) {
		this.winDrawRateStd = winDrawRateStd;
	}
	public Float getGoalStd() {
		return goalStd;
	}
	public void setGoalStd(Float goalStd) {
		this.goalStd = goalStd;
	}
	public Float getLoseStd() {
		return loseStd;
	}
	public void setLoseStd(Float loseStd) {
		this.loseStd = loseStd;
	}

	private Float winRateStd;
	private Float netGoalStd;
	private Integer pm;
	private Float winDrawRateStd;
	private Float goalStd;
	private Float loseStd;
}
