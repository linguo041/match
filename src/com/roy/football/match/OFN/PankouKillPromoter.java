package com.roy.football.match.OFN;

import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import com.roy.football.match.OFN.response.AsiaPl;
import com.roy.football.match.OFN.response.Company;
import com.roy.football.match.OFN.response.EuroPl;
import com.roy.football.match.OFN.statics.matrices.ClubMatrices;
import com.roy.football.match.OFN.statics.matrices.ClubMatrices.ClubMatrix;
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
import com.roy.football.match.process.CalculateResult;
import com.roy.football.match.process.KillPromoter;
import com.roy.football.match.util.MatchUtil;

public class PankouKillPromoter {
	private final static float LOW_PK_POINT = -0.02f;
	
	public PredictResult calculate (OFNCalculateResult calResult) {
		PredictResult predictRes = new PredictResult();
		OFNKillPromoteResult killPromoteResult = new OFNKillPromoteResult();

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
		if (pkMatrices != null) {
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
			
			System.out.println(String.format("Host [ goal: %.2f, variation: %.2f, AdjRate: %.2f, dxWeight %.2f]", hgoal, hvariation, hAdjRate, dxWeight));
			System.out.println(String.format("Guest[ goal: %.2f, variation: %.2f, AdjRate: %.2f, dxWeight %.2f]", ggoal, gvariation, gAdjRate, dxWeight));

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

			Float hotPoint = calResult.getHotPoint();
			League league = calResult.getLeague();

			if (pkMatrices != null) {
				float currPk = pkMatrices.getCurrentPk().getPanKou();
				ClubMatrices clubMatrices = calResult.getClubMatrices();

				if (clubMatrices != null) {
					List <TeamLabel> hostLabels = clubMatrices.getHostLabels();
					List <TeamLabel> guestLabels = clubMatrices.getGuestLabels();

					Set<ResultGroup> pkRes = killByPk(pkMatrices, hotPoint, predictPk, hostLabels, guestLabels, calResult.getClubMatrices());
					killPromoteResult.setKillByPk(pkRes);
				}
				
				Set<ResultGroup> plRes = killByEuro(calResult.getEuroMatrices(), currPk, calResult.getJinCai(), league);
				killPromoteResult.setKillByPl(plRes);
			}
		}
	}

	public OFNKillPromoteResult kill(OFNCalculateResult calResult) {
		OFNKillPromoteResult killResult = new OFNKillPromoteResult();
		
		PankouMatrices pkMatrices = calResult.getPkMatrices();
		Float predictPk = calResult.getPredictPanKou();

		if (predictPk == null && calResult.getJiaoShou() != null) {
			predictPk = calResult.getJiaoShou().getLatestPankou();
		}
		
		Float hotPoint = calResult.getHotPoint();
		League league = calResult.getLeague();
		
		if (pkMatrices != null) {
			float currPk = pkMatrices.getCurrentPk().getPanKou();
			ClubMatrices clubMatrices = calResult.getClubMatrices();

			if (clubMatrices != null) {
				List <TeamLabel> hostLabels = clubMatrices.getHostLabels();
				List <TeamLabel> guestLabels = clubMatrices.getGuestLabels();

				Set<ResultGroup> pkRes = killByPk(pkMatrices, hotPoint, predictPk, hostLabels, guestLabels, calResult.getClubMatrices());
				killResult.setKillByPk(pkRes);
			}
			
			Set<ResultGroup> plRes = killByEuro(calResult.getEuroMatrices(), currPk, calResult.getJinCai(), league);
			killResult.setKillByPl(plRes);
		}
		
		return killResult;
	}

	public void promote(OFNKillPromoteResult killPromoteResult,
			OFNCalculateResult calResult) {
		if (killPromoteResult != null) {
			PankouMatrices pkMatrices = calResult.getPkMatrices();
			Float predictPk = calResult.getPredictPanKou();
			ClubMatrices clubMatrices = calResult.getClubMatrices();
			MatchState matchState = calResult.getMatchState();
			
			Float hotPoint = calResult.getHotPoint();
			
			if (pkMatrices != null && clubMatrices != null) {
				List <TeamLabel> hostLabels = clubMatrices.getHostLabels();
				List <TeamLabel> guestLabels = clubMatrices.getGuestLabels();

				killPromoteResult.setPromoteByPk(promoteByPk(pkMatrices,
						predictPk, hostLabels, guestLabels, hotPoint,
						matchState, clubMatrices.getHostAttGuestDefInx(),
						clubMatrices.getGuestAttHostDefInx()));
			}
		}
	}

