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
//				euroMatrices.setYiShenBoMatrix(getEuroMatrix(comEuros.get(Company.YiShenBo), matchDt));
//				euroMatrices.setInterwettenMatrix(getEuroMatrix(comEuros.get(Company.Interwetten), matchDt));
				euroMatrices.setSnaiMatrix(getEuroMatrix(comEuros.get(Company.SNAI), matchDt));
				euroMatrices.setSwedenMatrix(getEuroMatrix(comEuros.get(Company.Sweden), matchDt));
				euroMatrices.setCurrEuroAvg(matchData.getEuroAvg());
				
				League league = League.getLeagueById(matchData.getLeagueId());
				setMainAvgDiff(euroMatrices, league);

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

	private void setMainAvgDiff (EuroMatrices euroMatrices, League league) {
		EuroMatrix majorComp = MatchUtil.getMainEuro(euroMatrices, league);

		if (majorComp != null) {
			EuroPl currMajorpl = majorComp.getCurrentEuro();
			EuroPl currAvgPl = euroMatrices.getCurrEuroAvg();

			if (currMajorpl != null && currAvgPl != null) {
				euroMatrices.setMainAvgWinDiff(MatchUtil.getEuDiff(currMajorpl.geteWin(), currAvgPl.geteWin(), false));
				euroMatrices.setMainAvgDrawDiff(MatchUtil.getEuDiff(currMajorpl.geteDraw(), currAvgPl.geteDraw(), false));
				euroMatrices.setMainAvgLoseDiff(MatchUtil.getEuDiff(currMajorpl.geteLose(), currAvgPl.geteLose(), false));
			}
		}
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
			EuroPl lastTemp = null;
			float winChange = 0;
			float drawChange = 0;
			float loseChange = 0;
			Map <EuroPl, Integer> shortPl = new HashMap<EuroPl, Integer>();

			for (EuroPl eu : euroPls) {
				if (temp != null) {
					Date thisDt = eu.geteDate();
					Date lastDt = temp.geteDate();

					float tempHours = MatchUtil.getDiffHours(thisDt, lastDt);
					float lastTimeToMatch = MatchUtil.getDiffHours(matchDt, lastDt);
					float thisTimeToMatch = MatchUtil.getDiffHours(matchDt, thisDt);

					if (lastTemp == null) {
						lastTemp = temp;
					}
					
					// the latest(in 24h) long hours's pankou
					if (lastTimeToMatch > PL_CHECKED_HOURS) {
						hours = PL_CHECKED_HOURS - thisTimeToMatch;

						if (hours >= 0) {
							pls.put(temp, hours);
							
							if (hours > 0.17) {
								winChange += (temp.geteWin() - lastTemp.geteWin()) * hours / lastTemp.geteWin();
								drawChange += (temp.geteDraw() - lastTemp.geteDraw()) * hours / lastTemp.geteDraw();
								loseChange += (temp.geteLose() - lastTemp.geteLose()) * hours / lastTemp.geteLose();
							}
						}
					} else {
						Float totalHours = pls.get(temp);
						totalHours = (totalHours == null ? 0 : totalHours) + tempHours;
						pls.put(temp, totalHours);
						
						if (tempHours > 0.17) {
							winChange += (temp.geteWin() - lastTemp.geteWin()) * tempHours / lastTemp.geteWin();
							drawChange += (temp.geteDraw() - lastTemp.geteDraw()) * tempHours / lastTemp.geteDraw();
							loseChange += (temp.geteLose() - lastTemp.geteLose()) * tempHours / lastTemp.geteLose();
						} else {
							Integer cnt = shortPl.get(temp);
							if (cnt == null) {
								cnt = 0;
							}
							shortPl.put(temp, ++cnt);
						}
					}
					
					if (tempHours > 0.17) {
						lastTemp = temp;
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
			
			if (lastTemp == null) {
				lastTemp = temp;
			}
			
			winChange += (temp.geteWin() - lastTemp.geteWin()) * (lastToNow > PL_CHECKED_HOURS ? 0 : lastToNow) / lastTemp.geteWin();
			drawChange += (temp.geteDraw() - lastTemp.geteDraw()) * (lastToNow > PL_CHECKED_HOURS ? 0 : lastToNow) / lastTemp.geteDraw();
			loseChange += (temp.geteLose() - lastTemp.geteLose()) * (lastToNow > PL_CHECKED_HOURS ? 0 : lastToNow) / lastTemp.geteLose();
			
			if (shortPl.size() > 0) {
				int maxCnt = 0;
				EuroPl maxPl = null;
				for (Map.Entry<EuroPl, Integer> entry : shortPl.entrySet()) {
					if (entry.getValue() > maxCnt) {
						maxCnt = entry.getValue();
						maxPl = entry.getKey();
					}
				}
				
				euMatrix.setSmWinDiff(MatchUtil.getEuDiff(maxPl.geteWin(), main.geteWin(), false));
				euMatrix.setSmDrawDiff(MatchUtil.getEuDiff(maxPl.geteDraw(), main.geteDraw(), false));
				euMatrix.setSmLoseDiff(MatchUtil.getEuDiff(maxPl.geteLose(), main.geteLose(), false));
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
			EuroPl lastTemp = null;
			float winChange = 0;
			float drawChange = 0;
			float loseChange = 0;
			Map <EuroPl, Integer> shortPl = new HashMap<EuroPl, Integer>();

			for (EuroPl eu : euroPls) {
				if (temp != null) {
					Date thisDt = eu.geteDate();
					Date lastDt = temp.geteDate();
					
					float tempHours = MatchUtil.getDiffHours(thisDt, lastDt);
					float lastTimeToMatch = MatchUtil.getDiffHours(matchDt, lastDt);
					float thisTimeToMatch = MatchUtil.getDiffHours(matchDt, thisDt);

					if (lastTemp == null) {
						lastTemp = temp;
					}

					// the latest(in 24h) long hours's pankou
					if (lastTimeToMatch > PL_CHECKED_HOURS) {
						hours = PL_CHECKED_HOURS - thisTimeToMatch;

						if (hours > 0.17) {
							winChange += (temp.geteWin() - lastTemp.geteWin()) * hours / lastTemp.geteWin();
							drawChange += (temp.geteDraw() - lastTemp.geteDraw()) * hours / lastTemp.geteDraw();
							loseChange += (temp.geteLose() - lastTemp.geteLose()) * hours / lastTemp.geteLose();
						}

						main = temp;
					} else {
						if (tempHours > 0.17) {
							winChange += (temp.geteWin() - lastTemp.geteWin()) * tempHours / lastTemp.geteWin();
							drawChange += (temp.geteDraw() - lastTemp.geteDraw()) * tempHours / lastTemp.geteDraw();
							loseChange += (temp.geteLose() - lastTemp.geteLose()) * tempHours / lastTemp.geteLose();
						} else {
							Integer cnt = shortPl.get(temp);
							if (cnt == null) {
								cnt = 0;
							}
							shortPl.put(temp, ++cnt);
						}
						

						if (tempHours >= hours) {
							hours = tempHours;
							main = temp;
						}
					}
					
					lastTemp = temp;
				}
				
				temp = eu;
			}
			
			float latestHour = MatchUtil.getDiffHours(new Date(), temp.geteDate());
			if (latestHour >= hours) {
				main = temp;
			}
			
			if (lastTemp == null) {
				lastTemp = temp;
			}
			
			winChange += (temp.geteWin() - lastTemp.geteWin()) * (latestHour > PL_CHECKED_HOURS ? 0 : latestHour) / lastTemp.geteWin();
			drawChange += (temp.geteDraw() - lastTemp.geteDraw()) * (latestHour > PL_CHECKED_HOURS ? 0 : latestHour) / lastTemp.geteDraw();
			loseChange += (temp.geteLose() - lastTemp.geteLose()) * (latestHour > PL_CHECKED_HOURS ? 0 : latestHour) / lastTemp.geteLose();
			
			if (shortPl.size() > 0) {
				int maxCnt = 0;
				EuroPl maxPl = null;
				for (Map.Entry<EuroPl, Integer> entry : shortPl.entrySet()) {
					if (entry.getValue() > maxCnt) {
						maxPl = entry.getKey();
					}
				}
				
				euMatrix.setSmWinDiff(MatchUtil.getEuDiff(maxPl.geteWin(), main.geteWin(), false));
				euMatrix.setSmDrawDiff(MatchUtil.getEuDiff(maxPl.geteDraw(), main.geteDraw(), false));
				euMatrix.setSmLoseDiff(MatchUtil.getEuDiff(maxPl.geteLose(), main.geteLose(), false));
			}

			euMatrix.setMainEuro(main);
			euMatrix.setWinChange(winChange / PL_CHECKED_HOURS);
			euMatrix.setDrawChange(drawChange / PL_CHECKED_HOURS);
			euMatrix.setLoseChange(loseChange / PL_CHECKED_HOURS);
		}

		return euMatrix;
	}

}
