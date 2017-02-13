package com.roy.football.match.OFN;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import com.roy.football.match.OFN.response.AsiaPl;
import com.roy.football.match.OFN.response.Company;
import com.roy.football.match.OFN.response.EuroPl;
import com.roy.football.match.OFN.statics.matrices.ClubMatrices;
import com.roy.football.match.OFN.statics.matrices.DaxiaoMatrices;
import com.roy.football.match.OFN.statics.matrices.EuroMatrices;
import com.roy.football.match.OFN.statics.matrices.EuroMatrices.EuroMatrix;
import com.roy.football.match.OFN.statics.matrices.JiaoShouMatrices;
import com.roy.football.match.OFN.statics.matrices.MatchState;
import com.roy.football.match.OFN.statics.matrices.MatchState.LatestMatchMatrices;
import com.roy.football.match.OFN.statics.matrices.OFNCalculateResult;
import com.roy.football.match.OFN.statics.matrices.OFNKillPromoteResult;
import com.roy.football.match.OFN.statics.matrices.PankouMatrices;
import com.roy.football.match.OFN.statics.matrices.PredictResult;
import com.roy.football.match.base.League;
import com.roy.football.match.base.ResultGroup;
import com.roy.football.match.base.TeamLabel;
import com.roy.football.match.okooo.MatchExchangeData;
import com.roy.football.match.util.EuroUtil;
import com.roy.football.match.util.MatchUtil;

public class PankouKillPromoter {
	private final static float LOW_PK_POINT = -0.02f;
	
	public PredictResult calculate (OFNCalculateResult calResult) {
		PredictResult predictRes = new PredictResult();
		OFNKillPromoteResult killPromoteResult = new OFNKillPromoteResult();
		Set<ResultGroup> pkKillGroups = new TreeSet<ResultGroup> ();
		Set<ResultGroup> plKillGroups = new TreeSet<ResultGroup> ();
		Set<ResultGroup> promoteGroups = new TreeSet<ResultGroup> ();
		killPromoteResult.setPromoteByPk(promoteGroups);
		killPromoteResult.setKillByPk(pkKillGroups);
		killPromoteResult.setKillByPl(plKillGroups);

		predict(predictRes, calResult);
		
		kill(killPromoteResult, calResult);
		promote(killPromoteResult, calResult);
		
		predictRes.setKpResult(killPromoteResult);

		return predictRes;
	}
	
	public void predict(PredictResult predictRes, OFNCalculateResult calResult) {
		float hgoal = 0;
		float ggoal = 0;
		
		// default to 1
		float hvariation = 1;
		float gvariation = 1;

		/*<<<<  calculate the score according to the base data & latest matches  <<<< */
		ClubMatrices clubMatrices = calResult.getClubMatrices();

		if (clubMatrices != null) {
			hgoal = clubMatrices.getHostAttGuestDefInx();
			ggoal = clubMatrices.getGuestAttHostDefInx();
		}

		// latest Match
		MatchState matchState = calResult.getMatchState();

		if (matchState != null) {
			float lhgoal = matchState.getHostAttackToGuest();
			float lggoal = matchState.getGuestAttackToHost();

			if (hgoal == 0 && ggoal == 0) {
				hgoal = lhgoal;
				ggoal = lggoal;
			} else {
				hgoal = 0.6f * hgoal + 0.4f * lhgoal;
				ggoal = 0.6f * ggoal + 0.4f * lggoal;
			}

			hvariation = matchState.getHostAttackVariationToGuest();
			gvariation = matchState.getGuestAttackVariationToHost();
		}
		
		JiaoShouMatrices jiaoShou = calResult.getJiaoShou();
		if (jiaoShou != null && jiaoShou.getMatchNum() > 3) {
			float jsHgoal = jiaoShou.getHgoalPerMatch();
			float jsGgoal = jiaoShou.getGgoalPerMatch();
			
			// Multiple 0.5 to avoid duplicate adding weight
			hgoal = 0.6f * hgoal + 0.4f * jsHgoal;
			ggoal = 0.6f * ggoal + 0.4f * jsGgoal;
		}
		/* >>>> end >>>>*/

		/*<<<<  adjust according to the pk and pl  <<<<*/
		// pankou
		float hAdjRate = 0;
		float gAdjRate = 0;

		Float predictPk = calResult.getPredictPanKou();
		if (predictPk == null && calResult.getJiaoShou() != null) {
			predictPk = calResult.getJiaoShou().getLatestPankou();
		}

//		Float hotPoint = calResult.getHotPoint();

		PankouMatrices pkMatrices = calResult.getPkMatrices();
		if (pkMatrices != null && predictPk != null) {
			// adjust the goals according to the diff of predict and original pk
			AsiaPl origPkpl = pkMatrices.getOriginPk();
			float origPk = MatchUtil.getCalculatedPk(origPkpl);
			float predictDiff = 0.5f * (predictPk - origPk);
			hgoal -= predictDiff;
			ggoal += predictDiff;

			float pkComp = pkMatrices.getHwinChangeRate() - pkMatrices.getAwinChangeRate();
			hAdjRate = -1 * pkComp;
			gAdjRate = pkComp;

		}
		
		// daxiao
		DaxiaoMatrices dxMatrices = calResult.getDxMatrices();
		float dxWeight = 0;
		if (dxMatrices != null && dxMatrices.getHours() > 0) {
			dxWeight = dxMatrices.getXiaoChangeRate() - dxMatrices.getDaChangeRate();
//			float dxPk = MatchUtil.getCalculatedPk(dxMatrices.getCurrentPk());
			
			hgoal += hvariation * hAdjRate;
			ggoal += gvariation * gAdjRate;

			hgoal *= (1 + dxWeight);
			ggoal *= (1 + dxWeight);
		}
		
		predictRes.setHostScore(hgoal);
		predictRes.setGuestScore(ggoal);
	}

	public void kill(OFNKillPromoteResult killPromoteResult, OFNCalculateResult calResult) {
		if (killPromoteResult != null) {
			PankouMatrices pkMatrices = calResult.getPkMatrices();
			Float predictPk = calResult.getPredictPanKou();

			if (predictPk == null && calResult.getJiaoShou() != null) {
				predictPk = calResult.getJiaoShou().getLatestPankou();
			}

			Float hotPoint = calResult.getMatchState().getHotPoint();
			League league = calResult.getLeague();

			if (pkMatrices != null) {
				ClubMatrices clubMatrices = calResult.getClubMatrices();

				if (clubMatrices != null) {
					List <TeamLabel> hostLabels = clubMatrices.getHostLabels();
					List <TeamLabel> guestLabels = clubMatrices.getGuestLabels();

					Set<ResultGroup> pkRes = killByPk(pkMatrices, hotPoint, predictPk, hostLabels, guestLabels, calResult.getClubMatrices());
					killPromoteResult.setKillByPk(pkRes);
				}
				
				killByEuro(killPromoteResult.getKillByPl(), calResult.getEuroMatrices(), pkMatrices, league);
			}

			killByExchange(killPromoteResult, calResult);
		}
	}

	public OFNKillPromoteResult kill(OFNCalculateResult calResult) {
		OFNKillPromoteResult killResult = new OFNKillPromoteResult();
		Set<ResultGroup> pkKillGroups = new TreeSet<ResultGroup> ();
		Set<ResultGroup> plKillGroups = new TreeSet<ResultGroup> ();
		Set<ResultGroup> promoteGroups = new TreeSet<ResultGroup> ();
		killResult.setPromoteByPk(promoteGroups);
		killResult.setKillByPk(pkKillGroups);
		killResult.setKillByPl(plKillGroups);
		
		PankouMatrices pkMatrices = calResult.getPkMatrices();
		Float predictPk = calResult.getPredictPanKou();

		if (predictPk == null && calResult.getJiaoShou() != null) {
			predictPk = calResult.getJiaoShou().getLatestPankou();
		}
		
		Float hotPoint = calResult.getMatchState().getHotPoint();
		League league = calResult.getLeague();
		
		if (pkMatrices != null) {
			ClubMatrices clubMatrices = calResult.getClubMatrices();

			if (clubMatrices != null) {
				List <TeamLabel> hostLabels = clubMatrices.getHostLabels();
				List <TeamLabel> guestLabels = clubMatrices.getGuestLabels();

				Set<ResultGroup> pkRes = killByPk(pkMatrices, hotPoint, predictPk, hostLabels, guestLabels, calResult.getClubMatrices());
				killResult.setKillByPk(pkRes);
			}
			
			killByEuro(killResult.getKillByPl(), calResult.getEuroMatrices(), pkMatrices, league);
		}
		
		killByExchange(killResult, calResult);
		
		return killResult;
	}

	public void promote(OFNKillPromoteResult killPromoteResult,
			OFNCalculateResult calResult) {
		if (killPromoteResult != null) {
			PankouMatrices pkMatrices = calResult.getPkMatrices();
			Float predictPk = calResult.getPredictPanKou();
			ClubMatrices clubMatrices = calResult.getClubMatrices();
			MatchState matchState = calResult.getMatchState();
			MatchExchangeData exchange = calResult.getExchanges();
			EuroMatrices euroMatrices= calResult.getEuroMatrices();

			Float hotPoint = matchState.getHotPoint();
			League league = calResult.getLeague();

			if (predictPk == null && calResult.getJiaoShou() != null) {
				predictPk = calResult.getJiaoShou().getLatestPankou();
			}

			Set<ResultGroup> promoteGps = new TreeSet<ResultGroup> ();

			if (pkMatrices != null && euroMatrices != null && predictPk != null) {
				promoteByEuro(promoteGps, euroMatrices, predictPk, pkMatrices, league);

				promoteByPk(promoteGps, pkMatrices, predictPk, hotPoint,
						matchState, clubMatrices, exchange, euroMatrices);
			}

			killPromoteResult.setPromoteByPk(promoteGps);
		}
	}

