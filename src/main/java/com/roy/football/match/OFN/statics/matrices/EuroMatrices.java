package com.roy.football.match.OFN.statics.matrices;

import java.util.Map;

import com.roy.football.match.OFN.response.Company;
import com.roy.football.match.OFN.response.EuroPl;
import com.roy.football.match.base.MatchData;
import com.roy.football.match.process.CalculateResult;

import lombok.Data;

@Data
public class EuroMatrices implements CalculateResult, MatchData{

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
	private double euWinVariance;
	private double euDrawVariance;
	private double euLoseVariance;

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
