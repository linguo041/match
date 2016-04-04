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
		
		ClubMatrices clubMs = calResult.getClubMatrices();
		if (clubMs != null) {
//			float attackDiff = compareAttack(clubMs.g);
		}
		
		
		kill(killPromoteResult, calResult);
		promote(killPromoteResult, calResult);

		return killPromoteResult;
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

			Float hotPoint = calResult.getHotPoint();

			if (pkMatrices != null) {
				float currPk = pkMatrices.getCurrentPk().getPanKou();
				Set<ResultGroup> pkRes = killByPk(pkMatrices, hotPoint, predictPk, hostLabels, guestLabels, calResult.getClubMatrices());
				Set<ResultGroup> plRes = killByEuro(calResult.getEuroMatrices(), currPk, calResult.getJinCai());

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
		
		Float hotPoint = calResult.getHotPoint();
		
		if (pkMatrices != null) {
			float currPk = pkMatrices.getCurrentPk().getPanKou();
			Set<ResultGroup> pkRes = killByPk(pkMatrices, hotPoint, predictPk, hostLabels, guestLabels, calResult.getClubMatrices());
			Set<ResultGroup> plRes = killByEuro(calResult.getEuroMatrices(), currPk, calResult.getJinCai());
			
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

	private Set<ResultGroup> killByPk (PankouMatrices pkMatrices, Float hotPoint, Float predictPk,
			List <TeamLabel> hostLabels, List <TeamLabel> guestLabels, ClubMatrices clubMs) {
		Set<ResultGroup> rgs = new TreeSet<ResultGroup> ();

		if (pkMatrices != null && predictPk != null) {
			AsiaPl origin = pkMatrices.getOriginPk();
			AsiaPl main = pkMatrices.getMainPk();
			AsiaPl current = pkMatrices.getCurrentPk();
			
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
					if (clubMs.getHostHomeMatrix().getWinRt() - clubMs.getGuestAwayMatrix().getWinDrawRt() >= -0.1 || hotPoint > 7) {
						skipWin = true;
					}
					if (clubMs.getGuestAwayMatrix().getWinRt() - clubMs.getHostHomeMatrix().getWinDrawRt() >= -0.1 || hotPoint < -7) {
						skipLose = true;
					}
				}
			} catch (Exception e) {
				// ignore..
			}
			
//			Float hWinWeight = pkMatrices.getHwinChangeRate();

			// the predict and main is nearly same, but the company chooses high pay to stop betting on win/lose.
			if (main.gethWin() > 1.01
					&& current.gethWin() > 1.01
					&& current.gethWin() - main.gethWin() >= -0.04
					&& Math.abs(predictPk - mainPk) < 0.21
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
			
			if (main.getaWin() > 1.01
					&& current.getaWin() > 1.01
					&& current.getaWin() - main.getaWin() >= -0.04
					&& Math.abs(predictPk - mainPk) < 0.21
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
	
	private Set<ResultGroup> killByEuro (EuroMatrices euMatrices, Float aomenPk, EuroPl jinCai) {
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
				float wjWinRt = 0;
				float wjLoseRt = 0;
				
				if (jinCai != null) {
					wjWinRt = MatchUtil.getEuDiff(mainWillEu.geteWin(), jinCai.geteWin(), false);
					wjLoseRt = MatchUtil.getEuDiff(mainWillEu.geteLose(), jinCai.geteLose(), false);
				}

				if (aomenPk > 0.4) {
					if (wjWinRt < 0.06 && aomenPk <= 1) {
						if (wlWinRt > 0.063) {
							rgs.add(ResultGroup.Three);
						}
						if (waWinRt < 0) {
							rgs.add(ResultGroup.Three);
						}
					}
				} else if (aomenPk >= 0) {
					if (wlWinRt > 0.068) {
						rgs.add(ResultGroup.Three);
					}
					if (waWinRt < 0) {
						rgs.add(ResultGroup.Three);
					}
				} else if (aomenPk > -0.5) {
					if (wjLoseRt < 0.06) {
						if (wlLoseRt > 0.068) {
							rgs.add(ResultGroup.Zero);
						}
						if (waLoseRt < 0.01) {
							rgs.add(ResultGroup.Zero);
						}
					}
				} else {
					if (wjLoseRt < 0.07 && aomenPk <= 1) {
						if (wlLoseRt > 0.06) {
							rgs.add(ResultGroup.Zero);
						}
						if (waLoseRt < 0 && aomenPk <= -1) {
							rgs.add(ResultGroup.Zero);
						}
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
				else if (main.getaWin() <= 0.9 && current.getaWin() <= 0.9) {
					// b2. company think low,
					if (currentPk <= mainPk && predictPk - mainPk > 0.3
							&& (guest6Match.getWinPkRate() > 0.4 || guest6Match.getWinDrawPkRate() > 0.6)) {
						rgs.add(ResultGroup.RangZero);
					}
				}
			} else if (main.getPanKou() > 0.4) {
				if (main.gethWin() <= 0.94 && current.gethWin() <= 0.94) {
					// company think high of the host
					if (mainPk - predictPk > 0.25) {
						// host is pretty good, or guest is pretty bad (this is not matter,
						// if guest is too bad, then the predict pk should be high, not align with the assumption), or host is good guest is bad,
						if (current.gethWin() <= 0.92 && (host6Match.getWinRate() >= 0.6 && guest6Match.getWinRate() <= 0.5
								|| guest6Match.getWinDrawRate() <= 0.5
								|| ((host6Match.getWinRate() >= 0.5 && host6Match.getWinDrawPkRate() >= 0.6)
									|| (guest6Match.getWinRate() <= 0.5 && guest6Match.getWinDrawRate() <= 0.7)))) {
							rgs.add(ResultGroup.Three);
						}
					}
					else if (Math.abs(predictPk - mainPk) < 0.19) {
						// host is good and stable, guest is not good
						if (host6Match.getWinRate() >= 0.5 && host6Match.getWinDrawRate() >= 0.6
								&& host6Match.getMatchGoal() > host6Match.getMatchMiss()
								&& guest6Match.getWinPkRate() <= 0.5 && guest6Match.getWinDrawPkRate() <= 0.7) {
							rgs.add(ResultGroup.Three);
						}
						// pk is support, and host is good or guest is bad
						else if ((currentPk - mainPk >= 0.08 || main.gethWin() <= 0.9 && current.gethWin() <= 0.9)
								&& (host6Match.getWinRate() >= 0.5 || host6Match.getWinDrawRate() >= 0.6)
									&& (guest6Match.getWinRate() <= 0.5 && guest6Match.getWinDrawRate() <= 0.7)) {
							rgs.add(ResultGroup.Three);
						}
					} else {
						// think high of guest.. check below
					}
				}
				else if (main.getaWin() <= 0.9 && current.getaWin() <= 0.9) {
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
					else if (Math.abs(predictPk - mainPk) < 0.19) {
						if (!MatchUtil.isHostHomeStrong(hostLabels) 
								&& (mainPk - currentPk > 0.08 || main.getaWin() <= 0.88 && current.getaWin() <= 0.88)
								&& ((guest6Match.getWinPkRate() >= 0.5 && guest6Match.getWinDrawPkRate() >= 0.6)
									|| (host6Match.getWinRate() <= 0.5 && host6Match.getWinDrawRate() <= 0.7))) {
							rgs.add(ResultGroup.One);
							rgs.add(ResultGroup.Zero);
						}
					}
				}
			} else if (main.getPanKou() > -0.4) {
				if (main.gethWin() <= 0.9 && current.gethWin() <= 0.9) {
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
					} else if (Math.abs(predictPk - mainPk) < 0.15) {
						// host performs good or guest performs bad
						if ((currentPk > mainPk || main.gethWin() <= 0.88 && current.gethWin() <= 0.88)
								&& ((host6Match.getWinPkRate() >= 0.5 && host6Match.getWinDrawPkRate() >= 0.6)
										&& (guest6Match.getWinPkRate() <= 0.5 && guest6Match.getWinDrawPkRate() <= 0.7))) {
							rgs.add(ResultGroup.Three);
							rgs.add(ResultGroup.One);
						}
					} else {
						// think high of guest.. check below
					}
				} else if (main.getaWin() <= 0.92 && current.getaWin() <= 0.92) {
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
					else if (Math.abs(predictPk - mainPk) < 0.15) {
						// host performs good or guest performs bad
						if (mainPk >= currentPk
								&& ((guest6Match.getWinPkRate() >= 0.5 && guest6Match.getWinDrawPkRate() >= 0.6)
										&& (host6Match.getWinPkRate() <= 0.5 && host6Match.getWinDrawPkRate() <= 0.7))) {
							rgs.add(ResultGroup.One);
							rgs.add(ResultGroup.Zero);
						}
					}
				}
			} else if (main.getPanKou() > -1) {
				if (main.gethWin() <= 0.9 && current.gethWin() <= 0.9) {
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
						if (currentPk >= mainPk
								&& ((host6Match.getWinPkRate() >= 0.5 && host6Match.getWinDrawPkRate() >= 0.6)
										&& (guest6Match.getWinPkRate() <= 0.5 && guest6Match.getWinDrawPkRate() <= 0.7))) {
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
									|| host6Match.getWinDrawPkRate() <= 0.5
									|| (guest6Match.getWinRate() >= 0.5 || guest6Match.getWinDrawPkRate() >= 0.6)
										&& (host6Match.getWinPkRate() <= 0.5 && host6Match.getWinDrawPkRate() <= 0.7))) {
							rgs.add(ResultGroup.Zero);
						}
					}
					else if (Math.abs(predictPk - mainPk) < 0.19) {
						if (currentPk <= mainPk
								&& ((guest6Match.getWinRate() >= 0.5 || guest6Match.getWinDrawPkRate() >= 0.6)
										&& (host6Match.getWinPkRate() <= 0.5 && host6Match.getWinDrawPkRate() <= 0.7))) {
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
								&& (host6Match.getWinPkRate() <= 0.5 && host6Match.getWinDrawPkRate() <= 0.7)) {
							rgs.add(ResultGroup.Zero);
						}
					}
				}
			}
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
	
	private float calKillRate (PankouMatrices pkMatrices, Float hotPoint, Float mainPk, Float predictPk, ClubMatrices clubMs, boolean killWin) {
		Float rate = 0f;
		
		/*
		 * host is hot, and hwin is high -> possibly can't avoid win -> mainPk > predictPk -> good
		 * host is hot, and awin is high ->  mainPk < predictPk -> good
		 */
		if (killWin) {
			rate += pkMatrices.getHwinChangeRate();
			
			if (hotPoint >= 5) {
				
			}
		} else {
			rate += pkMatrices.getAwinChangeRate();
		}
		
		return rate;
	}
}