	public OFNKillPromoteResult promote(OFNCalculateResult calResult) {
		OFNKillPromoteResult killPromoteResult = new OFNKillPromoteResult();
		
		PankouMatrices pkMatrices = calResult.getPkMatrices();
		Float predictPk = calResult.getPredictPanKou();
		ClubMatrices clubMatrices = calResult.getClubMatrices();
		MatchState matchState = calResult.getMatchState();
		MatchExchangeData exchange = calResult.getExchanges();
		EuroMatrices euroMatrices= calResult.getEuroMatrices();
		
		Float hotPoint = matchState.getHotPoint();
		League league = calResult.getLeague();

		Set<ResultGroup> promoteGps = new TreeSet<ResultGroup> ();

		if (pkMatrices != null && euroMatrices != null) {
			promoteByEuro(promoteGps, euroMatrices, predictPk, pkMatrices, league);

			promoteByPk(promoteGps, pkMatrices, predictPk, hotPoint,
					matchState, clubMatrices, exchange, euroMatrices);
		}

		killPromoteResult.setPromoteByPk(promoteGps);

		return killPromoteResult;
	}

	private Set<ResultGroup> killByPk (PankouMatrices pkMatrices, Float hotPoint, Float predictPk,
			List <TeamLabel> hostLabels, List <TeamLabel> guestLabels, ClubMatrices clubMs) {
		Set<ResultGroup> rgs = new TreeSet<ResultGroup> ();

		if (pkMatrices != null && predictPk != null) {
//			AsiaPl origin = pkMatrices.getOriginPk();
			AsiaPl main = pkMatrices.getMainPk();
			AsiaPl current = pkMatrices.getCurrentPk();
			Float hWinChgRtFloat = pkMatrices.getHwinChangeRate();
			Float gWinChgRtFloat = pkMatrices.getAwinChangeRate();
			
			float mainPk = MatchUtil.getCalculatedPk(main);
//			float currentPk = MatchUtil.getCalculatedPk(current);
			float asiaPk = main.getPanKou();
			
			boolean skipWin = false;
			boolean skipLose = false;
			if (hotPoint == null) {
				hotPoint = 0f;
			}
			
			try {
				if (clubMs != null) {
					if (clubMs.getHostHomeMatrix().getWinRt() - clubMs.getGuestAwayMatrix().getWinDrawRt() >= -0.1 || hotPoint > 6) {
						skipWin = true;
					}
					if (clubMs.getGuestAwayMatrix().getWinRt() - clubMs.getHostHomeMatrix().getWinDrawRt() >= -0.1 || hotPoint < -6) {
						skipLose = true;
					}
				}
			} catch (Exception e) {
				// ignore..
			}

			// the predict and main is nearly same, but the company chooses high pay to stop betting on win/lose.
			if ((hWinChgRtFloat > 0.12 || hWinChgRtFloat >= 0.06 && current.gethWin() > 1.02)
					&& current.gethWin() - main.gethWin() >= -0.04
					&& Math.abs(predictPk - mainPk) < 0.29
					&& !MatchUtil.isHostHomeStrong(hostLabels)) {

				if (asiaPk >= 1) {
					if (!skipWin) {
						rgs.add(ResultGroup.RangThree);
					}
				} else if (asiaPk >= -0.25 ) {
					if (!skipWin) {
						rgs.add(ResultGroup.Three);
					}
				} else {
					rgs.add(ResultGroup.Three);
					rgs.add(ResultGroup.One);
				}
			}
			
			if ((gWinChgRtFloat >= 0.12 || gWinChgRtFloat >= 0.06 && current.getaWin() > 1.02)
					&& current.getaWin() - main.getaWin() >= -0.04
					&& Math.abs(predictPk - mainPk) < 0.29
					&& !MatchUtil.isGuestDefensive(guestLabels)) {
				if (asiaPk <= -1) {
					if (!skipLose) {
						rgs.add(ResultGroup.RangZero);
					}
				} else if (asiaPk <= 0.25) {
					if (!skipLose) {
						rgs.add(ResultGroup.Zero);
					}
				} else {
					rgs.add(ResultGroup.One);
					rgs.add(ResultGroup.Zero);
				}
			}
		}

		return rgs;
	}
	
