package com.roy.football.match.OFN;

import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import com.roy.football.match.OFN.response.AsiaPl;
import com.roy.football.match.OFN.response.EuroPl;
import com.roy.football.match.OFN.statics.matrices.ClubMatrices;
import com.roy.football.match.OFN.statics.matrices.ClubMatrices.ClubMatrix;
import com.roy.football.match.OFN.statics.matrices.EuroMatrices;
import com.roy.football.match.OFN.statics.matrices.EuroMatrices.EuroMatrix;
import com.roy.football.match.OFN.statics.matrices.JiaoShouMatrices;
import com.roy.football.match.OFN.statics.matrices.MatchState;
import com.roy.football.match.OFN.statics.matrices.MatchState.LatestMatchMatrices;
import com.roy.football.match.OFN.statics.matrices.OFNCalculateResult;
import com.roy.football.match.OFN.statics.matrices.OFNKillPromoteResult;
import com.roy.football.match.OFN.statics.matrices.PankouMatrices;
import com.roy.football.match.base.League;
import com.roy.football.match.base.ResultGroup;
import com.roy.football.match.base.TeamLabel;
import com.roy.football.match.process.CalculateResult;
import com.roy.football.match.process.KillPromoter;
import com.roy.football.match.util.MatchUtil;

public class PankouKillPromoter implements KillPromoter<OFNKillPromoteResult, OFNCalculateResult> {
	
	public OFNKillPromoteResult calculate (OFNCalculateResult calResult) {
		OFNKillPromoteResult killPromoteResult = new OFNKillPromoteResult();
		
		kill(killPromoteResult, calResult);
		promote(killPromoteResult, calResult);
		checkHot(killPromoteResult, calResult);
		
		return killPromoteResult;
	}
	
	public void checkHot(OFNKillPromoteResult killPromoteResult, OFNCalculateResult calResult) {
		Set<ResultGroup> hot = checkHotMatch(calResult);
		killPromoteResult.setTooHot(hot);
	}

	@Override
	public void kill(OFNKillPromoteResult killPromoteResult, OFNCalculateResult calResult) {
		if (killPromoteResult != null) {
			PankouMatrices pkMatrices = calResult.getPkMatrices();
			Float predictPk = calResult.getPredictPanKou();
			List <TeamLabel> hostLabels = calResult.getHostLabels();
			List <TeamLabel> guestLabels = calResult.getGuestLabels();
			
			if (predictPk == null && calResult.getJiaoShou() != null) {
				predictPk = calResult.getJiaoShou().getLatestPankou();
			}
			
			if (pkMatrices != null) {
				float currPk = pkMatrices.getCurrentPk().getPanKou();
				Set<ResultGroup> pkRes = killZhuShangPan(pkMatrices, predictPk, hostLabels, guestLabels);
				Set<ResultGroup> plRes = killByWillLabDiff(calResult.getEuroMatrices(), currPk);

				killPromoteResult.setKillByPk(pkRes);
				killPromoteResult.setKillByPl(plRes);
			}
		}
	}

	@Override
	public OFNKillPromoteResult kill(OFNCalculateResult calResult) {
		OFNKillPromoteResult killResult = new OFNKillPromoteResult();
		
		PankouMatrices pkMatrices = calResult.getPkMatrices();
		Float predictPk = calResult.getPredictPanKou();
		List <TeamLabel> hostLabels = calResult.getHostLabels();
		List <TeamLabel> guestLabels = calResult.getGuestLabels();
		
		if (predictPk == null && calResult.getJiaoShou() != null) {
			predictPk = calResult.getJiaoShou().getLatestPankou();
		}
		
		if (pkMatrices != null) {
			float currPk = pkMatrices.getCurrentPk().getPanKou();
			Set<ResultGroup> pkRes = killZhuShangPan(pkMatrices, predictPk, hostLabels, guestLabels);
			Set<ResultGroup> plRes = killByWillLabDiff(calResult.getEuroMatrices(), currPk);
			
			killResult.setKillByPk(pkRes);
			killResult.setKillByPl(plRes);
		}
		
		return killResult;
	}
	
