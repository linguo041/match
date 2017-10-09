package com.roy.football.match.OFN;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import com.mysema.commons.lang.Pair;
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
import com.roy.football.match.base.MatchContinent;
import com.roy.football.match.base.ResultGroup;
import com.roy.football.match.base.TeamLabel;
import com.roy.football.match.okooo.MatchExchangeData;
import com.roy.football.match.util.EuroUtil;
import com.roy.football.match.util.MatchStateUtil;
import com.roy.football.match.util.MatchUtil;
import com.roy.football.match.util.PanKouUtil;
import com.roy.football.match.util.PanKouUtil.PKDirection;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class PankouKillPromoter {
	private final static float LOW_PK_POINT = -0.02f;
	
	public PredictResult calculate (OFNCalculateResult calResult) {
		PredictResult predictRes = new PredictResult();
		predict(predictRes, calResult);
		
//		kill(killPromoteResult, calResult);
//		promote(killPromoteResult, calResult);
		
		MatchPromoter pp = new MatchPromoter();
		predictRes.setKpResult(pp.promote(calResult));

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
			hvariation = matchState.getHostAttackVariationToGuest();
			gvariation = matchState.getGuestAttackVariationToHost();

			if (hgoal == 0 && ggoal == 0) {
				hgoal = lhgoal;
				ggoal = lggoal;
			} else {
				hgoal = 0.8f * hgoal + 0.2f * lhgoal;
				ggoal = 0.8f * ggoal + 0.2f * lggoal;
				hvariation = 0.9f * hvariation;
				gvariation = 0.9f * gvariation;
			}
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

	private void kill(OFNKillPromoteResult killPromoteResult, OFNCalculateResult calResult) {
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
		killResult.setPromoteByBase(promoteGroups);
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
				promoteByEuro(promoteGps, euroMatrices, predictPk, pkMatrices,
						league, hotPoint, matchState, calResult.isSameCityOrNeutral());

				promoteByPk(promoteGps, pkMatrices, predictPk, hotPoint,
						matchState, clubMatrices, exchange, euroMatrices, calResult.isSameCityOrNeutral(), league);
			}

			killPromoteResult.setPromoteByBase(promoteGps);
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
			promoteByEuro(promoteGps, euroMatrices, predictPk, pkMatrices,
					league, hotPoint, matchState, calResult.isSameCityOrNeutral());

			promoteByPk(promoteGps, pkMatrices, predictPk, hotPoint,
					matchState, clubMatrices, exchange, euroMatrices, calResult.isSameCityOrNeutral(), league);
		}

		killPromoteResult.setPromoteByBase(promoteGps);

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
							|| hasLatest && latestHostAttack >= 1.8 * latestGuestAttack && hotPoint > 6) {
						skipWin = true;
					}
					if (clubMs.getGuestAwayMatrix().getWinRt() - clubMs.getHostHomeMatrix().getWinDrawRt() >= -0.1
							|| hasLatest && latestGuestAttack >= 1.6 * latestHostAttack && hotPoint < -5) {
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
	
	private void killPkPlUnmatchChange (Float pkChange, Float winPlChange, Float losePlChange,
			Float waWinRt, Float waLoseRt, Float aomenWinChange, Float aomenLoseChange, Float pankou, Set<ResultGroup> killGps) {
		if (pankou > 0.1) {
			if ((winPlChange + pkChange > 0.125 || winPlChange + pkChange < -0.125)
					&& waWinRt > -0.021 && aomenWinChange > -0.015) {
				killGps.add(ResultGroup.Three);
			}
		} else if (pankou < -0.1) {
			if ((losePlChange - pkChange > 0.125 || losePlChange - pkChange < -0.125)
					&& waLoseRt > -0.021 && aomenLoseChange > -0.015) {
				killGps.add(ResultGroup.Zero);
			}
		} else {
			if ((winPlChange + pkChange > 0.125 || winPlChange + pkChange < -0.125)
					&& waWinRt > -0.021 && aomenWinChange > -0.015) {
				killGps.add(ResultGroup.Three);
			}
			
			if ((losePlChange - pkChange > 0.125 || losePlChange - pkChange < -0.125)
					&& waLoseRt > -0.021 && aomenLoseChange > -0.015) {
				killGps.add(ResultGroup.Zero);
			}
		}
	}
	
	private void killPkPlUnmatchChange (AsiaPl aomenPk, EuroPl currAomenEu, League le, Set<ResultGroup> killGps) {
		float calculatedPk = MatchUtil.getCalculatedPk(aomenPk);
		
		if (aomenPk.getPanKou() == 1.00f) {
			float avg = 1.48f;
			switch (le.getContinent()) {
				case Euro: avg = 1.472f; break;
				case Asia: avg = 1.473f; break;
				case America: avg = 1.490f; break;
				default: avg = 1.48f; break;
			}
			// 1.48-0.12 < pl < 1.48+0.12; 0.8 < pk < 1.2
			if (Math.abs((currAomenEu.getEWin() - avg) - (aomenPk.getPanKou() - calculatedPk)) >= 0.08) {
				killGps.add(ResultGroup.Three);
			}
		} else if (aomenPk.getPanKou() == 0.75f) {
			float avg = 1.65f;
			switch (le.getContinent()) {
				case Euro: avg = 1.647f; break;
				case Asia: avg = 1.639f; break;
				case America: avg = 1.638f; break;
				default: avg = 1.65f; break;
			}
			
			// 1.65-0.15 < pl < 1.65+0.15; 0.6 < pk < 0.9
			if (Math.abs((currAomenEu.getEWin() - avg) - (aomenPk.getPanKou() - calculatedPk)) >= 0.08) {
				killGps.add(ResultGroup.Three);
			}			
		} else if (aomenPk.getPanKou() == 0.50f) {
			float avg = 1.88f;
			switch (le.getContinent()) {
				case Euro: avg = 1.891f; break;
				case Asia: avg = 1.890f; break;
				case America: avg = 1.868f; break;
				default: avg = 1.88f; break;
			}
			
			// 1.88-0.18 < pl < 1.88+0.18; 0.35 < pk < 0.65
			if (Math.abs((currAomenEu.getEWin() - avg) - (aomenPk.getPanKou() - calculatedPk)) >= 0.08) {
				killGps.add(ResultGroup.Three);
			}			
		} else if (aomenPk.getPanKou() == 0.25f) {
			float avg = 2.15f;
			switch (le.getContinent()) {
				case Euro: avg = 2.155f; break;
				case Asia: avg = 2.175f; break;
				case America: avg = 2.147f; break;
				default: avg = 2.15f; break;
			}
			
			// 2.15-0.2 < pl < 2.15+0.2; 0.1 < pk < 0.4
			if (Math.abs((currAomenEu.getEWin() - avg) - (aomenPk.getPanKou() - calculatedPk)) >= 0.08) {
				killGps.add(ResultGroup.Three);
			}			
		} else if (aomenPk.getPanKou() == -0.25f) {
			float avg = 2.18f;
			switch (le.getContinent()) {
				case Euro: avg = 2.176f; break;
				case Asia: avg = 2.201f; break;
				case America: avg = 2.202f; break;
				default: avg = 2.18f; break;
			}
			
			// 2.18-0.2 < pl < 2.18+0.2; -0.1 > pk > -0.4
			if (Math.abs((currAomenEu.getELose() - avg) - (calculatedPk - aomenPk.getPanKou())) >= 0.08) {
				killGps.add(ResultGroup.Zero);
			}			
		} else if (aomenPk.getPanKou() == -0.50f) {
			float avg = 1.90f;
			switch (le.getContinent()) {
				case Euro: avg = 1.895f; break;
				case Asia: avg = 1.910f; break;
				case America: avg = 1.909f; break;
				default: avg = 1.90f; break;
			}
			
			// 1.90-0.18 < pl < 1.90+0.18; -0.35 > pk > -0.65
			if (Math.abs((currAomenEu.getELose() - avg) - (calculatedPk - aomenPk.getPanKou())) >= 0.08) {
				killGps.add(ResultGroup.Zero);
			}			
		} else if (aomenPk.getPanKou() == -0.75f) {
			float avg = 1.65f;
			switch (le.getContinent()) {
				case Euro: avg = 1.646f; break;
				case Asia: avg = 1.665f; break;
				case America: avg = 1.670f; break;
				default: avg = 1.65f; break;
			}
			
			// 1.65-0.18 < pl < 1.65+0.18; -0.6 > pk > -0.9
			if (Math.abs((currAomenEu.getELose() - avg) - (calculatedPk - aomenPk.getPanKou())) >= 0.08) {
				killGps.add(ResultGroup.Zero);
			}			
		} else if (aomenPk.getPanKou() == -1.0f) {
			float avg = 1.47f;
			switch (le.getContinent()) {
				case Euro: avg = 1.474f; break;
				case Asia: avg = 1.480f; break;
				case America: avg = 1.450f; break;
				default: avg = 1.47f; break;
			}
			
			// 1.47-0.18 < pl < 1.47+0.18; -0.85 > pk > -1.15
			if (Math.abs((currAomenEu.getELose() - avg) - (calculatedPk - aomenPk.getPanKou())) >= 0.08) {
				killGps.add(ResultGroup.Zero);
			}			
		}
	}
	
	private void killByEuro (Set<ResultGroup> killGps, Set<ResultGroup> plPkUnmatchKillGps, EuroMatrices euMatrices, PankouMatrices pkMatrices, League league) {

		if (euMatrices != null && pkMatrices != null) {
			Map<Company, EuroMatrix> companyEus = euMatrices.getCompanyEus();
			EuroMatrix will = companyEus.get(Company.William);
			EuroMatrix lab = companyEus.get(Company.Ladbrokes);
			EuroMatrix inter = companyEus.get(Company.Interwetten);
			EuroMatrix aomen = companyEus.get(Company.Aomen);
			EuroMatrix ysb = companyEus.get(Company.YiShenBo);
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
				EuroPl currInterEu = inter.getCurrentEuro();
				EuroPl currYsbEu = ysb.getCurrentEuro();
				float majorWinChange = majorComp.getWinChange();
				float majorDrawChange = majorComp.getDrawChange();
				float majorLoseChange = majorComp.getLoseChange();
				
				float willWinChange = will.getWinChange();
				float willDrawChange = will.getDrawChange();
				float willLoseChange = will.getLoseChange();
				
				float interWinChange = inter.getWinChange();
				float interDrawChange = inter.getDrawChange();
				float interLoseChange = inter.getLoseChange();
				
				float aomenWinChange = aomen.getWinChange();
				float aomenDrawChange = aomen.getDrawChange();
				float aomenLoseChange = aomen.getLoseChange();
				
				float ysbWinChange = ysb.getWinChange();
				float ysbDrawChange = ysb.getDrawChange();
				float ysbLoseChange = ysb.getLoseChange();

				float laWinRt = MatchUtil.getEuDiff(currLabEu.getEWin(), euroAvg.getEWin(), false);
				float laDrawRt = MatchUtil.getEuDiff(currLabEu.getEDraw(), euroAvg.getEDraw(), false);
				float laLoseRt = MatchUtil.getEuDiff(currLabEu.getELose(), euroAvg.getELose(), false);
				
				float iaWinRt = MatchUtil.getEuDiff(currInterEu.getEWin(), euroAvg.getEWin(), false);
				float iaDrawRt = MatchUtil.getEuDiff(currInterEu.getEDraw(), euroAvg.getEDraw(), false);
				float iaLoseRt = MatchUtil.getEuDiff(currInterEu.getELose(), euroAvg.getELose(), false);
				
				float yaWinRt = MatchUtil.getEuDiff(currYsbEu.getEWin(), euroAvg.getEWin(), false);
				float yaDrawRt = MatchUtil.getEuDiff(currYsbEu.getEDraw(), euroAvg.getEDraw(), false);
				float yaLoseRt = MatchUtil.getEuDiff(currYsbEu.getELose(), euroAvg.getELose(), false);

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
						if ((maWinRt > 0.06 && majorWinChange > -0.021 && majorLoseChange < 0.021
								|| iaWinRt > 0.051 && interWinChange > 0.011 && interLoseChange < 0.021
								|| aaWinRt > 0.021 && aomenWinChange > -0.011 && aomenLoseChange < 0.021)
								&& aaWinRt + aomenWinChange > -0.031
								&& waWinRt > 0.011 && willWinChange > -0.011) {
							killGps.add(ResultGroup.Three);
						}
						if ((maDrawRt > 0.06 && majorDrawChange > -0.025
								|| iaDrawRt > 0.051 && interDrawChange > -0.011
								|| aaDrawRt > 0.021 && aomenDrawChange > -0.011)
								&& (aaDrawRt + aomenDrawChange > -0.031)
								&& (iaDrawRt + interDrawChange > -0.021)
								&& waWinRt < -0.015
								&& waDrawRt > 0.011 && willDrawChange > -0.02) {
							killGps.add(ResultGroup.One);
						}
						if ((maLoseRt > 0.06 && majorLoseChange > -0.025 && majorWinChange < 0.021
								|| iaLoseRt > 0.051 && interLoseChange > 0.011 && interWinChange < 0.021
								|| aaLoseRt > 0.021 && aomenLoseChange > -0.011 && aomenWinChange < 0.021)
								&& (aaLoseRt + aomenLoseChange > -0.041)
								&& waWinRt < -0.015
								&& waLoseRt > 0.021 && willLoseChange > -0.021) {
							killGps.add(ResultGroup.Zero);
						}
					}
				} else if (aomenPk >= 0.4) {
					if (isAomenMajor) {
						if ((aaWinRt > 0.021 && aomenWinChange > -0.001
//									|| yaWinRt > 0.051 && ysbWinChange > -0.001
									|| jaWinDiff > -0.031 && jcWinChange > -0.001)
								&& yaWinRt > 0.01 && aaWinRt > -0.01) {
							killGps.add(ResultGroup.Three);
						}
						if ((aaDrawRt > 0.021 && aomenDrawChange > -0.001
//									|| yaDrawRt > 0.051 && ysbDrawChange > -0.012
									|| jaDrawDiff > 0.045 && jcDrawChange > -0.012)
								&& yaDrawRt > 0.01 && aaDrawRt > -0.012 && aomenDrawChange > -0.012) {
							killGps.add(ResultGroup.One);
						}
						if ((aaLoseRt > 0.021 && aomenLoseChange > -0.001
//									|| yaLoseRt > 0.051 && ysbLoseChange > -0.012
									|| jaLoseDiff > 0.051 && jcLoseChange > -0.012)
								&& yaLoseRt > 0.01 && aaLoseRt > -0.01) {
							killGps.add(ResultGroup.Zero);
						}
					} else {
						if ((maWinRt > 0.06 && majorWinChange > -0.02 && majorLoseChange < 0.02
								|| iaWinRt > 0.051 && interWinChange > 0.011 && interLoseChange < 0.02
								|| MatchContinent.Euro == league.getContinent() ? aaWinRt > 0.005 : aaWinRt > 0.021  // aomen follows will
									&& aomenWinChange > -0.01 && aomenLoseChange < 0.02)
								&& (aaWinRt + aomenWinChange > -0.031)
								&& waWinRt > -0.011 && willWinChange > -0.011) {
							killGps.add(ResultGroup.Three);
						}
						if ((maDrawRt > 0.05 && majorDrawChange > -0.025
								|| iaDrawRt > 0.051 && interDrawChange > -0.01
								|| aaDrawRt > 0.02 && aomenDrawChange > -0.01)
								&& (aaDrawRt + aomenDrawChange > -0.031)
								&& (iaDrawRt + interDrawChange > -0.021)
								&& waDrawRt > -0.011 && willDrawChange > -0.02) {
							killGps.add(ResultGroup.One);
						}
						if ((maLoseRt > 0.06 && majorLoseChange > -0.025 && majorWinChange < 0.02
								|| iaLoseRt > 0.051 && interLoseChange > 0.011 && interWinChange < 0.02
								|| aaLoseRt > 0.021 && aomenLoseChange > -0.011 && aomenWinChange < 0.02)
								&& (aaLoseRt + aomenLoseChange > -0.04)
								&& waLoseRt > -0.011 && willLoseChange > -0.021) {
							killGps.add(ResultGroup.Zero);
						}
					}
				} else if (aomenPk >= 0.25) {
					if (isAomenMajor) {
						if ((aaWinRt > 0.021 && aomenWinChange > -0.001
//								|| yaWinRt > 0.041 && ysbWinChange > -0.001
								|| jaWinDiff > -0.011 && jcWinChange > -0.001)
							&& yaWinRt > 0.01 && aaWinRt > -0.01) {
							killGps.add(ResultGroup.Three);
						}
						if ((aaDrawRt > 0.025 && aomenDrawChange > -0.001
//								|| yaDrawRt > 0.041 && ysbDrawChange > -0.012
								|| jaDrawDiff > 0.051 && jcDrawChange > -0.012)
							&& yaDrawRt > 0.01 && aaDrawRt > -0.015 && currAomenEu.getEDraw() >= 3.15f) {
							killGps.add(ResultGroup.One);
						}
						if ((aaLoseRt > 0.021 && aomenLoseChange > -0.001
//								|| yaLoseRt > 0.041 && ysbLoseChange > -0.012
								|| jaLoseDiff > 0.041 && jcLoseChange > -0.012)
							&& yaLoseRt > 0.01 && aaLoseRt > -0.015) {
							killGps.add(ResultGroup.Zero);
						}
					} else {
						if ((maWinRt > 0.051 && majorWinChange > -0.021 && majorLoseChange < 0.021
								|| iaWinRt > 0.051 && interWinChange > 0.011 && interLoseChange < 0.021
								|| MatchContinent.Euro == league.getContinent() ? aaWinRt > 0.005 : aaWinRt > 0.021  // aomen follows will
										&& aomenWinChange > -0.011 && aomenLoseChange < 0.021)
								&& (aaWinRt + aomenWinChange > -0.035)
								&& waWinRt > -0.011 && willWinChange > -0.011) {
							killGps.add(ResultGroup.Three);
						}
						if ((maDrawRt > 0.051 && majorDrawChange > -0.025
								|| iaDrawRt > 0.051 && interDrawChange > -0.011
								|| aaDrawRt > 0.021 && aomenDrawChange > -0.011)
								&& (aaDrawRt + aomenDrawChange > -0.031)
								&& (iaDrawRt + interDrawChange > -0.021)
								&& waDrawRt > -0.011 && willDrawChange > -0.02) {
							killGps.add(ResultGroup.One);
						}
						if ((maLoseRt > 0.06 && majorLoseChange > -0.025 && majorWinChange < 0.021
								|| iaLoseRt > 0.051 && interLoseChange > 0.011 && interWinChange < 0.021
								|| aaLoseRt > 0.021 && aomenLoseChange > -0.011 && aomenWinChange < 0.021)
								&& (aaLoseRt + aomenLoseChange > -0.041)
								&& waLoseRt > -0.011 && willLoseChange > -0.021) {
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
						if (aaDrawRt > 0.021 && aomenDrawChange > -0.001
								&& jaDrawDiff + jcDrawChange > 0.001
								&& waDrawRt > 0.011 && willDrawChange > -0.02) {
							killGps.add(ResultGroup.One);
						}
						if (aaLoseRt > 0.019 && aomenLoseChange > -0.001
								&& jaLoseDiff + jcLoseChange > -0.001
								&& laLoseRt > 0.031) {
							killGps.add(ResultGroup.Zero);
						}
					} else {
						if (maWinRt > 0.06 && majorWinChange > -0.021 && majorLoseChange < 0.02
								|| iaWinRt > 0.051 && interWinChange > 0.011 && interLoseChange < 0.021
								|| aaWinRt > 0.021 && aomenWinChange > -0.011 && aomenLoseChange < 0.021
								&& (aaWinRt + aomenWinChange > -0.031)
								&& waWinRt > -0.011 && willWinChange > -0.011) {
							killGps.add(ResultGroup.Three);
						}
						if ((maDrawRt > 0.051 && majorDrawChange > -0.025
								|| iaDrawRt > 0.051 && interDrawChange > -0.011
								|| aaDrawRt > 0.021 && aomenDrawChange > -0.011)
								&& (aaDrawRt + aomenDrawChange > -0.031)
								&& (iaDrawRt + interDrawChange > -0.021)
								&& waDrawRt > -0.011 && willDrawChange > -0.011) {
							killGps.add(ResultGroup.One);
						}
						if ((maLoseRt > 0.06 && majorLoseChange > -0.021 && majorWinChange < 0.021
								|| iaLoseRt > 0.051 && interLoseChange > 0.011 && interWinChange < 0.021
								|| aaLoseRt > 0.021 && aomenLoseChange > -0.011 && aomenWinChange < 0.021)
								&& (aaLoseRt + aomenLoseChange > -0.031)
								&& waLoseRt > -0.011 && willLoseChange > -0.011) {
							killGps.add(ResultGroup.Zero);
						}
					}
				} else if (aomenPk >= -0.25) {
					if (isAomenMajor) {
						if ((aaWinRt > 0.021 && aomenWinChange > -0.001
//								|| yaWinRt > 0.041 && ysbWinChange > -0.001
								|| jaWinDiff > 0.041 && jcWinChange > -0.001)
							&& yaWinRt > 0.01 && aaWinRt > -0.01) {
							killGps.add(ResultGroup.Three);
						}
						if ((aaDrawRt > 0.025 && aomenDrawChange > -0.001
//								|| yaDrawRt > 0.041 && ysbDrawChange > -0.012
								|| jaDrawDiff > 0.051 && jcDrawChange > -0.012)
							&& yaDrawRt > 0.01 && aaDrawRt > -0.015 && currAomenEu.getEDraw() >= 3.15f) {
							killGps.add(ResultGroup.One);
						}
						if ((aaLoseRt > 0.021 && aomenLoseChange > -0.001
//								|| yaLoseRt > 0.041 && ysbLoseChange > -0.012
								|| jaLoseDiff > -0.011 && jcLoseChange > -0.012)
							&& yaLoseRt > 0.01 && aaLoseRt > -0.015) {
							killGps.add(ResultGroup.Zero);
						}
					} else {
						if ((maWinRt > 0.06 && majorWinChange > -0.021 && majorLoseChange < 0.021
								|| iaWinRt > 0.051 && interWinChange > 0.011 && interLoseChange < 0.021
								|| aaWinRt > 0.021 && aomenWinChange > -0.011 && aomenLoseChange < 0.021)
								&& (aaWinRt + aomenWinChange > -0.041)
								&& waWinRt > -0.011 && willWinChange > -0.021) {
							killGps.add(ResultGroup.Three);
						}
						if ((maDrawRt > 0.051 && majorDrawChange > -0.025
								|| iaDrawRt > 0.051 && interDrawChange > -0.011
								|| aaDrawRt > 0.021 && aomenDrawChange > -0.011)
								&& (aaDrawRt + aomenDrawChange > -0.031)
								&& (iaDrawRt + interDrawChange > -0.021)
								&& waDrawRt > -0.011 && willDrawChange > -0.02) {
							killGps.add(ResultGroup.One);
						}
						if ((maLoseRt > 0.51 && majorLoseChange > -0.025 && majorWinChange < 0.021
								|| iaLoseRt > 0.051 && interLoseChange > 0.011 && interWinChange < 0.021
								|| MatchContinent.Euro == league.getContinent() ? aaLoseRt > 0.005 : aaLoseRt > 0.021
										&& aomenLoseChange > -0.011 && aomenWinChange < 0.021)
								&& (aaLoseRt + aomenLoseChange > -0.031)
								&& waLoseRt > -0.011 && willLoseChange > -0.011) {
							killGps.add(ResultGroup.Zero);
						}
					}
				} else if (aomenPk >= -1) {
					if (isAomenMajor) {
						if ((aaWinRt > -0.001 && aomenWinChange > -0.001
//								|| yaWinRt > 0.041 && ysbWinChange > -0.001
								|| jaWinDiff > 0.001 && jcWinChange > -0.001)
							&& yaWinRt > 0.01 && aaWinRt > -0.01) {
							killGps.add(ResultGroup.Three);
						}
						if ((aaDrawRt > 0.021 && aomenDrawChange > -0.001
//								|| yaDrawRt > 0.041 && ysbDrawChange > -0.012
								|| jaDrawDiff > 0.041 && jcDrawChange > -0.012)
							&& yaDrawRt > 0.01 && aaDrawRt > -0.015) {
							killGps.add(ResultGroup.One);
						}
						if ((aaLoseRt > 0.021 && aomenLoseChange > -0.001
//								|| yaLoseRt > 0.045 && ysbLoseChange > -0.012
								|| jaLoseDiff > -0.031 && jcLoseChange > -0.012)
							&& yaLoseRt > 0.01 && aaLoseRt > -0.015) {
							killGps.add(ResultGroup.Zero);
						}
					} else {
						if ((maWinRt > 0.06 && majorWinChange > -0.021 && majorLoseChange < 0.021
								|| iaWinRt > 0.051 && interWinChange > 0.011 && interLoseChange < 0.021
								|| aaWinRt > 0.021 && aomenWinChange > -0.011 && aomenLoseChange < 0.021)
								&& (aaWinRt + aomenWinChange > -0.041)
								&& waWinRt > -0.011 && willWinChange > -0.021) {
							killGps.add(ResultGroup.Three);
						}
						if ((maDrawRt > 0.051 && majorDrawChange > -0.025
								|| iaDrawRt > 0.051 && interDrawChange > -0.011
								|| aaDrawRt > 0.021 && aomenDrawChange > -0.011)
								&& (aaDrawRt + aomenDrawChange > -0.031)
								&& (iaDrawRt + interDrawChange > -0.021)
								&& waDrawRt > -0.011 && willDrawChange > -0.02) {
							killGps.add(ResultGroup.One);
						}
						if ((maLoseRt > 0.51 && majorLoseChange > -0.025 && majorWinChange < 0.021
								|| iaLoseRt > 0.051 && interLoseChange > 0.011 && interWinChange < 0.021
								|| MatchContinent.Euro == league.getContinent() ? aaLoseRt > 0.005 : aaLoseRt > 0.021
										&& aomenLoseChange > -0.011 && aomenWinChange < 0.021)
								&& (aaLoseRt + aomenLoseChange > -0.031)
								&& waLoseRt > -0.011 && willLoseChange > -0.011) {
							killGps.add(ResultGroup.Zero);
						}
					}
				} else {
					if ((maWinRt > 0.06 && majorWinChange > -0.021 && majorLoseChange < 0.021
							|| iaWinRt > 0.051 && interWinChange > 0.011 && interLoseChange < 0.021
							|| aaWinRt > 0.011 && aomenWinChange > -0.011 && aomenLoseChange < 0.021)
							&& (aaWinRt + aomenWinChange > -0.041)
							&& aaLoseRt < -0.015
							&& waWinRt > 0.021 && willWinChange > -0.021) {
						killGps.add(ResultGroup.Three);
					}
					if ((maDrawRt > 0.051 && majorDrawChange > -0.025
							|| iaDrawRt > 0.051 && interDrawChange > -0.011
							|| aaDrawRt > 0.021 && aomenDrawChange > -0.011)
							&& (aaDrawRt + aomenDrawChange > -0.031)
							&& (iaDrawRt + interDrawChange > -0.021)
							&& aaLoseRt < -0.015
							&& waDrawRt > 0.011 && willDrawChange > -0.02) {
						killGps.add(ResultGroup.One);
					}
					if ((maLoseRt > 0.061 && majorLoseChange > -0.025 && majorWinChange < 0.021
							|| iaLoseRt > 0.061 && interLoseChange > 0.011 && interWinChange < 0.021
							|| aaLoseRt > 0.021 && aomenLoseChange > -0.011 && aomenWinChange < 0.021)
							&& (aaLoseRt + aomenLoseChange > -0.031)
							&& waLoseRt > 0.011 && willLoseChange > -0.011) {
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
			MatchState matchState, Float predictPk, AsiaPl main, AsiaPl current, EuroMatrices euroMatrices,
			boolean isSameCityOrNeutral, League league) {
		if (rgs == null
				|| clubMatrices.getHostAllMatrix() != null
					&& clubMatrices.getHostAllMatrix().getNum() < 5) {
			rgs = new TreeSet<ResultGroup> ();
		}
		
		Pair<LatestMatchMatrices, LatestMatchMatrices> pair = MatchStateUtil.getComparedLatestMatchMatrices(matchState,
				isSameCityOrNeutral, league, false);
		
		LatestMatchMatrices hostMatches = pair.getFirst();
		LatestMatchMatrices guestMatches = pair.getSecond();
		
		if (hostMatches == null || guestMatches == null) {
			return rgs;
		}

		float hostAttGuestDefComp = clubMatrices.getHostAttGuestDefInx();
		float guestAttHostDefComp = clubMatrices.getGuestAttHostDefInx();
		
		Float latestHostAttack = matchState.getHostAttackToGuest();
		Float latestGuestAttack = matchState.getGuestAttackToHost();
		
		boolean hasLatest = latestHostAttack != null && latestGuestAttack != null;
		PKDirection pkDirection = PanKouUtil.getPKDirection(current, main);
		float mainPk = MatchUtil.getCalculatedPk(main);

		/*
		 * Important!
		 * 
		 * the latest performance was usually used to mislead the bet, so refer to the base data at least...
		 */
		
		// host performed good lately
		// check base (host must be good) again to strong the prediction
		if ((hotPoint >= 6f || hasLatest && latestHostAttack > 1.5 * latestGuestAttack && hotPoint >= 2.5f)
				&& hostAttGuestDefComp > 1.45 * guestAttHostDefComp) {
			// host attack is good or guest defense is bad
			if (guestMatches.getMatchMiss() >= 1.0f && hostMatches.getMatchGoal() >= 1.2f
					|| hostMatches.getWinRate() >= 0.6f && guestMatches.getWinDrawRate() <= 0.6f) {
				// pk supported
				if (pkDirection.ordinal() > PKDirection.Downer.ordinal()
						&& PanKouUtil.isUpSupport(predictPk, mainPk, main.getPanKou())) {
					rgs.add(ResultGroup.Three);
				}
			}
		}
		// guest performed good lately
		// check base (guest must be good) again to strong the prediction
		else if ((hotPoint <= -6f || hasLatest && latestGuestAttack > 1.45 * latestHostAttack && hotPoint <= -2.5f)
					&& guestAttHostDefComp >= 1.35 * hostAttGuestDefComp) {
			// host attack is good or guest defense is bad
			if (hostMatches.getMatchMiss() >= 1.0f && guestMatches.getMatchGoal() >= 1.2f
						|| guestMatches.getWinRate() >= 0.6f && hostMatches.getWinDrawRate() <= 0.6f) {
				if (pkDirection.ordinal() < PKDirection.Uper.ordinal()
						&& PanKouUtil.isDownSupport(predictPk, mainPk, main.getPanKou())) {
					rgs.add(ResultGroup.Zero);
				}
			}
		}
		// host and guest performed nearly same
		else {
			float upCondition1 = current.getPanKou() >= 0.25f ? 1.0f : 0.85f;
			float upCondition2 = current.getPanKou() >= 0.25f ? 1.35f : 1.25f;
			
			// host performed not bad, and host base is not bad
			if (hotPoint >= -3f && hotPoint <= 6 && latestHostAttack >= upCondition1 * latestGuestAttack
					&& hostAttGuestDefComp >= upCondition1 * guestAttHostDefComp ) {
				// host somewhere (latest or base) is good
				if ((latestHostAttack >= upCondition2 * latestGuestAttack
						|| hostAttGuestDefComp >= upCondition2 * guestAttHostDefComp)
						&& PanKouUtil.isUpSupport(predictPk, mainPk, main.getPanKou())) {
					// pk support
					// base host is good and guest has miss and host has goal  or host is good
					if ((current.getPanKou() >= 0.25f ? pkDirection.ordinal() > PKDirection.Down.ordinal()
								: pkDirection.ordinal() > PKDirection.Middle.ordinal())
							&& (hostMatches.getMatchGoal() >= 1.2f && guestMatches.getMatchMiss() >= 1.0f
									&& hostAttGuestDefComp - guestAttHostDefComp >= 0.35
								|| hostMatches.getWinRate() >= 0.6f && guestMatches.getWinDrawRate() <= 0.4f)) {
						rgs.add(ResultGroup.Three);
					}
					// pk support
					// base has no big gap
					// latest both has draw
					if ((current.getPanKou() >= 0.25f ? pkDirection.ordinal() < PKDirection.Up.ordinal()
							: pkDirection.ordinal() > PKDirection.Downer.ordinal())
							&& (hostAttGuestDefComp <= upCondition2 || hostAttGuestDefComp - guestAttHostDefComp <= 0.4f)
							&& hostMatches.getWinDrawRate() - hostMatches.getWinRate() >= 0.199f
							&& guestMatches.getWinDrawRate() >= 0.4f
							&& guestMatches.getWinDrawRate() - guestMatches.getWinRate() >= 0.199f) {
						rgs.add(ResultGroup.One);
					}
				}
			}
			
			float downCondition1 = current.getPanKou() <= 0f ? 0.95f : 0.85f;
			float downCondition2 = current.getPanKou() <= 0f ? 1.25f : 1.15f;
			
			// guest performed not bad, and guest base is not bad
			if (hotPoint <= 3f && hotPoint >= -6 && latestGuestAttack >= downCondition1 * latestHostAttack
					&& guestAttHostDefComp >= hostAttGuestDefComp) {
				// guest somewhere (latest or base) is good
				if ((latestGuestAttack >= downCondition2 * latestHostAttack
						|| guestAttHostDefComp >= downCondition2 * hostAttGuestDefComp)
						&& PanKouUtil.isDownSupport(predictPk, mainPk, main.getPanKou())) {
					if ((current.getPanKou() <= 0f ? pkDirection.ordinal() < PKDirection.Up.ordinal()
								: pkDirection.ordinal() < PKDirection.Middle.ordinal())
							&& (guestMatches.getMatchGoal() >= 1.2f && hostMatches.getMatchMiss() >= 1.0f
									&& guestAttHostDefComp - hostAttGuestDefComp >= 0.28
								|| guestMatches.getWinRate() >= 0.6f && hostMatches.getWinDrawRate() <= 0.4f)) {
						rgs.add(ResultGroup.Zero);
					} 
					if ((current.getPanKou() <= 0f ? pkDirection.ordinal() > PKDirection.Down.ordinal()
							: pkDirection.ordinal() < PKDirection.Uper.ordinal())
							&& ((guestAttHostDefComp <= upCondition2 || guestAttHostDefComp - hostAttGuestDefComp <= 0.35)
							&& guestMatches.getWinDrawRate() - guestMatches.getWinRate() >= 0.199f
							&& hostMatches.getWinDrawRate() >= 0.2f
							&& hostMatches.getWinDrawRate() - hostMatches.getWinRate() >= 0.199f)) {
						rgs.add(ResultGroup.One);
					}
				}
			}
		}

		return rgs;
	}
	
	private Set<ResultGroup> promoteByBase1 (Set<ResultGroup> rgs, Float hotPoint, ClubMatrices clubMatrices,
			MatchState matchState, Float predictPk, AsiaPl main, AsiaPl current, EuroMatrices euroMatrices,
			boolean isSameCityOrNeutral, League league) {
		if (rgs == null
				|| clubMatrices.getHostAllMatrix() != null
					&& clubMatrices.getHostAllMatrix().getNum() < 5) {
			rgs = new TreeSet<ResultGroup> ();
		}
		
		Pair<LatestMatchMatrices, LatestMatchMatrices> pair = MatchStateUtil.getComparedLatestMatchMatrices(matchState,
				isSameCityOrNeutral, league, false);
		
		LatestMatchMatrices hostMatches = pair.getFirst();
		LatestMatchMatrices guestMatches = pair.getSecond();
		
		if (hostMatches == null || guestMatches == null) {
			return rgs;
		}

		float hostAttGuestDefComp = clubMatrices.getHostAttGuestDefInx();
		float guestAttHostDefComp = clubMatrices.getGuestAttHostDefInx();
		
		Float latestHostAttack = matchState.getHostAttackToGuest();
		Float latestGuestAttack = matchState.getGuestAttackToHost();
		
		boolean hasLatest = latestHostAttack != null && latestGuestAttack != null;
		PKDirection pkDirection = PanKouUtil.getPKDirection(current, main);
		float mainPk = MatchUtil.getCalculatedPk(main);

		// host base is good
		if (hostAttGuestDefComp >= guestAttHostDefComp) {
			// 1. host is very good
			if (hostAttGuestDefComp >= 1.45 * guestAttHostDefComp) {
				// 2. latest is very good  -> host is very hot!!!!! -> careful about the opposite side
				if (latestHostAttack >= 1.45 * latestGuestAttack
						|| hotPoint >= 6.0f && latestHostAttack >= latestGuestAttack) {
					if (// no the opposite sign
						pkDirection.ordinal() > PKDirection.Down.ordinal() // 3. pk support
						&& hostAttGuestDefComp - guestAttHostDefComp >= 0.35f
							) {
						rgs.add(ResultGroup.Three);
					}
					// else - no 
				}
				// 2. latest is not bad and guest is not too good
				else if (latestHostAttack >= latestGuestAttack
						|| hotPoint <= 6.0f && hotPoint >= 3.0f && latestHostAttack >= 0.8 * latestGuestAttack) {
					if ((current.getPanKou() >= 0.5f ? pkDirection.ordinal() > PKDirection.Down.ordinal()
								: pkDirection.ordinal() > PKDirection.Middle.ordinal()) // 3. pk support
							&& guestMatches.getWinDrawRate() <= 0.6f // 3. guest is not good
							&& hostMatches.getWinRate() >= 0.4f && hostMatches.getWinDrawRate() >= 0.6f// 3. host is not bad
							&& hostAttGuestDefComp - guestAttHostDefComp >= 0.35f
						) {
						rgs.add(ResultGroup.Three);
					}
				} else if (latestHostAttack >= 0.7 * latestGuestAttack
						|| hotPoint <= 3.0f && hotPoint >= -3.0f && latestHostAttack >= 0.65 * latestGuestAttack) {
					if (pkDirection.ordinal() > PKDirection.Middle.ordinal() // 3. pk support
							&& guestMatches.getWinDrawRate() <= 0.6f // 3. guest is not good
							&& hostMatches.getWinRate() >= 0.4f && hostMatches.getWinDrawRate() >= 0.6f// 3. host is not bad
							&& hostAttGuestDefComp - guestAttHostDefComp >= 0.35f
						) {
						rgs.add(ResultGroup.Three);
					}
					if ((current.getPanKou() >= 0.5f ? pkDirection.ordinal() < PKDirection.Middle.ordinal()
								: pkDirection.ordinal() < PKDirection.Up.ordinal()) // 3. pk support
							&& (guestMatches.getWinDrawRate() - guestMatches.getWinRate() >= 0.199f // 3. guest has draw
									&& hostMatches.getWinDrawRate() - hostMatches.getWinRate() >= 0.199f // 3. host has draw
								|| hostAttGuestDefComp - guestAttHostDefComp <= 0.4f)
							) {
						rgs.add(ResultGroup.One);
					}
				}
				// else - no
			} else {
				// 2. latest is very good
				if (latestHostAttack >= 1.45 * latestGuestAttack
						|| hotPoint >= 6.0f && latestHostAttack >= latestGuestAttack) {
					if (pkDirection.ordinal() > PKDirection.Middle.ordinal() // 3. pk support
							&& guestMatches.getWinDrawRate() <= 0.6f // 3. guest is not good
							&& hostMatches.getWinRate() >= 0.4f && hostMatches.getWinDrawRate() >= 0.6f// 3. host is not bad
							&& hostMatches.getMatchGoal() >= 1.2f && guestMatches.getMatchMiss() >= 1.0f
							) {
						rgs.add(ResultGroup.Three);
					}
					if (pkDirection.ordinal() < PKDirection.Up.ordinal() // 3. pk support
							&& guestMatches.getWinDrawRate() >= 0.4f && guestMatches.getWinDrawRate() - guestMatches.getWinRate() >= 0.199f // 3. guest has draw
							&& hostMatches.getWinDrawRate() - hostMatches.getWinRate() >= 0.199f // 3. host has draw
							) {
						rgs.add(ResultGroup.One);
					}
					// else - no 
				}
				// 2. latest is not bad and guest is not too good
				else if (latestHostAttack >= 0.7 * latestGuestAttack
							|| hotPoint <= 6.0f && hotPoint >= -3.0f && latestHostAttack >= 0.6 * latestGuestAttack) {
					if (pkDirection.ordinal() > PKDirection.Middle.ordinal() // 3. pk support
							&& guestMatches.getWinDrawRate() <= 0.6f // 3. guest is not good
							&& hostMatches.getWinRate() >= 0.4f && hostMatches.getWinDrawRate() >= 0.6f// 3. host is not bad
							&& hostMatches.getMatchGoal() >= 1.2f && guestMatches.getMatchMiss() >= 1.0f
						) {
						rgs.add(ResultGroup.Three);
					}
					if (pkDirection.ordinal() < PKDirection.Up.ordinal() // 3. pk support
							&& guestMatches.getWinDrawRate() >= 0.4f && guestMatches.getWinDrawRate() - guestMatches.getWinRate() >= 0.199f // 3. guest has draw
							&& hostMatches.getWinDrawRate() - hostMatches.getWinRate() >= 0.199f // 3. host has draw
							) {
						rgs.add(ResultGroup.One);
					}
					if (pkDirection.ordinal() < PKDirection.Middle.ordinal() // 3. pk support
							&& hostMatches.getWinRate() <= 0.4f && hostMatches.getWinDrawRate() <= 0.6f // 3. host is not good
							&& guestMatches.getWinRate() >= 0.4f && guestMatches.getWinDrawRate() >= 0.6f// 3. guest is not bad
							) {
						rgs.add(ResultGroup.Zero);
					}
				}
				// else - no
			}
		} else if (guestAttHostDefComp >= hostAttGuestDefComp) {
			if (guestAttHostDefComp >= 1.4 * hostAttGuestDefComp) {
				// 2. latest is very good  -> guest is very hot!!!!! -> careful about the opposite side
				if (latestGuestAttack >= 1.4 * latestHostAttack
						|| hotPoint <= -6.0f && latestGuestAttack >= latestHostAttack) {
					if (// TODO - no the opposite sign
						pkDirection.ordinal() < PKDirection.Up.ordinal() // 3. pk support
						&& guestAttHostDefComp - hostAttGuestDefComp >= 0.3f
							) {
						rgs.add(ResultGroup.Zero);
					}
					// else - no 
				}
				// 2. latest is not bad and host is not too good
				else if (latestGuestAttack >= latestHostAttack
							|| hotPoint <= -3.0f && hotPoint >= -6.0f && latestGuestAttack >= 0.8 * latestHostAttack) {
					if ((current.getPanKou() <= -0.5f ? pkDirection.ordinal() < PKDirection.Up.ordinal()
								: pkDirection.ordinal() < PKDirection.Middle.ordinal()) // 3. pk support
							&& hostMatches.getWinDrawRate() <= 0.6f // 3. host is not good
							&& guestMatches.getWinRate() >= 0.4f && guestMatches.getWinDrawRate() >= 0.6f// 3. guest is not bad
							&& guestAttHostDefComp - hostAttGuestDefComp >= 0.3f
						) {
						rgs.add(ResultGroup.Zero);
					}
				} else if (latestGuestAttack >= 0.7 * latestHostAttack
						|| hotPoint <= 3.0f && hotPoint >= -3.0f && latestGuestAttack >= 0.5 * latestHostAttack) {
					if (pkDirection.ordinal() < PKDirection.Middle.ordinal() // 3. pk support
							&& hostMatches.getWinDrawRate() <= 0.6f // 3. host is not good
							&& guestMatches.getWinRate() >= 0.4f && guestMatches.getWinDrawRate() >= 0.6f// 3. guest is not bad
							&& guestAttHostDefComp - hostAttGuestDefComp >= 0.3f
						) {
						rgs.add(ResultGroup.Zero);
					}
					if ((current.getPanKou() <= -0.5f ? pkDirection.ordinal() > PKDirection.Middle.ordinal()
								: pkDirection.ordinal() > PKDirection.Up.ordinal()) // 3. pk support
							&& (guestMatches.getWinDrawRate() - guestMatches.getWinRate() >= 0.199f // 3. guest has draw
									&& hostMatches.getWinDrawRate() - hostMatches.getWinRate() >= 0.199f // 3. host has draw
								|| guestAttHostDefComp - hostAttGuestDefComp <= 0.35f)
							) {
						rgs.add(ResultGroup.One);
					}
				}
				// else - no
			} else {
				// 2. latest is very good
				if (latestGuestAttack >= 1.4 * latestHostAttack
						|| hotPoint <= -6.0f && latestGuestAttack >= latestHostAttack) {
					if (pkDirection.ordinal() < PKDirection.Middle.ordinal() // 3. pk support
							&& hostMatches.getWinDrawRate() <= 0.6f // 3. host is not good
							&& guestMatches.getWinRate() >= 0.4f && guestMatches.getWinDrawRate() >= 0.6f// 3. guest is not bad
							&& guestMatches.getMatchGoal() >= 1.0f && hostMatches.getMatchMiss() >= 1.0f
							) {
						rgs.add(ResultGroup.Zero);
					}
					if (pkDirection.ordinal() > PKDirection.Down.ordinal() // 3. pk support
							&& guestMatches.getWinDrawRate() - guestMatches.getWinRate() >= 0.199f // 3. guest has draw
							&& hostMatches.getWinDrawRate() >= 0.4f && hostMatches.getWinDrawRate() - hostMatches.getWinRate() >= 0.199f // 3. host has draw
							) {
						rgs.add(ResultGroup.One);
					}
					// else - no 
				}
				// 2. latest is not bad and guest is not too good
				else if (latestGuestAttack >= 0.7 * latestHostAttack 
						|| hotPoint <= 3.0f && hotPoint >= -6.0f && latestGuestAttack >= 0.5 * latestHostAttack) {
					if (pkDirection.ordinal() < PKDirection.Middle.ordinal() // 3. pk support
							&& hostMatches.getWinDrawRate() <= 0.6f // 3. guest is not good
							&& guestMatches.getWinRate() >= 0.4f && guestMatches.getWinDrawRate() >= 0.6f// 3. host is not bad
							&& guestMatches.getMatchGoal() >= 1.0f && hostMatches.getMatchMiss() >= 1.0f
						) {
						rgs.add(ResultGroup.Zero);
					}
					if (pkDirection.ordinal() > PKDirection.Down.ordinal() // 3. pk support
							&& guestMatches.getWinDrawRate() - guestMatches.getWinRate() >= 0.199f // 3. guest has draw
							&& hostMatches.getWinDrawRate() >= 0.4f && hostMatches.getWinDrawRate() - hostMatches.getWinRate() >= 0.199f // 3. host has draw
							) {
						rgs.add(ResultGroup.One);
					}
					if (pkDirection.ordinal() > PKDirection.Middle.ordinal() // 3. pk support
							&& guestMatches.getWinRate() <= 0.4f && guestMatches.getWinDrawRate() <= 0.6f // 3. host is not good
							&& hostMatches.getWinRate() >= 0.4f && hostMatches.getWinDrawRate() >= 0.6f// 3. guest is not bad
							) {
						rgs.add(ResultGroup.Three);
					}
				}
				// else - no
			}
		}

		return rgs;
	}
	
	private Set<ResultGroup> promoteByPk (Set<ResultGroup> rgs, PankouMatrices pkMatrices, Float predictPk, Float hotPoint,
			MatchState matchState, ClubMatrices clubMatrices, MatchExchangeData exchange, EuroMatrices euroMatrices,
			boolean isSameCityOrNeutral, League league) {
		if (pkMatrices != null && predictPk != null && matchState != null && clubMatrices != null) {
			AsiaPl main = pkMatrices.getMainPk();
			AsiaPl current = pkMatrices.getCurrentPk();
			Float hWinChgRtFloat = pkMatrices.getHwinChangeRate();
			Float gWinChgRtFloat = pkMatrices.getAwinChangeRate();

			float mainPk = MatchUtil.getCalculatedPk(main);
			float currentPk = MatchUtil.getCalculatedPk(current);

			promoteByBase1(rgs, hotPoint, clubMatrices, matchState, predictPk,
					main, current, euroMatrices, isSameCityOrNeutral, league);
			
			Pair<LatestMatchMatrices, LatestMatchMatrices> pair = MatchStateUtil.getComparedLatestMatchMatrices(matchState,
					isSameCityOrNeutral, league, true);
			
			LatestMatchMatrices hostMatches = pair.getFirst();
			LatestMatchMatrices guestMatches = pair.getSecond();
			
			Map<Company, EuroMatrix> companyEus = euroMatrices.getCompanyEus();
			EuroMatrix aomenEu = companyEus.get(Company.Aomen);
			EuroMatrix jincai = companyEus.get(Company.Jincai);
			EuroPl euroAvg = euroMatrices.getCurrEuroAvg();
			float jaWinDiff = MatchUtil.getEuDiff(jincai.getCurrentEuro().getEWin(), euroAvg.getEWin(), false);
			float jaDrawDiff = MatchUtil.getEuDiff(jincai.getCurrentEuro().getEDraw(), euroAvg.getEDraw(), false);
			float jaLoseDiff = MatchUtil.getEuDiff(jincai.getCurrentEuro().getELose(), euroAvg.getELose(), false);
			
			if (hostMatches == null || guestMatches == null) {
				return rgs;
			}

			List <TeamLabel> hostLabels = clubMatrices.getHostLabels();
//			List <TeamLabel> guestLabels = clubMatrices.getGuestLabels();
//			float hostAtt = clubMatrices.getHostAttGuestDefInx();
//			float guestAtt = clubMatrices.getGuestAttHostDefInx();
//			float hostGuestGoal = hostAtt / guestAtt;

			if (main.getPanKou() >= 1) {
				// a1. host pay is low
				if (hWinChgRtFloat < LOW_PK_POINT
						&& (current.gethWin() <= 0.86f || main.gethWin() <= 0.86f)
						&& aomenEu.getWinChange() < 0.015) {
					// b1. company think high of the host
					if (mainPk - predictPk > 0.29) {
						// c1. host is good
						if ((hostMatches.getWinPkRate() >= 0.5 || hostMatches.getWinDrawPkRate() >= 0.8)) {
							rgs.add(ResultGroup.RangThree);
						}
					}
					// b2. host is think low, ignore...
					else if (predictPk - mainPk > 0.38) {
						// ignore...
					}
					// b2. the predicted and real is near
					else if (Math.abs(predictPk - mainPk) <= 0.25) {
						// c2. host perform good, guest perform bad
						if ((hostMatches.getWinPkRate() >= 0.4 || hostMatches.getWinDrawPkRate() >= 0.6)
								&& (guestMatches.getWinPkRate() <= 0.4 || guestMatches.getWinDrawPkRate() <= 0.6)) {
							rgs.add(ResultGroup.RangThree);
						}
					}
				}
				// a2. guest pay is low
				else if (gWinChgRtFloat < LOW_PK_POINT 
						&& (current.getaWin() <= 0.84f || main.getaWin() <= 0.84f) 
						&& aomenEu.getLoseChange() < -0.001) {
					// b2. company think low,
					if (currentPk <= mainPk && predictPk - mainPk > 0.25
							&& (guestMatches.getWinPkRate() >= 0.4 || guestMatches.getWinDrawPkRate() >= 0.6)) {
						rgs.add(ResultGroup.RangZero);
					}
				}
			} else if (main.getPanKou() > 0.4) {
				if (hWinChgRtFloat < LOW_PK_POINT 
						&& (current.gethWin() <= 0.86f || main.gethWin() <= 0.86f)
						&& aomenEu.getWinChange() < 0.012) {
					// company think high of the host
					if (mainPk - predictPk > 0.45) {
						// predict is not correct
					} else if (mainPk - predictPk > 0.25) {
						// host is pretty good, or guest is pretty bad (this is not matter,
						// if guest is too bad, then the predict pk should be high, not align with the assumption), or host is good guest is bad,
						if ((hostMatches.getWinRate() >= 0.6f && guestMatches.getWinRate() <= 0.4f
								|| guestMatches.getWinDrawRate() <= 0.4f
								|| (hostMatches.getWinRate() >= 0.4f && hostMatches.getWinPkRate() >= 0.4f
									|| guestMatches.getWinDrawRate() <= 0.6f && guestMatches.getWinDrawPkRate() <= 0.6f))) {
							rgs.add(ResultGroup.Three);
						}
					}
					else if (mainPk - predictPk > -0.16) {
						// host is good and stable, guest is not good
						if (hostMatches.getWinRate() >= 0.4f && hostMatches.getWinPkRate() >= 0.4f
								&& hostMatches.getMatchGoal() > hostMatches.getMatchMiss()
								&& isPkSupportHot(predictPk, mainPk, hotPoint)
								&& guestMatches.getWinPkRate() <= 0.4f && guestMatches.getWinDrawPkRate() <= 0.6f) {
							rgs.add(ResultGroup.Three);
						}
						// pk is support, and host is good or guest is bad
						else if ((currentPk - mainPk >= 0.08f || current.gethWin() <= 0.82f)
								&& isPkSupportHot(predictPk, mainPk, hotPoint)
								&& (hostMatches.getWinRate() >= 0.4f || hostMatches.getWinPkRate() >= 0.6f)
									&& (guestMatches.getWinDrawRate() <= 0.6f && guestMatches.getWinDrawPkRate() <= 0.6f)) {
							rgs.add(ResultGroup.Three);
						}
					} else {
						// think high of guest.. check below
					}
				}
				else if (gWinChgRtFloat < LOW_PK_POINT 
						&& (current.getaWin() <= 0.86f || main.getaWin() <= 0.86f) 
						&& aomenEu.getLoseChange() < 0.01) {
					// company think high of the guest
					if (predictPk - mainPk > 0.45) {
						// predict is not correct
					} else if (predictPk - mainPk > 0.22) {
						// guest is pretty good, or host is pretty bad, or guest is good guest is bad,
						if (!MatchUtil.isHostHomeStrong(hostLabels)
								&& (jaLoseDiff < 0.011 && jaDrawDiff < 0.011)
								&& (guestMatches.getWinPkRate() >= 0.6f && hostMatches.getWinRate() <= 0.6f
									|| hostMatches.getWinDrawRate() <= 0.4f
									|| (guestMatches.getWinPkRate() >= 0.4f || guestMatches.getWinDrawPkRate() >= 0.6f)
										&& (hostMatches.getWinRate() <= 0.4f && hostMatches.getWinDrawRate() <= 0.6f))) {
							rgs.add(ResultGroup.One);
							rgs.add(ResultGroup.Zero);
						}
					}
					else if (predictPk - mainPk > -0.18) {
						if (!MatchUtil.isHostHomeStrong(hostLabels) && isPkSupportHot(predictPk, mainPk, hotPoint)
								&& (jaLoseDiff < 0.011 && jaDrawDiff < 0.011)
								&& (mainPk - currentPk >= 0.08 || current.getaWin() <= 0.84f)
								&& ((guestMatches.getWinPkRate() >= 0.4f || guestMatches.getWinDrawPkRate() >= 0.6f)
									&& (hostMatches.getWinRate() <= 0.4f && hostMatches.getWinDrawRate() <= 0.6f))) {
							rgs.add(ResultGroup.One);
							rgs.add(ResultGroup.Zero);
						}
					}
				}
			} else if (main.getPanKou() > -0.4) {
				if (hWinChgRtFloat < LOW_PK_POINT 
						&& (current.gethWin() <= 0.86f || main.gethWin() <= 0.86f)  
						&& aomenEu.getWinChange() < 0.01) {
					if (mainPk - predictPk > 0.45) {
						// predict is not correct
					}
					// company think high of the host
					else if (mainPk - predictPk > 0.21) {
						// host is pretty good (the teams are in same level, check win pk rate, instead of win rate),
						//  or guest is pretty bad (still check the pk rate),
						if ((jaWinDiff < 0.001 && jaDrawDiff < 0.011)
								&& (hostMatches.getWinPkRate() >= 0.6f && guestMatches.getWinRate() <= 0.6f
									|| guestMatches.getWinDrawPkRate() <= 0.4f
									|| (hostMatches.getWinPkRate() >= 0.4f || hostMatches.getWinDrawPkRate() >= 0.6f)
										&& guestMatches.getWinPkRate() <= 0.4f && guestMatches.getWinDrawPkRate() <= 0.6f)) {
							rgs.add(ResultGroup.Three);
							rgs.add(ResultGroup.One);
						}
					} else if (mainPk - predictPk > -0.16) {
						// host performs good or guest performs bad
						if ((currentPk - mainPk >= 0.08f || current.gethWin() <= 0.84f)
								&& (jaWinDiff < 0.001 && jaDrawDiff < 0.011)
								&& isPkSupportHot(predictPk, mainPk, hotPoint)
								&& (hostMatches.getWinDrawRate() >= 0.6f || hostMatches.getWinDrawPkRate() >= 0.6f)
								&& (guestMatches.getWinRate() <= 0.4f || guestMatches.getWinPkRate() <= 0.6f)) {
							rgs.add(ResultGroup.Three);
							rgs.add(ResultGroup.One);
						}
					} else {
						// think high of guest.. check below
					}
				} else if (gWinChgRtFloat < LOW_PK_POINT 
						&& (current.getaWin() <= 0.86f || main.getaWin() <= 0.86f)  
						&& aomenEu.getLoseChange() < 0.01) {
					if (predictPk - mainPk > 0.45) {
						// predict is not correct
					}
					// company think high of the guest
					else if (predictPk - mainPk > 0.21) {
						if (guestMatches.getWinRate() >= 0.6f && hostMatches.getWinRate() <= 0.6f
								|| hostMatches.getWinDrawPkRate() <= 0.4f
								|| (guestMatches.getWinPkRate() >= 0.4f || guestMatches.getWinDrawPkRate() >= 0.6f)
									&& hostMatches.getWinPkRate() <= 0.4f && hostMatches.getWinDrawPkRate() <= 0.6f) {
							rgs.add(ResultGroup.One);
							rgs.add(ResultGroup.Zero);
						}
					}
					else if (predictPk - mainPk > -0.12) {
						// host performs good or guest performs bad
						if (mainPk >= currentPk && isPkSupportHot(predictPk, mainPk, hotPoint)
								&& ((guestMatches.getWinPkRate() >= 0.4f || guestMatches.getWinDrawPkRate() >= 0.6f)
										&& (hostMatches.getWinPkRate() <= 0.4f && hostMatches.getWinDrawPkRate() <= 0.6f))) {
							rgs.add(ResultGroup.One);
							rgs.add(ResultGroup.Zero);
						}
					}
				}
			} else if (main.getPanKou() > -1) {
				if (hWinChgRtFloat < LOW_PK_POINT 
						&& (current.gethWin() <= 0.86f || main.gethWin() <= 0.86f)  
						&& aomenEu.getWinChange() < 0.01) {
					if (mainPk - predictPk > 0.45) {
						// predict is not correct
					}
					// company think high of the host
					else if (mainPk - predictPk > 0.25) {
						// host is pretty good, or guest is pretty bad, or host is good guest is bad,
						if ((jaWinDiff < 0.011 && jaDrawDiff < 0.011)
								&& (hostMatches.getWinPkRate() >= 0.6f && guestMatches.getWinPkRate() <= 0.6f
									|| guestMatches.getWinDrawPkRate() <= 0.4f
									|| (hostMatches.getWinPkRate() >= 0.4f || hostMatches.getWinDrawPkRate() >= 0.6f)
										&& (guestMatches.getWinPkRate() <= 0.6f && guestMatches.getWinDrawPkRate() <= 0.6f))) {
							rgs.add(ResultGroup.Three);
							rgs.add(ResultGroup.One);
						}
					}
					else if (Math.abs(predictPk - mainPk) < 0.19) {
						// host performs good or guest performs bad
						if ((currentPk - mainPk >= 0.08f || current.gethWin() <= 0.84f)
								&& isPkSupportHot(predictPk, mainPk, hotPoint)
								&& (jaWinDiff < 0.011 && jaDrawDiff < 0.011)
								&& ((hostMatches.getWinPkRate() >= 0.4f || hostMatches.getWinDrawPkRate() >= 0.6f)
										&& (guestMatches.getWinPkRate() <= 0.4f && guestMatches.getWinDrawPkRate() <= 0.6f))) {
							rgs.add(ResultGroup.Three);
							rgs.add(ResultGroup.One);
						}
					} else {
						// think high of guest.. check below
					}
				}
				else if (gWinChgRtFloat < LOW_PK_POINT 
						&& (current.getaWin() <= 0.86f || main.getaWin() <= 0.86f)  
						&& aomenEu.getLoseChange() < 0.01) {
					if (predictPk - mainPk > 0.45) {
						// predict is not correct
					}
					// company think high of the guest
					else if (predictPk - mainPk > 0.25) {
						// guest is pretty good, or host is pretty bad, or guest is good guest is bad,
						if ((guestMatches.getWinRate() >= 0.8
									|| hostMatches.getWinDrawPkRate() <= 0.5
									|| (guestMatches.getWinRate() >= 0.4f && guestMatches.getWinDrawPkRate() >= 0.6f)
										&& (hostMatches.getWinPkRate() <= 0.4f && hostMatches.getWinDrawPkRate() <= 0.6f))) {
							rgs.add(ResultGroup.Zero);
						}
					}
					else if (predictPk - mainPk > -0.1) {
						if (currentPk <= mainPk && isPkSupportHot(predictPk, mainPk, hotPoint)
								&& ((guestMatches.getWinRate() >= 0.4f && guestMatches.getWinDrawPkRate() >= 0.6f)
										&& (hostMatches.getWinPkRate() <= 0.4f && hostMatches.getWinDrawPkRate() <= 0.6f))) {
							rgs.add(ResultGroup.Zero);
						}
					}
				}
			} else {
				if (hWinChgRtFloat < LOW_PK_POINT 
						&& (currentPk - mainPk >= 0.08f || current.gethWin() <= 0.84f) 
						&& aomenEu.getWinChange() < 0.01) {
					if (currentPk >= mainPk && mainPk - predictPk > 0.3
							&& (jaWinDiff < 0.011 && jaDrawDiff < 0.011)
							&& (hostMatches.getWinPkRate() >= 0.4f || hostMatches.getWinDrawPkRate() >= 0.6f)) {
						rgs.add(ResultGroup.RangThree);
					}
				}
				// a2. guest pay is low
				else if (gWinChgRtFloat < LOW_PK_POINT 
						&& (current.getaWin() <= 0.86f || main.getaWin() <= 0.86f)  
						&& aomenEu.getLoseChange() < 0.01) {
					// b1. company think high of the guest
					if (predictPk - mainPk > 0.3) {
						if ((guestMatches.getWinPkRate() >= 0.4f && guestMatches.getWinDrawPkRate() >= 0.6f)) {
							rgs.add(ResultGroup.RangZero);
						}
					}
					// b2. guest is think low, ignore...
					else if (mainPk - predictPk > 0.38) {
						// ignore...
					}
					// b2. the predicted and real is near
					else if (Math.abs(predictPk - mainPk) < 0.2) {
						// c2. guest perform good, host perform bad
						if ((guestMatches.getWinPkRate() >= 0.4f || guestMatches.getWinDrawPkRate() >= 0.6f)
								&& (hostMatches.getWinPkRate() <= 0.4f && hostMatches.getWinDrawPkRate() <= 0.6f)) {
							rgs.add(ResultGroup.RangZero);
						}
					}
				}
			}
		}

		return rgs;
	}
	
	private void promoteByEuro (Set<ResultGroup> promoteGps, EuroMatrices euMatrices,
			Float predictPk, PankouMatrices pkMatrices, League league, Float hotPoint,
			MatchState matchState, boolean isSameCityOrNeutral) {

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
			
			if (Math.abs(predictPk - mainPkRt) > 0.35) {
				return;
			}

			boolean isAomenMajor = EuroUtil.isAomenTheMajor(league);
			
			Pair<LatestMatchMatrices, LatestMatchMatrices> pair = MatchStateUtil.getComparedLatestMatchMatrices(matchState,
					isSameCityOrNeutral, league);
			
			LatestMatchMatrices hostMatches = pair.getFirst();
			LatestMatchMatrices guestMatches = pair.getSecond();

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
				float willWinChange = will.getWinChange();
				float willDrawChange = will.getDrawChange();
				float willLoseChange = will.getLoseChange();
				
				float laWinRt = MatchUtil.getEuDiff(currLabEu.getEWin(), euroAvg.getEWin(), false);
				float laDrawRt = MatchUtil.getEuDiff(currLabEu.getEDraw(), euroAvg.getEDraw(), false);
				float laLoseRt = MatchUtil.getEuDiff(currLabEu.getELose(), euroAvg.getELose(), false);
				
				PKDirection pkDirection = PanKouUtil.getPKDirection(aomenCurrPk, aomenMainPk);

				// the main company and lab is higher than the average, or aomen is higher than average and aomen is adjusted high
				// Direction is upper, no need to think of down side, just check up side is rational for euro pl;
				// Direction is up, need think of down side, need check if down side is possible;
				// Direction is down, need think of up side;
				// Direction is downer, no need think of up side;
				if (currPk >= 1) {
					if (isAomenMajor) {
						if (pkDirection.ordinal() > PKDirection.Down.ordinal()
								&& (guestMatches.getMatchMiss() >= 1.0f
										|| hostMatches.getWinRate() >= 0.5f && guestMatches.getWinDrawRate() <= 0.6f)
								&& aaWinRt <= -0.01 && aaWinRt + aomenWinChange < 0.001 && aomenWinChange < 0.01
								&& laWinRt < 0.035
								&& iaWinRt < -0.02) {
							promoteGps.add(ResultGroup.Three);
						}
						if (pkDirection.ordinal() < PKDirection.Middle.ordinal()
								// guest defend is good or guest performed good
								&& (guestMatches.getMatchMiss() <= 1.4f
									|| guestMatches.getWinDrawRate() >= 0.5f && hostMatches.getWinRate() <= 0.6f)
								&& hotPoint < 7
								&& aaDrawRt < -0.02 && aaDrawRt + aomenDrawChange < -0.015 && aomenDrawChange < 0.0
								&& waDrawRt < -0.01
								&& iaDrawRt < 0.01
								&& (aaWinRt > 0.001 || aomenWinChange > 0.021 && aaWinRt > -0.021)
								) {
							promoteGps.add(ResultGroup.One);
						}
						if (pkDirection.ordinal() < PKDirection.Middle.ordinal()
								// guest defend is good and guest has goal and host has miss 
								// or guest performed good
								&& (hostMatches.getMatchMiss() >= 1.0f
										&& guestMatches.getMatchGoal() >= 1.2f && guestMatches.getMatchMiss() <= 1.6f
									|| guestMatches.getWinRate() >= 0.4f && hostMatches.getWinRate() <= 0.6f)
								&& hotPoint < 7
								&& aaLoseRt < -0.03 && aaLoseRt + aomenLoseChange < -0.025 && aomenLoseChange < 0.0
								&& laLoseRt < 0.035
								&& iaLoseRt < 0.01
								&& (aaWinRt > 0.001 || aomenWinChange > 0.021 && aaWinRt > -0.021)
								) {
							promoteGps.add(ResultGroup.Zero);
						}
					} else {
						if (pkDirection.ordinal() > PKDirection.Down.ordinal()
								&& (guestMatches.getMatchMiss() >= 1.0f
									|| hostMatches.getWinRate() >= 0.5f && guestMatches.getWinDrawRate() <= 0.6f)
								&& maWinRt < 0.03 && majorWinChange < 0.03
								&& laWinRt < 0.045
								&& iaWinRt < 0.02
								&& jaWinDiff < -0.05
								&& aaWinRt < -0.015 && aomenWinChange < 0.01) {
							promoteGps.add(ResultGroup.Three);
						}
						if (pkDirection.ordinal() < PKDirection.Middle.ordinal()
								&& (guestMatches.getMatchMiss() <= 1.4f
									|| guestMatches.getWinDrawRate() >= 0.5f && hostMatches.getWinRate() <= 0.6f)
								&& hotPoint < 7
								&& maDrawRt < 0.02 && majorDrawChange < 0.02
								&& waDrawRt < -0.01
								&& iaDrawRt < -0.01
								&& aaDrawRt < -0.051 && aomenDrawChange < 0.01
								&& (aaWinRt > -0.015 || aomenWinChange > 0.02 && aaWinRt > -0.025)) {
							promoteGps.add(ResultGroup.One);
						}
						if (pkDirection.ordinal() < PKDirection.Middle.ordinal()
								&& (hostMatches.getMatchMiss() >= 1.0f
										&& guestMatches.getMatchGoal() >= 1.2f && guestMatches.getMatchMiss() <= 1.6f
									|| guestMatches.getWinRate() >= 0.4f && hostMatches.getWinRate() <= 0.6f)
								&& hotPoint < 7
								&& maLoseRt < 0.03 && maLoseRt < 0.02
								&& laLoseRt < 0.035
								&& iaLoseRt < -0.01
								&& aaLoseRt < -0.065 && aomenLoseChange < 0.01
								&& (aaWinRt > -0.015 || aomenWinChange > 0.02 && aaWinRt > -0.025)) {
							promoteGps.add(ResultGroup.Zero);
						}
					}
				} else if (currPk >= 0.4) {
					if (isAomenMajor) {
						if (pkDirection.ordinal() > PKDirection.Down.ordinal()
								&& (guestMatches.getMatchMiss() >= 1.0f
									|| hostMatches.getWinRate() >= 0.5f && guestMatches.getWinDrawRate() <= 0.6f)
								&& aaWinRt < -0.012 && aaWinRt + aomenWinChange < 0.001
								&& laWinRt < 0.035
								&& iaWinRt < 0.012
								) {
							promoteGps.add(ResultGroup.Three);
						}
						if (pkDirection.ordinal() < PKDirection.Up.ordinal()
								&& (guestMatches.getMatchMiss() <= 1.4f
									|| guestMatches.getWinDrawRate() >= 0.5f && hostMatches.getWinRate() <= 0.6f)
								&& hotPoint < 7
								&& aaDrawRt < -0.02 && aaDrawRt + aomenDrawChange < -0.015
								&& waDrawRt < 0.012
								&& iaDrawRt < 0.012
								&& (aaWinRt > 0.012 || aomenWinChange > 0.021 && aaWinRt > -0.011)
								) {
							promoteGps.add(ResultGroup.One);
						}
						if (pkDirection.ordinal() < PKDirection.Up.ordinal()
								&& (hostMatches.getMatchMiss() >= 1.0f
										&& guestMatches.getMatchGoal() >= 1.2f && guestMatches.getMatchMiss() <= 1.6f
									|| guestMatches.getWinDrawRate() >= 0.5f && hostMatches.getWinDrawRate() <= 0.6f)
								&& hotPoint < 7
								&& aaLoseRt < -0.03 && aaLoseRt + aomenLoseChange < -0.02
								&& laLoseRt < 0.035
								&& waLoseRt < 0.02
								&& iaLoseRt < -0.01
								&& (aaWinRt > 0.012 || aomenWinChange > 0.021 && aaWinRt > -0.011)
								) {
							promoteGps.add(ResultGroup.Zero);
						}
					} else {
						if (pkDirection.ordinal() > PKDirection.Down.ordinal()
								&& (guestMatches.getMatchMiss() >= 1.0f
									|| hostMatches.getWinRate() >= 0.5f && guestMatches.getWinDrawRate() <= 0.6f)
								&& maWinRt < 0.03 && majorWinChange < 0.03
								&& laWinRt < 0.04
								&& iaWinRt < 0.02 && iaWinChange < 0.02
								&& jaWinDiff < -0.05
								&& aaWinRt < -0.015 && aomenWinChange < 0.012) {
							promoteGps.add(ResultGroup.Three);
						}
						if (pkDirection.ordinal() < PKDirection.Up.ordinal()
								&& (guestMatches.getMatchMiss() <= 1.4f
									|| guestMatches.getWinDrawRate() >= 0.4f && hostMatches.getWinRate() <= 0.6f)
								&& hotPoint < 7
								&& maDrawRt < 0.021 && majorDrawChange < 0.02
								&& waDrawRt < 0.001
								&& iaDrawRt < 0.001
								&& aaDrawRt < -0.012 && aomenDrawChange < 0.012
								&& (aaWinRt > -0.015 || aomenWinChange > 0.021 && aaWinRt > -0.025)
								&& (waDrawRt < -0.025 && willDrawChange < 0.012
										|| iaDrawRt < -0.025 && iaDrawChange < 0.012
										|| aaDrawRt <= -0.045)) {
							promoteGps.add(ResultGroup.One);
						}
						if (pkDirection.ordinal() < PKDirection.Up.ordinal()
								&& (hostMatches.getMatchMiss() >= 1.0f
										&& guestMatches.getMatchGoal() >= 1.2f && guestMatches.getMatchMiss() <= 1.6f
									|| guestMatches.getWinRate() >= 0.4f && hostMatches.getWinRate() <= 0.6f)
								&& hotPoint < 6
								&& maLoseRt < 0.03 && majorLoseChange < 0.02
								&& laLoseRt < 0.045
								&& iaLoseRt < 0.01
								&& aaLoseRt < -0.02 && aomenLoseChange < 0.01
								&& (aaWinRt > -0.015 || aomenWinChange > 0.021 && aaWinRt > -0.025)
								&& (iaLoseRt < -0.03 && iaLoseChange < 0.01
										|| aaLoseRt <= -0.051)) {
							promoteGps.add(ResultGroup.Zero);
						}
					}
				} else if (currPk >= 0.25) {
					if (isAomenMajor) {
						// aomen gives the euro direction
						if (pkDirection.ordinal() > PKDirection.Middle.ordinal()
								&& (guestMatches.getMatchMiss() >= 1.0f
									|| hostMatches.getWinRate() >= 0.5f && guestMatches.getWinDrawRate() <= 0.6f)
								&& aaWinRt < -0.012 && aaWinRt + aomenWinChange < -0.001 && aomenWinChange < 0.012
								&& laWinRt < 0.04
								&& iaWinRt < 0.012) {
							promoteGps.add(ResultGroup.Three);
						}
						if ((pkDirection.ordinal() > PKDirection.Middle.ordinal()
									&& aaDrawRt < -0.02 && aaDrawRt + aomenDrawChange < -0.015 && aomenDrawChange < -0.001
								|| pkDirection.ordinal() < PKDirection.Middle.ordinal()
									&& aaDrawRt < -0.02 && aaDrawRt + aomenDrawChange < -0.015 && aomenDrawChange < 0.001)
								&& waDrawRt < 0.001
								&& iaDrawRt < 0.001
								&& (guestMatches.getWinDrawRate() >= 0.5f && hostMatches.getWinRate() <= 0.6f)
								) {
							promoteGps.add(ResultGroup.One);
						}
						if (pkDirection.ordinal() < PKDirection.Middle.ordinal()
								&& (hostMatches.getMatchGoal() <= 1.8f && hostMatches.getMatchMiss() >= 1.0f
										&& guestMatches.getMatchGoal() >= 1.0f
									|| guestMatches.getWinDrawRate() >= 0.5f && hostMatches.getWinRate() <= 0.6f)
								&& hotPoint < 6
								&& aaLoseRt < -0.03 && aaLoseRt + aomenLoseChange < -0.025 && aomenLoseChange < 0.001
								&& laLoseRt < 0.04
								&& iaLoseRt < 0.01) {
							promoteGps.add(ResultGroup.Zero);
						}
					} else {
						// aomen is just referring
						if (pkDirection.ordinal() > PKDirection.Middle.ordinal()
								&& (guestMatches.getMatchMiss() >= 1.0f
									|| hostMatches.getWinRate() >= 0.5f && guestMatches.getWinDrawRate() <= 0.6f)
								&& maWinRt < 0.03 && majorWinChange < 0.03
								&& laWinRt < 0.03
								&& iaWinRt < 0.01 && iaWinChange < 0.01
								&& jaWinDiff < -0.05
								&& aaWinRt < -0.015 && aomenWinChange < 0.01) {
							promoteGps.add(ResultGroup.Three);
						}
						if ((pkDirection.ordinal() > PKDirection.Middle.ordinal()
									&& aaDrawRt < 0.012 && aaDrawRt + aomenDrawChange < 0.015 && aomenDrawChange < -0.001
								|| pkDirection.ordinal() < PKDirection.Middle.ordinal()
									&& aaDrawRt < 0.012 && aaDrawRt + aomenDrawChange < 0.015 && aomenDrawChange < 0.001)
								&& waDrawRt < 0.022
								&& iaDrawRt < 0.012
								&& maDrawRt < 0.022 && maDrawRt + majorDrawChange < 0.022 && majorDrawChange < 0.012
								&& (guestMatches.getWinDrawRate() >= 0.6f && hostMatches.getWinRate() <= 0.6f)
								) {
							promoteGps.add(ResultGroup.One);
						}
						if (pkDirection.ordinal() < PKDirection.Middle.ordinal()
								&& (hostMatches.getMatchGoal() <= 1.8f && hostMatches.getMatchMiss() >= 1.0f
										&& guestMatches.getMatchGoal() >= 1.0f
									|| guestMatches.getWinDrawRate() >= 0.5f && hostMatches.getWinRate() <= 0.6f)
								&& hotPoint < 6.5
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
							&& aaWinRt < -0.02 && aomenWinChange < 0.01
							&& (guestMatches.getMatchMiss() >= 1.0f
								|| hostMatches.getWinRate() >= 0.5f && guestMatches.getWinDrawRate() <= 0.6f)) {
						promoteGps.add(ResultGroup.Three);
					}
					
					if (aaDrawRt < -0.02 && aomenDrawChange < 0.01
							&& waDrawRt < -0.01
							&& iaDrawRt < 0.01 && iaDrawChange < 0.01
							&& (guestMatches.getMatchGoal() <= 1.5f && hostMatches.getMatchGoal() <= 1.5f
								|| hostMatches.getWinRate() <= 0.5f && hostMatches.getWinDrawRate() >= 0.4f
									&& guestMatches.getWinRate() <= 0.6f && guestMatches.getWinDrawRate() >= 0.4f)) {
						promoteGps.add(ResultGroup.One);
					}
					
					if ((currentPkRt < mainPkRt - 0.05 || aomenCurrPk.getaWin() <= 0.92 && aomenMainPk.getaWin() <= 0.92)
							&& maLoseRt < 0.03 && majorLoseChange < 0.02
							&& laLoseRt < (isAomenMajor ? 0.035 : 0.045)
							&& iaLoseRt < -0.01 && iaLoseChange < 0.01
							&& jaLoseDiff < -0.05
							&& aaLoseRt < -0.02 && aomenLoseChange < 0.001
							&& (hostMatches.getMatchMiss() >= 1.0f
								|| guestMatches.getWinRate() >= 0.5f && hostMatches.getWinDrawRate() <= 0.6f)) {
						promoteGps.add(ResultGroup.Zero);
					}
				} else if (currPk >= -0.25) {
					if (isAomenMajor) {
						if (pkDirection.ordinal() > PKDirection.Middle.ordinal()
								&& (guestMatches.getMatchMiss() >= 1.2f
										&& hostMatches.getMatchGoal() >= 1.2f && hostMatches.getMatchMiss() <= 1.6f
									|| hostMatches.getWinDrawRate() >= 0.5f && guestMatches.getWinRate() <= 0.6f)
								&& hotPoint > -5.2
								&& aaWinRt < -0.03 && aaWinRt + aomenWinChange < -0.025 && aomenWinChange < 0.0
								&& laWinRt < 0.04
								&& iaWinRt < -0.012
								&& waWinRt < 0.001
								) {
							promoteGps.add(ResultGroup.Three);
						}
						if ((pkDirection.ordinal() > PKDirection.Middle.ordinal()
									&& aaDrawRt < -0.02 && aaDrawRt + aomenDrawChange < -0.015 && aomenDrawChange < 0.001
								|| pkDirection.ordinal() < PKDirection.Middle.ordinal()
									&& aaDrawRt < -0.02 && aaDrawRt + aomenDrawChange < -0.015 && aomenDrawChange < -0.001)
								&& waDrawRt < -0.01
								&& iaDrawRt < 0.01
								&& (hostMatches.getMatchMiss() <= 1.4f
									|| hostMatches.getWinDrawRate() >= 0.5f && guestMatches.getWinRate() <= 0.6f)
								) {
							promoteGps.add(ResultGroup.One);
						}
						if (pkDirection.ordinal() < PKDirection.Middle.ordinal()
								&& (hostMatches.getMatchMiss() >= 1.0f
									|| guestMatches.getWinRate() >= 0.5f && hostMatches.getWinDrawRate() <= 0.6f)
								&& aaLoseRt < -0.01 && aaLoseRt + aomenLoseChange< -0.005 && aomenLoseChange < 0.01
								&& laLoseRt < 0.4
								&& iaLoseRt < 0.01
								) {
							promoteGps.add(ResultGroup.Zero);
						}
					} else {
						if (pkDirection.ordinal() > PKDirection.Middle.ordinal()
								&& (guestMatches.getMatchMiss() >= 1.2f
										&& hostMatches.getMatchGoal() >= 1.2f && hostMatches.getMatchMiss() <= 1.6f
									|| hostMatches.getWinDrawRate() >= 0.5f && guestMatches.getWinRate() <= 0.6f)
								&& hotPoint > -5.2
								&& maWinRt < 0.03 && majorWinChange < 0.02
								&& laWinRt < 0.04
								&& iaWinRt < 0.01
								&& aaWinRt < -0.02 && aomenWinChange < 0.01
								&& (iaWinRt < -0.025 && iaWinChange < 0.01 || aaWinRt <= -0.05)) {
							promoteGps.add(ResultGroup.Three);
						}
						if ((pkDirection.ordinal() > PKDirection.Middle.ordinal()
									&& aaDrawRt < 0.012 && aaDrawRt + aomenDrawChange < 0.015 && aomenDrawChange < 0.001
								|| pkDirection.ordinal() < PKDirection.Middle.ordinal()
									&& aaDrawRt < 0.012 && aaDrawRt + aomenDrawChange < 0.015 && aomenDrawChange < -0.001)
								&& maDrawRt <= 0.02 && majorDrawChange <= 0.02
								&& waDrawRt < 0.02
								&& iaDrawRt < 0.01
								&& (hostMatches.getMatchMiss() <= 1.4f
									|| hostMatches.getWinDrawRate() >= 0.5f && guestMatches.getWinRate() <= 0.6f)) {
							promoteGps.add(ResultGroup.One);
						}
						if (pkDirection.ordinal() < PKDirection.Middle.ordinal()
								&& (hostMatches.getMatchMiss() >= 1.0f
									|| guestMatches.getWinRate() >= 0.5f && hostMatches.getWinDrawRate() <= 0.6f)
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
						if (pkDirection.ordinal() > PKDirection.Middle.ordinal()
								&& (guestMatches.getMatchMiss() >= 1.2f
										&& hostMatches.getMatchGoal() >= 1.2f && hostMatches.getMatchMiss() <= 1.6f
									|| hostMatches.getWinDrawRate() >= 0.5f && guestMatches.getWinRate() <= 0.6f)
								&& hotPoint > -5.5f
								&& aaWinRt < -0.03 && aaWinRt + aomenWinChange < -0.025 && aomenWinChange < 0.001
								&& laWinRt < 0.03
								&& iaWinRt < 0.01
								&& (aaLoseRt > -0.001 || aomenLoseChange > 0.021 && aaLoseRt > -0.021)
								) {
							promoteGps.add(ResultGroup.Three);
						}
						if (pkDirection.ordinal() > PKDirection.Up.ordinal()
								&& (hostMatches.getMatchMiss() <= 1.2f
									|| hostMatches.getWinDrawRate() >= 0.5f && guestMatches.getWinRate() <= 0.6f)
								&& hotPoint > -5.5f
								&& aaDrawRt < -0.021 && aaDrawRt + aomenDrawChange < -0.015 && aomenDrawChange < 0.001
								&& waDrawRt < -0.01
								&& iaDrawRt < 0.01
								&& (aaLoseRt > -0.001 || aomenLoseChange > 0.021 && aaLoseRt > -0.021)
								) {
							promoteGps.add(ResultGroup.One);
						}
						if (pkDirection.ordinal() < PKDirection.Up.ordinal()
								&& (hostMatches.getMatchMiss() >= 1.0f
									|| guestMatches.getWinRate() >= 0.5f && hostMatches.getWinDrawRate() <= 0.6f)
								&& aaLoseRt < -0.01 && aaLoseRt + aomenLoseChange < -0.001 && aomenLoseChange < 0.01
								&& laLoseRt < 0.04
								&& iaLoseRt < -0.01
								) {
							promoteGps.add(ResultGroup.Zero);
						}
					} else {
						if (pkDirection.ordinal() > PKDirection.Middle.ordinal()
								&& (guestMatches.getMatchMiss() >= 1.2f
										&& hostMatches.getMatchGoal() >= 1.2f && hostMatches.getMatchMiss() <= 1.6f
									|| hostMatches.getWinDrawRate() >= 0.5f && guestMatches.getWinRate() <= 0.6f)
								&& hotPoint > -5.5f
								&& maWinRt < 0.03 && majorWinChange < 0.02
								&& laWinRt < 0.04
								&& iaWinRt < 0.01
								&& aaWinRt < -0.02 && aomenWinChange < 0.01
								&& (aaLoseRt > -0.015 || aomenLoseChange > 0.021 && aaLoseRt > -0.028)
								&& (iaWinRt <= -0.03 && iaWinChange < 0.01
									|| aaWinRt <= -0.065)) {
							promoteGps.add(ResultGroup.Three);
						}
						if (pkDirection.ordinal() > PKDirection.Up.ordinal()
								&& (hostMatches.getMatchMiss() <= 1.4f
									|| hostMatches.getWinDrawRate() >= 0.5f && guestMatches.getWinRate() <= 0.6f)
								&& hotPoint > -5.5f
								&& maDrawRt < 0.03 && majorDrawChange < 0.02
								&& waDrawRt < 0.03
								&& iaDrawRt < 0.01
								&& aaDrawRt < -0.02 && aomenDrawChange < 0.01
								&& (aaLoseRt > -0.015 || aomenLoseChange > 0.021 && aaLoseRt > -0.028)
								&& (iaDrawRt < -0.025 && iaDrawChange < 0.01
										|| aaDrawRt <= -0.045)) {
							promoteGps.add(ResultGroup.One);
						}
						if (pkDirection.ordinal() < PKDirection.Up.ordinal()
								&& (hostMatches.getMatchMiss() >= 1.0f
									|| guestMatches.getWinRate() >= 0.5f && hostMatches.getWinDrawRate() <= 0.6f)
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
						if (pkDirection.ordinal() > PKDirection.Middle.ordinal()
								&& (guestMatches.getMatchMiss() >= 1.2f
										&& hostMatches.getMatchGoal() >= 1.2f && hostMatches.getMatchMiss() <= 1.6f
									|| hostMatches.getWinDrawRate() >= 0.5f && guestMatches.getWinRate() <= 0.6f)
								&& hotPoint > -6.5f
								&& aaWinRt < -0.03 && aaWinRt + aomenWinChange < -0.025 && aomenWinChange < 0.0
								&& laWinRt < 0.035
								&& iaWinRt < -0.01
								&& (aaLoseRt > -0.001 || aomenLoseChange > 0.021 && aaLoseRt > -0.011)
								) {
							promoteGps.add(ResultGroup.Three);
						}
						if (pkDirection.ordinal() > PKDirection.Middle.ordinal()
								&& (hostMatches.getMatchMiss() <= 1.4f
									|| hostMatches.getWinDrawRate() >= 0.5f && guestMatches.getWinRate() <= 0.6f)
								&& hotPoint > -6.5f
								&& aaDrawRt < -0.02 && aaDrawRt + aomenDrawChange < -0.015 && aomenDrawChange < 0.0
								&& waDrawRt < -0.01
								&& iaDrawRt < 0.01
								&& (aaLoseRt > -0.001 || aomenLoseChange > 0.021 && aaLoseRt > -0.011)
								) {
							promoteGps.add(ResultGroup.One);
						}
						if (pkDirection.ordinal() < PKDirection.Up.ordinal()
								&& (hostMatches.getMatchMiss() >= 1.0f
									|| guestMatches.getWinRate() >= 0.5f && hostMatches.getWinDrawRate() <= 0.6f)
								&& aaLoseRt < -0.01 && aaLoseRt + aomenLoseChange < 0.001 && aomenLoseChange < 0.01
								&& laLoseRt < 0.04
								&& iaLoseRt < 0.01
								) {
							promoteGps.add(ResultGroup.Zero);
						}
					} else {
						if (pkDirection.ordinal() > PKDirection.Middle.ordinal()
								&& (guestMatches.getMatchMiss() >= 1.2f
										&& hostMatches.getMatchGoal() >= 1.2f && hostMatches.getMatchMiss() <= 1.6f
									|| hostMatches.getWinDrawRate() >= 0.5f && guestMatches.getWinRate() <= 0.6f)
								&& hotPoint > -6.5f
								&& maWinRt < 0.03 && majorWinChange < 0.02
								&& laWinRt < 0.035
								&& iaWinRt < -0.01
								&& (aaLoseRt > -0.015 || aomenLoseChange > 0.02 && aaLoseRt > -0.025)
								&& aaWinRt < -0.06 && aomenWinChange < 0.01) {
							promoteGps.add(ResultGroup.Three);
						}
						if (pkDirection.ordinal() > PKDirection.Middle.ordinal()
								&& (hostMatches.getMatchMiss() <= 1.4f
									|| hostMatches.getWinDrawRate() >= 0.5f && guestMatches.getWinRate() <= 0.6f)
								&& hotPoint > -6.5f
								&& maDrawRt < 0.03 && majorDrawChange < 0.02
								&& waDrawRt < -0.01
								&& iaDrawRt < -0.01
								&& (aaLoseRt > -0.015 || aomenLoseChange > 0.02 && aaLoseRt > -0.025)
								&& aaDrawRt < -0.045 && aomenDrawChange < 0.01) {
							promoteGps.add(ResultGroup.One);
						}
						if (pkDirection.ordinal() < PKDirection.Up.ordinal()
								&& (hostMatches.getMatchMiss() >= 1.0f
									|| guestMatches.getWinRate() >= 0.5f && hostMatches.getWinDrawRate() <= 0.6f)
								&& maLoseRt < 0.035 && majorLoseChange < 0.03
								&& laLoseRt < 0.045
								&& iaLoseRt < 0.02
								&& aaLoseRt < -0.015 && aomenLoseChange < 0.01) {
							promoteGps.add(ResultGroup.Zero);
						}
					}
				} else {
					if (pkDirection.ordinal() > PKDirection.Middle.ordinal()
							&& (guestMatches.getMatchMiss() >= 1.2f
									&& hostMatches.getMatchGoal() >= 1.2f && hostMatches.getMatchMiss() <= 1.4f
								|| hostMatches.getWinDrawRate() >= 0.5f && guestMatches.getWinRate() <= 0.6f)
							&& hotPoint > -6.5f
							&& maWinRt < 0.03 && majorWinChange < 0.02
							&& laWinRt < 0.035
							&& iaWinRt < -0.01
							&& (aaLoseRt > -0.001 || aomenLoseChange > 0.02 && aaLoseRt > -0.02)
							&& aaWinRt < -0.06 && aomenWinChange < 0.01) {
						promoteGps.add(ResultGroup.Three);
					}
					if (pkDirection.ordinal() > PKDirection.Middle.ordinal()
							&& (hostMatches.getMatchMiss() <= 1.4f
								|| hostMatches.getWinDrawRate() >= 0.5f && guestMatches.getWinRate() <= 0.6f)
							&& hotPoint > -6.5f
							&& maDrawRt < 0.03 && majorDrawChange < 0.02
							&& waDrawRt < -0.01
							&& iaDrawRt < -0.01
							&& (aaLoseRt > -0.001 || aomenLoseChange > 0.02 && aaLoseRt > -0.02)
							&& aaDrawRt < -0.045 && aomenDrawChange < 0.01) {
						promoteGps.add(ResultGroup.One);
					}
					if (pkDirection.ordinal() < PKDirection.Up.ordinal()
							&& (hostMatches.getMatchMiss() >= 1.0f
								|| guestMatches.getWinRate() >= 0.5f && hostMatches.getWinDrawRate() <= 0.6f)
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