	private void killByEuro (Set<ResultGroup> killGps, EuroMatrices euMatrices, PankouMatrices pkMatrices, League league) {

		if (euMatrices != null && pkMatrices != null) {
			Map<Company, EuroMatrix> companyEus = euMatrices.getCompanyEus();
			EuroMatrix will = companyEus.get(Company.William);
			EuroMatrix lab = companyEus.get(Company.Ladbrokes);
			EuroMatrix aomen = companyEus.get(Company.Aomen);
			EuroPl euroAvg = euMatrices.getCurrEuroAvg();
			EuroMatrix jincai = companyEus.get(Company.Jincai);
			EuroMatrix majorComp = EuroUtil.getMainEuro(euMatrices, league);
			
			AsiaPl aomenCurrPk = pkMatrices.getCurrentPk();
			AsiaPl aomenMainPk = pkMatrices.getMainPk();
			float aomenPk = aomenCurrPk.getPanKou();
			
			boolean isAomenMajor = EuroUtil.isAomenTheMajor(league);

			if (euroAvg != null && lab != null && majorComp != null && aomen != null) {
				EuroPl currLabEu = lab.getCurrentEuro();
				EuroPl currMajorEu = majorComp.getCurrentEuro();
				EuroPl currAomenEu = aomen.getCurrentEuro();
				EuroPl currWillEu = will.getCurrentEuro();
				float majorWinChange = majorComp.getWinChange();
				float majorDrawChange = majorComp.getDrawChange();
				float majorLoseChange = majorComp.getLoseChange();
				
				float willWinChange = will.getWinChange();
				float willDrawChange = will.getDrawChange();
				float willLoseChange = will.getLoseChange();
				
				float aomenWinChange = aomen.getWinChange();
				float aomenDrawChange = aomen.getDrawChange();
				float aomenLoseChange = aomen.getLoseChange();

				float laWinRt = MatchUtil.getEuDiff(currLabEu.getEWin(), euroAvg.getEWin(), false);
				float laDrawRt = MatchUtil.getEuDiff(currLabEu.getEDraw(), euroAvg.getEDraw(), false);
				float laLoseRt = MatchUtil.getEuDiff(currLabEu.getELose(), euroAvg.getELose(), false);

				float maWinRt = MatchUtil.getEuDiff(currMajorEu.getEWin(), euroAvg.getEWin(), false);
				float maDrawRt = MatchUtil.getEuDiff(currMajorEu.getEDraw(), euroAvg.getEDraw(), false);
				float maLoseRt = MatchUtil.getEuDiff(currMajorEu.getELose(), euroAvg.getELose(), false);
				
				float aaWinRt = MatchUtil.getEuDiff(currAomenEu.getEWin(), euroAvg.getEWin(), false);
				float aaDrawRt = MatchUtil.getEuDiff(currAomenEu.getEDraw(), euroAvg.getEDraw(), false);
				float aaLoseRt = MatchUtil.getEuDiff(currAomenEu.getELose(), euroAvg.getELose(), false);
				
				float waWinRt = MatchUtil.getEuDiff(currWillEu.getEWin(), euroAvg.getEWin(), false);
				float waDrawRt = MatchUtil.getEuDiff(currWillEu.getEDraw(), euroAvg.getEDraw(), false);
				float waLoseRt = MatchUtil.getEuDiff(currWillEu.getELose(), euroAvg.getELose(), false);
				
				float jcWinChange = -1f;
				float jcDrawChange = -1f;
				float jcLoseChange = -1f;
				float jaWinDiff = -1f;
				float jaDrawDiff = -1f;
				float jaLoseDiff = -1f;
				if (jincai != null) {
					jcWinChange = jincai.getWinChange();
					jcDrawChange = jincai.getDrawChange();
					jcLoseChange = jincai.getLoseChange();
					
					jaWinDiff = MatchUtil.getEuDiff(jincai.getCurrentEuro().getEWin(), euroAvg.getEWin(), false);
					jaDrawDiff = MatchUtil.getEuDiff(jincai.getCurrentEuro().getEDraw(), euroAvg.getEDraw(), false);
					jaLoseDiff = MatchUtil.getEuDiff(jincai.getCurrentEuro().getELose(), euroAvg.getELose(), false);
				}

				// the main company and lab is higher than the average, or aomen is higher than average and aomen is adjusted high
				if (aomenPk >= 1) {
					if (isAomenMajor) {
						if (aaWinRt + aomenWinChange > 0.001
								&& jaWinDiff + jcWinChange > -0.05
								&& willWinChange > 0.001) {
							killGps.add(ResultGroup.Three);
						}
					} else {
						if (maWinRt > 0.062 && majorWinChange > -0.0251
								|| waWinRt > 0.052 && laWinRt > 0.067 && aomenWinChange > -0.011
								|| aaWinRt > 0.021 && aomenWinChange > 0.011) {
							killGps.add(ResultGroup.Three);
						}
						if (maDrawRt > 0.06 && majorDrawChange > -0.0251
								|| waDrawRt > 0.067 && laDrawRt > 0.05 && aomenDrawChange > -0.0251
								|| aomenDrawChange > 0.011 && aaDrawRt > 0.05) {
							killGps.add(ResultGroup.One);
						}
						if (maLoseRt > 0.06 && majorLoseChange > -0.0251
								|| waLoseRt > 0.06 && laLoseRt > 0.07 && aomenLoseChange > -0.0251
								|| aomenLoseChange > 0.011 && aaLoseRt > 0.05) {
							killGps.add(ResultGroup.Zero);
						}
					}
				} else if (aomenPk >= 0.4) {
					if (isAomenMajor) {
						if ((aaWinRt > 0.019 && aomenWinChange > -0.001 || jaWinDiff > -0.041 && jcWinChange > -0.001)
								&& willWinChange > 0.001) {
							killGps.add(ResultGroup.Three);
						}
						if (aaDrawRt > 0.019 && aomenDrawChange > -0.001 && jaDrawDiff + jcDrawChange > 0.011
								&& waDrawRt > 0.031) {
							killGps.add(ResultGroup.One);
						}
						if (aaLoseRt > 0.019 && aomenLoseChange > -0.001 && jaLoseDiff + jcLoseChange > 0.001
								&& laLoseRt > 0.031) {
							killGps.add(ResultGroup.Zero);
						}
					} else {
						if (maWinRt > 0.06 && majorWinChange > -0.0251
								|| waWinRt > 0.052 && laWinRt > 0.065 && aomenWinChange > -0.0251
								|| aomenWinChange > 0.011 && aaWinRt > 0.035) {
							killGps.add(ResultGroup.Three);
						}
						if (maDrawRt > 0.06 && majorDrawChange > -0.0251
								|| waDrawRt > 0.067 && laDrawRt > 0.05 && aomenDrawChange > -0.0251
								|| aomenDrawChange > 0.011 && aaDrawRt > 0.05) {
							killGps.add(ResultGroup.One);
						}
						if (maLoseRt > 0.06 && majorLoseChange > -0.0251
								|| waLoseRt > 0.052 && laLoseRt > 0.067 && aomenLoseChange > -0.0251
								|| aomenLoseChange > 0.011 && aaLoseRt > 0.05) {
							killGps.add(ResultGroup.Zero);
						}
					}
				} else if (aomenPk >= 0.25) {
					if (isAomenMajor) {
						if ((aaWinRt > 0.019 && aomenWinChange > -0.001 || jaWinDiff > -0.031 && jcWinChange > -0.001)
								&& laWinRt > 0.031) {
							killGps.add(ResultGroup.Three);
						}
						if (aaDrawRt > 0.019 && aomenDrawChange > -0.001 && jaDrawDiff + jcDrawChange > 0.011
								&& waDrawRt > 0.021) {
							killGps.add(ResultGroup.One);
						}
						if (aaLoseRt > 0.019 && aomenLoseChange > -0.001 && jaLoseDiff + jcLoseChange > -0.001
								&& laLoseRt > 0.031) {
							killGps.add(ResultGroup.Zero);
						}
					} else {
						if (maWinRt > 0.058 && majorWinChange > -0.0251
								|| waWinRt > 0.052 && laWinRt > 0.067 && aomenWinChange > -0.0251
								|| aomenWinChange > 0.011 && aaWinRt > 0.035) {
							killGps.add(ResultGroup.Three);
						}
						if (maDrawRt > 0.06 && majorDrawChange > -0.0251
								|| waDrawRt > 0.067 && laDrawRt > 0.05 && aomenDrawChange > -0.0251
								|| aomenDrawChange > 0.011 && aaDrawRt > 0.045) {
							killGps.add(ResultGroup.One);
						}
						if (maLoseRt > 0.06 && majorLoseChange > -0.0251
								|| waLoseRt > 0.052 && laLoseRt > 0.067 && aomenLoseChange > -0.0251
								|| aomenLoseChange > 0.011 && aaLoseRt > 0.045) {
							killGps.add(ResultGroup.Zero);
						}
					}
				} else if (aomenPk >= 0) {
					if (isAomenMajor) {
						if (aaWinRt > 0.019 && aomenWinChange > -0.001
								&& jaWinDiff + jcWinChange > -0.001
								&& willWinChange > 0.001) {
							killGps.add(ResultGroup.Three);
						}
						if (aaDrawRt > 0.019 && aomenDrawChange > -0.001
								&& jaDrawDiff + jcDrawChange > 0.001
								&& waDrawRt > 0.021) {
							killGps.add(ResultGroup.One);
						}
						if (aaLoseRt > 0.019 && aomenLoseChange > -0.001
								&& jaLoseDiff + jcLoseChange > -0.001
								&& laLoseRt > 0.031) {
							killGps.add(ResultGroup.Zero);
						}
					} else {
						if (maWinRt > 0.058 && majorWinChange > -0.0251
								|| waWinRt > 0.052 && laWinRt > 0.067 && aomenWinChange > -0.0261
								|| aomenWinChange > 0.011 && aaWinRt > 0.04) {
							killGps.add(ResultGroup.Three);
						}
						if (maDrawRt > 0.06 && majorDrawChange > -0.0251
								|| waDrawRt > 0.067 && laDrawRt > 0.05 && aomenDrawChange > -0.0251
								|| aomenDrawChange > 0.011 && aaDrawRt > 0.045) {
							killGps.add(ResultGroup.One);
						}
						if (maLoseRt > 0.06 && majorLoseChange > -0.0251
								|| waLoseRt > 0.052 && laLoseRt > 0.067 && aomenLoseChange > -0.0251
								|| aomenLoseChange > 0.011 && aaLoseRt > 0.04) {
							killGps.add(ResultGroup.Zero);
						}
					}
				} else if (aomenPk >= -0.25) {
					if (isAomenMajor) {
						if (aaWinRt > 0.019 && aomenWinChange > -0.001
								&& jaWinDiff + jcWinChange > -0.001
								&& laWinRt > 0.031) {
							killGps.add(ResultGroup.Three);
						}
						if (aaDrawRt > 0.019 && aomenDrawChange > -0.001
								&& jaDrawDiff + jcDrawChange > 0.011
								&& waDrawRt > 0.021) {
							killGps.add(ResultGroup.One);
						}
						if ((aaLoseRt > 0.019 && aomenLoseChange > -0.001 || jaLoseDiff > -0.031 && jcLoseChange > -0.001)
								&& laLoseRt > 0.031) {
							killGps.add(ResultGroup.Zero);
						}
					} else {
						if (maWinRt > 0.06 && majorWinChange > -0.0251
								|| waWinRt > 0.052 && laWinRt > 0.067 && aomenWinChange > -0.0251
								|| aomenWinChange > 0.011 && aaWinRt > 0.04) {
							killGps.add(ResultGroup.Three);
						}
						if (maDrawRt > 0.06 && majorDrawChange > -0.0251
								|| waDrawRt > 0.067 && laDrawRt > 0.05 && aomenDrawChange > -0.0251
								|| aomenDrawChange > 0.011 && aaDrawRt > 0.045) {
							killGps.add(ResultGroup.One);
						}
						if (maLoseRt > 0.06 && majorLoseChange > -0.0251
								|| waLoseRt > 0.052 && laLoseRt > 0.067 && aomenLoseChange > -0.0251
								|| aomenLoseChange > 0.011 && aaLoseRt > 0.035) {
							killGps.add(ResultGroup.Zero);
						}
					}
				} else if (aomenPk >= -1) {
					if (isAomenMajor) {
						if (aaWinRt > 0.019 && aomenWinChange > -0.001
								&& jaWinDiff + jcWinChange > -0.001
								&& laWinRt > 0.031) {
							killGps.add(ResultGroup.Three);
						}
						if (aaDrawRt > 0.019 && aomenDrawChange > -0.001
								&& jaDrawDiff + jcDrawChange > 0.001
								&& waDrawRt > 0.021) {
							killGps.add(ResultGroup.One);
						}
						if ((aaLoseRt > 0.019 && aomenLoseChange > -0.001 || jaLoseDiff > -0.031 && jcLoseChange > -0.001)
								&& laLoseRt > 0.031) {
							killGps.add(ResultGroup.Zero);
						}
					} else {
						if (maWinRt > 0.06 && majorWinChange > -0.0251
								|| waWinRt > 0.052 && laWinRt > 0.067 && aomenWinChange > -0.0251
								|| aomenWinChange > 0.011 && aaWinRt > 0.05) {
							killGps.add(ResultGroup.Three);
						}
						if (maDrawRt > 0.06 && majorDrawChange > -0.0251
								|| waDrawRt > 0.065 && laDrawRt > 0.05 && aomenDrawChange > -0.0251
								|| aomenDrawChange > 0.011 && aaDrawRt > 0.04) {
							killGps.add(ResultGroup.One);
						}
						if (maLoseRt > 0.06 && majorLoseChange > -0.0251
								|| waLoseRt > 0.052 && laLoseRt > 0.067 && aomenLoseChange > -0.0251
								|| aomenLoseChange > 0.011 && aaLoseRt > 0.035) {
							killGps.add(ResultGroup.Zero);
						}
					}
				} else {
					if (maWinRt > 0.06 && majorWinChange > -0.0251
							|| waWinRt > 0.052 && laWinRt > 0.067 && aomenWinChange > -0.0251
							|| aomenWinChange > 0.011 && aaWinRt > 0.05) {
						killGps.add(ResultGroup.Three);
					}
					if (maDrawRt > 0.06 && majorDrawChange > -0.0251
							|| waDrawRt > 0.065 && laDrawRt > 0.05 && aomenDrawChange > -0.0251
							|| aomenDrawChange > 0.011 && aaDrawRt > 0.045) {
						killGps.add(ResultGroup.One);
					}
					if (maLoseRt > 0.06 && majorLoseChange > -0.0251
							|| waLoseRt > 0.052 && laLoseRt > 0.067 && aomenLoseChange > -0.0251
							|| aomenLoseChange > 0.011 && aaLoseRt > 0.035) {
						killGps.add(ResultGroup.Zero);
					}
				}
			}
		}
	}