	@Override
	public void promote(OFNKillPromoteResult killPromoteResult,
			OFNCalculateResult calResult) {
		if (killPromoteResult != null) {
			PankouMatrices pkMatrices = calResult.getPkMatrices();
			Float predictPk = calResult.getPredictPanKou();
			List <TeamLabel> hostLabels = calResult.getHostLabels();
			List <TeamLabel> guestLabels = calResult.getGuestLabels();
			MatchState matchState = calResult.getMatchState();
			
			if (pkMatrices != null) {
				killPromoteResult.setPromoteByPk(promoteByPk(pkMatrices, predictPk, hostLabels, guestLabels, matchState));
			}
		}
	}

	@Override
	public OFNKillPromoteResult promote(OFNCalculateResult calResult) {
		OFNKillPromoteResult killPromoteResult = new OFNKillPromoteResult();
		
		PankouMatrices pkMatrices = calResult.getPkMatrices();
		Float predictPk = calResult.getPredictPanKou();
		List <TeamLabel> hostLabels = calResult.getHostLabels();
		List <TeamLabel> guestLabels = calResult.getGuestLabels();
		MatchState matchState = calResult.getMatchState();
		
		if (pkMatrices != null) {
			killPromoteResult.setPromoteByPk(promoteByPk(pkMatrices, predictPk, hostLabels, guestLabels, matchState));
		}
		
		return killPromoteResult;
	}
	
	private Set<ResultGroup> killZhuShangPan (PankouMatrices pkMatrices, Float predictPk,
			List <TeamLabel> hostLabels, List <TeamLabel> guestLabels) {
		Set<ResultGroup> rgs = new TreeSet<ResultGroup> ();

		if (pkMatrices != null && predictPk != null) {
			AsiaPl origin = pkMatrices.getOriginPk();
			AsiaPl main = pkMatrices.getMainPk();
			AsiaPl current = pkMatrices.getCurrentPk();
			
			float mainPk = MatchUtil.getCalculatedPk(main);
			float currentPk = MatchUtil.getCalculatedPk(current);
			float asiaPk = main.getPanKou();

			// the predict and main is nearly same, but the company chooses high pay to stop betting on win/lose.
			if (main.gethWin() > 1.01
					&& (current.gethWin() > 1.01 || origin.gethWin() > 1.01)
					&& currentPk <= mainPk
					&& Math.abs(predictPk - mainPk) < 0.2
					&& !MatchUtil.isHostHomeStrong(hostLabels)) {
				if (asiaPk >= 1) {
					rgs.add(ResultGroup.RangThree);
				} else if (asiaPk >= -0.25 ) {
					rgs.add(ResultGroup.Three);
				} else {
					rgs.add(ResultGroup.Three);
					rgs.add(ResultGroup.One);
				}
			}
			
			if (main.getaWin() > 1.01
					&& (current.getaWin() > 1.01 || origin.getaWin() > 1.01)
					&& currentPk >= mainPk
					&& Math.abs(predictPk - mainPk) < 0.2
					&& !MatchUtil.isGuestDefensive(guestLabels)) {
				if (asiaPk <= -1) {
					rgs.add(ResultGroup.RangZero);
				} else if (asiaPk <= 0.25) {
					rgs.add(ResultGroup.One);
				} else {
					rgs.add(ResultGroup.One);
					rgs.add(ResultGroup.Zero);
				}
			}
		}

		return rgs;
	}
	
