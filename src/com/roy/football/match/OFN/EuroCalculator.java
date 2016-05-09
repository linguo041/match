package com.roy.football.match.OFN;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.coremedia.iso.boxes.CompositionTimeToSample.Entry;
import com.roy.football.match.OFN.response.AsiaPl;
import com.roy.football.match.OFN.response.Company;
import com.roy.football.match.OFN.response.EuroPl;
import com.roy.football.match.OFN.response.OFNMatchData;
import com.roy.football.match.OFN.statics.matrices.EuroMatrices;
import com.roy.football.match.OFN.statics.matrices.EuroMatrices.EuroMatrix;
import com.roy.football.match.OFN.statics.matrices.PankouMatrices;
import com.roy.football.match.base.League;
import com.roy.football.match.process.Calculator;
import com.roy.football.match.util.MatchUtil;

public class EuroCalculator extends AbstractBaseDataCalculator implements Calculator<EuroMatrices, OFNMatchData>{
	
	private final static int PL_CHECKED_HOURS = 32;

	@Override
	public EuroMatrices calucate(OFNMatchData matchData) {
		if (matchData != null) {
			Map<Company, List<EuroPl>> comEuros = matchData.getEuroPls();
			Date matchDt = matchData.getMatchTime();
			
			if (comEuros != null && comEuros.size() > 0) {
				EuroMatrices euroMatrices = new EuroMatrices();
				
				euroMatrices.setJincaiMatrix(getEuroMatrix(comEuros.get(Company.Jincai), matchDt));
				euroMatrices.setWilliamMatrix(getEuroMatrix(comEuros.get(Company.William), matchDt));
				euroMatrices.setAomenMatrix(getEuroMatrix(comEuros.get(Company.Aomen), matchDt));
				euroMatrices.setLadMatrix(getEuroMatrix(comEuros.get(Company.Ladbrokes), matchDt));
				euroMatrices.setYiShenBoMatrix(getEuroMatrix(comEuros.get(Company.YiShenBo), matchDt));
				euroMatrices.setInterwettenMatrix(getEuroMatrix(comEuros.get(Company.Interwetten), matchDt));
				euroMatrices.setSnaiMatrix(getEuroMatrix(comEuros.get(Company.SNAI), matchDt));
				euroMatrices.setCurrEuroAvg(matchData.getEuroAvg());
				
				League league = League.getLeagueById(matchData.getLeagueId());
				euroMatrices.setMainAvgDrawDiff(getMainAvgDrawDiff(euroMatrices, league));
				euroMatrices.setMainDrawChange(getMainDrawChange(euroMatrices, league));
				
				return euroMatrices;
			}
		}
		return null;
	}

	@Override
	public void calucate(EuroMatrices Result, OFNMatchData matchData) {
		// TODO Auto-generated method stub
		
	}
	
	private EuroMatrix getEuroMatrix (List<EuroPl> euroPls, Date matchDt) {
		if (euroPls != null && euroPls.size() > 0) {

			if (euroPls.size() > 10) {
				return getAbsoluteEuroMatrix(euroPls, matchDt);
			} else {
				return getRelativeEuroMatrix(euroPls, matchDt);
			}

		}
		return null;
	}
	
	private EuroMatrix getMainEuro (EuroMatrices euroMatrices, League league) {
		Company company = league.getMajorCompany();
		if (company != null) {
			switch (company) {
				case Aomen:
					return euroMatrices.getAomenMatrix();
				case SNAI:
					return euroMatrices.getSnaiMatrix();
				default:
					return euroMatrices.getWilliamMatrix();
			}
		}

		return euroMatrices.getWilliamMatrix();
	}
	
	private float getMainDrawChange (EuroMatrices euroMatrices, League league) {
		EuroMatrix majorComp = getMainEuro(euroMatrices, league);
		
//		float originDraw = majorComp.getOriginEuro().geteDraw();
		float mainDraw = majorComp.getMainEuro().geteDraw();
		float currDraw = majorComp.getCurrentEuro().geteDraw();
		
		return (currDraw - mainDraw) / mainDraw;
	}
	
	private float getMainAvgDrawDiff (EuroMatrices euroMatrices, League league) {
		EuroMatrix majorComp = getMainEuro(euroMatrices, league);

		if (majorComp != null) {
			EuroPl currMajorpl = majorComp.getCurrentEuro();
			EuroPl currAvgPl = euroMatrices.getCurrEuroAvg();

			if (currMajorpl != null && currAvgPl != null) {
				return MatchUtil.getEuDiff(currMajorpl.geteDraw(), currAvgPl.geteDraw(), false);
			}
		}
		
		return -1;
	}
	