	// win is hot
	//  - jc increase, other increase or no change   --  no win
	//  - jc increase, other decrease                -- no win
	//  - jc decrease, other decrease and jc is very low           -- win
	//  - jc decrease, other increase                -- win
	private void killByExchange (OFNKillPromoteResult killPromoteResult,
			OFNCalculateResult calResult) {
		MatchExchangeData exchange = calResult.getExchanges();
		EuroMatrices euroMatrices = calResult.getEuroMatrices();
		
		if (exchange == null || euroMatrices == null) {
			return;
		}
		
		Map<Company, EuroMatrix> companyEus = euroMatrices.getCompanyEus();
		EuroMatrix jincaiMatrix = companyEus.get(Company.Jincai);
		EuroMatrix aomenMatrix = companyEus.get(Company.Aomen);
		Set<ResultGroup> exRes = killPromoteResult.getKillByExchange();

		if (exRes == null) {
			exRes = new TreeSet<ResultGroup> ();
			killPromoteResult.setKillByExchange(exRes);
		}

		if (exchange != null && jincaiMatrix != null && aomenMatrix != null) {
			EuroPl euroAvg = euroMatrices.getCurrEuroAvg();
			EuroPl euroJc = jincaiMatrix.getCurrentEuro();
			EuroPl euroAomen = aomenMatrix.getCurrentEuro();
			
			float jaWinDiff = MatchUtil.getEuDiff(euroJc.getEWin(), euroAvg.getEWin(), false);
			float jaDrawDiff = MatchUtil.getEuDiff(euroJc.getEDraw(), euroAvg.getEDraw(), false);
			float jaLoseDiff = MatchUtil.getEuDiff(euroJc.getELose(), euroAvg.getELose(), false);
			
			float aaWinDiff = MatchUtil.getEuDiff(euroAomen.getEWin(), euroAvg.getEWin(), false);
			float aaDrawDiff = MatchUtil.getEuDiff(euroAomen.getEDraw(), euroAvg.getEDraw(), false);
			float aaLoseDiff = MatchUtil.getEuDiff(euroAomen.getELose(), euroAvg.getELose(), false);
			
			Long jcTotal= exchange.getJcTotalExchange();
			Integer jcWinGain = exchange.getJcWinGain();
			Integer jcDrawGain = exchange.getJcDrawGain();
			Integer jcLoseGain = exchange.getJcLoseGain();
			
//			float jcWinChange = jincaiMatrix.getWinChange();
//			float jcDrawChange = jincaiMatrix.getDrawChange();
//			float jcLoseChange = jincaiMatrix.getLoseChange();

			if (jcTotal > 1000000) {
				// win is hot, and jc is not low, and aomen is not low
				if (jcWinGain < -60 && jaWinDiff > -0.035 && aaWinDiff > -0.018) {
					exRes.add(ResultGroup.Three);
				}
				
				if (jcDrawGain < -60 && jaDrawDiff > 0.015 && aaDrawDiff > -0.018) {
					exRes.add(ResultGroup.One);
				}
				
				if (jcLoseGain < -60 && jaLoseDiff > -0.035 && aaLoseDiff > -0.018) {
					exRes.add(ResultGroup.Zero);
				}
			} else if (jcTotal > 800000) {
				// win is hot, but win is not so good
				if (jcWinGain < -80 && jaWinDiff > -0.035 && aaWinDiff > -0.018) {
					exRes.add(ResultGroup.Three);
				}
				
				if (jcDrawGain < -80 && jaDrawDiff > 0.015 && aaDrawDiff > -0.018) {
					exRes.add(ResultGroup.One);
				}
				
				if (jcLoseGain < -80 && jaLoseDiff > -0.035 && aaLoseDiff > -0.018) {
					exRes.add(ResultGroup.Zero);
				}
			} else if (jcTotal > 500000) {
				// win is hot, but win is not so good
				if (jcWinGain < -100 && jaWinDiff > -0.035 && aaWinDiff > -0.018) {
					exRes.add(ResultGroup.Three);
				}
				
				if (jcDrawGain < -100 && jaDrawDiff > 0.015 && aaDrawDiff > -0.018) {
					exRes.add(ResultGroup.One);
				}
				
				if (jcLoseGain < -100 && jaLoseDiff > -0.035 && aaLoseDiff > -0.018) {
					exRes.add(ResultGroup.Zero);
				}
			}
		}
	}

	private Set<ResultGroup> promoteByBase (Set<ResultGroup> rgs, Float hotPoint, ClubMatrices clubMatrices,
			float currentPk, Float predictPk, AsiaPl main, AsiaPl current, MatchExchangeData exchange, EuroMatrices euroMatrices) {
		if (rgs == null) {
			rgs = new TreeSet<ResultGroup> ();
		}
		
		Map<Company, EuroMatrix> companyEus = euroMatrices.getCompanyEus();
		EuroMatrix jincaiMatrix = companyEus.get(Company.Jincai);

		float hostAttGuestDefComp = clubMatrices.getHostAttGuestDefInx();
		float guestAttHostDefComp = clubMatrices.getGuestAttHostDefInx();

		// host performed good lately
		if (hotPoint >= 6) {
			// host is so good --> win
			if (hostAttGuestDefComp > 1.6 * guestAttHostDefComp) {
				if (main.gethWin() < 0.92 && current.gethWin() <= main.gethWin()) {
					Boolean jcSupport = isJcExchangePromote(exchange, jincaiMatrix, ResultGroup.Three);;
					
					if (jcSupport == null || jcSupport) {
						rgs.add(ResultGroup.Three);
					}
				}
			}
		}
		// guest performed good lately
		else if (hotPoint <= -6) {
			// guest is so good
			if (guestAttHostDefComp >= 1.25 * hostAttGuestDefComp) {
				if (main.getaWin() < 0.92 && current.getaWin() <= main.getaWin()) {
					Boolean jcSupport = isJcExchangePromote(exchange, jincaiMatrix, ResultGroup.Zero);
					
					if (jcSupport == null || jcSupport) {
						rgs.add(ResultGroup.Zero);
					}
				}
			}
		} else {
			// host is good (not very good), and host didn't perform bad
			if (hostAttGuestDefComp < 1.6 * guestAttHostDefComp && hostAttGuestDefComp > 1.1 * guestAttHostDefComp && hotPoint >= -4) {
				// but the company think low of host -- lose
				if (currentPk < predictPk - 0.15 && main.getaWin() < 0.92 && current.getaWin() <= main.getaWin()) {
					rgs.add(ResultGroup.One);
					rgs.add(ResultGroup.Zero);
				}
			}
			// guest is good (not very good), and guest didn't perform bad 
			else if ((guestAttHostDefComp < 1.4 * hostAttGuestDefComp) && guestAttHostDefComp > 0.7 * hostAttGuestDefComp && hotPoint <= 4) {
				// but the company think low of guest -- win
				if (currentPk > predictPk + 0.15 && main.gethWin() < 0.92 && current.gethWin() <= main.gethWin()) {
					rgs.add(ResultGroup.Three);
					rgs.add(ResultGroup.One);
				}
			}
		}

		return rgs;
	}
	
