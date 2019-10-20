package com.roy.football.match.base;

public enum TeamLabel {

	Powerful(0.6f, 1.8f, 3, 0.8f, 2f, 1f, MatrixType.All),
	HomeStrong(0.5f, 1.2f, 6, 0.8f, 1.2f, 1.2f, MatrixType.Home),
	AwayWeak(0.2f, -0.3f, 15, 0.4f, 1f, 1.2f, MatrixType.Away),
	Offensive(0.38f, 0.8f, 10, 0.55f, 1.5f, 100f, MatrixType.All),
	Defensive(0.35f, 0.8f, 10, 0.6f, 0.01f, 1.4f, MatrixType.All);
	
	TeamLabel(Float winRateStd, Float netGoalStd, Integer pm,
			Float winDrawRateStd, Float goalStd, Float loseStd) {
		this(winRateStd, netGoalStd, pm, winDrawRateStd, goalStd, loseStd, null);
	}
	
	TeamLabel(Float winRateStd, Float netGoalStd, Integer pm,
			Float winDrawRateStd, Float goalStd, Float missStd, MatrixType type) {
		this.winRateStd = winRateStd;
		this.netGoalStd = netGoalStd;
		this.pm = pm;
		this.winDrawRateStd = winDrawRateStd;
		this.goalStd = goalStd;
		this.missStd = missStd;
		this.type = type;
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


	public MatrixType getType() {
		return type;
	}

	public void setType(MatrixType type) {
		this.type = type;
	}

	public Float getMissStd() {
		return missStd;
	}

	public void setMissStd(Float missStd) {
		this.missStd = missStd;
	}

	private Float winRateStd;
	private Float netGoalStd;
	private Integer pm;
	private Float winDrawRateStd;
	private Float goalStd;
	private Float missStd;
	private MatrixType type;
}