	public OFNKillPromoteResult promote(OFNCalculateResult calResult) {
		OFNKillPromoteResult killPromoteResult = new OFNKillPromoteResult();
		
		PankouMatrices pkMatrices = calResult.getPkMatrices();
		Float predictPk = calResult.getPredictPanKou();
		ClubMatrices clubMatrices = calResult.getClubMatrices();
		List <TeamLabel> hostLabels = clubMatrices.getHostLabels();
		List <TeamLabel> guestLabels = clubMatrices.getGuestLabels();
		MatchState matchState = calResult.getMatchState();
		
		Float hotPoint = calResult.getHotPoint();
		
		if (pkMatrices != null) {
			killPromoteResult.setPromoteByPk(promoteByPk(pkMatrices,
					predictPk, hostLabels, guestLabels, hotPoint,
					matchState, clubMatrices.getHostAttGuestDefInx(),
					clubMatrices.getGuestAttHostDefInx()));
		}
		
		return killPromoteResult;
	}

	private Set<ResultGroup> killByPk (PankouMatrices pkMatrices, Float hotPoint, Float predictPk,
			List <TeamLabel> hostLabels, List <TeamLabel> guestLabels, ClubMatrices clubMs) {
		Set<ResultGroup> rgs = new TreeSet<ResultGroup> ();

		if (pkMatrices != null && predictPk != null) {
			AsiaPl origin = pkMatrices.getOriginPk();
			AsiaPl main = pkMatrices.getMainPk();
			AsiaPl current = pkMatrices.getCurrentPk();
			Float hWinChgRtFloat = pkMatrices.getHwinChangeRate();
			Float gWinChgRtFloat = pkMatrices.getAwinChangeRate();
			
			float mainPk = MatchUtil.getCalculatedPk(main);
			float currentPk = MatchUtil.getCalculatedPk(current);
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
	
	private Set<ResultGroup> killByEuro (EuroMatrices euMatrices, Float aomenPk, EuroPl jinCai, League league) {
		Set<ResultGroup> rgs = new TreeSet<ResultGroup> ();
		
		if (euMatrices != null) {
			EuroMatrix will = euMatrices.getWilliamMatrix();
			EuroMatrix lab = euMatrices.getLadMatrix();
			EuroMatrix aomen = euMatrices.getAomenMatrix();
			EuroMatrix majorComp = will;
			
			Company company = league.getMajorCompany();
			if (company != null) {
				switch (company) {
					case Aomen:
						majorComp = aomen;
						break;
					case SNAI:
						majorComp = euMatrices.getSnaiMatrix();
						break;
					default:
						majorComp = will;
						break;
				}
			}
			
			if (will != null && lab != null) {
				EuroPl mainWillEu = will.getCurrentEuro();
				EuroPl mainLabEu = lab.getCurrentEuro();
				
				float wlWinRt = MatchUtil.getEuDiff(mainLabEu.geteWin(), mainWillEu.geteWin(), false);
				float wlDrawRt = MatchUtil.getEuDiff(mainLabEu.geteDraw(), mainWillEu.geteDraw(), false);
				float wlLoseRt = MatchUtil.getEuDiff(mainLabEu.geteLose(), mainWillEu.geteLose(), false);
				
				if (aomenPk >= 1) {
					if (wlWinRt > 0.061) {
						rgs.add(ResultGroup.Three);
					}
					if (wlDrawRt > 0.081) {
						rgs.add(ResultGroup.One);
					}
					if (wlLoseRt > 0.091) {
						rgs.add(ResultGroup.Zero);
					}
				} else if (aomenPk >= 0.4) {
					if (wlWinRt > 0.063) {
						rgs.add(ResultGroup.Three);
					}
					if (wlDrawRt > 0.068) {
						rgs.add(ResultGroup.One);
					}
					if (wlLoseRt > 0.073) {
						rgs.add(ResultGroup.Zero);
					}
				} else if (aomenPk >= 0.25) {
					if (wlWinRt > 0.063) {
						rgs.add(ResultGroup.Three);
					}
					if (wlDrawRt > 0.067) {
						rgs.add(ResultGroup.One);
					}
					if (wlLoseRt > 0.071) {
						rgs.add(ResultGroup.Zero);
					}
				} else if (aomenPk >= 0) {
					if (wlWinRt > 0.068) {
						rgs.add(ResultGroup.Three);
					}
					if (wlDrawRt > 0.065) {
						rgs.add(ResultGroup.One);
					}
					if (wlLoseRt > 0.068) {
						rgs.add(ResultGroup.Zero);
					}
				} else if (aomenPk >= -0.25) {
					if (wlWinRt > 0.068) {
						rgs.add(ResultGroup.Three);
					}
					if (wlDrawRt > 0.065) {
						rgs.add(ResultGroup.One);
					}
					if (wlLoseRt > 0.068) {
						rgs.add(ResultGroup.Zero);
					}
				} else if (aomenPk >= -1) {
					if (wlWinRt > 0.075) {
						rgs.add(ResultGroup.Three);
					}
					if (wlDrawRt > 0.07) {
						rgs.add(ResultGroup.One);
					}
					if (wlLoseRt > 0.065) {
						rgs.add(ResultGroup.Zero);
					}
				} else {
					if (wlWinRt > 0.085) {
						rgs.add(ResultGroup.Three);
					}
					if (wlDrawRt > 0.075) {
						rgs.add(ResultGroup.One);
					}
					if (wlLoseRt > 0.065) {
						rgs.add(ResultGroup.Zero);
					}
				}
			}
			
			if (majorComp != will && majorComp != null && will != null) {
				// use current since the main is not correct by the company changed frequently
				EuroPl willEu = will.getCurrentEuro();
				EuroPl majorEu = majorComp.getCurrentEuro();

				float wlWinRt = MatchUtil.getEuDiff(willEu.geteWin(), majorEu.geteWin(), true);
				float wlDrawRt = MatchUtil.getEuDiff(willEu.geteDraw(), majorEu.geteDraw(), true);
				float wlLoseRt = MatchUtil.getEuDiff(willEu.geteLose(), majorEu.geteLose(), true);
				
				if (aomenPk >= 0.4) {
					if (wlWinRt > 0.05) {
						rgs.add(ResultGroup.Three);
					}
					if (wlDrawRt > 0.06) {
						rgs.add(ResultGroup.One);
					}
					if (wlLoseRt > 0.07) {
						rgs.add(ResultGroup.Zero);
					}
				} else if (aomenPk >= 0.25) {
					if (wlWinRt > 0.05) {
						rgs.add(ResultGroup.Three);
					}
					// will follows the major, but has big disagreement with the major
					// and take big risk, so will must has more info, 
					if (wlDrawRt > 0.055) {
						rgs.add(ResultGroup.One);
					}
					if (wlLoseRt > 0.06) {
						rgs.add(ResultGroup.Zero);
					}
				} else if (aomenPk >= 0) {
					if (wlWinRt > 0.055) {
						rgs.add(ResultGroup.Three);
					}
					// will follows the major, but has big disagreement with the major
					// and take big risk, so will must has more info, 
					if (wlDrawRt > 0.04) {
						rgs.add(ResultGroup.One);
					}
					if (wlLoseRt > 0.055) {
						rgs.add(ResultGroup.Zero);
					}
				}  else if (aomenPk >= -0.25) {
					if (wlWinRt > 0.06) {
						rgs.add(ResultGroup.Three);
					}
					// will follows the major, but has big disagreement with the major
					// and take big risk, so will must has more info, 
					if (wlDrawRt > 0.055) {
						rgs.add(ResultGroup.One);
					}
					if (wlLoseRt > 0.05) {
						rgs.add(ResultGroup.Zero);
					}
				} else {
					if (wlWinRt > 0.07) {
						rgs.add(ResultGroup.Three);
					}
					if (wlDrawRt > 0.06) {
						rgs.add(ResultGroup.One);
					}
					if (wlLoseRt > 0.05) {
						rgs.add(ResultGroup.Zero);
					}
				}
			}

		}
		
		return rgs;
	}
	
	private Set<ResultGroup> promoteByBase (Set<ResultGroup> rgs, Float hotPoint, Float hostAttGuestDefComp, Float guestAttHostDefComp,
			float currentPk, Float predictPk, AsiaPl main, AsiaPl current) {
		if (rgs == null) {
			rgs = new TreeSet<ResultGroup> ();
		}

		// host performed good lately
		if (hotPoint >= 6) {
			// host is so good --> win
			if (hostAttGuestDefComp - guestAttHostDefComp > 0.8) {
				if (main.gethWin() < 0.90 && current.gethWin() <= main.gethWin()) {
					rgs.add(ResultGroup.Three);
				}
			}
			// host is not so good and guest is not so good
			else if (hostAttGuestDefComp - guestAttHostDefComp <= 0.5 && hostAttGuestDefComp - guestAttHostDefComp > -0.2){
				// but the company think low of host -- lose
				if (currentPk < predictPk - 0.15 && main.getaWin() <= 0.93 && current.getaWin() <= main.getaWin()) {
					rgs.add(ResultGroup.One);
					rgs.add(ResultGroup.Zero);
				}
			}
		}
		// guest performed good lately
		else if (hotPoint <= -6) {
			// guest is so good
			if (guestAttHostDefComp - hostAttGuestDefComp >= 0.35) {
				if (main.getaWin() < 0.90 && current.getaWin() <= main.getaWin()) {
					rgs.add(ResultGroup.Zero);
				}
			}
			// guest is not so good and host is not so good
			else if (guestAttHostDefComp - hostAttGuestDefComp <= 0.1 && guestAttHostDefComp - hostAttGuestDefComp > -0.5) {
				// but the company think low of guest -- win
				if (currentPk > predictPk + 0.15 && main.gethWin() <= 0.93 && current.gethWin() <= main.gethWin()) {
					rgs.add(ResultGroup.Three);
					rgs.add(ResultGroup.One);
				}
			}
		}

		// host is good (not very good), and host didn't perform bad
		if (hostAttGuestDefComp - guestAttHostDefComp < 0.6 && hostAttGuestDefComp - guestAttHostDefComp > 0.3 && hotPoint >= -3) {
			// but the company think low of host -- lose
			if (currentPk < predictPk - 0.15 && main.getaWin() < 0.94 && current.getaWin() <= main.getaWin()) {
				rgs.add(ResultGroup.One);
				rgs.add(ResultGroup.Zero);
			}
		}
		// guest is good (not very good), and guest didn't perform bad 
		else if ((guestAttHostDefComp - hostAttGuestDefComp >= -0.1) && hotPoint <= 3) {
			// but the company think low of guest -- win
			if (currentPk > predictPk + 0.15 && main.gethWin() < 0.94 && current.gethWin() <= main.gethWin()) {
				rgs.add(ResultGroup.Three);
				rgs.add(ResultGroup.One);
			}
		}

		return rgs;
	}
	
	private Set<ResultGroup> promoteByPk (PankouMatrices pkMatrices, Float predictPk,
			List <TeamLabel> hostLabels, List <TeamLabel> guestLabels, Float hotPoint,
			MatchState matchState, Float hostAttGuestDefComp, Float guestAttHostDefComp) {
		Set<ResultGroup> rgs = new TreeSet<ResultGroup> ();
		
		if (pkMatrices != null && predictPk != null && matchState != null) {
			AsiaPl main = pkMatrices.getMainPk();
			AsiaPl current = pkMatrices.getCurrentPk();
			Float hWinChgRtFloat = pkMatrices.getHwinChangeRate();
			Float gWinChgRtFloat = pkMatrices.getAwinChangeRate();

			float mainPk = MatchUtil.getCalculatedPk(main);
			float currentPk = MatchUtil.getCalculatedPk(current);
			
			promoteByBase(rgs, hotPoint, hostAttGuestDefComp, guestAttHostDefComp,
					currentPk, predictPk, main, current);
			
			LatestMatchMatrices host6Match = matchState.getHostState6();
			LatestMatchMatrices guest6Match = matchState.getGuestState6();
			
			if (host6Match == null || guest6Match == null) {
				return rgs;
			}

			if (main.getPanKou() >= 1) {
				// a1. host pay is low
				if (hWinChgRtFloat < LOW_PK_POINT && current.gethWin() <= 0.92) {
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
				else if (gWinChgRtFloat < LOW_PK_POINT && current.getaWin() <= 0.9) {
					// b2. company think low,
					if (currentPk <= mainPk && predictPk - mainPk > 0.3
							&& (guest6Match.getWinPkRate() > 0.4 || guest6Match.getWinDrawPkRate() > 0.6)) {
						rgs.add(ResultGroup.RangZero);
					}
				}
			} else if (main.getPanKou() > 0.4) {
				if (hWinChgRtFloat < LOW_PK_POINT && current.gethWin() <= 0.92) {
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
				else if (gWinChgRtFloat < LOW_PK_POINT && current.getaWin() <= 0.9) {
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
				if (hWinChgRtFloat < LOW_PK_POINT && current.gethWin() <= 0.9) {
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
				} else if (gWinChgRtFloat < LOW_PK_POINT && current.getaWin() <= 0.92) {
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
				if (hWinChgRtFloat < LOW_PK_POINT && current.gethWin() <= 0.9) {
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
				else if (gWinChgRtFloat < LOW_PK_POINT && current.getaWin() <= 0.92) {
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
				if (hWinChgRtFloat < LOW_PK_POINT && current.gethWin() <= 0.9) {
					if (currentPk >= mainPk && mainPk - predictPk > 0.3
							&& (host6Match.getWinPkRate() >= 0.5 || host6Match.getWinDrawPkRate() >= 0.6)) {
						rgs.add(ResultGroup.Three);
						rgs.add(ResultGroup.One);
					}
				}
				// a2. guest pay is low
				else if (gWinChgRtFloat < LOW_PK_POINT && current.getaWin() <= 0.92) {
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
	
	private boolean isPkSupportHot (Float predictPk, Float mainPk, Float hotPoint) {
		if (hotPoint >= 5 && mainPk < predictPk) {
			return false;
		} else if (hotPoint <= -5 && predictPk > mainPk) {
			return false;
		} else {
			return true;
		}
	}

	private int compareClub (ClubMatrices clubs, Long lid) {
		int clubNum = 18;
		
		try {
			League le = League.getLeagueById(lid);
			if (le != null) {
				clubNum = le.getClubNum();
			}
		} catch (Exception e) {
			
		}
		
		ClubMatrix hostClub = clubs.getHostAllMatrix();
		ClubMatrix hostHomeClub = clubs.getHostHomeMatrix();
		ClubMatrix guestClub = clubs.getGuestAllMatrix();
		ClubMatrix guestAwayClub = clubs.getGuestAwayMatrix();
		
		if (hostClub != null && guestClub != null) {
			if (hostClub.getPm() <= clubNum/5 && guestClub.getPm() >= 2*clubNum/5) {
				return 1;
			} else if (hostClub.getPm() >= 2*clubNum/5 && guestClub.getPm() <= clubNum/5) {
				return -1;
			}
		}

		if (hostHomeClub != null && guestAwayClub != null && guestClub != null) {
			if (hostHomeClub.getWinDrawRt() > 0.6 && guestAwayClub.getWinDrawRt() < 0.6
					&& guestAwayClub.getPm() > 3*clubNum/5
					&& guestAwayClub.getWinGoals() < 0) {
				return 1;
			}
			
			if (guestClub.getPm() <= 2*clubNum/5 && hostHomeClub.getPm() >= 3*clubNum/5) {
				return -1;
			}
		}
		
		return 0;
	}
}