	private Set<ResultGroup> killByWillLabDiff (EuroMatrices euMatrices, Float aomenPk) {
		Set<ResultGroup> rgs = new TreeSet<ResultGroup> ();
		
		if (euMatrices != null) {
			EuroMatrix will = euMatrices.getWilliamMatrix();
			EuroMatrix lab = euMatrices.getLadMatrix();
			EuroMatrix aomen = euMatrices.getAomenMatrix();
			
			if (will != null && lab != null) {
				// use current since the main is not correct by the company changed frequently
				EuroPl mainWillEu = will.getCurrentEuro();
				EuroPl mainLabEu = lab.getCurrentEuro();
				EuroPl mainAomenEu = aomen.getCurrentEuro();
				
				float wlWinRt = MatchUtil.getEuDiff(mainWillEu.geteWin(), mainLabEu.geteWin(), true);
				float wlLoseRt = MatchUtil.getEuDiff(mainWillEu.geteLose(), mainLabEu.geteLose(), true);
				float waWinRt = MatchUtil.getEuDiff(mainWillEu.geteWin(), mainAomenEu.geteWin(), false);
				float waLoseRt = MatchUtil.getEuDiff(mainWillEu.geteLose(), mainAomenEu.geteLose(), false);
				if (aomenPk > 0.25) {
					if (wlWinRt > 0.06) {
						rgs.add(ResultGroup.Three);
					}
					if (waWinRt < 0 && aomenPk <= 1) {
						rgs.add(ResultGroup.Three);
					}
				} else if (aomenPk >= 0) {
					if (wlWinRt > 0.065) {
						rgs.add(ResultGroup.Three);
					}
					if (waWinRt < 0) {
						rgs.add(ResultGroup.Three);
					}
				} else if (aomenPk > -0.5) {
					if (wlLoseRt > 0.065) {
						rgs.add(ResultGroup.Zero);
					}
					if (waLoseRt < 0.01) {
						rgs.add(ResultGroup.Zero);
					}
				} else {
					if (wlLoseRt > 0.06) {
						rgs.add(ResultGroup.Zero);
					}
					if (waLoseRt < 0 && aomenPk <= -1) {
						rgs.add(ResultGroup.Zero);
					}
				}
			}
		}
		
		return rgs;
	}
	
