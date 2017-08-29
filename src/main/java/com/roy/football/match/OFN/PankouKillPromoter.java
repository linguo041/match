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
		Set<ResultGroup> plPkUnmatchKillGroups = new TreeSet<ResultGroup> ();
		Set<ResultGroup> promoteGroups = new TreeSet<ResultGroup> ();
		killPromoteResult.setPromoteByPk(promoteGroups);
		killPromoteResult.setKillByPk(pkKillGroups);
		killPromoteResult.setKillByPl(plKillGroups);
		killPromoteResult.setKillByPlPkUnmatch(plPkUnmatchKillGroups);

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
		/*<<<<  0.3*base + 0.45*latest + 0.25*jiaoshou  >>>> */
		ClubMatrices clubMatrices = calResult.getClubMatrices();

		if (clubMatrices != null) {
			hgoal = clubMatrices.getHostAttGuestDefInx();
			ggoal = clubMatrices.getGuestAttHostDefInx();
		}

		// latest Match
		MatchState matchState = calResult.getMatchState();

		if (matchState != null && matchState.getHostAttackToGuest() != null) {
			float lhgoal = matchState.getHostAttackToGuest();
			float lggoal = matchState.getGuestAttackToHost();

			if (hgoal == 0 && ggoal == 0) {
				hgoal = lhgoal;
				ggoal = lggoal;
			} else {
				hgoal = 0.4f * hgoal + 0.6f * lhgoal;
				ggoal = 0.4f * ggoal + 0.6f * lggoal;
			}

			hvariation = matchState.getHostAttackVariationToGuest();
			gvariation = matchState.getGuestAttackVariationToHost();
		}
		
		JiaoShouMatrices jiaoShou = calResult.getJiaoShou();
		if (jiaoShou != null && jiaoShou.getMatchNum() > 3) {
			float jsHgoal = jiaoShou.getHgoalPerMatch();
			float jsGgoal = jiaoShou.getGgoalPerMatch();
			
			// Multiple 0.5 to avoid duplicate adding weight
			hgoal = 0.75f * hgoal + 0.25f * jsHgoal;
			ggoal = 0.75f * ggoal + 0.25f * jsGgoal;
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
		float bhgoal = hgoal;
		float bggoal = ggoal;

		PankouMatrices pkMatrices = calResult.getPkMatrices();
		if (pkMatrices != null && predictPk != null) {
			// adjust the predict goals according to the company's original pk
			// 	expected = predict(base) refers to standard (company's original)
			AsiaPl origPkpl = pkMatrices.getOriginPk();
			float origPk = MatchUtil.getCalculatedPk(origPkpl);
			float predictDiff = 0.5f * (predictPk - origPk);
			hgoal -= predictDiff;
			ggoal += predictDiff;
			
			bhgoal = hgoal;
			bggoal = ggoal;

			// agjust the predicted goals according to the changes
			float pkComp = pkMatrices.getHwinChangeRate() - pkMatrices.getAwinChangeRate();
			hAdjRate = -1 * pkComp;
			gAdjRate = pkComp;
			
			hgoal += hvariation * hAdjRate;
			ggoal += gvariation * gAdjRate;
		}

		// daxiao
		DaxiaoMatrices dxMatrices = calResult.getDxMatrices();
		float dxWeight = 0;
		if (dxMatrices != null && dxMatrices.getHours() > 0) {
			// adjust total goals refers to the standard (company's pk) 
			float dxPk = MatchUtil.getCalculatedPk(dxMatrices.getOriginPk());
			float predictDxPk = bhgoal+bggoal;
			float predictDxDiff = 0.5f * (predictDxPk - dxPk);
			hgoal -= predictDxDiff;
			ggoal -= predictDxDiff;
			
			dxWeight = dxMatrices.getXiaoChangeRate();
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

			MatchState matchState = calResult.getMatchState();
			League league = calResult.getLeague();

			if (pkMatrices != null) {
				ClubMatrices clubMatrices = calResult.getClubMatrices();

				if (clubMatrices != null) {
					List <TeamLabel> hostLabels = clubMatrices.getHostLabels();
					List <TeamLabel> guestLabels = clubMatrices.getGuestLabels();

					Set<ResultGroup> pkRes = killByPk(pkMatrices, matchState, predictPk, hostLabels, guestLabels, calResult.getClubMatrices());
					killPromoteResult.setKillByPk(pkRes);
				}
				
				killByEuro(killPromoteResult.getKillByPl(), killPromoteResult.getKillByPlPkUnmatch(), calResult.getEuroMatrices(), pkMatrices, league);
			}

//			killByExchange(killPromoteResult, calResult);
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
		
		MatchState matchState = calResult.getMatchState();
		League league = calResult.getLeague();
		
		if (pkMatrices != null) {
			ClubMatrices clubMatrices = calResult.getClubMatrices();

			if (clubMatrices != null) {
				List <TeamLabel> hostLabels = clubMatrices.getHostLabels();
				List <TeamLabel> guestLabels = clubMatrices.getGuestLabels();

				Set<ResultGroup> pkRes = killByPk(pkMatrices, matchState, predictPk, hostLabels, guestLabels, calResult.getClubMatrices());
				killResult.setKillByPk(pkRes);
			}
			
			killByEuro(killResult.getKillByPl(), killResult.getKillByPlPkUnmatch(), calResult.getEuroMatrices(), pkMatrices, league);
		}
		
//		killByExchange(killResult, calResult);
		
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

	private Set<ResultGroup> killByPk (PankouMatrices pkMatrices, MatchState matchState, Float predictPk,
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
			
			boolean hasLatest = false;
			Float latestHostAttack = 0f;
			Float latestGuestAttack = 0f;
			Float hotPoint = 0f;
			
			if (matchState != null) {
				latestHostAttack = matchState.getHostAttackToGuest();
				latestGuestAttack = matchState.getGuestAttackToHost();
				
				hasLatest = latestHostAttack != null && latestGuestAttack != null;
				hotPoint = matchState.getHotPoint();
			}
			
			boolean skipWin = false;
			boolean skipLose = false;
			
			try {
				if (clubMs != null) {
					if (clubMs.getHostHomeMatrix().getWinRt() - clubMs.getGuestAwayMatrix().getWinDrawRt() >= -0.1
							|| hasLatest && latestHostAttack >= 1.8 * latestGuestAttack
							|| hotPoint > 6) {
						skipWin = true;
					}
					if (clubMs.getGuestAwayMatrix().getWinRt() - clubMs.getHostHomeMatrix().getWinDrawRt() >= -0.1
							|| hasLatest && latestGuestAttack >= 1.7 * latestHostAttack
							|| hotPoint < -6) {
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
	
	private void killPkPlUnmatchChange (Float pkChange, Float winPlChange, Float losePlChange, Float pankou, Set<ResultGroup> killGps) {
		if (pankou > 0.1) {
			if (winPlChange + pkChange > 0.1 || winPlChange + pkChange < -0.1) {
				killGps.add(ResultGroup.Three);
			}
		} else if (pankou < -0.1) {
			if (losePlChange - pkChange > 0.1 || losePlChange - pkChange < -0.1) {
				killGps.add(ResultGroup.Zero);
			}
		} else {
			if (winPlChange + pkChange > 0.1 || winPlChange + pkChange < -0.1) {
				killGps.add(ResultGroup.Three);
			}
			
			if (losePlChange - pkChange > 0.1 || losePlChange - pkChange < -0.1) {
				killGps.add(ResultGroup.Zero);
			}
		}
	}
	
	private void killByEuro (Set<ResultGroup> killGps, Set<ResultGroup> plPkUnmatchKillGps, EuroMatrices euMatrices, PankouMatrices pkMatrices, League league) {

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
				
				killPkPlUnmatchChange(MatchUtil.getCalculatedPk(aomenCurrPk) - MatchUtil.getCalculatedPk(aomenMainPk),
						aomenWinChange * currAomenEu.getEWin(),
						aomenLoseChange * currAomenEu.getELose(), aomenPk, plPkUnmatchKillGps);

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
			MatchState matchState, Float predictPk, Float mainPk, Float currentPk, AsiaPl main, AsiaPl current, EuroMatrices euroMatrices) {
		if (rgs == null) {
			rgs = new TreeSet<ResultGroup> ();
		}

		float hostAttGuestDefComp = clubMatrices.getHostAttGuestDefInx();
		float guestAttHostDefComp = clubMatrices.getGuestAttHostDefInx();
		
		Float latestHostAttack = matchState.getHostAttackToGuest();
		Float latestGuestAttack = matchState.getGuestAttackToHost();
		
		boolean hasLatest = latestHostAttack != null && latestGuestAttack != null;

		// host performed good lately
		if (hotPoint >= 6 || hasLatest && latestHostAttack > 1.6 * latestGuestAttack) {
			// host is so good --> win
			if (hostAttGuestDefComp > 1.5 * guestAttHostDefComp) {
				if (currentPk - mainPk >= -0.04 && currentPk - mainPk <= 0.16  // pk is up, but not more
						|| main.gethWin() <= 0.92 && current.gethWin() <= 0.92) {
					rgs.add(ResultGroup.Three);
				}
			}
		}
		// guest performed good lately
		else if (hotPoint <= -6 || hasLatest && latestGuestAttack > 1.4 * latestHostAttack) {
			// guest is so good
			if (guestAttHostDefComp >= 1.25 * hostAttGuestDefComp) {
				if (mainPk - currentPk >= -0.04 && mainPk - currentPk <= 0.16
						|| main.getaWin() <= 0.92 && current.getaWin() <= 0.92) {
					rgs.add(ResultGroup.Zero);
				}
			}
		} else {
			// host is good (not very good), and host didn't perform bad
			if (hostAttGuestDefComp < 1.5 * guestAttHostDefComp && hostAttGuestDefComp >= 0.7 * guestAttHostDefComp
					&& (hotPoint <= 4 && latestGuestAttack >= 0.6 * latestHostAttack)) {
				if ((mainPk - currentPk > 0.03
						|| main.getaWin() <= 0.92 && current.getaWin() <= 0.92)
						&& predictPk < mainPk + 0.2) {
					rgs.add(ResultGroup.One);
					rgs.add(ResultGroup.Zero);
				}
				
			}
			// guest is good (not very good), and guest didn't perform bad 
			else if (guestAttHostDefComp < 1.4 * hostAttGuestDefComp && guestAttHostDefComp > 0.7 * hostAttGuestDefComp
					&& (hotPoint >= -4 && latestHostAttack >= 0.7 * latestGuestAttack)) {

				if ((currentPk - mainPk > 0.03  // pk is up, but not more
						|| main.gethWin() <= 0.92 && current.gethWin() <= 0.92)
						&& predictPk > mainPk - 0.2) { // since the two teams are equality, the base won't not override the predict
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

			promoteByBase(rgs, hotPoint, clubMatrices, matchState, predictPk,
					mainPk, currentPk, main, current, euroMatrices);
			
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
						if (host6Match.getWinPkRate() >= 0.6 && guest6Match.getWinRate() <= 0.5
								|| guest6Match.getWinDrawPkRate() <= 0.5
								|| (host6Match.getWinPkRate() >= 0.5 && host6Match.getWinDrawPkRate() >= 0.6)
									&& guest6Match.getWinPkRate() <= 0.5 && guest6Match.getWinDrawPkRate() <= 0.6) {
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
			EuroMatrix inter = companyEus.get(Company.Interwetten);
			EuroMatrix aomen = companyEus.get(Company.Aomen);
			EuroMatrix jincai = companyEus.get(Company.Jincai);
			EuroPl euroAvg = euMatrices.getCurrEuroAvg();
			EuroMatrix majorComp = EuroUtil.getHighPaidEuro(euMatrices, league);
			AsiaPl aomenCurrPk = pkMatrices.getCurrentPk();
			AsiaPl aomenMainPk = pkMatrices.getMainPk();
			float currPk = aomenCurrPk.getPanKou();
			float mainPkRt = MatchUtil.getCalculatedPk(aomenMainPk);
			float currentPkRt = MatchUtil.getCalculatedPk(aomenCurrPk);

			boolean isAomenMajor = EuroUtil.isAomenTheMajor(league);

			if (euroAvg != null && majorComp != null && aomen != null) {
				EuroPl currMajorEu = majorComp.getCurrentEuro();
				EuroPl currLabEu = lab.getCurrentEuro();
				EuroPl currAomenEu = aomen.getCurrentEuro();
				EuroPl currWillEu = will.getCurrentEuro();
				EuroPl currInterEu = inter.getCurrentEuro();
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

				float maWinRt = MatchUtil.getEuDiff(currMajorEu.getEWin(), euroAvg.getEWin(), false);
				float maDrawRt = MatchUtil.getEuDiff(currMajorEu.getEDraw(), euroAvg.getEDraw(), false);
				float maLoseRt = MatchUtil.getEuDiff(currMajorEu.getELose(), euroAvg.getELose(), false);
				
				float aaWinRt = MatchUtil.getEuDiff(currAomenEu.getEWin(), euroAvg.getEWin(), false);
				float aaDrawRt = MatchUtil.getEuDiff(currAomenEu.getEDraw(), euroAvg.getEDraw(), false);
				float aaLoseRt = MatchUtil.getEuDiff(currAomenEu.getELose(), euroAvg.getELose(), false);
				
				float iaWinRt = MatchUtil.getEuDiff(currInterEu.getEWin(), euroAvg.getEWin(), false);
				float iaDrawRt = MatchUtil.getEuDiff(currInterEu.getEDraw(), euroAvg.getEDraw(), false);
				float iaLoseRt = MatchUtil.getEuDiff(currInterEu.getELose(), euroAvg.getELose(), false);
				float iaWinChange = inter.getWinChange();
				float iaDrawChange = inter.getDrawChange();
				float iaLoseChange = inter.getLoseChange();

				float waWinRt = MatchUtil.getEuDiff(currWillEu.getEWin(), euroAvg.getEWin(), false);
				float waDrawRt = MatchUtil.getEuDiff(currWillEu.getEDraw(), euroAvg.getEDraw(), false);
				float waLoseRt = MatchUtil.getEuDiff(currWillEu.getELose(), euroAvg.getELose(), false);
				
				float laWinRt = MatchUtil.getEuDiff(currLabEu.getEWin(), euroAvg.getEWin(), false);
				float laDrawRt = MatchUtil.getEuDiff(currLabEu.getEDraw(), euroAvg.getEDraw(), false);
				float laLoseRt = MatchUtil.getEuDiff(currLabEu.getELose(), euroAvg.getELose(), false);

				// the main company and lab is higher than the average, or aomen is higher than average and aomen is adjusted high
				// TODO
				if (currPk >= 1) {
					if (isAomenMajor) {
						if (aaWinRt <= -0.01 && aaWinRt + aomenWinChange < 0.001 && aomenWinChange < 0.01
								&& laWinRt < 0.035
								&& iaWinRt < -0.02
								&& aomenCurrPk.gethWin() <= 0.92 && aomenMainPk.gethWin() <= 0.92) {
							promoteGps.add(ResultGroup.Three);
						}
						if (aaDrawRt < -0.02 && aaDrawRt + aomenDrawChange < -0.015 && aomenDrawChange < 0.01
								&& waDrawRt < -0.01
								&& iaDrawRt < 0.01
								&& (currentPkRt <= mainPkRt - 0.05 || aomenMainPk.getaWin() <= 0.86 && aomenCurrPk.getaWin() <= 0.86)  // pk decrease
								) {
							promoteGps.add(ResultGroup.One);
						}
						if (aaLoseRt < -0.03 && aaLoseRt + aomenLoseChange < -0.025 && aomenLoseChange < 0.01
								&& laLoseRt < 0.035
								&& iaLoseRt < 0.01
								&& (currentPkRt <= mainPkRt - 0.04 // pk decrease
									|| aomenMainPk.getaWin() <= 0.86 && aomenCurrPk.getaWin() <= 0.86)) {
							promoteGps.add(ResultGroup.Zero);
						}
					} else {
						if ((mainPkRt + 0.05 < currentPkRt || aomenCurrPk.gethWin() <= 0.92 && aomenMainPk.gethWin() <= 0.92)
								&& maWinRt < 0.03 && majorWinChange < 0.03
								&& laWinRt < 0.045
								&& iaWinRt < 0.02
								&& jaWinDiff < -0.05
								&& aaWinRt < -0.015 && aomenWinChange < 0.01) {
							promoteGps.add(ResultGroup.Three);
						}
						if ((currentPkRt <= mainPkRt - 0.05 || aomenCurrPk.getaWin() <= 0.86 && aomenMainPk.getaWin() <= 0.86)
								&& maDrawRt < 0.03 && majorDrawChange < 0.02
								&& waDrawRt < -0.01
								&& iaDrawRt < 0.01
								&& aaDrawRt < -0.045 && aomenDrawChange < 0.01) {
							promoteGps.add(ResultGroup.One);
						}
						if ((currentPkRt <= mainPkRt - 0.05 || aomenCurrPk.getaWin() <= 0.86 && aomenMainPk.getaWin() <= 0.86)
								&& maLoseRt < 0.03 && maLoseRt < 0.02
								&& laLoseRt < 0.035
								&& iaLoseRt < -0.01
								&& aaLoseRt < -0.065 && aomenLoseChange < 0.01) {
							promoteGps.add(ResultGroup.Zero);
						}
					}
				} else if (currPk >= 0.4) {
					if (isAomenMajor) {
						if (aaWinRt < -0.01 && aaWinRt + aomenWinChange < 0.001 && aomenWinChange < 0.01
								&& laWinRt < 0.035
								&& iaWinRt < 0.01
								&& aomenCurrPk.gethWin() <= 0.92 && aomenMainPk.gethWin() <= 0.92  // pankou no up or down in case to hot
								) {
							promoteGps.add(ResultGroup.Three);
						}
						if (aaDrawRt < -0.02 && aaDrawRt + aomenDrawChange < -0.015 && aomenDrawChange < 0.01
								&& waDrawRt < -0.01
								&& iaDrawRt < 0.01
								&& (currentPkRt <= mainPkRt - 0.05 || aomenMainPk.getaWin() <= 0.86 && aomenCurrPk.getaWin() <= 0.86)  // pk decrease
								) {
							promoteGps.add(ResultGroup.One);
						}
						if (aaLoseRt < -0.03 && aaLoseRt + aomenLoseChange < -0.025 && aomenLoseChange < 0.01
								&& laLoseRt < 0.035
								&& iaLoseRt < -0.01
								&& (currentPkRt <= mainPkRt - 0.05 // pk decrease
									|| aomenMainPk.getaWin() <= 0.86 && aomenCurrPk.getaWin() <= 0.86)) {
							promoteGps.add(ResultGroup.Zero);
						}
					} else {
						if ((mainPkRt + 0.05 <= currentPkRt || aomenCurrPk.gethWin() <= 0.94 &&  aomenMainPk.gethWin() <= 0.94) // pankou no up or down in case to hot
								&& maWinRt < 0.03 && majorWinChange < 0.03
								&& laWinRt < 0.04
								&& iaWinRt < 0.02 && iaWinChange < 0.02
								&& jaWinDiff < -0.05
								&& aaWinRt < -0.015 && aomenWinChange < 0.01) {
							promoteGps.add(ResultGroup.Three);
						}
						if ((currentPkRt <= mainPkRt - 0.05 || aomenMainPk.getaWin() <= 0.88 && aomenCurrPk.getaWin() <= 0.88)
								&& maDrawRt < 0.03 && majorDrawChange < 0.02
								&& waDrawRt < 0.02
								&& iaDrawRt < 0.02
								&& aaDrawRt < -0.01 && aomenDrawChange < 0.01
								&& (waDrawRt < -0.025 && iaDrawRt < 0.01 || iaDrawRt < -0.025 && iaDrawChange < 0.01 || aaDrawRt <= -0.045)) {
							promoteGps.add(ResultGroup.One);
						}
						if ((currentPkRt <= mainPkRt - 0.05 || aomenMainPk.getaWin() <= 0.88 && aomenCurrPk.getaWin() <= 0.88)
								&& maLoseRt < 0.03 && majorLoseChange < 0.02
								&& laLoseRt < 0.045
								&& iaLoseRt < 0.01
								&& aaLoseRt < -0.02 && aomenLoseChange < 0.01
								&& (iaLoseRt < -0.03 && iaLoseChange < 0.01 || aaLoseRt <= -0.045)) {
							promoteGps.add(ResultGroup.Zero);
						}
					}
				} else if (currPk >= 0.25) {
					if (isAomenMajor) {
						if ((mainPkRt + 0.05 <= currentPkRt || aomenCurrPk.gethWin() < 0.9 && aomenMainPk.gethWin() < 0.9)
								&& aaWinRt < -0.01 && aaWinRt + aomenWinChange < -0.001 && aomenWinChange < 0.01
								&& laWinRt < 0.04
								&& iaWinRt < 0.01) {
							promoteGps.add(ResultGroup.Three);
						}
						if (aaDrawRt < -0.02 && aaDrawRt + aomenDrawChange < -0.015 && aomenDrawChange < 0.01
								&& waDrawRt < -0.01
								&& iaDrawRt < 0.01
								&& (currentPkRt <= mainPkRt - 0.05 || aomenMainPk.getaWin() <= 0.94 && aomenCurrPk.getaWin() <= 0.94)) {
							promoteGps.add(ResultGroup.One);
						}
						if ((currentPkRt <= mainPkRt - 0.05 || aomenCurrPk.getaWin() <= 0.92 && aomenMainPk.getaWin() <= 0.92)
								&& aaLoseRt < -0.03 && aaLoseRt + aomenLoseChange < -0.025 && aomenLoseChange < 0.01
								&& laLoseRt < 0.04
								&& iaLoseRt < 0.01) {
							promoteGps.add(ResultGroup.Zero);
						}
					} else {
						if ((mainPkRt + 0.05 <= currentPkRt || aomenCurrPk.gethWin() <= 0.92 && aomenMainPk.gethWin() <= 0.92)
								&& maWinRt < 0.03 && majorWinChange < 0.03
								&& laWinRt < 0.03
								&& iaWinRt < 0.01 && iaWinChange < 0.01
								&& jaWinDiff < -0.05
								&& aaWinRt < -0.015 && aomenWinChange < 0.01) {
							promoteGps.add(ResultGroup.Three);
						}
						if (maDrawRt < 0.03 && majorDrawChange < 0.02
								&& waDrawRt < 0.02
								&& iaDrawRt < 0.01
								&& aaDrawRt < -0.01 && aomenDrawChange < 0.01
								&& (waDrawRt < -0.02 || iaDrawRt < -0.02 && iaDrawChange < 0.01
										|| aaDrawRt <= -0.03 || aaDrawRt <= -0.01 && currAomenEu.getEDraw() <= 3.15)) {
							promoteGps.add(ResultGroup.One);
						}
						if ((currentPkRt <= mainPkRt - 0.05 || aomenCurrPk.getaWin() <= 0.9 && aomenMainPk.getaWin() <= 0.9)
								&& maLoseRt < 0.03 && majorLoseChange < 0.02
								&& laLoseRt < 0.04
								&& iaLoseRt < 0.02
								&& aaLoseRt < -0.02 && aomenLoseChange < 0.01
								&& (iaLoseRt < -0.03 && iaLoseChange < 0.01 || aaLoseRt <= -0.05)) {
							promoteGps.add(ResultGroup.Zero);
						}
					}
				} else if (currPk >= 0) {
					if ((mainPkRt + 0.05 < currentPkRt  || aomenCurrPk.gethWin() <= 0.92 && aomenMainPk.gethWin() <= 0.92)
							&& maWinRt < 0.03 && majorWinChange < 0.02
							&& laWinRt < (isAomenMajor ? 0.035 : 0.045)
							&& iaWinRt < -0.01 && iaWinChange < 0.01
							&& jaWinDiff < -0.05
							&& aaWinRt < -0.02 && aomenWinChange < 0.01) {
						promoteGps.add(ResultGroup.Three);
					}
					
					if (aaDrawRt < -0.02 && aomenDrawChange < 0.01
							&& waDrawRt < -0.01
							&& iaDrawRt < 0.01 && iaDrawChange < 0.01) {
						promoteGps.add(ResultGroup.One);
					}
					
					if ((currentPkRt < mainPkRt - 0.05 || aomenCurrPk.getaWin() <= 0.92 && aomenMainPk.getaWin() <= 0.92)
							&& maLoseRt < 0.03 && majorLoseChange < 0.02
							&& laLoseRt < (isAomenMajor ? 0.035 : 0.045)
							&& iaLoseRt < -0.01 && iaLoseChange < 0.01
							&& jaLoseDiff < -0.05
							&& aaLoseRt < -0.02 && aomenLoseChange < 0.001) {
						promoteGps.add(ResultGroup.Zero);
					}
				} else if (currPk >= -0.25) {
					if (isAomenMajor) {
						if (aaWinRt < -0.03 && aaWinRt + aomenWinChange < -0.025 && aomenWinChange < 0.001
								&& laWinRt < 0.04
								&& iaWinRt < -0.01
								&& (mainPkRt + 0.05 < currentPkRt || aomenCurrPk.gethWin() <= 0.92 && aomenMainPk.gethWin() <= 0.92)) {
							promoteGps.add(ResultGroup.Three);
						}
						if (aaDrawRt < -0.02 && aaDrawRt + aomenDrawChange < -0.015 && aomenDrawChange < 0.001
								&& waDrawRt < -0.01
								&& iaDrawRt < 0.01
								&& (mainPkRt + 0.05 < currentPkRt || aomenCurrPk.gethWin() <= 0.92 && aomenMainPk.gethWin() <= 0.92)
								) {
							promoteGps.add(ResultGroup.One);
						}
						if (aaLoseRt < -0.01 && aaLoseRt + aomenLoseChange< -0.005 && aomenLoseChange < 0.01
								&& laLoseRt < 0.4
								&& iaLoseRt < 0.01
								&& (currentPkRt < mainPkRt - 0.04 || aomenCurrPk.getaWin() <= 0.92 && aomenMainPk.getaWin() <= 0.92)) {
							promoteGps.add(ResultGroup.Zero);
						}
					} else {
						if ((mainPkRt + 0.05 <= currentPkRt || aomenCurrPk.gethWin() < 0.9 && aomenMainPk.gethWin() < 0.9)
								&& maWinRt < 0.03 && majorWinChange < 0.02
								&& laWinRt < 0.04
								&& iaWinRt < 0.01
								&& aaWinRt < -0.02 && aomenWinChange < 0.01
								&& (iaWinRt < -0.025 && iaWinChange < 0.01 || aaWinRt <= -0.05)) {
							promoteGps.add(ResultGroup.Three);
						}
						if (maDrawRt <= 0.03 && majorDrawChange <= 0.02
								&& waDrawRt < 0.02
								&& iaDrawRt < 0.01
								&& aaDrawRt < -0.01 && aomenDrawChange < 0.01
								&& (waDrawRt < -0.02 || iaDrawRt < -0.02 && iaDrawChange < 0.001 ||
										aaDrawRt <= -0.03 || aaDrawRt <= -0.01 && currAomenEu.getEDraw() <= 3.15) ) {
							promoteGps.add(ResultGroup.One);
						}
						if ((currentPkRt <= mainPkRt - 0.05 || aomenCurrPk.getaWin() <= 0.92 && aomenMainPk.getaWin() <= 0.92)
								&& maLoseRt < 0.03 && majorLoseChange < 0.03
								&& laLoseRt < 0.04
								&& iaLoseRt < 0.02 && iaLoseChange < 0.02
								&& jaLoseDiff < -0.05
								&& aaLoseRt < -0.015 && aomenLoseChange < 0.01) {
							promoteGps.add(ResultGroup.Zero);
						}
					}
				} else if (currPk >= -0.8) {
					if (isAomenMajor) {
						if (aaWinRt < -0.03 && aaWinRt + aomenWinChange < -0.025 && aomenWinChange < 0.01
								&& laWinRt < 0.035
								&& iaWinRt < 0.01
								&& (mainPkRt + 0.05 < currentPkRt || aomenCurrPk.gethWin() <= 0.86 && aomenMainPk.gethWin() <= 0.86)) {
							promoteGps.add(ResultGroup.Three);
						}
						if (aaDrawRt < -0.02 && aaDrawRt + aomenDrawChange < -0.015 && aomenDrawChange < 0.001
								&& waDrawRt < -0.01
								&& iaDrawRt < 0.01
								&& (mainPkRt + 0.05 < currentPkRt || aomenCurrPk.gethWin() <= 0.86 && aomenMainPk.gethWin() <= 0.86)) {
							promoteGps.add(ResultGroup.One);
						}
						if (aaLoseRt < -0.01 && aaLoseRt + aomenLoseChange < -0.001 && aomenLoseChange < 0.01
								&& laLoseRt < 0.04
								&& iaLoseRt < -0.01
								&& (currentPkRt < mainPkRt - 0.05 || aomenCurrPk.getaWin() <= 0.94 && aomenMainPk.getaWin() <= 0.94)) {
							promoteGps.add(ResultGroup.Zero);
						}
					} else {
						if ((mainPkRt + 0.05 <= currentPkRt || aomenCurrPk.gethWin() < 0.92 && aomenMainPk.gethWin() < 0.92)
								&& maWinRt < 0.03 && majorWinChange < 0.02
								&& laWinRt < 0.04
								&& iaWinRt < 0.01
								&& aaWinRt < -0.02 && aomenWinChange < 0.01
								&& (iaWinRt <= -0.03 && iaWinChange < 0.01 || aaWinRt <= -0.06)) {
							promoteGps.add(ResultGroup.Three);
						}
						if ((mainPkRt + 0.05 <= currentPkRt || aomenCurrPk.gethWin() < 0.92 && aomenMainPk.gethWin() < 0.92)
								&& maDrawRt < 0.03 && majorDrawChange < 0.02
								&& waDrawRt < 0.03
								&& iaDrawRt < 0.01
								&& aaDrawRt < -0.02 && aomenDrawChange < 0.01
								&& (iaDrawRt < -0.025 && iaDrawChange < 0.01 || aaDrawRt <= -0.04)) {
							promoteGps.add(ResultGroup.One);
						}
						if ((currentPkRt <= mainPkRt - 0.05 || aomenCurrPk.getaWin() <= 0.92 && aomenMainPk.getaWin() <= 0.92)
								&& maLoseRt < 0.03 && majorLoseChange < 0.03
								&& laLoseRt < 0.045
								&& iaLoseRt < 0.02 && iaLoseChange < 0.02
								&& jaLoseDiff < -0.05
								&& aaLoseRt < -0.015 && aomenLoseChange < 0.01) {
							promoteGps.add(ResultGroup.Zero);
						}
					}
				} else if (currPk >= -1) {
					if (isAomenMajor) {
						if (aaWinRt < -0.03 && aaWinRt + aomenWinChange < -0.025 && aomenWinChange < 0.01
								&& laWinRt < 0.035
								&& iaWinRt < -0.01
								&& (mainPkRt + 0.05 < currentPkRt || aomenCurrPk.gethWin() <= 0.86 && aomenMainPk.gethWin() <= 0.86)
								) {
							promoteGps.add(ResultGroup.Three);
						}
						if (aaDrawRt < -0.02 && aaDrawRt + aomenDrawChange < -0.015 && aomenDrawChange < 0.01
								&& waDrawRt < -0.01
								&& iaDrawRt < 0.01
								&& (mainPkRt + 0.05 < currentPkRt || aomenCurrPk.gethWin() <= 0.86 && aomenMainPk.gethWin() <= 0.86)
								) {
							promoteGps.add(ResultGroup.One);
						}
						if (aaLoseRt < -0.01 && aaLoseRt + aomenLoseChange < 0.001 && aomenLoseChange < 0.01
								&& laLoseRt < 0.04
								&& iaLoseRt < 0.01
								&& (currentPkRt <= mainPkRt - 0.05 || aomenCurrPk.getaWin() <= 0.92 && aomenMainPk.getaWin() <= 0.92)) {
							promoteGps.add(ResultGroup.Zero);
						}
					} else {
						if ((mainPkRt + 0.05 <= currentPkRt || aomenCurrPk.gethWin() < 0.9 && aomenMainPk.gethWin() < 0.9)
								&& maWinRt < 0.03 && majorWinChange < 0.02
								&& laWinRt < 0.035
								&& iaWinRt < -0.01
								&& aaWinRt < -0.06 && aomenWinChange < 0.01) {
							promoteGps.add(ResultGroup.Three);
						}
						if ((mainPkRt + 0.05 <= currentPkRt || aomenCurrPk.gethWin() < 0.9 && aomenMainPk.gethWin() < 0.9)
								&& maDrawRt < 0.03 && majorDrawChange < 0.02
								&& waDrawRt < -0.01
								&& iaDrawRt < -0.01
								&& aaDrawRt < -0.045 && aomenDrawChange < 0.01) {
							promoteGps.add(ResultGroup.One);
						}
						if ((currentPkRt <= mainPkRt - 0.05 || aomenCurrPk.getaWin() <= 0.92 && aomenMainPk.getaWin() <= 0.92)
								&& maLoseRt < 0.035 && majorLoseChange < 0.03
								&& laLoseRt < 0.045
								&& iaLoseRt < 0.02
								&& aaLoseRt < -0.015 && aomenLoseChange < 0.01) {
							promoteGps.add(ResultGroup.Zero);
						}
					}
				} else {
					if ((mainPkRt + 0.05 < currentPkRt || aomenCurrPk.gethWin() <= 0.9 && aomenMainPk.gethWin() <= 0.9)
							&& maWinRt < 0.03 && majorWinChange < 0.02
							&& laWinRt < 0.035
							&& iaWinRt < -0.01
							&& aaWinRt < -0.06 && aomenWinChange < 0.01) {
						promoteGps.add(ResultGroup.Three);
					}
					if ((mainPkRt + 0.05 < currentPkRt || aomenCurrPk.gethWin() <= 0.9 && aomenMainPk.gethWin() <= 0.9)
							&& maDrawRt < 0.03 && majorDrawChange < 0.02
							&& waDrawRt < -0.01
							&& iaDrawRt < -0.01
							&& aaDrawRt < -0.045 && aomenDrawChange < 0.01) {
						promoteGps.add(ResultGroup.One);
					}
					if ((currentPkRt < mainPkRt - 0.05 || aomenCurrPk.getaWin() <= 0.94 && aomenMainPk.getaWin() <= 0.94)
							&& maLoseRt < 0.035 && majorLoseChange < 0.03
							&& laLoseRt < 0.045
							&& iaLoseRt < 0.02
							&& aaLoseRt < -0.001 && aomenLoseChange < 0.01) {
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
