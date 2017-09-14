package com.roy.football.match.OFN;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.coremedia.iso.boxes.CompositionTimeToSample.Entry;
import com.google.common.collect.Maps;
import com.roy.football.match.OFN.response.AsiaPl;
import com.roy.football.match.OFN.response.Company;
import com.roy.football.match.OFN.response.EuroPl;
import com.roy.football.match.OFN.response.OFNMatchData;
import com.roy.football.match.OFN.statics.matrices.EuroMatrices;
import com.roy.football.match.OFN.statics.matrices.EuroMatrices.EuroMatrix;
import com.roy.football.match.OFN.statics.matrices.PankouMatrices;
import com.roy.football.match.base.League;
import com.roy.football.match.crawler.controller.OFNMatchService;
import com.roy.football.match.process.Calculator;
import com.roy.football.match.util.EuroUtil;
import com.roy.football.match.util.MatchUtil;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class EuroCalculator extends AbstractBaseDataCalculator implements Calculator<EuroMatrices, OFNMatchData>{
	
	private final static int PL_CHECKED_HOURS = 32;

	@Override
	public EuroMatrices calucate(OFNMatchData matchData) {
		if (matchData != null) {
			Map<Company, List<EuroPl>> comEuros = matchData.getEuroPls();
			Date matchDt = matchData.getMatchTime();
			
			if (comEuros != null && comEuros.size() > 0) {
				EuroMatrices euroMatrices = new EuroMatrices();
				Map<Company, EuroMatrix> companyEus = Maps.newHashMap();
				euroMatrices.setCompanyEus(companyEus);
				
				for (Map.Entry<Company, List<EuroPl>> entry : comEuros.entrySet()) {
					Company comany = entry.getKey();
					List<EuroPl> pls = entry.getValue();
					
					if (pls != null && pls.size() > 0) {
						companyEus.put(comany, getEuroMatrix(pls, matchDt));
					}
				}

				EuroPl avg = matchData.getEuroAvg();
				if (avg == null || avg.getEWin() == null) {
					avg = calEuroAvg(companyEus);
				}
				euroMatrices.setCurrEuroAvg(avg);
				
				League league = matchData.getLeague();
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
	
	private EuroPl calEuroAvg (Map<Company, EuroMatrix> companyEus) {
		float win = 0, draw = 0, lose = 0;
		int num = 0;
		
		for (Map.Entry<Company, EuroMatrix> entry : companyEus.entrySet()) {
			EuroMatrix em = entry.getValue();
			
			if (em != null && em.getCurrentEuro() != null) {
				EuroPl eu = em.getCurrentEuro();
				num ++;
				win += eu.getEWin();
				draw += eu.getEDraw();
				lose += eu.getELose();
			}
		}
		
		if (num > 0) {
			return new EuroPl(win / num, draw / num, lose / num, null);
		} else {
			return null;
		}
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
		EuroMatrix majorComp = EuroUtil.getMainEuro(euroMatrices, league);

		if (majorComp != null) {
			EuroPl currMajorpl = majorComp.getCurrentEuro();
			EuroPl currAvgPl = euroMatrices.getCurrEuroAvg();

			if (currMajorpl != null && currAvgPl != null) {
				euroMatrices.setMainAvgWinDiff(MatchUtil.getEuDiff(currMajorpl.getEWin(), currAvgPl.getEWin(), false));
				euroMatrices.setMainAvgDrawDiff(MatchUtil.getEuDiff(currMajorpl.getEDraw(), currAvgPl.getEDraw(), false));
				euroMatrices.setMainAvgLoseDiff(MatchUtil.getEuDiff(currMajorpl.getELose(), currAvgPl.getELose(), false));
			}
		}
	}
	
	private EuroMatrix getAbsoluteEuroMatrix (List<EuroPl> euroPls, Date matchDt) {
		EuroMatrix euMatrix = null;
		Map <EuroPl, Float> pls = new HashMap<EuroPl, Float>();

		if (euroPls != null && euroPls.size() > 0) {
			euMatrix = new EuroMatrix();
			euMatrix.setOriginEuro(euroPls.get(0));
			
			for (int index = euroPls.size()-1; index >=0; index--) {
				if (MatchUtil.getDiffHours(matchDt, euroPls.get(index).getEDate()) >= 0.3f) {
					euMatrix.setCurrentEuro(euroPls.get(index));
					break;
				} else {
					// remove the pl which is too close the started time, and not quite useful
					euroPls.remove(index);
				}
			}

			float hours = 0;
			EuroPl temp = null;
			EuroPl lastTemp = null;
			float winChange = 0;
			float drawChange = 0;
			float loseChange = 0;
			Map <EuroPl, Integer> shortPl = new HashMap<EuroPl, Integer>();

			for (EuroPl eu : euroPls) {
				if (temp != null) {
					Date thisDt = eu.getEDate();
					Date lastDt = temp.getEDate();

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
								winChange += (temp.getEWin() - lastTemp.getEWin()) * hours / lastTemp.getEWin();
								drawChange += (temp.getEDraw() - lastTemp.getEDraw()) * hours / lastTemp.getEDraw();
								loseChange += (temp.getELose() - lastTemp.getELose()) * hours / lastTemp.getELose();
							}
						}
					} else {
						Float totalHours = pls.get(temp);
						totalHours = (totalHours == null ? 0 : totalHours) + tempHours;
						pls.put(temp, totalHours);
						
						if (tempHours > 0.17) {
							winChange += (temp.getEWin() - lastTemp.getEWin()) * tempHours / lastTemp.getEWin();
							drawChange += (temp.getEDraw() - lastTemp.getEDraw()) * tempHours / lastTemp.getEDraw();
							loseChange += (temp.getELose() - lastTemp.getELose()) * tempHours / lastTemp.getELose();
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
			float lastToNow = MatchUtil.getDiffHours(currentDate.before(matchDt) ? currentDate : matchDt, temp.getEDate());
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
			
			winChange += (temp.getEWin() - lastTemp.getEWin()) * (lastToNow > PL_CHECKED_HOURS ? 0 : lastToNow) / lastTemp.getEWin();
			drawChange += (temp.getEDraw() - lastTemp.getEDraw()) * (lastToNow > PL_CHECKED_HOURS ? 0 : lastToNow) / lastTemp.getEDraw();
			loseChange += (temp.getELose() - lastTemp.getELose()) * (lastToNow > PL_CHECKED_HOURS ? 0 : lastToNow) / lastTemp.getELose();
			
			if (shortPl.size() > 0) {
				int maxCnt = 0;
				EuroPl maxPl = null;
				for (Map.Entry<EuroPl, Integer> entry : shortPl.entrySet()) {
					if (entry.getValue() > maxCnt) {
						maxCnt = entry.getValue();
						maxPl = entry.getKey();
					}
				}
				
				euMatrix.setSmWinDiff(MatchUtil.getEuDiff(maxPl.getEWin(), main.getEWin(), false));
				euMatrix.setSmDrawDiff(MatchUtil.getEuDiff(maxPl.getEDraw(), main.getEDraw(), false));
				euMatrix.setSmLoseDiff(MatchUtil.getEuDiff(maxPl.getELose(), main.getELose(), false));
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
			
			for (int index = euroPls.size()-1; index >=0; index--) {
				if (MatchUtil.getDiffHours(matchDt, euroPls.get(index).getEDate()) >= 0.3f) {
					euMatrix.setCurrentEuro(euroPls.get(index));
					break;
				} else {
					// remove the pl which is too close the started time, and not quite useful
					euroPls.remove(index);
				}
			}

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
					Date thisDt = eu.getEDate();
					Date lastDt = temp.getEDate();
					
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
							winChange += (temp.getEWin() - lastTemp.getEWin()) * hours / lastTemp.getEWin();
							drawChange += (temp.getEDraw() - lastTemp.getEDraw()) * hours / lastTemp.getEDraw();
							loseChange += (temp.getELose() - lastTemp.getELose()) * hours / lastTemp.getELose();
						}

						main = temp;
					} else {
						if (tempHours > 0.17) {
							winChange += (temp.getEWin() - lastTemp.getEWin()) * tempHours / lastTemp.getEWin();
							drawChange += (temp.getEDraw() - lastTemp.getEDraw()) * tempHours / lastTemp.getEDraw();
							loseChange += (temp.getELose() - lastTemp.getELose()) * tempHours / lastTemp.getELose();
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
			
			float latestHour = MatchUtil.getDiffHours(new Date(), temp.getEDate());
			if (latestHour >= hours) {
				main = temp;
			}
			
			if (lastTemp == null) {
				lastTemp = temp;
			}
			
			winChange += (temp.getEWin() - lastTemp.getEWin()) * (latestHour > PL_CHECKED_HOURS ? 0 : latestHour) / lastTemp.getEWin();
			drawChange += (temp.getEDraw() - lastTemp.getEDraw()) * (latestHour > PL_CHECKED_HOURS ? 0 : latestHour) / lastTemp.getEDraw();
			loseChange += (temp.getELose() - lastTemp.getELose()) * (latestHour > PL_CHECKED_HOURS ? 0 : latestHour) / lastTemp.getELose();
			
			if (shortPl.size() > 0) {
				int maxCnt = 0;
				EuroPl maxPl = null;
				for (Map.Entry<EuroPl, Integer> entry : shortPl.entrySet()) {
					if (entry.getValue() > maxCnt) {
						maxPl = entry.getKey();
					}
				}
				
				euMatrix.setSmWinDiff(MatchUtil.getEuDiff(maxPl.getEWin(), main.getEWin(), false));
				euMatrix.setSmDrawDiff(MatchUtil.getEuDiff(maxPl.getEDraw(), main.getEDraw(), false));
				euMatrix.setSmLoseDiff(MatchUtil.getEuDiff(maxPl.getELose(), main.getELose(), false));
			}

			euMatrix.setMainEuro(main);
			euMatrix.setWinChange(winChange / PL_CHECKED_HOURS);
			euMatrix.setDrawChange(drawChange / PL_CHECKED_HOURS);
			euMatrix.setLoseChange(loseChange / PL_CHECKED_HOURS);
		}

		return euMatrix;
	}

}