	private Set<ResultGroup> promoteByPk (PankouMatrices pkMatrices, Float predictPk,
			List <TeamLabel> hostLabels, List <TeamLabel> guestLabels, MatchState matchState) {
		Set<ResultGroup> rgs = new TreeSet<ResultGroup> ();
		
		if (pkMatrices != null && predictPk != null && matchState != null) {
			AsiaPl main = pkMatrices.getMainPk();
			AsiaPl current = pkMatrices.getCurrentPk();

			float mainPk = MatchUtil.getCalculatedPk(main);
			float currentPk = MatchUtil.getCalculatedPk(current);
			
			LatestMatchMatrices host6Match = matchState.getHostState6();
			LatestMatchMatrices guest6Match = matchState.getGuestState6();
			
			if (host6Match == null || guest6Match == null) {
				return rgs;
			}

			if (main.getPanKou() >= 1) {
				// a1. host pay is low
				if (main.gethWin() <= 0.92 && current.gethWin() <= 0.92) {
					// b1. company think high of the host
					if (mainPk - predictPk > 0.3) {
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
				else if (main.getaWin() <= 0.9 && current.getaWin() <= 0.9) {
					// b2. company think low,
					if (currentPk <= mainPk && predictPk - mainPk > 0.3
							&& (guest6Match.getWinPkRate() > 0.4 || guest6Match.getWinDrawPkRate() > 0.6)) {
						rgs.add(ResultGroup.RangZero);
					}
				}
			} else if (main.getPanKou() > 0.4) {
				if (main.gethWin() <= 0.92 && current.gethWin() <= 0.92) {
					// company think high of the host
					if (mainPk - predictPk > 0.25) {
						// host is pretty good, or guest is pretty bad, or host is good guest is bad,
						if (host6Match.getWinRate() >= 0.8
								|| guest6Match.getWinDrawRate() <= 0.4
								|| (host6Match.getWinRate() >= 0.5 || host6Match.getWinDrawPkRate() >= 0.6)
									&& (guest6Match.getWinRate() <= 0.4 && guest6Match.getWinDrawRate() <= 0.5)) {
							rgs.add(ResultGroup.Three);
						}
					}
					else if (Math.abs(predictPk - mainPk) < 0.19) {
						// host performs good or guest performs bad
						if (currentPk >= mainPk
								&& ((host6Match.getWinRate() >= 0.5 || host6Match.getWinDrawRate() >= 0.6)
										|| (guest6Match.getWinRate() <= 0.4 && guest6Match.getWinDrawRate() <= 0.5))) {
							rgs.add(ResultGroup.Three);
						}
					} else {
						// think high of guest.. check below
					}
				}
				else if (main.getaWin() <= 0.92 && current.getaWin() <= 0.92) {
					// company think high of the guest
					if (predictPk - mainPk > 0.21) {
						// guest is pretty good, or host is pretty bad, or guest is good guest is bad,
						if (!MatchUtil.isHostHomeStrong(hostLabels)
								&& (guest6Match.getWinRate() >= 0.8
									|| host6Match.getWinDrawRate() <= 0.4
									|| (guest6Match.getWinRate() >= 0.5 || guest6Match.getWinDrawPkRate() >= 0.6)
										&& (host6Match.getWinRate() <= 0.4 && host6Match.getWinDrawRate() <= 0.5))) {
							rgs.add(ResultGroup.One);
							rgs.add(ResultGroup.Zero);
						}
					}
					else if (Math.abs(predictPk - mainPk) < 0.19  ) {
						if (!MatchUtil.isHostHomeStrong(hostLabels) 
								&& currentPk <= mainPk
								&& ((guest6Match.getWinRate() >= 0.5 || guest6Match.getWinDrawRate() >= 0.6)
										|| (host6Match.getWinRate() <= 0.4 && host6Match.getWinDrawRate() <= 0.5))) {
							rgs.add(ResultGroup.One);
							rgs.add(ResultGroup.Zero);
						}
					}
				}
			} else if (main.getPanKou() > -0.4) {
				if (main.gethWin() <= 0.92 && current.gethWin() <= 0.92) {
					// company think high of the host
					if (mainPk - predictPk > 0.21) {
						// host is pretty good (the teams are in same level, check win pk rate, instead of win rate),
						//  or guest is pretty bad (still check the pk rate),
						if (host6Match.getWinPkRate() >= 0.6
								|| guest6Match.getWinDrawPkRate() <= 0.4
								|| (host6Match.getWinPkRate() >= 0.5 || host6Match.getWinDrawPkRate() >= 0.6)
									&& guest6Match.getWinPkRate() <= 0.4) {
							rgs.add(ResultGroup.Three);
							rgs.add(ResultGroup.One);
						}
					} else if (Math.abs(predictPk - mainPk) < 0.15) {
						// host performs good or guest performs bad
						if (currentPk >= mainPk
								&& ((host6Match.getWinPkRate() >= 0.5 || host6Match.getWinDrawPkRate() >= 0.6)
										|| (guest6Match.getWinPkRate() <= 0.4 && guest6Match.getWinDrawPkRate() <= 0.5))) {
							rgs.add(ResultGroup.Three);
							rgs.add(ResultGroup.One);
						}
					} else {
						// think high of guest.. check below
					}
				} else if (main.getaWin() <= 0.92 && current.getaWin() <= 0.92) {
					// company think high of the guest
					if (predictPk - mainPk > 0.21) {
						if (guest6Match.getWinPkRate() >= 0.6
								|| host6Match.getWinDrawPkRate() <= 0.4
								|| (guest6Match.getWinPkRate() >= 0.5 || guest6Match.getWinDrawPkRate() >= 0.6)
									&& host6Match.getWinPkRate() <= 0.4) {
							rgs.add(ResultGroup.One);
							rgs.add(ResultGroup.Zero);
						}
					}
					else if (Math.abs(predictPk - mainPk) < 0.15) {
						// host performs good or guest performs bad
						if (mainPk >= currentPk
								&& ((guest6Match.getWinPkRate() >= 0.5 || guest6Match.getWinDrawPkRate() >= 0.6)
										|| (host6Match.getWinPkRate() <= 0.4 && host6Match.getWinDrawPkRate() <= 0.5))) {
							rgs.add(ResultGroup.One);
							rgs.add(ResultGroup.Zero);
						}
					}
				}
			} else if (main.getPanKou() > -1) {
				if (main.gethWin() <= 0.92 && current.gethWin() <= 0.92) {
					// company think high of the host
					if (mainPk - predictPk > 0.25) {
						// host is pretty good, or guest is pretty bad, or host is good guest is bad,
						if (host6Match.getWinPkRate() >= 0.6
								|| guest6Match.getWinDrawPkRate() <= 0.5
								|| (host6Match.getWinPkRate() >= 0.5 || host6Match.getWinDrawPkRate() >= 0.6)
									&& (guest6Match.getWinPkRate() <= 0.5 && guest6Match.getWinDrawPkRate() <= 0.6)) {
							rgs.add(ResultGroup.Three);
							rgs.add(ResultGroup.One);
						}
					}
					else if (Math.abs(predictPk - mainPk) < 0.19) {
						// host performs good or guest performs bad
						if (currentPk >= mainPk
								&& ((host6Match.getWinPkRate() >= 0.5 || host6Match.getWinDrawPkRate() >= 0.6)
										&& (guest6Match.getWinPkRate() <= 0.5 && guest6Match.getWinDrawPkRate() <= 0.6))) {
							rgs.add(ResultGroup.Three);
							rgs.add(ResultGroup.One);
						}
					} else {
						// think high of guest.. check below
					}
				}
				else if (main.getaWin() <= 0.92 && current.getaWin() <= 0.92) {
					// company think high of the guest
					if (predictPk - mainPk > 0.25) {
						// guest is pretty good, or host is pretty bad, or guest is good guest is bad,
						if ((guest6Match.getWinRate() >= 0.8
									|| host6Match.getWinDrawPkRate() <= 0.4
									|| (guest6Match.getWinRate() >= 0.5 || guest6Match.getWinDrawPkRate() >= 0.6)
										&& (host6Match.getWinPkRate() <= 0.5 && host6Match.getWinDrawPkRate() <= 0.6))) {
							rgs.add(ResultGroup.Zero);
						}
					}
					else if (Math.abs(predictPk - mainPk) < 0.19) {
						if (currentPk <= mainPk
								&& ((guest6Match.getWinRate() >= 0.5 || guest6Match.getWinDrawPkRate() >= 0.6)
										&& (host6Match.getWinPkRate() <= 0.5 && host6Match.getWinDrawPkRate() <= 0.6))) {
							rgs.add(ResultGroup.Zero);
						}
					}
				}
			} else {
				if (main.gethWin() <= 0.9 && current.gethWin() <= 0.9) {
					if (currentPk >= mainPk && mainPk - predictPk > 0.3
							&& (host6Match.getWinPkRate() >= 0.5 || host6Match.getWinDrawPkRate() >= 0.6)) {
						rgs.add(ResultGroup.Three);
						rgs.add(ResultGroup.One);
					}
				}
				// a2. guest pay is low
				else if (main.getaWin() <= 0.92 && current.getaWin() <= 0.92) {
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
								&& (host6Match.getWinPkRate() <= 0.5 && host6Match.getWinDrawPkRate() <= 0.6)) {
							rgs.add(ResultGroup.Zero);
						}
					}
				}
			}
		}

		return rgs;
	}
	
