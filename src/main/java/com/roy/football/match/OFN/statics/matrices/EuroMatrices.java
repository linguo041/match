package com.roy.football.match.OFN.statics.matrices;

import java.util.Map;

import com.roy.football.match.OFN.response.Company;
import com.roy.football.match.OFN.response.EuroPl;
import com.roy.football.match.base.MatchData;
import com.roy.football.match.process.CalculateResult;

import lombok.Data;

public class EuroMatrices implements CalculateResult, MatchData{

	public EuroPl getCurrEuroAvg() {
		return currEuroAvg;
	}

	public void setCurrEuroAvg(EuroPl currEuroAvg) {
		this.currEuroAvg = currEuroAvg;
	}

	public float getMainAvgDrawDiff() {
		return mainAvgDrawDiff;
	}

	public void setMainAvgDrawDiff(float mainAvgDrawDiff) {
		this.mainAvgDrawDiff = mainAvgDrawDiff;
	}

	public float getMainAvgWinDiff() {
		return mainAvgWinDiff;
	}

	public void setMainAvgWinDiff(float mainAvgWinDiff) {
		this.mainAvgWinDiff = mainAvgWinDiff;
	}

	public float getMainAvgLoseDiff() {
		return mainAvgLoseDiff;
	}

	public void setMainAvgLoseDiff(float mainAvgLoseDiff) {
		this.mainAvgLoseDiff = mainAvgLoseDiff;
	}

	public Map<Company, EuroMatrix> getCompanyEus() {
		return companyEus;
	}

	public void setCompanyEus(Map<Company, EuroMatrix> companyEus) {
		this.companyEus = companyEus;
	}

//	private EuroMatrix jincaiMatrix;
//	private EuroMatrix williamMatrix;
//	private EuroMatrix aomenMatrix;
//	private EuroMatrix ladMatrix;
//	private EuroMatrix yiShenBoMatrix;
//	private EuroMatrix interwettenMatrix;
//	private EuroMatrix snaiMatrix;
//	private EuroMatrix swedenMatrix;
	private EuroPl currEuroAvg;
	private Map<Company, EuroMatrix> companyEus;

	private float mainAvgWinDiff;
	private float mainAvgDrawDiff;
	private float mainAvgLoseDiff;

	@Data
	public static class EuroMatrix {
		
		private EuroPl originEuro;
		private EuroPl mainEuro;
		private EuroPl currentEuro;
		private EuroPl leAvgEuro;
		private float winChange;
		private float drawChange;
		private float loseChange;
		private float smWinDiff;
		private float smDrawDiff;
		private float smLoseDiff;
	}
}
