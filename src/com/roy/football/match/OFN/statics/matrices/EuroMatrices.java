package com.roy.football.match.OFN.statics.matrices;

import com.roy.football.match.OFN.response.EuroPl;
import com.roy.football.match.base.MatchData;
import com.roy.football.match.process.CalculateResult;

public class EuroMatrices implements CalculateResult, MatchData{

	@Override
	public String toString() {
		return "EuroMatrices [williamMatrix=" + williamMatrix
				+ ", aomenMatrix=" + aomenMatrix + ", ladMatrix=" + ladMatrix
				+ ", yiShenBoMatrix=" + yiShenBoMatrix + ", interwettenMatrix="
				+ interwettenMatrix + "]";
	}

	public EuroMatrix getWilliamMatrix() {
		return williamMatrix;
	}

	public void setWilliamMatrix(EuroMatrix williamMatrix) {
		this.williamMatrix = williamMatrix;
	}

	public EuroMatrix getAomenMatrix() {
		return aomenMatrix;
	}

	public void setAomenMatrix(EuroMatrix aomenMatrix) {
		this.aomenMatrix = aomenMatrix;
	}

	public EuroMatrix getLadMatrix() {
		return ladMatrix;
	}

	public void setLadMatrix(EuroMatrix ladMatrix) {
		this.ladMatrix = ladMatrix;
	}

	public EuroMatrix getYiShenBoMatrix() {
		return yiShenBoMatrix;
	}

	public void setYiShenBoMatrix(EuroMatrix yiShenBoMatrix) {
		this.yiShenBoMatrix = yiShenBoMatrix;
	}

	public EuroMatrix getInterwettenMatrix() {
		return interwettenMatrix;
	}

	public void setInterwettenMatrix(EuroMatrix interwettenMatrix) {
		this.interwettenMatrix = interwettenMatrix;
	}

	public EuroMatrix getSnaiMatrix() {
		return snaiMatrix;
	}

	public void setSnaiMatrix(EuroMatrix snaiMatrix) {
		this.snaiMatrix = snaiMatrix;
	}

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

	public EuroMatrix getJincaiMatrix() {
		return jincaiMatrix;
	}

	public void setJincaiMatrix(EuroMatrix jincaiMatrix) {
		this.jincaiMatrix = jincaiMatrix;
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

	public EuroMatrix getSwedenMatrix() {
		return swedenMatrix;
	}

	public void setSwedenMatrix(EuroMatrix swedenMatrix) {
		this.swedenMatrix = swedenMatrix;
	}

	private EuroMatrix jincaiMatrix;
	private EuroMatrix williamMatrix;
	private EuroMatrix aomenMatrix;
	private EuroMatrix ladMatrix;
	private EuroMatrix yiShenBoMatrix;
	private EuroMatrix interwettenMatrix;
	private EuroMatrix snaiMatrix;
	private EuroMatrix swedenMatrix;
	private EuroPl currEuroAvg;

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


		private EuroPl originEuro;
		private EuroPl mainEuro;
		private EuroPl currentEuro;
		private float winChange;
		private float drawChange;
		private float loseChange;
	}
}