	private Set<ResultGroup> promoteByPk (Set<ResultGroup> rgs, PankouMatrices pkMatrices, Float predictPk, Float hotPoint,
			MatchState matchState, ClubMatrices clubMatrices, MatchExchangeData exchange, EuroMatrices euroMatrices) {
		if (pkMatrices != null && predictPk != null && matchState != null && clubMatrices != null) {
			AsiaPl main = pkMatrices.getMainPk();
			AsiaPl current = pkMatrices.getCurrentPk();
			Float hWinChgRtFloat = pkMatrices.getHwinChangeRate();
			Float gWinChgRtFloat = pkMatrices.getAwinChangeRate();

			float mainPk = MatchUtil.getCalculatedPk(main);
			float currentPk = MatchUtil.getCalculatedPk(current);
			
			promoteByBase(rgs, hotPoint, clubMatrices,
					currentPk, predictPk, main, current, exchange, euroMatrices);
			
			LatestMatchMatrices host6Match = matchState.getHostState6();
			LatestMatchMatrices guest6Match = matchState.getGuestState6();
			
			Map<Company, EuroMatrix> companyEus = euroMatrices.getCompanyEus();
			EuroMatrix aomenEu = companyEus.get(Company.Aomen);
			
			if (host6Match == null || guest6Match == null) {
				return rgs;
			}

			List <TeamLabel> hostLabels = clubMatrices.getHostLabels();
//			List <TeamLabel> guestLabels = clubMatrices.getGuestLabels();
//			float hostAtt = clubMatrices.getHostAttGuestDefInx();
//			float guestAtt = clubMatrices.getGuestAttHostDefInx();
//			float hostGuestGoal = hostAtt / guestAtt;

			if (main.getPanKou() >= 1) {
				// a1. host pay is low
				if (hWinChgRtFloat < LOW_PK_POINT && current.gethWin() <= 0.92 && aomenEu.getWinChange() < 0.01) {
					// b1. company think high of the host
					if (mainPk - predictPk > 0.29) {
						// c1. host is good
						if ((host6Match.getWinPkRate() >= 0.5 || host6Match.getWinDrawPkRate() >= 0.8)) {
							rgs.add(ResultGroup.RangThree);
						}
					}
					// b2. host is think low, ignore...
					else if (predictPk - mainPk > 0.38) {
						// ignore...
					}
					// b2. the predicted and real is near
					else if (Math.abs(predictPk - mainPk) < 0.2) {
						// c2. host perform good, guest perform bad
						if ((host6Match.getWinPkRate() > 0.4 || host6Match.getWinDrawPkRate() > 0.5)
								&& (guest6Match.getWinPkRate() < 0.4 || guest6Match.getWinDrawPkRate() < 0.5)) {
							rgs.add(ResultGroup.RangThree);
						}
					}
				}
				// a2. guest pay is low
				else if (gWinChgRtFloat < LOW_PK_POINT && current.getaWin() <= 0.9 && aomenEu.getLoseChange() < 0.01) {
					// b2. company think low,
					if (currentPk <= mainPk && predictPk - mainPk > 0.3
							&& (guest6Match.getWinPkRate() > 0.4 || guest6Match.getWinDrawPkRate() > 0.6)) {
						rgs.add(ResultGroup.RangZero);
					}
				}
			} else if (main.getPanKou() > 0.4) {
				if (hWinChgRtFloat < LOW_PK_POINT && current.gethWin() <= 0.92 && aomenEu.getWinChange() < 0.01) {
					// company think high of the host
					if (mainPk - predictPk > 0.25) {
						// host is pretty good, or guest is pretty bad (this is not matter,
						// if guest is too bad, then the predict pk should be high, not align with the assumption), or host is good guest is bad,
						if ((host6Match.getWinRate() >= 0.6 && guest6Match.getWinRate() <= 0.5
								|| guest6Match.getWinDrawRate() <= 0.5
								|| ((host6Match.getWinRate() >= 0.5 && host6Match.getWinDrawPkRate() >= 0.6)
									|| (guest6Match.getWinRate() <= 0.5 && guest6Match.getWinDrawRate() <= 0.7)))) {
							rgs.add(ResultGroup.Three);
						}
					}
					else if (mainPk - predictPk > -0.08) {
						// host is good and stable, guest is not good
						if (host6Match.getWinRate() >= 0.5 && host6Match.getWinDrawRate() >= 0.6
								&& host6Match.getMatchGoal() > host6Match.getMatchMiss()
								&& isPkSupportHot(predictPk, mainPk, hotPoint)
								&& guest6Match.getWinPkRate() <= 0.5 && guest6Match.getWinDrawPkRate() <= 0.7) {
							rgs.add(ResultGroup.Three);
						}
						// pk is support, and host is good or guest is bad
						else if ((currentPk - mainPk >= 0.08 || current.gethWin() <= 0.9)
								&& isPkSupportHot(predictPk, mainPk, hotPoint)
								&& (host6Match.getWinRate() >= 0.5 || host6Match.getWinDrawRate() >= 0.6)
									&& (guest6Match.getWinRate() <= 0.5 && guest6Match.getWinDrawRate() <= 0.7)) {
							rgs.add(ResultGroup.Three);
						}
					} else {
						// think high of guest.. check below
					}
				}
				else if (gWinChgRtFloat < LOW_PK_POINT && current.getaWin() <= 0.9 && aomenEu.getLoseChange() < 0.01) {
					// company think high of the guest
					if (predictPk - mainPk > 0.22) {
						// guest is pretty good, or host is pretty bad, or guest is good guest is bad,
						if (!MatchUtil.isHostHomeStrong(hostLabels)
								&& (guest6Match.getWinPkRate() >= 0.6 && host6Match.getWinRate() <= 0.6
									|| host6Match.getWinDrawRate() <= 0.5
									|| (guest6Match.getWinPkRate() >= 0.5 && guest6Match.getWinDrawPkRate() >= 0.6)
										&& (host6Match.getWinRate() <= 0.5 && host6Match.getWinDrawRate() <= 0.7))) {
							rgs.add(ResultGroup.One);
							rgs.add(ResultGroup.Zero);
						}
					}
					else if (predictPk - mainPk > -0.12) {
						if (!MatchUtil.isHostHomeStrong(hostLabels) && isPkSupportHot(predictPk, mainPk, hotPoint)
								&& (mainPk - currentPk > 0.08 || current.getaWin() <= 0.88)
								&& ((guest6Match.getWinPkRate() >= 0.5 && guest6Match.getWinDrawPkRate() >= 0.6)
									|| (host6Match.getWinRate() <= 0.5 && host6Match.getWinDrawRate() <= 0.7))) {
							rgs.add(ResultGroup.One);
							rgs.add(ResultGroup.Zero);
						}
					}
				}
			} else if (main.getPanKou() > -0.4) {
				if (hWinChgRtFloat < LOW_PK_POINT && current.gethWin() <= 0.9 && aomenEu.getWinChange() < 0.01) {
					// company think high of the host
					if (mainPk - predictPk > 0.21) {
						// host is pretty good (the teams are in same level, check win pk rate, instead of win rate),
						//  or guest is pretty bad (still check the pk rate),
						if (host6Match.getWinPkRate() >= 0.6 && guest6Match.getWinRate() <= 0.6
								|| guest6Match.getWinDrawPkRate() <= 0.5
								|| (host6Match.getWinPkRate() >= 0.5 && host6Match.getWinDrawPkRate() >= 0.6)
									&& guest6Match.getWinPkRate() <= 0.5 && guest6Match.getWinDrawPkRate() <= 0.7) {
							rgs.add(ResultGroup.Three);
							rgs.add(ResultGroup.One);
						}
					} else if (mainPk - predictPk > -0.1) {
						// host performs good or guest performs bad
						if ((currentPk > mainPk || current.gethWin() <= 0.88)
								&& isPkSupportHot(predictPk, mainPk, hotPoint)
								&& ((host6Match.getWinPkRate() >= 0.5 && host6Match.getWinDrawPkRate() >= 0.6)
										&& (guest6Match.getWinPkRate() <= 0.5 && guest6Match.getWinDrawPkRate() <= 0.7))) {
							rgs.add(ResultGroup.Three);
							rgs.add(ResultGroup.One);
						}
					} else {
						// think high of guest.. check below
					}
				} else if (gWinChgRtFloat < LOW_PK_POINT && current.getaWin() <= 0.92 && aomenEu.getLoseChange() < 0.01) {
					// company think high of the guest
					if (predictPk - mainPk > 0.21) {
						if (guest6Match.getWinRate() >= 0.6 && host6Match.getWinRate() <= 0.6
								|| host6Match.getWinDrawPkRate() <= 0.5
								|| (guest6Match.getWinPkRate() >= 0.5 || guest6Match.getWinDrawPkRate() >= 0.6)
									&& host6Match.getWinPkRate() <= 0.5 && host6Match.getWinDrawPkRate() <= 0.7) {
							rgs.add(ResultGroup.One);
							rgs.add(ResultGroup.Zero);
						}
					}
					else if (predictPk - mainPk > -0.12) {
						// host performs good or guest performs bad
						if (mainPk >= currentPk && isPkSupportHot(predictPk, mainPk, hotPoint)
								&& ((guest6Match.getWinPkRate() >= 0.5 && guest6Match.getWinDrawPkRate() >= 0.6)
										&& (host6Match.getWinPkRate() <= 0.5 && host6Match.getWinDrawPkRate() <= 0.7))) {
							rgs.add(ResultGroup.One);
							rgs.add(ResultGroup.Zero);
						}
					}
				}
			} else if (main.getPanKou() > -1) {
				if (hWinChgRtFloat < LOW_PK_POINT && current.gethWin() <= 0.9 && aomenEu.getWinChange() < 0.01) {
					// company think high of the host
					if (mainPk - predictPk > 0.25) {
						// host is pretty good, or guest is pretty bad, or host is good guest is bad,
						if (host6Match.getWinPkRate() >= 0.6 && guest6Match.getWinPkRate() <= 0.6
								|| guest6Match.getWinDrawPkRate() <= 0.5
								|| (host6Match.getWinPkRate() >= 0.5 || host6Match.getWinDrawPkRate() >= 0.6)
									&& (guest6Match.getWinPkRate() <= 0.5 && guest6Match.getWinDrawPkRate() <= 0.7)) {
							rgs.add(ResultGroup.Three);
							rgs.add(ResultGroup.One);
						}
					}
					else if (Math.abs(predictPk - mainPk) < 0.19) {
						// host performs good or guest performs bad
						if (currentPk >= mainPk && isPkSupportHot(predictPk, mainPk, hotPoint)
								&& ((host6Match.getWinPkRate() >= 0.5 && host6Match.getWinDrawPkRate() >= 0.6)
										&& (guest6Match.getWinPkRate() <= 0.5 && guest6Match.getWinDrawPkRate() <= 0.7))) {
							rgs.add(ResultGroup.Three);
							rgs.add(ResultGroup.One);
						}
					} else {
						// think high of guest.. check below
					}
				}
				else if (gWinChgRtFloat < LOW_PK_POINT && current.getaWin() <= 0.92 && aomenEu.getLoseChange() < 0.01) {
					// company think high of the guest
					if (predictPk - mainPk > 0.25) {
						// guest is pretty good, or host is pretty bad, or guest is good guest is bad,
						if ((guest6Match.getWinRate() >= 0.8
									|| host6Match.getWinDrawPkRate() <= 0.5
									|| (guest6Match.getWinRate() >= 0.5 || guest6Match.getWinDrawPkRate() >= 0.6)
										&& (host6Match.getWinPkRate() <= 0.5 && host6Match.getWinDrawPkRate() <= 0.7))) {
							rgs.add(ResultGroup.Zero);
						}
					}
					else if (predictPk - mainPk > -0.1) {
						if (currentPk <= mainPk && isPkSupportHot(predictPk, mainPk, hotPoint)
								&& ((guest6Match.getWinRate() >= 0.5 || guest6Match.getWinDrawPkRate() >= 0.6)
										&& (host6Match.getWinPkRate() <= 0.5 && host6Match.getWinDrawPkRate() <= 0.7))) {
							rgs.add(ResultGroup.Zero);
						}
					}
				}
			} else {
				if (hWinChgRtFloat < LOW_PK_POINT && current.gethWin() <= 0.9 && aomenEu.getWinChange() < 0.01) {
					if (currentPk >= mainPk && mainPk - predictPk > 0.3
							&& (host6Match.getWinPkRate() >= 0.5 || host6Match.getWinDrawPkRate() >= 0.6)) {
						rgs.add(ResultGroup.Three);
						rgs.add(ResultGroup.One);
					}
				}
				// a2. guest pay is low
				else if (gWinChgRtFloat < LOW_PK_POINT && current.getaWin() <= 0.92 && aomenEu.getLoseChange() < 0.01) {
					// b1. company think high of the guest
					if (predictPk - mainPk > 0.3) {
						if ((guest6Match.getWinPkRate() >= 0.5 && guest6Match.getWinDrawPkRate() >= 0.7)) {
							rgs.add(ResultGroup.Zero);
						}
					}
					// b2. guest is think low, ignore...
					else if (mainPk - predictPk > 0.38) {
						// ignore...
					}
					// b2. the predicted and real is near
					else if (Math.abs(predictPk - mainPk) < 0.2) {
						// c2. guest perform good, host perform bad
						if ((guest6Match.getWinPkRate() >= 0.5 || guest6Match.getWinDrawPkRate() >= 0.6)
								&& (host6Match.getWinPkRate() <= 0.5 && host6Match.getWinDrawPkRate() <= 0.7)) {
							rgs.add(ResultGroup.Zero);
						}
					}
				}
			}
		}

		return rgs;
	}
	
