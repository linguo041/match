package com.roy.football.match.OFN;

import java.util.List;

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
		ResultGroup hot = checkHotMatch(calResult);
		killPromoteResult.setTooHot(hot);
	}

	@Override
	public void kill(OFNKillPromoteResult killPromoteResult, OFNCalculateResult calResult) {
		if (killPromoteResult != null) {
			PankouMatrices pkMatrices = calResult.getPkMatrices();
			Float predictPk = calResult.getPredictPanKou();
			List <TeamLabel> hostLabels = calResult.getHostLabels();
			List <TeamLabel> guestLabels = calResult.getGuestLabels();
			
			if (pkMatrices != null) {
				float currPk = pkMatrices.getCurrentPk().getPanKou();
				ResultGroup pkRes = killZhuShangPan(pkMatrices, predictPk, hostLabels, guestLabels);
				ResultGroup plRes = killByWillLabDiff(calResult.getEuroMatrices(), currPk);

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
		
		if (pkMatrices != null) {
			killResult.setKillByPk(killZhuShangPan(pkMatrices, predictPk, hostLabels, guestLabels));
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
			
			if (pkMatrices != null) {
				killPromoteResult.setPromoteByPk(promoteByPk(pkMatrices, predictPk, hostLabels, guestLabels));
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
		
		if (pkMatrices != null) {
			killPromoteResult.setPromoteByPk(promoteByPk(pkMatrices, predictPk, hostLabels, guestLabels));
		}
		
		return killPromoteResult;
	}
	
	private ResultGroup killZhuShangPan (PankouMatrices pkMatrices, Float predictPk,
			List <TeamLabel> hostLabels, List <TeamLabel> guestLabels) {
		if (pkMatrices != null && predictPk != null) {
			AsiaPl origin = pkMatrices.getOriginPk();
			AsiaPl main = pkMatrices.getMainPk();
			AsiaPl current = pkMatrices.getCurrentPk();
			
			float mainPk = MatchUtil.getCalculatedPk(main);
			float currentPk = MatchUtil.getCalculatedPk(current);

			if (main.gethWin() > 1.01
					&& (current.gethWin() > 1.01 || origin.gethWin() > 1.01)
					&& mainPk - predictPk >= 0.12
					&& currentPk <= mainPk
					&& !MatchUtil.isHostHomeStrong(hostLabels)) {
				if (mainPk <= -0.5) {
					return ResultGroup.ThreeOne;
				} else if (mainPk < 0.75 && mainPk != 0) {
					return ResultGroup.Three;
				}
			}
			
			if (main.getaWin() > 1.01
					&& (current.getaWin() > 1.01 || origin.getaWin() > 1.01)
					&& predictPk - mainPk >= 0.12
					&& currentPk >= mainPk
					&& !MatchUtil.isGuestDefensive(guestLabels)) {
				if (mainPk >= 0.5) {
					return ResultGroup.OneZero;
				} else if (mainPk >= -0.5) {
					return ResultGroup.Zero;
				}
			}
		}

		return null;
	}
	
	private ResultGroup killByWillLabDiff (EuroMatrices euMatrices, Float aomenPk) {
		if (euMatrices != null) {
			EuroMatrix will = euMatrices.getWilliamMatrix();
			EuroMatrix lab = euMatrices.getLadMatrix();
			EuroMatrix aomen = euMatrices.getAomenMatrix();
			
			if (will != null && lab != null) {
				EuroPl mainWillEu = will.getMainEuro();
				EuroPl mainLabEu = lab.getMainEuro();
				EuroPl mainAomenEu = aomen.getMainEuro();
				
				float wlWinRt = MatchUtil.getEuDiff(mainWillEu.geteWin(), mainLabEu.geteWin(), true);
				float wlLoseRt = MatchUtil.getEuDiff(mainWillEu.geteLose(), mainLabEu.geteLose(), true);
				float waWinRt = MatchUtil.getEuDiff(mainWillEu.geteWin(), mainAomenEu.geteWin(), false);
				float waLoseRt = MatchUtil.getEuDiff(mainWillEu.geteLose(), mainAomenEu.geteLose(), false);
				if (aomenPk > 0.25) {
					if (wlWinRt > 0.055) {
						return ResultGroup.Three;
					}
					if (waWinRt < 0.01 && aomenPk <= 1) {
						return ResultGroup.Three;
					}
				} else if (aomenPk >= 0) {
					if (wlWinRt > 0.065) {
						return ResultGroup.Three;
					}
					if (waWinRt < 0.01) {
						return ResultGroup.Three;
					}
				} else if (aomenPk > -0.5) {
					if (wlLoseRt > 0.065) {
						return ResultGroup.Zero;
					}
					if (waLoseRt < 0.01) {
						return ResultGroup.Zero;
					}
				} else {
					if (wlLoseRt > 0.055) {
						return ResultGroup.Zero;
					}
					if (waLoseRt < 0.01 && aomenPk <= -1) {
						return ResultGroup.Zero;
					}
				}
			}
		}
		
		return null;
	}
	
	private ResultGroup promoteByPk (PankouMatrices pkMatrices, Float predictPk,
			List <TeamLabel> hostLabels, List <TeamLabel> guestLabels) {
		if (pkMatrices != null && predictPk != null) {
			AsiaPl main = pkMatrices.getMainPk();
			AsiaPl current = pkMatrices.getCurrentPk();

			float mainPk = MatchUtil.getCalculatedPk(main);
			float currentPk = MatchUtil.getCalculatedPk(current);

			if (main.gethWin() <= 0.96
					&& current.gethWin() <= 0.96
					&& predictPk - mainPk >= 0.12
					&& currentPk >= mainPk
					&& !MatchUtil.isGuestDefensive(guestLabels)) {
				if (mainPk >= 0.5) {
					return ResultGroup.Three;
				} else if (mainPk >= -0.5) {
					return ResultGroup.ThreeOne;
				}
			}
			
			if (main.getaWin() <= 0.96
					&& current.getaWin() <= 0.96
					&& mainPk - predictPk >= 0.12
					&& currentPk <= mainPk
					&& !MatchUtil.isHostHomeStrong(hostLabels)) {
				if (mainPk <= -0.5) {
					return ResultGroup.Zero;
				} else if (mainPk <= 0.5) {
					return ResultGroup.OneZero;
				}
			}
		}

		return null;
	}
	
	private ResultGroup checkHotMatch (OFNCalculateResult calResult) {
		ResultGroup result = null;
		
		ClubMatrices clubs = calResult.getClubMatrices();
		MatchState matchState = calResult.getMatchState();
		EuroMatrices euMatrices = calResult.getEuroMatrices();
		PankouMatrices pkMatrices = calResult.getPkMatrices();
		
		float mainPk = MatchUtil.getCalculatedPk(pkMatrices.getMainPk());
		float currentPk = MatchUtil.getCalculatedPk(pkMatrices.getCurrentPk());

		int clubPm = compareClub(clubs, calResult.getLeagueId());
		
		LatestMatchMatrices host6Match = matchState.getHostState6();
		LatestMatchMatrices guest6Match = matchState.getGuestState6();
		if (host6Match != null && guest6Match != null) {
			if (currentPk >= mainPk && clubPm == 1 && (host6Match.getWinRate() >= 0.5 || host6Match.getWinDrawRate() >= 0.7) ) {
				result = ResultGroup.Three;
			} else if (currentPk <= mainPk && clubPm == -1 && (guest6Match.getWinRate() >= 0.5 || guest6Match.getWinDrawRate() >= 0.7)) {
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
			return ResultGroup.Three;
		} else if (result == ResultGroup.Zero && waLoseRt >= 0.05) {
			return ResultGroup.Zero;
		}

		return null;
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