	private Set<ResultGroup> promoteByPk1 (PankouMatrices pkMatrices, Float predictPk,
			List <TeamLabel> hostLabels, List <TeamLabel> guestLabels, MatchState matchState) {
		Set<ResultGroup> rgs = new TreeSet<ResultGroup> ();
		
		if (pkMatrices != null && predictPk != null && matchState != null) {
			AsiaPl main = pkMatrices.getMainPk();
			AsiaPl current = pkMatrices.getCurrentPk();

			float mainPk = MatchUtil.getCalculatedPk(main);
			float currentPk = MatchUtil.getCalculatedPk(current);
			
			LatestMatchMatrices host6Match = matchState.getHostState6();
			LatestMatchMatrices guest6Match = matchState.getGuestState6();

			// pay rate is low, the opponent is not good, itself is not bad
			if (main.gethWin() <= 0.92
					&& current.gethWin() <= 0.92
					&& (host6Match.getWinPkRate() > 0.4 || host6Match.getWinDrawPkRate() > 0.5)
					&& !MatchUtil.isGuestDefensive(guestLabels)) {
				// win is low paid, and predict host is better
				// host must be not HOT
				if (currentPk >= mainPk) {
					if (mainPk > 0.5 && Math.abs(predictPk - mainPk) < 0.3) {
						rgs.add(ResultGroup.Three);
					} else if (mainPk >= -0.5 && Math.abs(predictPk - mainPk) < 0.2) {
						rgs.add(ResultGroup.Three);
						rgs.add(ResultGroup.One);
					}
				}
				
				// need to consider the host's advantage of in home, so the predicted pk should be obvious(means diff is larger than 0.25)
				if (predictPk < -0.75) {
					if (mainPk - predictPk > 0.38) {
						rgs.add(ResultGroup.Three);
						rgs.add(ResultGroup.One);
					}
				} else if (predictPk < -0.1) {
					if (mainPk - predictPk >= 0.28) {
						rgs.add(ResultGroup.Three);
						rgs.add(ResultGroup.One);
					}
				}
			}
			
			if (main.getaWin() <= 0.92
					&& current.getaWin() <= 0.92
					&& (guest6Match.getWinPkRate() > 0.4 || guest6Match.getWinDrawPkRate() > 0.5)
					&& !MatchUtil.isHostHomeStrong(hostLabels)) {
				// lose is low paid, and predict guest is better
				// guest must be not HOT
				if (currentPk <= mainPk) {
					if (mainPk < -0.5 && Math.abs(predictPk - mainPk) < 0.3) {
						rgs.add(ResultGroup.Zero);
					} else if (mainPk <= 0.5 && Math.abs(predictPk - mainPk) < 0.2) {
						rgs.add(ResultGroup.One);
						rgs.add(ResultGroup.Zero);
					}
				}
				
				// company's pk is too low, lots of bets go to win 
				if (predictPk > 0.7) {
					if (predictPk - mainPk >= 0.28) {
						rgs.add(ResultGroup.Zero);
						rgs.add(ResultGroup.One);
					}
				} else if (predictPk > 0.1) {
					if (predictPk - mainPk >= 0.2) {
						rgs.add(ResultGroup.Zero);
						rgs.add(ResultGroup.One);
					}
				}
			}
		}

		return rgs;
	}
	
