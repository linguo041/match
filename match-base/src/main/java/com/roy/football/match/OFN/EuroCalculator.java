package com.roy.football.match.OFN;

import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.math3.stat.StatUtils;
import org.apache.commons.math3.util.FastMath;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import com.coremedia.iso.boxes.CompositionTimeToSample.Entry;
import com.google.common.collect.Lists;
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
		
		double[] winEuArray = new double[10];
		double[] drawEuArray = new double[10];
		double[] loseEuArray = new double[10];
		int index = 0;
		
		for (Map.Entry<Company, EuroMatrix> entry : euroMatrices.getCompanyEus().entrySet()) {
			if (!Company.Jincai.equals(entry.getKey()) && entry.getValue() != null) {
				winEuArray[index] = entry.getValue().getCurrentEuro().getEWin();
				drawEuArray[index] = entry.getValue().getCurrentEuro().getEDraw();
				loseEuArray[index] = entry.getValue().getCurrentEuro().getELose();
				index++;
			}
		}
		
		euroMatrices.setEuWinVariance(FastMath.sqrt(StatUtils.variance(winEuArray, 0, index)));
		euroMatrices.setEuDrawVariance(FastMath.sqrt(StatUtils.variance(drawEuArray, 0, index)));
		euroMatrices.setEuLoseVariance(FastMath.sqrt(StatUtils.variance(loseEuArray, 0, index)));
	}
	
	public static void main (String args[]) {
		double[] winEuArray = new double[10];
		int index = 0;
		winEuArray[index] = 10;
		index++;
		winEuArray[index] = 11;
		index++;
		winEuArray[index] = 9;
		index++;
		winEuArray[index] = 12;
		index++;
		winEuArray[index] = 8;
		index++;
		
		System.out.println(FastMath.sqrt(StatUtils.variance(winEuArray, 0, index)));
		
		System.out.println(Lists.newArrayList("a", "b"));
	}
	
	private EuroMatrix getAbsoluteEuroMatrix (List<EuroPl> euroPls, Date matchDt) {
		if (CollectionUtils.isEmpty(euroPls)) {
			return null;
		}
		
		EuroMatrix euMatrix = new EuroMatrix();
		
		euroPls = filterPls(euroPls, euMatrix, matchDt);
		
		Map <EuroPl, Float> pls = new HashMap<EuroPl, Float>();

		if (!CollectionUtils.isEmpty(euroPls)) {
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
					float weight = 0.5f * tempHours * (getTimeWeight(thisTimeToMatch) + getTimeWeight(lastTimeToMatch));

					if (lastTemp == null) {
						lastTemp = temp;
					}
					
					// the latest(in 24h) long hours's pankou
					// y=1-x/56
					if (lastTimeToMatch > PL_CHECKED_HOURS) {
						hours = PL_CHECKED_HOURS - thisTimeToMatch;
						weight = 0.5f * hours * (getTimeWeight(thisTimeToMatch) + getTimeWeight(PL_CHECKED_HOURS));

						if (hours >= 0) {
							pls.put(temp, weight);
							
							if (hours > 0.17) {
								winChange += (temp.getEWin() - lastTemp.getEWin()) * hours / lastTemp.getEWin();
								drawChange += (temp.getEDraw() - lastTemp.getEDraw()) * hours / lastTemp.getEDraw();
								loseChange += (temp.getELose() - lastTemp.getELose()) * hours / lastTemp.getELose();
							}
						}
					} else {
						Float totalWeights = pls.get(temp);
						totalWeights = (totalWeights == null ? 0 : totalWeights) + weight;
						pls.put(temp, totalWeights);
						
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

			Float totalHours = pls.get(temp);
			float lastToStart = MatchUtil.getDiffHours(matchDt, temp.getEDate());
			float weight = 0.5f * lastToStart * (getTimeWeight(lastToStart) + getTimeWeight(0f));
			totalHours = (totalHours == null ? 0 : totalHours) + weight;
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
			
			winChange += (temp.getEWin() - lastTemp.getEWin()) * (lastToStart > PL_CHECKED_HOURS ? 0 : lastToStart) / lastTemp.getEWin();
			drawChange += (temp.getEDraw() - lastTemp.getEDraw()) * (lastToStart > PL_CHECKED_HOURS ? 0 : lastToStart) / lastTemp.getEDraw();
			loseChange += (temp.getELose() - lastTemp.getELose()) * (lastToStart > PL_CHECKED_HOURS ? 0 : lastToStart) / lastTemp.getELose();
			
			if (shortPl.size() > 0) {
				int maxCnt = 0;
				EuroPl maxPl = null;
				for (Map.Entry<EuroPl, Integer> entry : shortPl.entrySet()) {
					if (entry.getValue() > maxCnt) {
						maxCnt = entry.getValue();
						maxPl = entry.getKey();
					}
				}
				
				if (maxPl == null || main == null) {
					log.info("null found for eu: {}", euroPls.toString());
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
		if (CollectionUtils.isEmpty(euroPls)) {
			return null;
		}
		
		EuroMatrix euMatrix = new EuroMatrix();
		
		euroPls = filterPls(euroPls, euMatrix, matchDt);

		if (!CollectionUtils.isEmpty(euroPls)) {
			EuroPl main = null;
			float weight = 0;
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
					float tempWeight = 0.5f * tempHours * (getTimeWeight(thisTimeToMatch) + getTimeWeight(lastTimeToMatch));

					if (lastTemp == null) {
						lastTemp = temp;
					}

					// the latest(in 24h) long hours's pankou
					if (lastTimeToMatch > PL_CHECKED_HOURS) {
						hours = PL_CHECKED_HOURS - thisTimeToMatch;
						tempWeight = 0.5f * tempHours * (getTimeWeight(thisTimeToMatch) + getTimeWeight(PL_CHECKED_HOURS));

						if (hours > 0.17) {
							winChange += (temp.getEWin() - lastTemp.getEWin()) * hours / lastTemp.getEWin();
							drawChange += (temp.getEDraw() - lastTemp.getEDraw()) * hours / lastTemp.getEDraw();
							loseChange += (temp.getELose() - lastTemp.getELose()) * hours / lastTemp.getELose();
						}

						main = temp;
						weight = tempWeight;
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
						

						if (tempWeight >= weight) {
							hours = tempHours;
							weight = tempWeight;
							main = temp;
						}
					}
					
					lastTemp = temp;
				}
				
				temp = eu;
			}
			
			float latestHour = MatchUtil.getDiffHours(matchDt, temp.getEDate());
			float tempWeight = 0.5f * latestHour * (getTimeWeight(latestHour) + getTimeWeight(0));
			if (tempWeight >= weight) {
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
	
	private List<EuroPl> filterPls (List<EuroPl> euroPls, EuroMatrix euMatrix, Date matchDt) {
		if (euroPls == null || euroPls.size() <= 0) {
			return Lists.newArrayList();
		}
		
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
		
		return euroPls;
	}
	
	private float getTimeWeight (float hoursToBegin) {
		return 1 - 0.5f * hoursToBegin / PL_CHECKED_HOURS;
	}

}