	private void promoteByEuro (Set<ResultGroup> promoteGps, EuroMatrices euMatrices, Float predictPkRt, PankouMatrices pkMatrices, League league) {

		if (euMatrices != null) {
			Map<Company, EuroMatrix> companyEus = euMatrices.getCompanyEus();
			EuroMatrix will = companyEus.get(Company.William);
			EuroMatrix lab = companyEus.get(Company.Ladbrokes);
			EuroMatrix aomen = companyEus.get(Company.Aomen);
			EuroMatrix jincai = companyEus.get(Company.Jincai);
			EuroPl euroAvg = euMatrices.getCurrEuroAvg();
			EuroMatrix majorComp = EuroUtil.getHighPaidEuro(euMatrices, league);
			AsiaPl aomenCurrPk = pkMatrices.getCurrentPk();
			AsiaPl aomenMainPk = pkMatrices.getMainPk();
			float currPk = aomenCurrPk.getPanKou();
			float mainPkRt = MatchUtil.getCalculatedPk(aomenMainPk);

			boolean isAomenMajor = EuroUtil.isAomenTheMajor(league);

			if (euroAvg != null && lab != null && majorComp != null && aomen != null) {
				EuroPl currLabEu = lab.getCurrentEuro();
				EuroPl currMajorEu = majorComp.getCurrentEuro();
				EuroPl currAomenEu = aomen.getCurrentEuro();
				EuroPl currWillEu = will.getCurrentEuro();
				float majorWinChange = majorComp.getWinChange();
				float majorDrawChange = majorComp.getDrawChange();
				float majorLoseChange = majorComp.getLoseChange();
				
				float jcWinChange = -1f;
				float jcDrawChange = -1f;
				float jcLoseChange = -1f;
				float jaWinDiff = -1f;
				float jaDrawDiff = -1f;
				float jaLoseDiff = -1f;
				if (jincai != null) {
					jcWinChange = jincai.getWinChange();
					jcDrawChange = jincai.getDrawChange();
					jcLoseChange = jincai.getLoseChange();
					
					jaWinDiff = MatchUtil.getEuDiff(jincai.getCurrentEuro().getEWin(), euroAvg.getEWin(), false);
					jaDrawDiff = MatchUtil.getEuDiff(jincai.getCurrentEuro().getEDraw(), euroAvg.getEDraw(), false);
					jaLoseDiff = MatchUtil.getEuDiff(jincai.getCurrentEuro().getELose(), euroAvg.getELose(), false);
				}
				
				
				float aomenWinChange = aomen.getWinChange();
				float aomenDrawChange = aomen.getDrawChange();
				float aomenLoseChange = aomen.getLoseChange();

				float laWinRt = MatchUtil.getEuDiff(currLabEu.getEWin(), euroAvg.getEWin(), false);
//				float laDrawRt = MatchUtil.getEuDiff(currLabEu.getEDraw(), euroAvg.getEDraw(), false);
				float laLoseRt = MatchUtil.getEuDiff(currLabEu.getELose(), euroAvg.getELose(), false);

				float maWinRt = MatchUtil.getEuDiff(currMajorEu.getEWin(), euroAvg.getEWin(), false);
				float maDrawRt = MatchUtil.getEuDiff(currMajorEu.getEDraw(), euroAvg.getEDraw(), false);
				float maLoseRt = MatchUtil.getEuDiff(currMajorEu.getELose(), euroAvg.getELose(), false);
				
				float aaWinRt = MatchUtil.getEuDiff(currAomenEu.getEWin(), euroAvg.getEWin(), false);
				float aaDrawRt = MatchUtil.getEuDiff(currAomenEu.getEDraw(), euroAvg.getEDraw(), false);
				float aaLoseRt = MatchUtil.getEuDiff(currAomenEu.getELose(), euroAvg.getELose(), false);

//				float waWinRt = MatchUtil.getEuDiff(currWillEu.getEWin(), euroAvg.getEWin(), false);
				float waDrawRt = MatchUtil.getEuDiff(currWillEu.getEDraw(), euroAvg.getEDraw(), false);
//				float waLoseRt = MatchUtil.getEuDiff(currWillEu.getELose(), euroAvg.getELose(), false);

				// the main company and lab is higher than the average, or aomen is higher than average and aomen is adjusted high
				// TODO
				if (currPk >= 1) {
					if (isAomenMajor) {
						if (aaWinRt < -0.001 && aomenWinChange < 0.001
								&& jaWinDiff < -0.075 && jcWinChange < 0.001
								&& laWinRt < 0.041
								&& (aomenCurrPk.gethWin() <= 0.92 && aomenCurrPk.gethWin() <= aomenMainPk.gethWin()
									|| aaWinRt + aomenWinChange < -0.035
									|| jcWinChange < -0.02)) {
							promoteGps.add(ResultGroup.Three);
						}
						if (aaDrawRt < 0.001 && aomenDrawChange < -0.001
								&& jaDrawDiff < -0.045 && jcDrawChange < -0.001
								&& waDrawRt < 0.031
								&& (mainPkRt - predictPkRt < -0.375
									|| aomenCurrPk.getaWin() < 0.9 && aomenCurrPk.getaWin() < aomenMainPk.getaWin()
									|| aaDrawRt + aomenDrawChange < -0.045
									|| jaDrawDiff + jcDrawChange < -0.065)) {
							promoteGps.add(ResultGroup.One);
						}
						if (aaLoseRt < -0.051 && aomenLoseChange < -0.001
								&& jaLoseDiff < -0.055 && jcLoseChange < -0.001
								&& laLoseRt < 0.041
								&& (mainPkRt - predictPkRt < -0.375
									|| aomenCurrPk.getaWin() < 0.9 && aomenCurrPk.getaWin() < aomenMainPk.getaWin()
									|| aaLoseRt + aomenLoseChange < -0.085
									|| jaLoseDiff + jcLoseChange < -0.101)) {
							promoteGps.add(ResultGroup.Zero);
						}
					} else {
						if (aomenCurrPk.gethWin() <= 0.92 && aomenCurrPk.gethWin() <= aomenMainPk.gethWin() 
								&& maWinRt + majorWinChange < 0.031
								&& laWinRt < 0.031
								&& aaWinRt + aomenWinChange < 0.001
								&& majorWinChange < 0.001
								&& jcWinChange < 0.001
								|| maWinRt + majorWinChange < -0.051 && maWinRt < -0.031 && majorWinChange < 0.001) {
							promoteGps.add(ResultGroup.Three);
						}
						if (mainPkRt - predictPkRt < -0.21 && aomenCurrPk.getaWin() < 0.9 && aomenCurrPk.getaWin() <= aomenMainPk.getaWin()
								&& (maDrawRt + majorDrawChange < -0.055
									&& majorDrawChange < -0.001
									&& waDrawRt < -0.001
									&& aomenDrawChange + aaDrawRt < -0.055
									&& jcDrawChange < -0.001)) {
							promoteGps.add(ResultGroup.One);
						}
						if (mainPkRt - predictPkRt < -0.21 && aomenCurrPk.getaWin() < 0.9 && aomenCurrPk.getaWin() <= aomenMainPk.getaWin()
								&& (maLoseRt + majorLoseChange < -0.055
									&& majorLoseChange < -0.001
									&& laLoseRt < 0.021
									&& aomenLoseChange + aaLoseRt < -0.055
									&& jcLoseChange < -0.001)) {
							promoteGps.add(ResultGroup.Zero);
						}
					}
				} else if (currPk >= 0.4) {
					if (isAomenMajor) {
						if (aaWinRt < -0.001 && aomenWinChange < 0.001
								&& jaWinDiff < -0.075 && jcWinChange < 0.001
								&& laWinRt < 0.041
								&& (aomenCurrPk.gethWin() <= 0.92 && aomenCurrPk.gethWin() <= aomenMainPk.gethWin()
									|| aaWinRt + aomenWinChange < -0.035
									|| jcWinChange < -0.02)) {
							promoteGps.add(ResultGroup.Three);
						}
						if (aaDrawRt < 0.001 && aomenDrawChange < -0.001
								&& jaDrawDiff < -0.03 && jcDrawChange < -0.001
								&& waDrawRt < 0.031
								&& (mainPkRt - predictPkRt < -0.375
									|| aomenCurrPk.getaWin() < 0.9 && aomenCurrPk.getaWin() < aomenMainPk.getaWin()
									|| aaDrawRt + aomenDrawChange < -0.045
									|| jaDrawDiff + jcDrawChange < -0.065)) {
							promoteGps.add(ResultGroup.One);
						}
						if (aaLoseRt < -0.04 && aomenLoseChange < -0.001
								&& jaLoseDiff < -0.055 && jcLoseChange < -0.001
								&& laLoseRt < 0.041
								&& (mainPkRt - predictPkRt < -0.375
									|| aomenCurrPk.getaWin() < 0.9 && aomenCurrPk.getaWin() < aomenMainPk.getaWin()
									|| aaLoseRt + aomenLoseChange < -0.075
									|| jaLoseDiff + jcLoseChange < -0.101)) {
							promoteGps.add(ResultGroup.Zero);
						}
					} else {
						if (mainPkRt - predictPkRt > -0.19 && aomenCurrPk.gethWin() < 0.92 && aomenCurrPk.gethWin() <= aomenMainPk.gethWin()
								&& (maWinRt + majorWinChange < 0.031
									&& laWinRt < 0.031
									&& aaWinRt + aomenWinChange < 0.001
									&& jcWinChange < 0.001
									&& majorWinChange < 0.001
									|| maWinRt + majorWinChange < -0.051 && maWinRt < -0.031 && majorWinChange < 0.001)) {
							promoteGps.add(ResultGroup.Three);
						}
						if (mainPkRt - predictPkRt < 0.21 && aomenCurrPk.getaWin() <= 0.92 && aomenCurrPk.getaWin() <= aomenMainPk.getaWin()
								&& (maDrawRt + majorDrawChange < -0.0451
									&& waDrawRt < 0.001
									&& aomenDrawChange + aaDrawRt < -0.045
									&& jcDrawChange < -0.001
									&& majorDrawChange < -0.001)) {
							promoteGps.add(ResultGroup.One);
						}
						if (mainPkRt - predictPkRt < 0.21 && aomenCurrPk.getaWin() <= 0.9 && aomenCurrPk.getaWin() <= aomenMainPk.getaWin()
								&& (maLoseRt + majorLoseChange < -0.045
									&& laLoseRt < 0.031
									&& aomenLoseChange + aaLoseRt < -0.045
									&& jcLoseChange < -0.001
									&& majorLoseChange < -0.001)) {
							promoteGps.add(ResultGroup.Zero);
						}
					}
				} else if (currPk >= 0.25) {
					if (isAomenMajor) {
						if (aaWinRt < -0.001 && aomenWinChange < 0.001
								&& jaWinDiff < -0.075 && jcWinChange < 0.001
								&& laWinRt < 0.041
								&& (aomenCurrPk.gethWin() <= 0.92 && aomenCurrPk.gethWin() <= aomenMainPk.gethWin()
									|| aaWinRt + aomenWinChange < -0.035
									|| jcWinChange < -0.02)) {
							promoteGps.add(ResultGroup.Three);
						}
						if (aaDrawRt < -0.001 && aomenDrawChange < 0.001
								&& jaDrawDiff < -0.03 && jcDrawChange < -0.001
								&& waDrawRt < 0.031
								&& (mainPkRt - predictPkRt < -0.375
									|| aomenCurrPk.getaWin() < 0.9 && aomenCurrPk.getaWin() < aomenMainPk.getaWin()
									|| aaDrawRt + aomenDrawChange < -0.045
									|| jaDrawDiff + jcDrawChange < -0.065)) {
							promoteGps.add(ResultGroup.One);
						}
						if (aaLoseRt < -0.001 && aomenLoseChange < 0.001
								&& jaLoseDiff < -0.055 && jcLoseChange < -0.001
								&& laLoseRt < 0.041
								&& (mainPkRt - predictPkRt < -0.375
									|| aomenCurrPk.getaWin() < 0.9 && aomenCurrPk.getaWin() < aomenMainPk.getaWin()
									|| aaLoseRt + aomenLoseChange < -0.065
									|| jaLoseDiff + jcLoseChange < -0.081)) {
							promoteGps.add(ResultGroup.Zero);
						}
					} else {
						if (mainPkRt - predictPkRt > -0.16 && aomenCurrPk.gethWin() < 0.92 && aomenCurrPk.gethWin() <= aomenMainPk.gethWin()
								&& (maWinRt + majorWinChange < 0.031
									&& laWinRt < 0.031
									&& aaWinRt + aomenWinChange < 0.001
									&& jcWinChange < 0.001
									&& majorWinChange < 0.001
									|| maWinRt + majorWinChange < -0.051 && maWinRt < -0.031 && majorWinChange < 0.001)) {
							promoteGps.add(ResultGroup.Three);
						}
						if (mainPkRt - predictPkRt < 0.21 && aomenCurrPk.getaWin() <= 0.92 && aomenCurrPk.getaWin() <= aomenMainPk.getaWin()
								&& (maDrawRt + majorDrawChange < -0.0251
									&& waDrawRt < -0.001
									&& jcDrawChange < -0.001
									&& aomenDrawChange + aaDrawRt < -0.0251)
									&& majorDrawChange < -0.001) {
							promoteGps.add(ResultGroup.One);
						}
						if (mainPkRt - predictPkRt < 0.21 && aomenCurrPk.getaWin() <= 0.91 && aomenCurrPk.getaWin() <= aomenMainPk.getaWin()
								&& (maLoseRt + majorLoseChange < -0.021
									&& laLoseRt < 0.031
									&& aomenLoseChange + aaLoseRt < -0.031
									&& jcLoseChange < -0.001
									&& majorLoseChange < -0.001)) {
							promoteGps.add(ResultGroup.Zero);
						}
					}
				} else if (currPk >= 0) {
					if (mainPkRt - predictPkRt > -0.16 && aomenCurrPk.gethWin() < 0.9 && aomenCurrPk.gethWin() <= aomenMainPk.gethWin()
							&& (maWinRt + majorWinChange < 0.001
								&& laWinRt < 0.021
								&& aaWinRt + aomenWinChange < 0.001
								&& jcWinChange < 0.001
								&& (isAomenMajor ? aomenWinChange < -0.001 : majorWinChange < -0.001)
								|| maWinRt + majorWinChange < -0.051 && maWinRt < -0.031 && majorWinChange < 0.011)) {
						promoteGps.add(ResultGroup.Three);
					}
					
					// no draw
					
					if (mainPkRt - predictPkRt < 0.16 && aomenCurrPk.getaWin() <= 0.92 && aomenCurrPk.getaWin() <= aomenMainPk.getaWin()
							&& (maLoseRt + majorLoseChange < 0.001
								&& laLoseRt < 0.031
								&& aomenLoseChange + aaLoseRt < 0.001
								&& jcLoseChange < 0.001
								&& (isAomenMajor ? aomenLoseChange < -0.001 : majorLoseChange < -0.001)
								|| maLoseRt + majorLoseChange < -0.051 && maLoseRt < -0.031 && majorLoseChange < 0.011)) {
						promoteGps.add(ResultGroup.Zero);
					}
				} else if (currPk >= -0.25) {
					if (isAomenMajor) {
						if (aaWinRt < -0.001 && aomenWinChange < -0.001
								&& jaWinDiff < -0.055 && jcWinChange < 0.001
								&& laWinRt < 0.041
								&& (mainPkRt - predictPkRt > 0.375
									|| aomenCurrPk.gethWin() < 0.9 && aomenCurrPk.gethWin() < aomenMainPk.gethWin()
									|| aaWinRt + aomenWinChange < -0.065
									|| jaWinDiff + jcWinChange < -0.085)) {
							promoteGps.add(ResultGroup.Three);
						}
						if (aaDrawRt < -0.001 && aomenDrawChange < -0.001
								&& jaDrawDiff < -0.03 && jcDrawChange < -0.001
								&& waDrawRt < 0.031
								&& (mainPkRt - predictPkRt > 0.375
									|| aomenCurrPk.getaWin() < 0.9 && aomenCurrPk.getaWin() < aomenMainPk.getaWin()
									|| aaDrawRt + aomenDrawChange < -0.045
									|| jaDrawDiff + jcDrawChange < -0.065)) {
							promoteGps.add(ResultGroup.One);
						}
						if (aaLoseRt < -0.001 && aomenLoseChange < 0.001
								&& jaLoseDiff < -0.075 && jcLoseChange < -0.001
								&& laLoseRt < 0.041
								&& (aomenCurrPk.getaWin() < 0.9 && aomenCurrPk.getaWin() <= aomenMainPk.getaWin()
									|| aaLoseRt + aomenLoseChange < -0.045
									|| jcLoseChange < -0.02)) {
							promoteGps.add(ResultGroup.Zero);
						}
					} else {
						if (mainPkRt - predictPkRt > -0.15 && aomenCurrPk.gethWin() < 0.9 && aomenCurrPk.gethWin() <= aomenMainPk.gethWin()
								&& (maWinRt + majorWinChange < -0.021
									&& laWinRt < 0.031
									&& aaWinRt + aomenWinChange < -0.021
									&& jcWinChange < -0.001
									&& (isAomenMajor ? aomenWinChange < -0.001 : majorWinChange < -0.001))) {
							promoteGps.add(ResultGroup.Three);
						}
						if (mainPkRt - predictPkRt > -0.15 && aomenCurrPk.gethWin() <= 0.92 && aomenCurrPk.gethWin() <= aomenMainPk.gethWin()
								&& (maDrawRt + majorDrawChange < -0.0251
									&& waDrawRt < -0.001
									&& aomenDrawChange + aaDrawRt < -0.001
									&& jcDrawChange < -0.001
									&& (isAomenMajor ? aomenDrawChange < -0.001 : majorDrawChange < -0.001))) {
							promoteGps.add(ResultGroup.One);
						}
						if (mainPkRt - predictPkRt < 0.21 && aomenCurrPk.getaWin() <= 0.92 && aomenCurrPk.getaWin() <= aomenMainPk.getaWin()
								&& (maLoseRt + majorLoseChange < 0.031
									&& laLoseRt < 0.031
									&& aomenLoseChange + aaLoseRt < 0.001
									&& jcLoseChange < 0.001
									&& (isAomenMajor ? aomenLoseChange < -0.001 : majorLoseChange < 0.001)
									|| maLoseRt + majorLoseChange < -0.051 && maLoseRt < -0.031 && majorLoseChange < 0.001)) {
							promoteGps.add(ResultGroup.Zero);
						}
					}
				} else if (currPk >= -1) {
					if (isAomenMajor) {
						if (aaWinRt < -0.001 && aomenWinChange < -0.001
								&& jaWinDiff < -0.055 && jcWinChange < 0.001
								&& laWinRt < 0.041
								&& (mainPkRt - predictPkRt > 0.375
									|| aomenCurrPk.gethWin() < 0.9 && aomenCurrPk.gethWin() < aomenMainPk.gethWin()
									|| aaWinRt + aomenWinChange < -0.75
									|| jaWinDiff + jcWinChange < -0.085)) {
							promoteGps.add(ResultGroup.Three);
						}
						if (aaDrawRt < -0.001 && aomenDrawChange < -0.001
								&& jaDrawDiff < -0.03 && jcDrawChange < -0.001
								&& waDrawRt < 0.031
								&& (mainPkRt - predictPkRt > 0.375
									|| aomenCurrPk.getaWin() < 0.9 && aomenCurrPk.getaWin() < aomenMainPk.getaWin()
									|| aaDrawRt + aomenDrawChange < -0.045
									|| jaDrawDiff + jcDrawChange < -0.065)) {
							promoteGps.add(ResultGroup.One);
						}
						if (aaLoseRt < -0.001 && aomenLoseChange < 0.001
								&& jaLoseDiff < -0.075 && jcLoseChange < -0.001
								&& laLoseRt < 0.041
								&& (aomenCurrPk.getaWin() < 0.9 && aomenCurrPk.getaWin() <= aomenMainPk.getaWin()
									|| aaLoseRt + aomenLoseChange < -0.045
									|| jcLoseChange < -0.02)) {
							promoteGps.add(ResultGroup.Zero);
						}
					} else {
						if (mainPkRt - predictPkRt > -0.15 && aomenCurrPk.gethWin() < 0.9 && aomenCurrPk.gethWin() <= aomenMainPk.gethWin()
								&& (maWinRt + majorWinChange < -0.045
									&& laWinRt < 0.031
									&& aaWinRt + aomenWinChange < -0.045
									&& jcWinChange < -0.001
									&& (isAomenMajor ? aomenWinChange < -0.001 : majorWinChange < -0.001))) {
							promoteGps.add(ResultGroup.Three);
						}
						if (mainPkRt - predictPkRt > -0.15 && aomenCurrPk.gethWin() <= 0.9 && aomenCurrPk.gethWin() <= aomenMainPk.gethWin()
								&& (maDrawRt + majorDrawChange < -0.045
									&& waDrawRt < -0.001
									&& aomenDrawChange + aaDrawRt < -0.045
									&& jcDrawChange < -0.001
									&& (isAomenMajor ? aomenDrawChange < -0.001 : majorDrawChange < -0.001))) {
							promoteGps.add(ResultGroup.One);
						}
						if (mainPkRt - predictPkRt < 0.21 && aomenCurrPk.getaWin() <= 0.92 && aomenCurrPk.getaWin() <= aomenMainPk.getaWin()
								&& (maLoseRt + majorLoseChange < 0.031
									&& laLoseRt < 0.031
									&& aomenLoseChange + aaLoseRt < 0.001
									&& jcLoseChange < 0.001
									&& (isAomenMajor ? aomenLoseChange < -0.001 : majorLoseChange < 0.001)
									|| maLoseRt + majorLoseChange < -0.051 && maLoseRt < -0.031 && majorLoseChange < 0.001)) {
							promoteGps.add(ResultGroup.Zero);
						}
					}
				} else {
					if (aomenCurrPk.gethWin() < 0.9 && aomenCurrPk.gethWin() <= aomenMainPk.gethWin()
							&& maWinRt + majorWinChange < -0.055
							&& laWinRt < 0.031
							&& aaWinRt + aomenWinChange < -0.055
							&& jcWinChange < -0.001
							&& (isAomenMajor ? aomenWinChange < -0.001 : majorWinChange < -0.001)) {
						promoteGps.add(ResultGroup.Three);
					}
					if (aomenCurrPk.gethWin() < 0.9 && aomenCurrPk.gethWin() <= aomenMainPk.gethWin()
							&& maDrawRt + majorDrawChange < -0.055
							&& waDrawRt < -0.001
							&& aomenDrawChange + aaDrawRt < -0.055
							&& jcDrawChange < -0.001
							&& (isAomenMajor ? aomenDrawChange < -0.001 : majorDrawChange < -0.001)) {
						promoteGps.add(ResultGroup.One);
					}
					if (aomenCurrPk.getaWin() <= 0.92 && aomenCurrPk.getaWin() <= aomenMainPk.getaWin()
							&& maLoseRt + majorLoseChange < 0.031
							&& laLoseRt < 0.031
							&& aomenLoseChange + aaLoseRt < 0.001
							&& jcLoseChange < 0.001
							|| maLoseRt + majorLoseChange < -0.045 && maLoseRt < -0.031 && majorLoseChange < 0.001) {
						promoteGps.add(ResultGroup.Zero);
					}
				}
			}
		}
	}
	
