package com.roy.football.match.OFN.statics.matrices;

import java.util.Map;

import com.roy.football.match.OFN.response.Company;
import com.roy.football.match.OFN.response.EuroPl;
import com.roy.football.match.base.MatchData;
import com.roy.football.match.process.CalculateResult;

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

	public static class EuroMatrix {
		@Override
		public String toString() {
			return "CompanyEuroMatrices [originEuro=" + originEuro
					+ ", mianEuro=" + mainEuro + ", currentEuro=" + currentEuro
					+ "]";
		}
		public EuroPl getOriginEuro() {
			return originEuro;
		}
		public void setOriginEuro(EuroPl originEuro) {
			this.originEuro = originEuro;
		}
		public EuroPl getMainEuro() {
			return mainEuro;
		}
		public void setMainEuro(EuroPl mainEuro) {
			this.mainEuro = mainEuro;
		}
		public EuroPl getCurrentEuro() {
			return currentEuro;
		}
		public void setCurrentEuro(EuroPl currentEuro) {
			this.currentEuro = currentEuro;
		}
		public float getWinChange() {
			return winChange;
		}
		public void setWinChange(float winChange) {
			this.winChange = winChange;
		}
		public float getDrawChange() {
			return drawChange;
		}
		public void setDrawChange(float drawChange) {
			this.drawChange = drawChange;
		}
		public float getLoseChange() {
			return loseChange;
		}
		public void setLoseChange(float loseChange) {
			this.loseChange = loseChange;
		}
		public float getSmWinDiff() {
			return smWinDiff;
		}
		public void setSmWinDiff(float smWinDiff) {
			this.smWinDiff = smWinDiff;
		}
		public float getSmDrawDiff() {
			return smDrawDiff;
		}
		public void setSmDrawDiff(float smDrawDiff) {
			this.smDrawDiff = smDrawDiff;
		}
		public float getSmLoseDiff() {
			return smLoseDiff;
		}
		public void setSmLoseDiff(float smLoseDiff) {
			this.smLoseDiff = smLoseDiff;
		}

		private EuroPl originEuro;
		private EuroPl mainEuro;
		private EuroPl currentEuro;
		private float winChange;
		private float drawChange;
		private float loseChange;
		private float smWinDiff;
		private float smDrawDiff;
		private float smLoseDiff;
	}
}
