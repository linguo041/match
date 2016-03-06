package com.roy.football.match.OFN.statics.matrices;

import com.roy.football.match.OFN.response.AsiaPl;
import com.roy.football.match.base.MatchData;
import com.roy.football.match.process.CalculateResult;

public class PankouMatrices implements CalculateResult, MatchData{

	@Override
	public String toString() {
		return "PankouMatrices [originPk=" + originPk + ", mainPk=" + mainPk
				+ ", currentPk=" + currentPk + "]";
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

	private AsiaPl originPk;
	private AsiaPl mainPk;
	private AsiaPl currentPk;
}