	private boolean isPkSupportHot (Float predictPk, Float mainPk, Float hotPoint) {
		if (hotPoint >= 5 && mainPk < predictPk - 0.022 * hotPoint) {
			return false;
		} else if (hotPoint <= -5 && mainPk > predictPk - 0.022 * hotPoint) {
			return false;
		} else {
			return true;
		}
	}
	
	private Boolean isJcExchangePromote (MatchExchangeData exchange, EuroMatrix jinCaiMatrix, ResultGroup rg) {
		if (exchange != null && exchange.getJcWinExchange() != null && jinCaiMatrix != null) {
			Long jcTotal= exchange.getJcTotalExchange();
			Integer jcWinGain = exchange.getJcWinGain();
			Integer jcDrawGain = exchange.getJcDrawGain();
			Integer jcLoseGain = exchange.getJcLoseGain();
			
			switch (rg) {
				case RangThree:
				case Three:
					if (jinCaiMatrix.getWinChange() < 0.021 && isExchangePromoted(jcTotal, jcWinGain)) {
						return true;
					}
					break;
				case One:
					if (jinCaiMatrix.getDrawChange() < 0.021 && isExchangePromoted(jcTotal, jcDrawGain)) {
						return true;
					}
					break;
				case RangZero:
				case Zero:
					if (jinCaiMatrix.getLoseChange() < 0.021 && isExchangePromoted(jcTotal, jcLoseGain)) {
						return true;
					}
					break;
			}

			return false;
		}

		return null;
	}
	
	private boolean isExchangePromoted (Long jcTotal, Integer jcGain) {
		if (jcTotal > 500000 && jcGain > -30) {
			return true;
		} else if (jcTotal > 800000 && jcGain > -25) {
			return true;
		} else if (jcTotal > 1000000 && jcGain > -20) {
			return true;
		}
		
		return false;
	}
}
