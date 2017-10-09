package com.roy.football.match.OFN;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.math3.stat.StatUtils;
import org.apache.commons.math3.util.FastMath;
import org.springframework.stereotype.Component;

import com.roy.football.match.OFN.MatchPromoter.MatchPull;
import com.roy.football.match.OFN.MatchPromoter.MatchRank;
import com.roy.football.match.OFN.response.AsiaPl;
import com.roy.football.match.OFN.response.Company;
import com.roy.football.match.OFN.response.EuroPl;
import com.roy.football.match.OFN.statics.matrices.ClubMatrices;
import com.roy.football.match.OFN.statics.matrices.EuroMatrices;
import com.roy.football.match.OFN.statics.matrices.MatchState;
import com.roy.football.match.OFN.statics.matrices.OFNCalculateResult;
import com.roy.football.match.OFN.statics.matrices.OFNKillPromoteResult;
import com.roy.football.match.OFN.statics.matrices.PankouMatrices;
import com.roy.football.match.OFN.statics.matrices.ClubMatrices.ClubMatrix;
import com.roy.football.match.OFN.statics.matrices.EuroMatrices.EuroMatrix;
import com.roy.football.match.OFN.statics.matrices.MatchState.LatestMatchMatrices;
import com.roy.football.match.base.League;
import com.roy.football.match.base.MatchContinent;
import com.roy.football.match.base.ResultGroup;
import com.roy.football.match.base.TeamLabel;
import com.roy.football.match.util.EuroUtil;
import com.roy.football.match.util.MatchStateUtil;
import com.roy.football.match.util.MatchUtil;
import com.roy.football.match.util.PanKouUtil;
import com.roy.football.match.util.PanKouUtil.PKDirection;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class MatchPromoter {
	private static float FACTOR_H = 1.0f;
	private static float FACTOR_G = 1.0f;

	public OFNKillPromoteResult promote (OFNCalculateResult calResult) {
		OFNKillPromoteResult kpRes = new OFNKillPromoteResult();
		
		rankByBase(kpRes.getRank(), kpRes.getPull(), calResult.getClubMatrices());
		rankByLatest(kpRes.getRank(), kpRes.getPull(), calResult.getMatchState());
		promote(kpRes, calResult.getPkMatrices(),
				calResult.getEuroMatrices(), calResult.getPredictPanKou(), calResult.getLeague());
		
		return kpRes;
	}
	
	private void rankByBase (MatchRank rank, MatchPull pull, ClubMatrices clubMatrices) {
		// no enough match to calculate, give up ranking
		if (clubMatrices == null || clubMatrices.getHostAllMatrix() == null
				|| clubMatrices.getHostAllMatrix().getNum() <= 5) {
			return;
		}
		
		float hostAttGuestDefComp = clubMatrices.getHostAttGuestDefInx();
		float guestAttHostDefComp = clubMatrices.getGuestAttHostDefInx();
		ClubMatrix hostMatrix = clubMatrices.getHostAllMatrix();
		ClubMatrix guestMatrix = clubMatrices.getGuestAllMatrix();
		boolean divideHostGuest = MatchStateUtil.divideHostGuest();
		
		if (divideHostGuest) {
			hostMatrix = clubMatrices.getHostHomeMatrix();
			guestMatrix = clubMatrices.getGuestAwayMatrix();
		}
		
		// The factors calculated based on the history (TODO - split the factors per league or even per team)
		float diff = FACTOR_H * hostAttGuestDefComp - FACTOR_G * guestAttHostDefComp;
		
		// degree = diff / 0.125; so 4 degree means half goal, and 8 degree means one goal
		// The max degree calculated by diff is 16, and if the possibilities of win-draw-lose equal, split 16 into 3 parts,
		// which turns to be 5-5-5, but the related diff is nearly 0, so how to calculate 0 to relate to 5?
		// Here is the thought: we add 5 more to the win degree, and make the total 21, then split the rest 15 according to the diff.
		int wDegree = 0;
		int dDegree = 0;
		int lDegree = 0;
		int restDegree = 16;
		int adjustDegree = 0;
		
		// adjust by home win rate for host and way win rate for guest
		diff = calculateWinDegree(hostMatrix, guestMatrix, hostAttGuestDefComp, guestAttHostDefComp);
		adjustDegree += Math.abs(Math.round(8 * diff));
		
		if (diff >= 0 ) {
			wDegree += 5 + adjustDegree;
			restDegree -= wDegree;
			dDegree = adjustDrawDegreeReferToDrawRate(diff,  restDegree, hostMatrix, guestMatrix);
			lDegree = restDegree - dDegree;
		} else {
			lDegree += 5 + adjustDegree;
			restDegree -= lDegree;
			dDegree = adjustDrawDegreeReferToDrawRate(diff,  restDegree, hostMatrix, guestMatrix);
			wDegree = restDegree - dDegree;
		}
		
		rank.setWRank(wDegree);
		rank.setDRank(dDegree);
		rank.setLRank(lDegree);
	}

	private void rankByLatest (MatchRank rank, MatchPull pull, MatchState matchState) {
		if (matchState == null) {
			return;
		}
		
		Float latestHostAttack = matchState.getHostAttackToGuest();
		Float latestGuestAttack = matchState.getGuestAttackToHost();
		Float hotPoint = matchState.getHotPoint();

		LatestMatchMatrices hostMatches = matchState.getHostState6();
		LatestMatchMatrices guestMatches = matchState.getGuestState6();
		boolean divideHostGuest = MatchStateUtil.divideHostGuest();
		
		if (divideHostGuest) {
			hostMatches = matchState.getHostHome5();
			guestMatches = matchState.getGuestAway5();
		}
		
		// adjust base degree based on latest status
		int wDegree = rank.getWRank();
		int dDegree = rank.getDRank();
		int lDegree = rank.getLRank();
		
		// by goal
		float diff = FACTOR_H * latestHostAttack - FACTOR_G * latestGuestAttack;
		int degree = Math.round(8 * diff);
		if (degree >= 8) {
			// latest is better than base
			if (wDegree < 8) {
				wDegree ++;
				lDegree --;
			}
		} else if (degree >= 4) {
			// latest is worse than base
			if (wDegree > 10) {
				wDegree --;
				dDegree ++;
			} else if (wDegree > 6) {
				
			} else {
				wDegree ++;
				lDegree --;
			}
		} else if (degree >= -4) {
			// latest is worse than base
			if (wDegree > 10) {
				wDegree --;
				lDegree ++;
			} 
			// latest is good than base
			if (lDegree > 10) {
				lDegree --;
				wDegree ++;
			}
		} else if (degree >= -8){
			// latest is worse than base
			if (lDegree > 10) {
				lDegree --;
				dDegree ++;
			} else if (lDegree > 6) {
				
			} else {
				lDegree ++;
				wDegree --;
			}
		} else {
			if (lDegree <= 8) {
				lDegree ++;
				wDegree --;
			}
		}
		
		// by hotpoint
		if (hotPoint > 6) {
			// latest is better than base
			if (wDegree < 4) {
				wDegree += 2;
				lDegree -= 2;
			} else if (wDegree < 8) {
				if (hotPoint > 10 && wDegree < 6) {
					wDegree += 2;
					lDegree -= 2;
				} else {
					wDegree ++;
					lDegree --;
				}
			}
		} else if (hotPoint >= 3) {
			// latest is worse than base
			if (wDegree > 10) {
				wDegree --;
				dDegree ++;
			} else if (wDegree > 6) {
				
			} else if (wDegree > 3) {
				wDegree ++;
				lDegree --;
			}
		} else if (hotPoint > -3) {
			// latest is worse than base
			if (wDegree > 10) {
				wDegree --;
				lDegree ++;
			} 
			// latest is good than base
			if (lDegree > 10) {
				lDegree --;
				wDegree ++;
			}
		} else if (hotPoint >= -6){
			// latest is worse than base
			if (lDegree > 10) {
				lDegree --;
				dDegree ++;
			} else if (lDegree > 5) {
				
			} else {
				wDegree --;
				lDegree ++;
			}
		} else {
			if (lDegree < 4) {
				lDegree += 2;
				wDegree -= 2;
			} else if (lDegree <= 8) {
				if (hotPoint <= -10 && lDegree < 6) {
					lDegree += 2;
					wDegree -= 2;
				} else {
					lDegree ++;
					wDegree --;
				}
			}
		}

		rank.setWRank(wDegree);
		rank.setDRank(dDegree);
		rank.setLRank(lDegree);
		
		float hPull = 0f;
		float gPull = 0f;
		
		if (hotPoint > 0) {
			hPull = hotPoint;
		} else {
			gPull = Math.abs(hotPoint);
		}
		
		pullOfHeavyMatch(pull, matchState);
		
		pull.setHPull(pull.getHPull() + hPull);
		pull.setGPull(pull.getGPull() + gPull);
	}
	
	private void pullOfHeavyMatch (MatchPull pull, MatchState matchState) {
		
	}
	
	private void rankByJiaoShou (MatchRank rank, MatchPull pull) {
		
	}

	private void promote (OFNKillPromoteResult killPromoteResult, PankouMatrices pkMatrices,
			EuroMatrices euroMatrices, Float predictPk, League le) {
		if (pkMatrices == null) {
			return;
		}
		
		MatchRank rank = killPromoteResult.getRank();
		MatchPull pull = killPromoteResult.getPull();
		Set<ResultGroup> promote = killPromoteResult.getPromoteByBase();

		// Euro pl
		Map<Company, EuroMatrix> companyEus = euroMatrices.getCompanyEus();
		EuroPl euroAvg = euroMatrices.getCurrEuroAvg();
		EuroMatrix aomen = companyEus.get(Company.Aomen);
		EuroMatrix jincai = companyEus.get(Company.Jincai);
		EuroMatrix will = companyEus.get(Company.William);
		float jcWinChange = jincai.getWinChange();
		float jcDrawChange = jincai.getDrawChange();
		float jcLoseChange = jincai.getLoseChange();
		float aomenWinChange = aomen.getWinChange();
		float aomenDrawChange = aomen.getDrawChange();
		float aomenLoseChange = aomen.getLoseChange();
		float willWinChange = will.getWinChange();
		float willDrawChange = will.getDrawChange();
		float willLoseChange = will.getLoseChange();
		
		float jaWinDiff = MatchUtil.getEuDiff(jincai.getCurrentEuro().getEWin(), euroAvg.getEWin(), false);
		float jaDrawDiff = MatchUtil.getEuDiff(jincai.getCurrentEuro().getEDraw(), euroAvg.getEDraw(), false);
		float jaLoseDiff = MatchUtil.getEuDiff(jincai.getCurrentEuro().getELose(), euroAvg.getELose(), false);
		float aaWinDiff = MatchUtil.getEuDiff(aomen.getCurrentEuro().getEWin(), euroAvg.getEWin(), false);
		float aaDrawDiff = MatchUtil.getEuDiff(aomen.getCurrentEuro().getEDraw(), euroAvg.getEDraw(), false);
		float aaLoseDiff = MatchUtil.getEuDiff(aomen.getCurrentEuro().getELose(), euroAvg.getELose(), false);
		float waWinDiff = MatchUtil.getEuDiff(will.getCurrentEuro().getEWin(), euroAvg.getEWin(), false);
		float waDrawDiff = MatchUtil.getEuDiff(will.getCurrentEuro().getEDraw(), euroAvg.getEDraw(), false);
		float waLoseDiff = MatchUtil.getEuDiff(will.getCurrentEuro().getELose(), euroAvg.getELose(), false);
		
		// pk
		AsiaPl main = pkMatrices.getMainPk();
		AsiaPl current = pkMatrices.getCurrentPk();
		
		killByEuro(killPromoteResult.getKillByPl(), euroMatrices, pkMatrices, le);
		killPkPlUnmatchChange(current, aomen.getCurrentEuro(), le, killPromoteResult.getKillByPlPkUnmatch());
		
		float mainPk = MatchUtil.getCalculatedPk(main);
		float currentPk = MatchUtil.getCalculatedPk(current);
		float pmPkDiff = predictPk - mainPk;
		float pcPkDiff = predictPk - currentPk;
		PKDirection pkDirection = PanKouUtil.getPKDirection(current, main);
		boolean isAomenMajor = EuroUtil.isAomenTheMajor(le) || le.getContinent() == MatchContinent.Asia;
		Float upChange = pkMatrices.getHwinChangeRate();
		Float downChange = pkMatrices.getAwinChangeRate();
		ResultGroup firstOption = null;
		ResultGroup secondOption = null;
		
		killByPull(predictPk, current.getPanKou(), currentPk, mainPk, pull, killPromoteResult.getKillByPull());
		killByPk(killPromoteResult.getKillByPk(), rank, pull, pkMatrices, predictPk);
		
		if (current.getPanKou() >= 1) {
			if (isAomenMajor) {
				if (rank.getWRank() >= 7
						&& (pmPkDiff <= 0.35f || pcPkDiff <= 0.35f)
						&& (pkDirection.ordinal() > PKDirection.Downer.ordinal()
								|| pkDirection.ordinal() == PKDirection.Downer.ordinal() && upChange <= 0.04f)
						&& (aaWinDiff < 0.021f && aomenWinChange < 0.011f || aaWinDiff < -0.021f)
						&& waWinDiff <= 0.03f
						&& jaWinDiff <= -0.02f) {
					firstOption = ResultGroup.Three;
				}
				if ((rank.getWRank() >= 10 ? rank.getDRank() >= 4 : rank.getDRank() >= 5)
						&& pkDirection.ordinal() < PKDirection.Uper.ordinal()
						&& aaDrawDiff <= 0.005f && aomenDrawChange < 0.011f
						&& jaDrawDiff <= 0.035f && jcDrawChange < 0.005f // jc draw diff is always high 
						) {
					if (aaDrawDiff <= -0.025f || aomen.getOriginEuro().getEDraw() <= 3.30f
							|| jaDrawDiff <= -0.025f || jincai.getCurrentEuro().getEDraw() <= 3.30f) {
						if (firstOption == null) {
							firstOption = ResultGroup.One;
						} else {
							secondOption = ResultGroup.One;
						}
					}
				}
				if ((rank.getWRank() >= 10 ? rank.getLRank() >= 4 : rank.getLRank() >= 5)
						&& pkDirection.ordinal() < PKDirection.Uper.ordinal()
						&& aaLoseDiff < 0.001f && aomenLoseChange < 0.011f
						&& jaLoseDiff < 0.03f && jcLoseChange < 0.001f
						) {
					if (aaLoseDiff < -0.025f || jaLoseDiff < -0.025f) {
						if (firstOption == null) {
							firstOption = ResultGroup.Zero;
						} else {
							secondOption = ResultGroup.Zero;
						}
					}
				}
			} else {
				if (rank.getWRank() >= 7
						&& (pmPkDiff <= 0.35f || pcPkDiff <= 0.35f)
						&& (pkDirection.ordinal() > PKDirection.Downer.ordinal()
								|| pkDirection.ordinal() == PKDirection.Downer.ordinal() && upChange <= 0.04f)
						&& (aaWinDiff < 0.021f && aomenWinChange < 0.011f || aaWinDiff < -0.021f)
						&& waWinDiff < 0.035f
						&& jaWinDiff <= -0.035f) {
					firstOption = ResultGroup.Three;
				}
				if ((rank.getWRank() >= 10 ? rank.getDRank() >= 4 : rank.getDRank() >= 5)
						&& pkDirection.ordinal() < PKDirection.Uper.ordinal()
						&& aaDrawDiff <= -0.011f && aomenDrawChange < 0.011f
						&& jaDrawDiff <= 0.035f && jcDrawChange < 0.001f
						) {
					if (aaDrawDiff <= -0.035f || aomen.getOriginEuro().getEDraw() <= 3.30f
							|| jaDrawDiff <= -0.035f || jincai.getCurrentEuro().getEDraw() <= 3.30f) {
						if (firstOption == null) {
							firstOption = ResultGroup.One;
						} else {
							secondOption = ResultGroup.One;
						}
					}
				}
				if ((rank.getWRank() >= 10 ? rank.getLRank() >= 4 : rank.getLRank() >= 5)
						&& pkDirection.ordinal() < PKDirection.Uper.ordinal()
						&& aaLoseDiff < -0.03f && aomenLoseChange < 0.011f
						&& jaLoseDiff < 0.03f && jcLoseChange < 0.005f
						) {
					if (aaLoseDiff < -0.045f || jaLoseDiff < -0.045f) {
						if (firstOption == null) {
							firstOption = ResultGroup.Zero;
						} else {
							secondOption = ResultGroup.Zero;
						}
					}
				}
			}
		} else if (current.getPanKou() >= 0.4) {
			if (isAomenMajor) {
				if (rank.getWRank() >= 6
						&& (pmPkDiff <= 0.3f || pcPkDiff <= 0.3f)
						&& (pkDirection.ordinal() > PKDirection.Down.ordinal()
								|| pkDirection.ordinal() == PKDirection.Downer.ordinal() && upChange <= 0.04f)
						&& (aaWinDiff < 0.021f && aomenWinChange < 0.011f || aaWinDiff < -0.021f)
						&& waWinDiff <= 0.03f
						&& jaWinDiff <= -0.02f) {
					firstOption = ResultGroup.Three;
				}
				if ((rank.getWRank() >= 10 ? rank.getDRank() >= 4 : rank.getDRank() >= 5)
						&& pkDirection.ordinal() < PKDirection.Uper.ordinal()
						&& aaDrawDiff <= 0.011f && aomenDrawChange < 0.019f
						&& jaDrawDiff < 0.035f && jcDrawChange < 0.005f
						) {
					if (aaDrawDiff <= -0.021f || aomen.getOriginEuro().getEDraw() <= 3.20f
							|| jaDrawDiff <= -0.021f || jincai.getCurrentEuro().getEDraw() <= 3.20f) {
						if (firstOption == null) {
							firstOption = ResultGroup.One;
						} else {
							secondOption = ResultGroup.One;
						}
					}
				}
				if ((rank.getWRank() >= 10 ? rank.getLRank() >= 4 : rank.getLRank() >= 5)
						&& (pmPkDiff >= -0.3f || pcPkDiff >= -0.3f)
						&& pkDirection.ordinal() < PKDirection.Uper.ordinal()
						&& aaLoseDiff < -0.011f && aomenLoseChange < 0.011f
						&& jaLoseDiff < 0.03f && jcLoseChange < 0.005f
						) {
					if (aaLoseDiff < -0.03f || jaLoseDiff < -0.03f) {
						if (firstOption == null) {
							firstOption = ResultGroup.Zero;
						} else {
							secondOption = ResultGroup.Zero;
						}
					}
				}
			} else {
				if (rank.getWRank() >= 6
						&& (pmPkDiff <= 0.3f || pcPkDiff <= 0.3f)
						&& (pkDirection.ordinal() > PKDirection.Down.ordinal()
								|| pkDirection.ordinal() == PKDirection.Downer.ordinal() && upChange <= 0.04f)
						&& (aaWinDiff < 0.011f && aomenWinChange < 0.011f || aaWinDiff < -0.021f)
						&& waWinDiff < 0.035f
						&& jaWinDiff <= -0.035f) {
					firstOption = ResultGroup.Three;
				}
				if ((rank.getWRank() >= 10 ? rank.getDRank() >= 4 : rank.getDRank() >= 5)
						&& pkDirection.ordinal() < PKDirection.Uper.ordinal()
						&& aaDrawDiff <= 0.011f && aomenDrawChange < 0.019f
						&& jaDrawDiff <= 0.035f && jcDrawChange < 0.005f
						) {
					if (aaDrawDiff <= -0.03f || aomen.getOriginEuro().getEDraw() <= 3.30f
							|| jaDrawDiff <= -0.03f || jincai.getCurrentEuro().getEDraw() <= 3.30f) {
						if (firstOption == null) {
							firstOption = ResultGroup.One;
						} else {
							secondOption = ResultGroup.One;
						}
					}
				}
				if ((rank.getWRank() >= 10 ? rank.getLRank() >= 4 : rank.getLRank() >= 5)
						&& (pmPkDiff >= -0.3f || pcPkDiff >= -0.3f)
						&& pkDirection.ordinal() < PKDirection.Uper.ordinal()
						&& aaLoseDiff < -0.021f && aomenLoseChange < 0.011f
						&& aaLoseDiff < 0.03f && jcLoseChange < 0.005f
						) {
					if (aaLoseDiff < -0.035f || jaLoseDiff < -0.035f) {
						if (firstOption == null) {
							firstOption = ResultGroup.Zero;
						} else {
							secondOption = ResultGroup.Zero;
						}
					}
				}
			}
		} else if (current.getPanKou() >= 0.25) {
			if (isAomenMajor) {
				if (rank.getWRank() >= 5
						&& (pmPkDiff <= 0.3f || pcPkDiff <= 0.3f)
						&& (pkDirection.ordinal() > PKDirection.Middle.ordinal()
								|| pkDirection.ordinal() == PKDirection.Downer.ordinal() && upChange <= 0.04f)
						&& (aaWinDiff < 0.021f && aomenWinChange < 0.011f || aaWinDiff < -0.021f)
						&& waWinDiff <= 0.03f
						&& jaWinDiff <= -0.02f) {
					firstOption = ResultGroup.Three;
				}
				if ((rank.getWRank() >= 10 ? rank.getDRank() >= 4 : rank.getDRank() >= 5)
						&& aaDrawDiff < 0.011f && aomenDrawChange < 0.019f
						&& jaDrawDiff <= 0.025f && jcDrawChange < 0.005f
						) {
					if (aaDrawDiff <= -0.021f || aomen.getOriginEuro().getEDraw() <= 3.20f
							|| jaDrawDiff <= -0.021f || jincai.getCurrentEuro().getEDraw() <= 3.20f) {
						if (firstOption == null) {
							firstOption = ResultGroup.One;
						} else {
							secondOption = ResultGroup.One;
						}
					}
				}
				if ((rank.getWRank() >= 10 ? rank.getLRank() >= 4 : rank.getLRank() >= 5)
						&& (pmPkDiff >= -0.3f || pcPkDiff >= -0.3f)
						&& (pkDirection.ordinal() < PKDirection.Middle.ordinal()
								|| rank.getLRank() >= 6 && pkDirection.ordinal() < PKDirection.Up.ordinal())
						&& aaLoseDiff < 0.005f && aomenLoseChange < 0.011f
						&& jaLoseDiff < 0.025f && jcLoseChange < 0.005f
						) {
					if (aaLoseDiff < -0.025f || jaLoseDiff < -0.025f) {
						if (firstOption == null) {
							firstOption = ResultGroup.Zero;
						} else {
							secondOption = ResultGroup.Zero;
						}
					}
				}
			} else {
				if (rank.getWRank() >= 5
						&& (pmPkDiff <= 0.3f || pcPkDiff <= 0.3f)
						&& (pkDirection.ordinal() > PKDirection.Middle.ordinal()
								|| pkDirection.ordinal() == PKDirection.Downer.ordinal() && upChange <= 0.04f)
						&& (aaWinDiff < 0.011f && aomenWinChange < 0.011f || aaWinDiff < -0.021f)
						&& waWinDiff <= 0.03f
						&& jaWinDiff <= -0.035f) {
					firstOption = ResultGroup.Three;
				}
				if ((rank.getWRank() >= 10 ? rank.getDRank() >= 4 : rank.getDRank() >= 5)
							&& aaDrawDiff <= -0.011f && aomenDrawChange < 0.019f
							&& jaDrawDiff <= 0.019f && jcDrawChange < 0.005f
						) {
					if (aaDrawDiff <= -0.03f || aomen.getOriginEuro().getEDraw() <= 3.20f
							|| jaDrawDiff <= -0.03f || jincai.getCurrentEuro().getEDraw() <= 3.20f) {
						if (firstOption == null) {
							firstOption = ResultGroup.One;
						} else {
							secondOption = ResultGroup.One;
						}
					}
				}
				if ((rank.getWRank() >= 10 ? rank.getLRank() >= 4 : rank.getLRank() >= 5)
						&& (pmPkDiff >= -0.3f || pcPkDiff >= -0.3f)
						&& (pkDirection.ordinal() < PKDirection.Middle.ordinal()
								|| rank.getLRank() >= 6 && pkDirection.ordinal() < PKDirection.Up.ordinal())
						&& aaLoseDiff < -0.011f && aomenLoseChange < 0.011f
						&& jaLoseDiff < 0.019f && jcLoseChange < 0.005f
						) {
					if ((aaLoseDiff < -0.035f || jaLoseDiff < -0.035f)) {
						if (firstOption == null) {
							firstOption = ResultGroup.Zero;
						} else {
							secondOption = ResultGroup.Zero;
						}
					}
				}
			}
		} else if (current.getPanKou() >= 0) {
			if (rank.getWRank() >= 5
					&& (Math.abs(pmPkDiff) <= 0.3f || Math.abs(pcPkDiff) <= 0.3f)
					&& (pkDirection.ordinal() != PKDirection.Downer.ordinal()
							|| pkDirection.ordinal() == PKDirection.Downer.ordinal() && upChange <= 0.04f)
					&& aaWinDiff < -0.001f && aomenWinChange < 0.011f
					&& jaWinDiff < -0.001f && jcWinChange < 0.001f
					&& waWinDiff <= 0.03f) {
				if ((aaWinDiff < -0.025f || jaWinDiff < -0.035f)) {
					firstOption = ResultGroup.Three;
				}
			}
			if (rank.getDRank() >= 6
					&& aaDrawDiff <= -0.001f && aomenDrawChange < 0.011f
					&& jaDrawDiff <= -0.001f && jcDrawChange < 0.005f
					) {
				if (aaDrawDiff <= -0.025f || aomen.getOriginEuro().getEDraw() <= 3.20f
						|| jaDrawDiff <= -0.025f || jincai.getCurrentEuro().getEDraw() <= 3.20f) {
					if (firstOption == null) {
						firstOption = ResultGroup.One;
					} else {
						secondOption = ResultGroup.One;
					}
				}
			}
			if (rank.getLRank() >= 5
					&& (Math.abs(pmPkDiff) <= 0.3f || Math.abs(pcPkDiff) <= 0.3f)
					&& (pkDirection.ordinal() != PKDirection.Uper.ordinal()
							|| pkDirection.ordinal() == PKDirection.Uper.ordinal() && upChange <= 0.04f)
					&& aaLoseDiff < -0.001f && aomenLoseChange < 0.011f
					&& jaLoseDiff < -0.001f && jcLoseChange < 0.005f
					&& waLoseDiff <= 0.03f
					) {
				if ((aaLoseDiff < -0.025f || jaLoseDiff < -0.035f)) {
					if (firstOption == null) {
						firstOption = ResultGroup.Zero;
					} else {
						secondOption = ResultGroup.Zero;
					}
				}
			}
		} else if (current.getPanKou() >= -0.25) {
			if (isAomenMajor) {
				if (rank.getLRank() >= 5
						&& (pmPkDiff >= -0.3f || pcPkDiff >= -0.3f)
						&& (pkDirection.ordinal() < PKDirection.Middle.ordinal()
								|| pkDirection.ordinal() == PKDirection.Uper.ordinal() && downChange <= 0.04f)
						&& (aaLoseDiff < 0.021f && aomenLoseChange < 0.011f || aaLoseDiff < -0.021f)
						&& waLoseDiff <= 0.03f
						&& jaLoseDiff <= -0.02f) {
					firstOption = ResultGroup.Zero;
				}
				if ((rank.getLRank() >= 10 ? rank.getDRank() >= 4 : rank.getDRank() >= 5)
						&& aaDrawDiff < 0.011f && aomenDrawChange < 0.019f
						&& jaDrawDiff <= 0.025f && jcDrawChange < 0.005f 
						) {
					if (aaDrawDiff <= -0.025f || aomen.getOriginEuro().getEDraw() <= 3.20f
							|| jaDrawDiff <= -0.025f || jincai.getCurrentEuro().getEDraw() <= 3.20f) {
						if (firstOption == null) {
							firstOption = ResultGroup.One;
						} else {
							secondOption = ResultGroup.One;
						}
					}
				}
				if ((rank.getLRank() >= 10 ? rank.getWRank() >= 4 : rank.getWRank() >= 5)
						&& (pmPkDiff <= 0.3f || pcPkDiff <= 0.3f)
						&& (pkDirection.ordinal() > PKDirection.Middle.ordinal()
								|| rank.getWRank() >= 5 && pkDirection.ordinal() > PKDirection.Down.ordinal())
						&& aaWinDiff < -0.001f && aomenWinChange < 0.011f
						&& jaWinDiff < 0.025f && jcWinChange < 0.005f
						) {
					if (aaWinDiff < -0.025f || jaWinDiff < -0.025f) {
						if (firstOption == null) {
							firstOption = ResultGroup.Three;
						} else {
							secondOption = ResultGroup.Three;
						}
					}
				}
			} else {
				if (rank.getLRank() >= 5
						&& (pmPkDiff >= -0.3f || pcPkDiff >= -0.3f)
						&& (pkDirection.ordinal() < PKDirection.Middle.ordinal()
								|| pkDirection.ordinal() == PKDirection.Uper.ordinal() && downChange <= 0.04f)
						&& (aaLoseDiff < 0.011f && aomenLoseChange < 0.011f || aaLoseDiff < -0.021f)
						&& waLoseDiff <= 0.03f
						&& jaLoseDiff <= -0.035f) {
					firstOption = ResultGroup.Zero;
				}
				if ((rank.getLRank() >= 10 ? rank.getDRank() >= 4 : rank.getDRank() >= 5)
						&& aaDrawDiff <= -0.001f && aomenDrawChange < 0.019f
						&& jaDrawDiff < 0.019f && jcDrawChange < 0.005f
						) {
					if (aaDrawDiff <= -0.025f || aomen.getOriginEuro().getEDraw() <= 3.20f
							|| jaDrawDiff <= -0.025f || jincai.getCurrentEuro().getEDraw() <= 3.20f) {
						if (firstOption == null) {
							firstOption = ResultGroup.One;
						} else {
							secondOption = ResultGroup.One;
						}
					}
				}
				if ((rank.getLRank() >= 10 ? rank.getWRank() >= 4 : rank.getWRank() >= 5)
						&& (pmPkDiff <= 0.3f || pcPkDiff <= 0.3f)
						&& (pkDirection.ordinal() > PKDirection.Middle.ordinal()
								|| rank.getWRank() >= 5 && pkDirection.ordinal() > PKDirection.Down.ordinal())
						&& aaWinDiff < -0.001f && aomenWinChange < 0.011f
						&& jaWinDiff < 0.019f && jcWinChange < 0.005f
						) {
					if (aaWinDiff < -0.035f || jaWinDiff < -0.045f) {
						if (firstOption == null) {
							firstOption = ResultGroup.Three;
						} else {
							secondOption = ResultGroup.Three;
						}
					}
				}
			}
		} else if (current.getPanKou() >= -0.8) {
			if (isAomenMajor) {
				if (rank.getLRank() >= 6
						&& (pmPkDiff >= -0.3f || pcPkDiff >= -0.3f)
						&& (pkDirection.ordinal() < PKDirection.Up.ordinal()
								|| pkDirection.ordinal() == PKDirection.Uper.ordinal() && downChange <= 0.04f)
						&& (aaLoseDiff < 0.021f && aomenLoseChange < 0.011f || aaLoseDiff < -0.021f)
						&& waLoseDiff <= 0.03f
						&& jaLoseDiff <= -0.02f) {
					firstOption = ResultGroup.Zero;
				}
				if ((rank.getLRank() >= 10 ? rank.getDRank() >= 4 : rank.getDRank() >= 5)
						&& pkDirection.ordinal() > PKDirection.Middle.ordinal()
						&& aaDrawDiff < 0.011f && aomenDrawChange < 0.019f
						&& jaDrawDiff <= 0.035f && jcDrawChange < 0.005f
						) {
					if (aaDrawDiff <= -0.025f || aomen.getOriginEuro().getEDraw() <= 3.30f
							|| jaDrawDiff <= -0.025f || jincai.getCurrentEuro().getEDraw() <= 3.30f) {
						if (firstOption == null) {
							firstOption = ResultGroup.One;
						} else {
							secondOption = ResultGroup.One;
						}
					}
				}
				if ((rank.getLRank() >= 10 ? rank.getWRank() >= 4 : rank.getWRank() >= 5)
						&& (pmPkDiff <= 0.3f || pcPkDiff <= 0.3f)
						&& pkDirection.ordinal() > PKDirection.Middle.ordinal()
						&& aaWinDiff < -0.001f && aomenWinChange < 0.011f
						&& aaWinDiff < 0.035f && jcWinChange < 0.005f
						) {
					if (aaWinDiff < -0.035f || jaWinDiff < -0.045f) {
						if (firstOption == null) {
							firstOption = ResultGroup.Three;
						} else {
							secondOption = ResultGroup.Three;
						}
					}
				}
			} else {
				if (rank.getLRank() >= 6
						&& (pmPkDiff >= -0.3f || pcPkDiff >= -0.3f)
						&& (pkDirection.ordinal() < PKDirection.Up.ordinal()
								|| pkDirection.ordinal() == PKDirection.Uper.ordinal() && downChange <= 0.04f)
						&& (aaLoseDiff < 0.021f && aomenLoseChange < 0.011f || aaLoseDiff < -0.021f)
						&& waLoseDiff < 0.035f
						&& jaLoseDiff <= -0.035f) {
					firstOption = ResultGroup.Zero;
				}
				if ((rank.getLRank() >= 10 ? rank.getDRank() >= 4 : rank.getDRank() >= 5)
						&& pkDirection.ordinal() > PKDirection.Middle.ordinal()
						&& aaDrawDiff <= 0.001f && aomenDrawChange < 0.019f
						&& jaDrawDiff <= 0.035f && jcDrawChange < 0.005f
						) {
					if (aaDrawDiff <= -0.03f || aomen.getOriginEuro().getEDraw() <= 3.30f
							|| jaDrawDiff <= -0.035f || jincai.getCurrentEuro().getEDraw() <= 3.30f) {
						if (firstOption == null) {
							firstOption = ResultGroup.One;
						} else {
							secondOption = ResultGroup.One;
						}
					}
				}
				if ((rank.getLRank() >= 10 ? rank.getWRank() >= 4 : rank.getWRank() >= 5)
						&& (pmPkDiff <= 0.3f || pcPkDiff <= 0.3f)
						&& pkDirection.ordinal() > PKDirection.Middle.ordinal()
						&& aaWinDiff < -0.001f && aomenWinChange < 0.011f
						&& jaWinDiff < 0.03f && jcWinChange < 0.005f
						) {
					if (aaWinDiff < -0.035f || jaWinDiff < -0.045f) {
						if (firstOption == null) {
							firstOption = ResultGroup.Three;
						} else {
							secondOption = ResultGroup.Three;
						}
					}
				}
			}
		} else {
			if (isAomenMajor) {
				if (rank.getLRank() >= 7
						&& (pmPkDiff >= -0.35f || pcPkDiff >= -0.35f)
						&& (pkDirection.ordinal() < PKDirection.Uper.ordinal()
								|| pkDirection.ordinal() == PKDirection.Uper.ordinal() && downChange <= 0.04f)
						&& (aaLoseDiff < 0.021f && aomenLoseChange < 0.011f || aaLoseDiff < -0.021f)
						&& waLoseDiff <= 0.03f
						&& jaLoseDiff <= -0.02f) {
					firstOption = ResultGroup.Zero;
				}
				if ((rank.getLRank() >= 10 ? rank.getDRank() >= 4 : rank.getDRank() >= 5)
						&& pkDirection.ordinal() > PKDirection.Middle.ordinal()
						&& aaDrawDiff <= -0.001f && aomenDrawChange < 0.011f
						&& jaDrawDiff <= 0.035f && jcDrawChange < 0.005f 
						) {
					if (aaDrawDiff <= -0.025f || aomen.getOriginEuro().getEDraw() <= 3.30f
							|| jaDrawDiff <= -0.025f || jincai.getCurrentEuro().getEDraw() <= 3.30f) {
						if (firstOption == null) {
							firstOption = ResultGroup.One;
						} else {
							secondOption = ResultGroup.One;
						}
					}
				}
				if ((rank.getLRank() >= 10 ? rank.getWRank() >= 4 : rank.getWRank() >= 5)
						&& pkDirection.ordinal() > PKDirection.Middle.ordinal()
						&& aaWinDiff < -0.001f && aomenWinChange < 0.011f
						&& jaWinDiff < 0.03f && jcWinChange < 0.005f
						) {
					if (aaWinDiff < -0.035f || jaWinDiff < -0.035f) {
						if (firstOption == null) {
							firstOption = ResultGroup.Three;
						} else {
							secondOption = ResultGroup.Three;
						}
					}
				}
			} else {
				if (rank.getLRank() >= 7
						&& (pmPkDiff >= -0.35f || pcPkDiff >= -0.35f)
						&& (pkDirection.ordinal() < PKDirection.Uper.ordinal()
								|| pkDirection.ordinal() == PKDirection.Uper.ordinal() && downChange <= 0.04f)
						&& (aaLoseDiff < 0.021f && aomenLoseChange < 0.011f || aaLoseDiff < -0.021f)
						&& waLoseDiff < 0.035f
						&& jaLoseDiff <= -0.035f) {
					firstOption = ResultGroup.Zero;
				}
				if ((rank.getLRank() >= 10 ? rank.getDRank() >= 4 : rank.getDRank() >= 5)
						&& pkDirection.ordinal() > PKDirection.Middle.ordinal()
						&& aaDrawDiff <= -0.001f && aomenDrawChange < 0.011f
						&& aaDrawDiff <= 0.035f && jcDrawChange < 0.005f 
						) {
					if (aaDrawDiff <= -0.035f || aomen.getOriginEuro().getEDraw() <= 3.30f
							|| jaDrawDiff <= -0.035f || jincai.getCurrentEuro().getEDraw() <= 3.30f) {
						if (firstOption == null) {
							firstOption = ResultGroup.One;
						} else {
							secondOption = ResultGroup.One;
						}
					}
				}
				if ((rank.getLRank() >= 10 ? rank.getWRank() >= 4 : rank.getWRank() >= 5)
						&& pkDirection.ordinal() > PKDirection.Middle.ordinal()
						&& aaWinDiff < -0.021f && aomenWinChange < 0.011f
						&& jaWinDiff < 0.03f && jcWinChange < 0.005f
						) {
					if (aaWinDiff < -0.045f || jaWinDiff < -0.05f) {
						if (firstOption == null) {
							firstOption = ResultGroup.Three;
						} else {
							secondOption = ResultGroup.Three;
						}
					}
				}
			}
		}

		if (firstOption != null) {
			promote.add(firstOption);
		}
		if (secondOption != null) {
			promote.add(secondOption);
		}
	}
	
	private void killByEuro (Set<ResultGroup> killGps, EuroMatrices euMatrices, PankouMatrices pkMatrices, League league) {

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
	
	private void killByPull (float predictPk, float pankou,float currentPk, float mainPk, MatchPull pull, Set<ResultGroup> killByPull) {
		if (predictPk - mainPk >= 0.15f && predictPk - currentPk >= 0.13f && pull.getHPull() >= 6) {
			if (pankou >= -0.25) {
				killByPull.add(ResultGroup.Three);
			}
		} else if (mainPk - predictPk >= 0.15f && currentPk - predictPk >= 0.13f && pull.getGPull() >= 6) {
			if (pankou <= 0.25) {
				killByPull.add(ResultGroup.Zero);
			}
		}
	}
	
	private void killByPk (Set<ResultGroup> killByPk, MatchRank rank, MatchPull pull,
			PankouMatrices pkMatrices, Float predictPk) {
		if (pkMatrices != null && predictPk != null) {
			AsiaPl origin = pkMatrices.getOriginPk();
			AsiaPl main = pkMatrices.getMainPk();
			AsiaPl current = pkMatrices.getCurrentPk();
			Float upChange = pkMatrices.getHwinChangeRate();
			Float downChange = pkMatrices.getAwinChangeRate();
			float mainPk = MatchUtil.getCalculatedPk(main);
			float currentPk = MatchUtil.getCalculatedPk(current);
			PKDirection pkDirection = PanKouUtil.getPKDirection(current, main);

			// the predict and main is nearly same, but the company chooses high pay to stop betting on win/lose.
			if ((pkDirection.ordinal() < PKDirection.Middle.ordinal() || current.gethWin() > 1.02)
					&& upChange >= 0.06f
					&& currentPk - mainPk <= 0.04f
					&& Math.abs(predictPk - mainPk) < 0.29) {

				if (current.getPanKou() >= 1.0f && rank.getWRank() <= 9) {
					killByPk.add(ResultGroup.RangThree);
				} else if (current.getPanKou() >= 0.5f && rank.getWRank() <= 8) {
					killByPk.add(ResultGroup.Three);
				} else if (current.getPanKou() >= -0.25f && rank.getWRank() <= 7) {
					killByPk.add(ResultGroup.Three);
				} else if (current.getPanKou() <= -0.5f) {
					if (rank.getWRank() <= 6) {
						killByPk.add(ResultGroup.Three);
					}
					if (rank.getDRank() <= 6) {
						killByPk.add(ResultGroup.One);
					}
				}
			}
			
			if ((pkDirection.ordinal() > PKDirection.Middle.ordinal() || current.getaWin() > 1.02)
					&& downChange >= 0.06f
					&& currentPk - mainPk >= -0.04f
					&& Math.abs(predictPk - mainPk) < 0.29) {

				if (current.getPanKou() <= -1.0f && rank.getLRank() <= 9) {
					killByPk.add(ResultGroup.RangZero);
				} else if (current.getPanKou() <= -0.5f && rank.getLRank() <= 8) {
					killByPk.add(ResultGroup.Zero);
				} else if (current.getPanKou() <= 0.25f && rank.getLRank() <= 7) {
					killByPk.add(ResultGroup.Zero);
				} else if (current.getPanKou() >= 0.5f) {
					if (rank.getLRank() <= 6) {
						killByPk.add(ResultGroup.Zero);
					}
					if (rank.getDRank() <= 6) {
						killByPk.add(ResultGroup.One);
					}
				}
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
			if ((currAomenEu.getEWin() - avg) - (aomenPk.getPanKou() - calculatedPk) >= 0.08) {
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
			if ((currAomenEu.getEWin() - avg) - (aomenPk.getPanKou() - calculatedPk) >= 0.08) {
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
			if ((currAomenEu.getEWin() - avg) - (aomenPk.getPanKou() - calculatedPk) >= 0.08) {
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
			if ((currAomenEu.getEWin() - avg) - (aomenPk.getPanKou() - calculatedPk) >= 0.08) {
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
			if ((currAomenEu.getELose() - avg) - (calculatedPk - aomenPk.getPanKou()) >= 0.08) {
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
			if ((currAomenEu.getELose() - avg) - (calculatedPk - aomenPk.getPanKou()) >= 0.08) {
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
			if ((currAomenEu.getELose() - avg) - (calculatedPk - aomenPk.getPanKou()) >= 0.08) {
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
			if ((currAomenEu.getELose() - avg) - (calculatedPk - aomenPk.getPanKou()) >= 0.08) {
				killGps.add(ResultGroup.Zero);
			}			
		}
	}
	
	private float calculateWinDegree (ClubMatrix hostMatrix, ClubMatrix guestMatrix,
			float hostAttGuestDefComp, float guestAttHostDefComp) {
		float rawDiff = FACTOR_H * hostAttGuestDefComp - FACTOR_G * guestAttHostDefComp;
		float winRt = hostMatrix.getWinRt() * 0.6f + (1 - guestMatrix.getWinDrawRt()) * 0.4f;
		float loseRt = guestMatrix.getWinRt() * 0.6f + (1 - hostMatrix.getWinDrawRt()) * 0.4f;
		float diff = rawDiff;

		if (rawDiff > 0.45) {
			// but host wins little and guest lose little
			if (winRt < 0.4f) {
				diff -= 0.15f;
			}
			// or host wins lot and guest lose lot
			else if (winRt > 0.55f) {
				diff += 0.15f;
			}
		}
		// guest attack is good
		else if (rawDiff < -0.35) {
			// but guest win little and host lose little
			if (loseRt < 0.42f) {
				diff += 0.15f;
			}
			// or guest wins lot and host lose lot
			else if (loseRt >= 0.5f) {
				diff -= 0.15f;
			}
		} else {
			// host wins more or guest lose more
			if (winRt > 0.4f) {
				diff += 0.15f;
			}
			// or guest wins more or host lose more
			if (loseRt > 0.4f) {
				diff -= 0.15f;
			}
		}
		
		if (rawDiff > 1.5f) {
			diff -= (guestAttHostDefComp - 0.35)/0.8f;
		} else if (rawDiff > 1.3f) {
			diff -= (guestAttHostDefComp - 0.35)/1f;
		} else if (rawDiff > 1.05f) {
			diff -= (guestAttHostDefComp - 0.45)/1f;
		} else if (rawDiff > 0.65f) {
			diff += (hostAttGuestDefComp - 1.82)/1.2f;
		} else if (rawDiff > 0.25f) {
			diff += (hostAttGuestDefComp - 1.52)/1.2f;
		} else if (rawDiff > -0.25f) {
			diff -= (guestAttHostDefComp - 1.4)/1.2f;
		} else if (rawDiff > -0.6f) {
			diff -= (guestAttHostDefComp - 1.65)/1.2f;
		} else if (rawDiff > -0.85f) {
			diff -= (guestAttHostDefComp - 1.78)/1.2f;
		} else if (rawDiff > -1.15f){
			diff += (hostAttGuestDefComp - 0.5)/1f;
		} else {
			diff += (hostAttGuestDefComp - 0.45)/1f;
		}
		
		return diff;
	}
	
	private int adjustDrawDegreeReferToDrawRate (float diff, int totalDegree, ClubMatrix hostMatrix, ClubMatrix guestMatrix) {
		int drawDegree = (totalDegree + 1) / 2;
		int restDegree = totalDegree - drawDegree;
		
		float hGoalPerMatch = (float)hostMatrix.getGoals() / hostMatrix.getNum();
		float gGoalPerMatch = (float)guestMatrix.getGoals() / guestMatrix.getNum();
		float hMissPerMatch = (float)hostMatrix.getMisses() / hostMatrix.getNum();
		float gMissPerMatch = (float)guestMatrix.getMisses() / guestMatrix.getNum();

		// Win - 
		if (diff > 0) {
			if (guestMatrix.getWinRt() > 0.4) {
				drawDegree --;
				restDegree ++;
			}
			
			// guest goals more -> guest win more possible
			if (gGoalPerMatch > 1.4f) {
				drawDegree --;
				restDegree ++;
			} else if (hGoalPerMatch < 1.45) {
				drawDegree ++;
				restDegree --;
				
				if (gMissPerMatch < 1.2) {
					drawDegree ++;
					restDegree --;
				}
			}
		} else {
			if (hostMatrix.getWinRt() > 0.4) {
				drawDegree --;
				restDegree ++;
			}
			
			// host goals more -> host win more possible
			if (hGoalPerMatch > 1.4f) {
				drawDegree --;
				restDegree ++;
			} else if (gGoalPerMatch < 1.4) {
				drawDegree ++;
				restDegree --;
				
				if (hMissPerMatch < 1.2) {
					drawDegree ++;
					restDegree --;
				}
			}
		}
		
		return drawDegree;
	}
	
	@Data
	public static class MatchRank {
		private int wRank;
		private int dRank;
		private int lRank;
		
		public String toString(){
			return wRank + "-" + dRank + "-" + lRank;
		}
	}
	
	@Data
	public static class MatchPull {
		private float hPull;
		private float gPull;
	}
	
	public static void main (String args[]) {
		predictDraw(3.5833f, 0.2236f);
		predictDraw(2.5833f, 0.9236f);
		predictDraw(1.2534f, 1.9666f);
		predictDraw(1.3714f, 1.3000f);
		predictDraw(1.6214f, 1.4500f);
		predictDraw(1.1114f, 1.2500f);
		predictDraw(1.0577f, 0.4571f);
	}
	
	private static void predictDraw (float a, float b) {
		float dRate = 0;
		double[] temp = new double[]{a, b};
		double devariance = FastMath.sqrt(StatUtils.populationVariance(temp));
		double mean = StatUtils.mean(temp);
		
		dRate += 0.4f;
		dRate -= devariance * mean * 0.618;
		
		System.out.println(a + "   " + b + "    " + devariance  + "    "  + mean  + "    "+ devariance * mean * 0.382 + "   " + dRate);
	}
}