	private EuroMatrix getAbsoluteEuroMatrix (List<EuroPl> euroPls, Date matchDt) {
		EuroMatrix euMatrix = null;
		Map <EuroPl, Float> pls = new HashMap<EuroPl, Float>();

		if (euroPls != null && euroPls.size() > 0) {
			euMatrix = new EuroMatrix();
			euMatrix.setOriginEuro(euroPls.get(0));
			EuroPl currentEuroPl = euroPls.get(euroPls.size()-1);
			euMatrix.setCurrentEuro(currentEuroPl);

			float hours = 0;
			EuroPl temp = null;
			float winChange = 0;
			float drawChange = 0;
			float loseChange = 0;

			for (EuroPl eu : euroPls) {
				if (temp != null) {
					Date thisDt = eu.geteDate();
					Date lastDt = temp.geteDate();

					float tempHours = MatchUtil.getDiffHours(thisDt, lastDt);
					float lastTimeToMatch = MatchUtil.getDiffHours(matchDt, lastDt);
					float thisTimeToMatch = MatchUtil.getDiffHours(matchDt, thisDt);
					
					// the latest(in 24h) long hours's pankou
					if (lastTimeToMatch > PL_CHECKED_HOURS) {
						hours = PL_CHECKED_HOURS - thisTimeToMatch;

						if (hours >= 0) {
							pls.put(temp, hours);
							winChange += (eu.geteWin() - temp.geteWin()) * hours / temp.geteWin();
							drawChange += (eu.geteDraw() - temp.geteDraw()) * hours / temp.geteWin();
							loseChange += (eu.geteLose() - temp.geteLose()) * hours / temp.geteWin();
						}
					} else {
						Float totalHours = pls.get(temp);
						totalHours = (totalHours == null ? 0 : totalHours) + tempHours;
						pls.put(temp, totalHours);
						
						winChange += (eu.geteWin() - temp.geteWin()) * tempHours / temp.geteWin();
						drawChange += (eu.geteDraw() - temp.geteDraw()) * tempHours / temp.geteWin();
						loseChange += (eu.geteLose() - temp.geteLose()) * tempHours / temp.geteWin();
					}
				}

				temp = eu;
			}

			Date currentDate = new Date();
			Float totalHours = pls.get(temp);
			float lastToNow = MatchUtil.getDiffHours(currentDate.before(matchDt) ? currentDate : matchDt, temp.geteDate());
			totalHours = (totalHours == null ? 0 : totalHours) + lastToNow;
			pls.put(temp, totalHours);


			EuroPl main = null;
			float maxHour = 0;
			for (Map.Entry <EuroPl, Float> pl : pls.entrySet()) {
				if (pl.getValue() > maxHour) {
					maxHour = pl.getValue();
					main = pl.getKey();
				}
			}
			
			euMatrix.setMainEuro(main);
			euMatrix.setWinChange(winChange / PL_CHECKED_HOURS);
			euMatrix.setDrawChange(drawChange / PL_CHECKED_HOURS);
			euMatrix.setLoseChange(loseChange / PL_CHECKED_HOURS);
		}

		return euMatrix;
	}
	
	private EuroMatrix getRelativeEuroMatrix (List<EuroPl> euroPls, Date matchDt) {
		EuroMatrix euMatrix = null;

		if (euroPls != null && euroPls.size() > 0) {
			euMatrix = new EuroMatrix();
			euMatrix.setOriginEuro(euroPls.get(0));
			euMatrix.setOriginEuro(euroPls.get(0));
			EuroPl currentEuroPl = euroPls.get(euroPls.size()-1);
			euMatrix.setCurrentEuro(currentEuroPl);

			EuroPl main = null;
			float hours = 0;
			EuroPl temp = null;
			float winChange = 0;
			float drawChange = 0;
			float loseChange = 0;

			for (EuroPl eu : euroPls) {
				if (temp != null) {
					Date thisDt = eu.geteDate();
					Date lastDt = temp.geteDate();
					
					float tempHours = MatchUtil.getDiffHours(thisDt, lastDt);
					float lastTimeToMatch = MatchUtil.getDiffHours(matchDt, lastDt);
					float thisTimeToMatch = MatchUtil.getDiffHours(matchDt, thisDt);

					// the latest(in 24h) long hours's pankou
					if (lastTimeToMatch > PL_CHECKED_HOURS) {
						hours = PL_CHECKED_HOURS - thisTimeToMatch;

						if (hours > 0) {
							winChange += (eu.geteWin() - temp.geteWin()) * hours / temp.geteWin();
							drawChange += (eu.geteDraw() - temp.geteDraw()) * hours / temp.geteWin();
							loseChange += (eu.geteLose() - temp.geteLose()) * hours / temp.geteWin();
						}

						main = temp;
					} else {
						winChange += (eu.geteWin() - temp.geteWin()) * tempHours / temp.geteWin();
						drawChange += (eu.geteDraw() - temp.geteDraw()) * tempHours / temp.geteWin();
						loseChange += (eu.geteLose() - temp.geteLose()) * tempHours / temp.geteWin();

						if (tempHours >= hours) {
							hours = tempHours;
							main = temp;
						}
					}
				}
				
				temp = eu;
			}
			
			if (MatchUtil.getDiffHours(new Date(), temp.geteDate()) >= hours) {
				main = temp;
			}

			euMatrix.setMainEuro(main);
			euMatrix.setWinChange(winChange / PL_CHECKED_HOURS);
			euMatrix.setDrawChange(drawChange / PL_CHECKED_HOURS);
			euMatrix.setLoseChange(loseChange / PL_CHECKED_HOURS);
		}

		return euMatrix;
	}

}
