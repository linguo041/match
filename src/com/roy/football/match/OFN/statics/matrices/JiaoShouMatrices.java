package com.roy.football.match.OFN.statics.matrices;

import com.roy.football.match.base.MatchData;
import com.roy.football.match.process.CalculateResult;

public class JiaoShouMatrices implements CalculateResult, MatchData {

	@Override
	public String toString() {
		return "JiaoShouMatrices [latestPankou=" + latestPankou
				+ ", latestDaxiao=" + latestDaxiao + ", winRate=" + winRate
				+ ", winDrawRate=" + winDrawRate + ", winPkRate=" + winPkRate
				+ ", winDrawPkRate=" + winDrawPkRate + ", matchNum=" + matchNum
				+ "]";
	}

	public Float getLatestPankou() {
		return latestPankou;
	}
	public void setLatestPankou(Float latestPankou) {
		this.latestPankou = latestPankou;
	}
	public Float getLatestDaxiao() {
		return latestDaxiao;
	}
	public void setLatestDaxiao(Float latestDaxiao) {
		this.latestDaxiao = latestDaxiao;
	}
	public Float getWinRate() {
		return winRate;
	}
	public void setWinRate(Float winRate) {
		this.winRate = winRate;
	}
	public Float getWinDrawRate() {
		return winDrawRate;
	}
	public void setWinDrawRate(Float winDrawRate) {
		this.winDrawRate = winDrawRate;
	}
	public Integer getMatchNum() {
		return matchNum;
	}
	public void setMatchNum(Integer matchNum) {
		this.matchNum = matchNum;
	}

	public Float getWinPkRate() {
		return winPkRate;
	}

	public void setWinPkRate(Float winPkRate) {
		this.winPkRate = winPkRate;
	}

	public Float getWinDrawPkRate() {
		return winDrawPkRate;
	}

	public void setWinDrawPkRate(Float winDrawPkRate) {
		this.winDrawPkRate = winDrawPkRate;
	}

	private Float latestPankou;
	private Float latestDaxiao;
	private Float winRate;
	private Float winDrawRate;
	private Float winPkRate;
	private Float winDrawPkRate;
	private Integer matchNum;
}