	private Set<ResultGroup> checkHotMatch (OFNCalculateResult calResult) {
		Set<ResultGroup> rgs = new TreeSet<ResultGroup> ();

		if (calResult == null) {
			return rgs;
		}
		
		ResultGroup result = null;
		
		ClubMatrices clubs = calResult.getClubMatrices();
		MatchState matchState = calResult.getMatchState();
		EuroMatrices euMatrices = calResult.getEuroMatrices();
		PankouMatrices pkMatrices = calResult.getPkMatrices();
		Float predictPk = calResult.getPredictPanKou();
		
		float mainPk = MatchUtil.getCalculatedPk(pkMatrices.getMainPk());
//		float currentPk = MatchUtil.getCalculatedPk(pkMatrices.getCurrentPk());

		int clubPm = compareClub(clubs, calResult.getLeagueId());
		
		LatestMatchMatrices host6Match = matchState.getHostState6();
		LatestMatchMatrices guest6Match = matchState.getGuestState6();
		if (host6Match != null && guest6Match != null) {
			// host is better, host has good state, host's pay is low, all these things indicate that host will win, then something other results may happen.
			if (mainPk - predictPk > 0.25  && pkMatrices.getMainPk().gethWin() < 0.92 && clubPm == 1 && (host6Match.getWinRate() >= 0.4 || host6Match.getWinDrawRate() >= 0.6) ) {
				result = ResultGroup.Three;
			} else if (mainPk - predictPk < -0.15 && pkMatrices.getMainPk().getaWin() < 0.92 && clubPm == -1 && (guest6Match.getWinRate() >= 0.4 || guest6Match.getWinDrawRate() >= 0.6)) {
				result = ResultGroup.Zero;
			}
			
		}
		
		EuroMatrix aomen = euMatrices.getAomenMatrix();
		EuroMatrix will = euMatrices.getWilliamMatrix();
		EuroPl amCurrentEu = aomen.getCurrentEuro();
		EuroPl willCurrentEu = will.getCurrentEuro();
		float waWinRt = MatchUtil.getEuDiff(willCurrentEu.geteWin(), amCurrentEu.geteWin(), false);
		float waLoseRt = MatchUtil.getEuDiff(willCurrentEu.geteLose(), amCurrentEu.geteLose(), false);
		
		if (result == ResultGroup.Three && waWinRt >= 0.05) {
			rgs.add(ResultGroup.Three);
		} else if (result == ResultGroup.Zero && waLoseRt >= 0.05) {
			rgs.add(ResultGroup.Zero);
		}

		return rgs;
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
